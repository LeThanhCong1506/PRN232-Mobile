package com.example.scamazon_frontend.ui.screens.checkout

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.VietQRGenerator
import com.example.scamazon_frontend.data.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentQRViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    // Overall state: Loading / Error / Success (success means we have payment data)
    private val _qrState = MutableStateFlow<Resource<String>>(Resource.Loading())
    val qrState: StateFlow<Resource<String>> = _qrState.asStateFlow()

    // Locally generated QR bitmap (instant, no network dependency)
    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _paymentStatus = MutableStateFlow("pending")
    val paymentStatus: StateFlow<String> = _paymentStatus.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(DEFAULT_TIMEOUT_SECONDS)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    // Order info for display
    var orderNumber: String = ""
        private set
    var orderAmount: Double = 0.0
        private set
    var paymentReference: String = ""
        private set

    private var pollingJob: Job? = null
    private var currentOrderId: Int = 0

    fun createPaymentQR(orderId: Int) {
        _qrState.value = Resource.Loading()
        _qrBitmap.value = null
        currentOrderId = orderId

        viewModelScope.launch {
            try {
                val result = paymentRepository.getPaymentStatus(orderId)
                when (result) {
                    is Resource.Success -> {
                        val data = result.data!!

                        // Store order info for display
                        orderNumber = data.orderNumber ?: ""
                        orderAmount = data.amount
                        paymentReference = data.paymentReference ?: ""

                        // Check if already paid
                        if (data.isPaid || data.paymentStatus.equals("COMPLETED", ignoreCase = true)) {
                            _paymentStatus.value = "success"
                            _qrState.value = Resource.Success("paid")
                            return@launch
                        }

                        // Check if expired
                        if (data.remainingSeconds <= 0) {
                            _qrState.value = Resource.Error("Payment has expired. Please create a new order.")
                            _paymentStatus.value = "expired"
                            return@launch
                        }

                        // Use backend remaining seconds for countdown
                        _remainingSeconds.value = data.remainingSeconds

                        // Generate QR code locally using VietQR EMVCo format
                        val amount = data.amount.toLong()
                        val addInfo = data.paymentReference ?: "ORDER$orderId"

                        val qrContent = VietQRGenerator.generateQRContent(
                            bankBin = BANK_BIN,
                            accountNumber = ACCOUNT_NUMBER,
                            amount = amount,
                            addInfo = addInfo
                        )

                        // Generate bitmap on background thread
                        val bitmap = withContext(Dispatchers.Default) {
                            VietQRGenerator.generateQRBitmap(qrContent, QR_BITMAP_SIZE)
                        }

                        _qrBitmap.value = bitmap
                        _qrState.value = Resource.Success(qrContent)

                        // Start polling for payment confirmation
                        startPolling(orderId, data.remainingSeconds)
                    }
                    is Resource.Error -> {
                        _qrState.value = Resource.Error(result.message ?: "Failed to load payment info")
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _qrState.value = Resource.Error(e.message ?: "Unexpected error loading payment info")
            }
        }
    }

    private fun startPolling(orderId: Int, initialRemainingSeconds: Int) {
        pollingJob?.cancel()

        val timeoutSeconds = initialRemainingSeconds.coerceAtLeast(60)
        _remainingSeconds.value = timeoutSeconds

        pollingJob = viewModelScope.launch {
            var elapsed = 0
            while (elapsed < timeoutSeconds) {
                delay(POLL_INTERVAL_MS)
                elapsed += (POLL_INTERVAL_MS / 1000).toInt()
                _remainingSeconds.value = (timeoutSeconds - elapsed).coerceAtLeast(0)

                try {
                    val result = paymentRepository.getPaymentStatus(orderId)
                    when (result) {
                        is Resource.Success -> {
                            val data = result.data!!
                            Log.d(TAG, "Poll check: isPaid=${data.isPaid}, status=${data.paymentStatus}")

                            if (data.isPaid || data.paymentStatus.equals("COMPLETED", ignoreCase = true)) {
                                orderNumber = data.orderNumber ?: orderNumber
                                orderAmount = data.amount
                                _paymentStatus.value = "success"
                                return@launch
                            }
                            // Sync remaining seconds from backend
                            if (data.remainingSeconds > 0) {
                                _remainingSeconds.value = data.remainingSeconds
                            }
                            if (data.remainingSeconds <= 0) {
                                _paymentStatus.value = "expired"
                                _qrState.value = Resource.Error("Payment has expired.")
                                return@launch
                            }
                        }
                        is Resource.Error -> {
                            Log.w(TAG, "Poll error: ${result.message}")
                        }
                        is Resource.Loading -> {}
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Poll exception", e)
                }
            }
            _paymentStatus.value = "expired"
            _qrState.value = Resource.Error("Payment timeout. Please try again.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        // Recycle bitmap to free memory
        _qrBitmap.value?.recycle()
        _qrBitmap.value = null
    }

    companion object {
        private const val TAG = "PaymentQR"
        private const val POLL_INTERVAL_MS = 3000L // Poll every 3 seconds for faster detection
        private const val DEFAULT_TIMEOUT_SECONDS = 1800 // 30 minutes (matches backend)
        private const val BANK_BIN = "970415"              // VietinBank (ICB) BIN
        private const val ACCOUNT_NUMBER = "102876493175"  // SePay VietinBank account
        private const val QR_BITMAP_SIZE = 800            // QR image size in pixels
    }
}
