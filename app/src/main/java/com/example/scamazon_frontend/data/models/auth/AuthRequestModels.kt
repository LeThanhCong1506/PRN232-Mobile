package com.example.scamazon_frontend.data.models.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirmPassword") val confirmPassword: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("address") val address: String?
)

data class SocialLoginRequest(
    @SerializedName("code") val code: String,
    @SerializedName("redirectUri") val redirectUri: String? = null
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("token") val token: String,
    @SerializedName("newPassword") val newPassword: String
)
