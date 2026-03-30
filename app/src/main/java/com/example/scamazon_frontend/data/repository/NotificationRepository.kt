package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.network.api.NotificationApi
import com.example.scamazon_frontend.data.network.api.NotificationListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(private val api: NotificationApi) {
    suspend fun getNotifications(): Resource<NotificationListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyNotifications()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load notifications")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun markAsRead(id: Int): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.markAsRead(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(true)
                    } else {
                        Resource.Error(body?.message ?: "Failed to mark as read")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun markAllAsRead(): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.markAllAsRead()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(true)
                    } else {
                        Resource.Error(body?.message ?: "Failed to mark all as read")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
