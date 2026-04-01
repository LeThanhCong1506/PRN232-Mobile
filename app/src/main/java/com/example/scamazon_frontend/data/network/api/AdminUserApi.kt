package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.admin.AdminUserDto
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AdminUserApi {
    // Backend: GET /api/users (returns List<UserDto>)
    @GET("users")
    suspend fun getUsers(): Response<List<AdminUserDto>>

    // Backend: PATCH /api/users/{id}/toggle-status
    // Dedicated endpoint to toggle IsActive (ban/unban)
    @PATCH("users/{id}/toggle-status")
    suspend fun toggleStatus(
        @Path("id") id: Int
    ): Response<Any>
}
