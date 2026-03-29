package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.admin.AdminUserDto
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Body

interface AdminUserApi {
    // Backend: GET /api/users (returns List<UserDto>)
    @GET("users")
    suspend fun getUsers(): Response<List<AdminUserDto>>

    // Backend: PUT /api/users/{id}
    // Used for blocking/unblocking by updating status
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: AdminUserDto
    ): Response<String>
}
