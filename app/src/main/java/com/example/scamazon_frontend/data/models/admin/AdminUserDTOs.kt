package com.example.scamazon_frontend.data.models.admin

import com.google.gson.annotations.SerializedName

data class AdminUserDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("roleName") val roleName: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("ward") val ward: String?,
    @SerializedName("isActive") val isActive: Boolean? = true,
    @SerializedName("createdAt") val createdAt: String?
)
