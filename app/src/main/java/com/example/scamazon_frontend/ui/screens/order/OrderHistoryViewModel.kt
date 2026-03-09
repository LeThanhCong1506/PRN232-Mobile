package com.example.scamazon_frontend.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.order.CancelOrderRequest
import com.example.scamazon_frontend.data.models.order.OrderDetailResponse
import com.example.scamazon_frontend.data.models.order.OrderResponse
import com.example.scamazon_frontend.data.models.order.OrderFilterRequest
import com.example.scamazon_frontend.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderHistoryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<OrderResponse>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<OrderResponse>>> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<Resource<OrderDetailResponse>?>(null)
    val orderDetailState: StateFlow<Resource<OrderDetailResponse>?> = _orderDetailState.asStateFlow()

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter.asStateFlow()

    init {
        fetchOrders()
    }

    fun onFilterChange(status: String?) {
        _selectedFilter.value = status
        fetchOrders()
    }

    fun fetchOrders() {
        _ordersState.value = Resource.Loading()
        viewModelScope.launch {
            val result = orderRepository.getMyOrders(
                status = _selectedFilter.value,
                paymentStatus = null,
                search = null,
                pageNumber = 1,
                pageSize = 50
            )
            if (result is Resource.Success && result.data != null) {
                _ordersState.value = Resource.Success(result.data.items)
            } else if (result is Resource.Error) {
                _ordersState.value = Resource.Error(result.message ?: "Failed to load orders")
            }
        }
    }

    fun fetchOrderDetail(id: Int) {
        _orderDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _orderDetailState.value = orderRepository.getOrderDetail(id)
        }
    }

    fun cancelOrder(id: Int, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.cancelOrder(id, reason)
            if (result is Resource.Success) {
                fetchOrderDetail(id)
                fetchOrders()
                onSuccess()
            } else {
                onError(result.message ?: "Failed to cancel order")
            }
        }
    }
}
