package com.example.scamazon_frontend.ui.screens.notification

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.notification.NotificationDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<List<NotificationDto>>>(Resource.Loading())
    val notificationsState = _notificationsState.asStateFlow()

    private val localNotifications = MockData.notifications.toMutableList()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _notificationsState.value = Resource.Success(localNotifications.toList())
    }

    fun markAsRead(id: Int) {
        val index = localNotifications.indexOfFirst { it.id == id }
        if (index != -1) {
            localNotifications[index] = localNotifications[index].copy(isRead = true)
            _notificationsState.value = Resource.Success(localNotifications.toList())
        }
    }

    fun markAllAsRead() {
        for (i in localNotifications.indices) {
            localNotifications[i] = localNotifications[i].copy(isRead = true)
        }
        _notificationsState.value = Resource.Success(localNotifications.toList())
    }
}
