package com.example.scamazon_frontend.ui.screens.order

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.order.OrderDetailDataDto
import com.example.scamazon_frontend.data.models.order.OrderSummaryDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderHistoryViewModel : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<OrderSummaryDto>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<OrderSummaryDto>>> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<Resource<OrderDetailDataDto>?>(null)
    val orderDetailState: StateFlow<Resource<OrderDetailDataDto>?> = _orderDetailState.asStateFlow()

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        _ordersState.value = Resource.Success(MockData.orderSummaries)
    }

    fun fetchOrderDetail(id: Int) {
        _orderDetailState.value = Resource.Loading()
        _orderDetailState.value = Resource.Success(MockData.getOrderDetail(id))
    }
}
