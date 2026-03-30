package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.auth.AuthResponse
import com.example.scamazon_frontend.data.models.auth.LoginRequest
import com.example.scamazon_frontend.data.models.auth.RegisterRequest
import com.example.scamazon_frontend.data.models.auth.SocialLoginRequest
import com.example.scamazon_frontend.data.models.auth.ForgotPasswordRequest
import com.example.scamazon_frontend.data.models.auth.ResetPasswordRequest
import com.example.scamazon_frontend.data.models.profile.ProfileDataDto
import com.example.scamazon_frontend.data.models.profile.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApi {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getProfile(): Response<ProfileDataDto>

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileDataDto>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: SocialLoginRequest): Response<AuthResponse>

    @POST("auth/github")
    suspend fun githubLogin(@Body request: SocialLoginRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<String>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<String>>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<ProfileDataDto>
}

// Wrapper cho response base từ backend
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val errors: List<String>? = null
)
