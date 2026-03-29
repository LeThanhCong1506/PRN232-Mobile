package com.example.scamazon_frontend.ui.screens.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.AdminUserDto
import com.example.scamazon_frontend.data.repository.AdminUserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminUserViewModel(private val repository: AdminUserRepository) : ViewModel() {
    private val _usersState = MutableStateFlow<Resource<List<AdminUserDto>>>(Resource.Loading())
    val usersState: StateFlow<Resource<List<AdminUserDto>>> = _usersState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Resource<String>?>(null)
    val updateStatusState: StateFlow<Resource<String>?> = _updateStatusState.asStateFlow()

    // Filtered users based on search
    val filteredUsers: StateFlow<List<AdminUserDto>> = combine(_usersState, _searchQuery) { state, query ->
        if (state is Resource.Success) {
            val users = state.data ?: emptyList()
            if (query.isBlank()) users
            else users.filter { 
                it.username.contains(query, ignoreCase = true) || 
                it.email.contains(query, ignoreCase = true) ||
                it.fullName?.contains(query, ignoreCase = true) == true
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadUsers()
    }

    fun loadUsers() {
        _usersState.value = Resource.Loading()
        viewModelScope.launch {
            _usersState.value = repository.getUsers()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleUserStatus(user: AdminUserDto) {
        _updateStatusState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.toggleUserStatus(user)
            _updateStatusState.value = result
            if (result is Resource.Success) {
                // Refresh list
                loadUsers()
            }
        }
    }

    fun resetUpdateStatus() {
        _updateStatusState.value = null
    }
}
