package com.example.scamazon_frontend.ui.screens.admin.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimDto
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimRequest
import com.example.scamazon_frontend.data.repository.WarrantyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminWarrantyViewModel(
    private val warrantyRepo: WarrantyRepository
) : ViewModel() {

    private val _claimsState = MutableStateFlow<Resource<List<AdminWarrantyClaimDto>>>(Resource.Loading())
    val claimsState: StateFlow<Resource<List<AdminWarrantyClaimDto>>> = _claimsState.asStateFlow()

    private val _resolveState = MutableStateFlow<Resource<Any>?>(null)
    val resolveState: StateFlow<Resource<Any>?> = _resolveState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        loadClaims()
    }

    fun loadClaims(status: String? = null) {
        viewModelScope.launch {
            _claimsState.value = Resource.Loading()
            val apiStatus = if (status == null || status == "all") null else status.uppercase()
            _claimsState.value = warrantyRepo.getAdminWarrantyClaims(status = apiStatus)
        }
    }

    fun resolveClaim(claimId: Int, resolution: String, note: String? = null) {
        viewModelScope.launch {
            _resolveState.value = Resource.Loading()
            val request = ResolveWarrantyClaimRequest(resolution = resolution.uppercase(), resolutionNote = note)
            val result = warrantyRepo.resolveWarrantyClaim(claimId, request)
            _resolveState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to resolve claim")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun onFilterChange(filter: String) {
        _selectedFilter.value = filter
        loadClaims(if (filter == "all") null else filter)
    }

    fun resetResolveState() {
        _resolveState.value = null
    }
}
