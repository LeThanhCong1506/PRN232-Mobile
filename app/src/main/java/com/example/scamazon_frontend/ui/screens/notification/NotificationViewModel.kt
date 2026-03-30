package com.example.scamazon_frontend.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.notification.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        // Optimistic update: immediately reflect change in UI
        val current = _notificationsState.value
        if (current is Resource.Success) {
            val updated = current.data?.map {
                if (it.id == id) it.copy(isRead = true) else it
            } ?: emptyList()
            _notificationsState.value = Resource.Success(updated)
            _unreadCount.value = updated.count { it.isRead == false }
        }
        // Fire-and-forget: sync with server in background
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        // Optimistic update: immediately mark all as read
        val current = _notificationsState.value
        if (current is Resource.Success) {
            val updated = current.data?.map { it.copy(isRead = true) } ?: emptyList()
            _notificationsState.value = Resource.Success(updated)
            _unreadCount.value = 0
        }
        // Fire-and-forget: sync with server
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }
}
