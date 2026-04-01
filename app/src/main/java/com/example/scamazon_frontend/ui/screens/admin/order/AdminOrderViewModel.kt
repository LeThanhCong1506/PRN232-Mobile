package com.example.scamazon_frontend.ui.screens.admin.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.order.AdminOrderSummaryDto
import com.example.scamazon_frontend.data.models.order.OrderDetailDataDto
import com.example.scamazon_frontend.data.repository.AdminOrderRepository
import com.example.scamazon_frontend.data.repository.PaymentRepository
import com.example.scamazon_frontend.data.network.SignalRNotificationClient
import com.example.scamazon_frontend.core.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminOrderViewModel(
    private val adminOrderRepo: AdminOrderRepository,
    private val signalRClient: SignalRNotificationClient,
    private val tokenManager: TokenManager,
    private val paymentRepo: PaymentRepository? = null
) : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<AdminOrderSummaryDto>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<AdminOrderSummaryDto>>> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<Resource<OrderDetailDataDto>?>(null)
    val orderDetailState: StateFlow<Resource<OrderDetailDataDto>?> = _orderDetailState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Resource<Any>?>(null)
    val updateStatusState: StateFlow<Resource<Any>?> = _updateStatusState.asStateFlow()

    private val _verifyPaymentState = MutableStateFlow<Resource<Any>?>(null)
    val verifyPaymentState: StateFlow<Resource<Any>?> = _verifyPaymentState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        fetchOrders()
        setupSignalR()
    }

    private fun setupSignalR() {
        tokenManager.getToken()?.let { token ->
            signalRClient.connect(token)
            signalRClient.onAdminOrderUpdated {
                fetchOrders(_selectedFilter.value)
            }
        }
    }

    fun fetchOrders(statusFilter: String? = null) {
        viewModelScope.launch {
            _ordersState.value = Resource.Loading()
            val apiStatus = if (statusFilter == null || statusFilter == "all") null else statusFilter.uppercase()
            _ordersState.value = adminOrderRepo.getAdminOrders(status = apiStatus)
        }
    }

    fun fetchOrderDetail(id: Int) {
        viewModelScope.launch {
            _orderDetailState.value = Resource.Loading()
            _orderDetailState.value = adminOrderRepo.getAdminOrderDetail(id)
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String, trackingNumber: String? = null, carrier: String? = null) {
        viewModelScope.launch {
            _updateStatusState.value = Resource.Loading()
            val result = adminOrderRepo.updateOrderStatus(orderId, newStatus.uppercase(), trackingNumber = trackingNumber, carrier = carrier)
            _updateStatusState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to update status")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun onFilterChange(filter: String) {
        _selectedFilter.value = filter
    }

    fun verifyPayment(orderId: Int) {
        viewModelScope.launch {
            val repo = paymentRepo ?: return@launch
            _verifyPaymentState.value = Resource.Loading()
            val result = repo.verifyPaymentManually(orderId)
            _verifyPaymentState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to verify payment")
                is Resource.Loading -> Resource.Loading()
            }
            if (result is Resource.Success) {
                fetchOrderDetail(orderId)
            }
        }
    }

    fun resetUpdateState() {
        _updateStatusState.value = null
    }

    fun resetVerifyPaymentState() {
        _verifyPaymentState.value = null
    }
}
