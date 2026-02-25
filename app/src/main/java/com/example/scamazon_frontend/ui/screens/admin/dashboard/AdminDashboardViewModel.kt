package com.example.scamazon_frontend.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.DashboardStatsDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminDashboardViewModel : ViewModel() {

    private val _statsState = MutableStateFlow<Resource<DashboardStatsDto>>(Resource.Loading())
    val statsState: StateFlow<Resource<DashboardStatsDto>> = _statsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        _statsState.value = Resource.Success(MockData.dashboardStats)
    }
}
