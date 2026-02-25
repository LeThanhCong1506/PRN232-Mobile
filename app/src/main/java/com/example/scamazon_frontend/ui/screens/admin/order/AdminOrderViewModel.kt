package com.example.scamazon_frontend.ui.screens.admin.order

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.order.AdminOrderSummaryDto
import com.example.scamazon_frontend.data.models.order.OrderDetailDataDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminOrderViewModel : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<AdminOrderSummaryDto>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<AdminOrderSummaryDto>>> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<Resource<OrderDetailDataDto>?>(null)
    val orderDetailState: StateFlow<Resource<OrderDetailDataDto>?> = _orderDetailState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Resource<Any>?>(null)
    val updateStatusState: StateFlow<Resource<Any>?> = _updateStatusState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        _ordersState.value = Resource.Success(MockData.adminOrders.orders)
    }

    fun fetchOrderDetail(id: Int) {
        _orderDetailState.value = Resource.Loading()
        _orderDetailState.value = Resource.Success(MockData.getOrderDetail(id))
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        _updateStatusState.value = Resource.Success(Unit)
    }

    fun onFilterChange(filter: String) {
        _selectedFilter.value = filter
    }

    fun resetUpdateState() {
        _updateStatusState.value = null
    }
}
