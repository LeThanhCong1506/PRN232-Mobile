package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.notification.NotificationDto
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class NotificationListResponse(
    val items: List<NotificationDto>,
    val unreadCount: Int
)

interface NotificationApi {
    @GET("Notification")
    suspend fun getMyNotifications(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<BackendApiResponse<NotificationListResponse>>

    @PUT("Notification/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): Response<BackendApiResponse<Any>>

    @PUT("Notification/read-all")
    suspend fun markAllAsRead(): Response<BackendApiResponse<Any>>
}
