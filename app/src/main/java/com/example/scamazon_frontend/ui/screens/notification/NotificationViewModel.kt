package com.example.scamazon_frontend.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.notification.NotificationDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.scamazon_frontend.data.repository.NotificationRepository

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<List<NotificationDto>>>(Resource.Loading())
    val notificationsState = _notificationsState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = Resource.Loading()
            val result = repository.getNotifications()
            if (result is Resource.Success) {
                _notificationsState.value = Resource.Success(result.data?.items ?: emptyList())
                _unreadCount.value = result.data?.unreadCount ?: 0
            } else if (result is Resource.Error) {
                _notificationsState.value = Resource.Error(result.message ?: "Failed")
            }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            val res = repository.markAsRead(id)
            if (res is Resource.Success) {
                loadNotifications() // Reload
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val res = repository.markAllAsRead()
            if (res is Resource.Success) {
                loadNotifications() // Reload
            }
        }
    }
}
