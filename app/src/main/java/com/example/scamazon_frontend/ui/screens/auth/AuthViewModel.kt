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

import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState.asStateFlow()

    fun login(request: LoginRequest) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.login(request)
            if (result is Resource.Success) {
                result.data?.token?.let { tokenManager.saveToken(it) }
                result.data?.role?.let { tokenManager.saveUserRole(it.lowercase()) }
            }
            _loginState.value = result
        }
    }

    fun register(request: RegisterRequest) {
        _registerState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.register(request)
            if (result is Resource.Success) {
                result.data?.token?.let { tokenManager.saveToken(it) }
                result.data?.role?.let { tokenManager.saveUserRole(it.lowercase()) }
            }
            _registerState.value = result
        }
    }
    
    fun resetState() {
        _loginState.value = null
        _registerState.value = null
    }
}
