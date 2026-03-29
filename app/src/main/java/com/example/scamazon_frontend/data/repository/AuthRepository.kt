package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.SocialAuthConfig
import com.example.scamazon_frontend.data.models.auth.AuthResponse
import com.example.scamazon_frontend.data.models.auth.LoginRequest
import com.example.scamazon_frontend.data.models.auth.RegisterRequest
import com.example.scamazon_frontend.data.models.auth.SocialLoginRequest
import com.example.scamazon_frontend.data.models.auth.ForgotPasswordRequest
import com.example.scamazon_frontend.data.models.auth.ResetPasswordRequest
import com.example.scamazon_frontend.data.models.profile.ProfileDataDto
import com.example.scamazon_frontend.data.models.profile.UpdateProfileRequest
import com.example.scamazon_frontend.data.network.api.ApiResponse
import com.example.scamazon_frontend.data.network.api.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class AuthRepository(
    private val authApi: AuthApi
) {
    suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response body is null")
                } else {
                    // Read the actual error message from the backend
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrBlank()) {
                        errorBody
                    } else {
                        "Login failed (${response.code()})"
                    }
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during login")
            }
        }
    }

    suspend fun register(request: RegisterRequest): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.register(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response body is null")
                } else {
                    // Read the actual error message from the backend
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrBlank()) {
                        errorBody
                    } else {
                        "Registration failed (${response.code()})"
                    }
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during registration")
            }
        }
    }

    suspend fun getProfile(): Resource<ProfileDataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.getProfile()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Profile data is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrBlank()) {
                        errorBody
                    } else {
                        "Failed to fetch profile (${response.code()})"
                    }
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching profile")
            }
        }
    }

    suspend fun googleLogin(serverAuthCode: String): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.googleLogin(
                    SocialLoginRequest(code = serverAuthCode, redirectUri = "")
                )
                if (response.isSuccessful) {
                    response.body()?.let { Resource.Success(it) }
                        ?: Resource.Error("Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(
                        if (!errorBody.isNullOrBlank()) errorBody
                        else "Google login failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Google login failed")
            }
        }
    }

    suspend fun githubLogin(code: String): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.githubLogin(
                    SocialLoginRequest(code = code, redirectUri = SocialAuthConfig.GITHUB_REDIRECT_URI)
                )
                if (response.isSuccessful) {
                    response.body()?.let { Resource.Success(it) }
                        ?: Resource.Error("Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(
                        if (!errorBody.isNullOrBlank()) errorBody
                        else "GitHub login failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "GitHub login failed")
            }
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<ProfileDataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.updateProfile(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Update response is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrBlank()) {
                        errorBody
                    } else {
                        "Failed to update profile (${response.code()})"
                    }
                    Resource.Error(errorMessage)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred updating profile")
            }
        }
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Resource<ApiResponse<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.forgotPassword(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(
                        if (!errorBody.isNullOrBlank()) errorBody
                        else "Forgot password failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Forgot password failure")
            }
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Resource<ApiResponse<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.resetPassword(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(
                        if (!errorBody.isNullOrBlank()) errorBody
                        else "Reset password failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Reset password failure")
            }
        }
    }

    suspend fun uploadAvatar(filePart: MultipartBody.Part): Resource<ProfileDataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.uploadAvatar(filePart)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(
                        if (!errorBody.isNullOrBlank()) errorBody
                        else "Upload avatar failed (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Upload avatar failure")
            }
        }
    }
}
