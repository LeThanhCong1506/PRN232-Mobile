package com.example.scamazon_frontend.ui.screens.checkout

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentQRViewModel : ViewModel() {

    // QR URL state
    private val _qrState = MutableStateFlow<Resource<String>>(Resource.Loading())
    val qrState: StateFlow<Resource<String>> = _qrState.asStateFlow()

    // Payment status polling
    private val _paymentStatus = MutableStateFlow("pending")
    val paymentStatus: StateFlow<String> = _paymentStatus.asStateFlow()

    fun createPaymentQR(orderId: Int) {
        _qrState.value = Resource.Loading()
        // Simulate QR with a placeholder image 
        _qrState.value = Resource.Success("https://img.vietqr.io/image/970422-1234567890-compact.jpg?amount=42500000&addInfo=ORD099")
    }

    // Simulate payment success for demo
    fun simulatePaymentSuccess() {
        _paymentStatus.value = "success"
    }
}
