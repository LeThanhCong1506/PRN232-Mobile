package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.AdminUserDto
import com.example.scamazon_frontend.data.network.api.AdminUserApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminUserRepository(private val api: AdminUserApi) {
    suspend fun getUsers(): Resource<List<AdminUserDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUsers()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("No users found")
                    }
                } else {
                    Resource.Error("Failed to fetch users: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun toggleUserStatus(user: AdminUserDto): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.toggleStatus(user.userId)
                if (response.isSuccessful) {
                    Resource.Success("User status updated successfully")
                } else {
                    Resource.Error("Failed to toggle status: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
