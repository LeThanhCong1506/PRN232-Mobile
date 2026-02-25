package com.example.scamazon_frontend.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.TokenManager
import com.example.scamazon_frontend.data.models.auth.AuthResponse
import com.example.scamazon_frontend.data.models.auth.LoginRequest
import com.example.scamazon_frontend.data.models.auth.RegisterRequest
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState.asStateFlow()

    fun login(request: LoginRequest) {
        _loginState.value = Resource.Loading()
        // Simulate login: admin credentials → Admin role, else Customer role
        val isAdmin = request.username == "admin" || request.email == "admin@example.com"
        val user = if (isAdmin) MockData.mockAdminUser else MockData.mockUser
        val response = AuthResponse(user = user, token = "mock-jwt-token")
        tokenManager.saveToken(response.token)
        tokenManager.saveUserRole(response.user.role)
        _loginState.value = Resource.Success(response)
    }

    fun register(request: RegisterRequest) {
        _registerState.value = Resource.Loading()
        val response = MockData.mockAuthResponse
        tokenManager.saveToken(response.token)
        tokenManager.saveUserRole(response.user.role)
        _registerState.value = Resource.Success(response)
    }
    
    fun resetState() {
        _loginState.value = null
        _registerState.value = null
    }
}
