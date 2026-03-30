package com.example.scamazon_frontend.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.DailyRevenueDto
import com.example.scamazon_frontend.data.models.admin.DashboardStatsDto
import com.example.scamazon_frontend.data.repository.AdminOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminDashboardViewModel(
    private val adminOrderRepo: AdminOrderRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow<Resource<DashboardStatsDto>>(Resource.Loading())
    val statsState: StateFlow<Resource<DashboardStatsDto>> = _statsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _revenueChartState = MutableStateFlow<Resource<List<DailyRevenueDto>>>(Resource.Loading())
    val revenueChartState: StateFlow<Resource<List<DailyRevenueDto>>> = _revenueChartState.asStateFlow()

    init {
        loadStats()
        loadRevenueChart()
    }

    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = Resource.Loading()
            _statsState.value = adminOrderRepo.getDashboard()
        }
    }

    fun loadRevenueChart(days: Int = 7) {
        viewModelScope.launch {
            _revenueChartState.value = Resource.Loading()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            val to = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -days + 1)
            val from = sdf.format(cal.time)
            _revenueChartState.value = adminOrderRepo.getRevenueChart(from, to)
        }
    }
}
