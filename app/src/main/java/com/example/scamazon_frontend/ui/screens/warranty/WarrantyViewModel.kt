package com.example.scamazon_frontend.ui.screens.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.repository.WarrantyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WarrantyViewModel(
    private val repository: WarrantyRepository
) : ViewModel() {

    private val _warrantiesState = MutableStateFlow<Resource<List<MyWarrantyDto>>>(Resource.Loading())
    val warrantiesState: StateFlow<Resource<List<MyWarrantyDto>>> = _warrantiesState.asStateFlow()

    private val _warrantyDetailState = MutableStateFlow<Resource<MyWarrantyDto>?>(null)
    val warrantyDetailState: StateFlow<Resource<MyWarrantyDto>?> = _warrantyDetailState.asStateFlow()

    private val _submitClaimState = MutableStateFlow<Resource<SubmitWarrantyClaimResponseDto>?>(null)
    val submitClaimState: StateFlow<Resource<SubmitWarrantyClaimResponseDto>?> = _submitClaimState.asStateFlow()

    private val _issueDescription = MutableStateFlow("")
    val issueDescription: StateFlow<String> = _issueDescription.asStateFlow()

    private val _contactPhone = MutableStateFlow("")
    val contactPhone: StateFlow<String> = _contactPhone.asStateFlow()

    init {
        loadMyWarranties()
    }

    fun setIssueDescription(value: String) { _issueDescription.value = value }
    fun setContactPhone(value: String) { _contactPhone.value = value }

    fun loadMyWarranties() {
        _warrantiesState.value = Resource.Loading()
        viewModelScope.launch {
            _warrantiesState.value = repository.getMyWarranties()
        }
    }

    fun loadWarrantyDetail(id: Int) {
        _warrantyDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _warrantyDetailState.value = repository.getWarrantyById(id)
        }
    }

    fun submitClaim(warrantyId: Int) {
        if (_issueDescription.value.isBlank()) return
        _submitClaimState.value = Resource.Loading()
        viewModelScope.launch {
            val request = SubmitWarrantyClaimRequest(_issueDescription.value, _contactPhone.value)
            _submitClaimState.value = repository.submitWarrantyClaim(warrantyId, request)
        }
    }

    fun resetSubmitState() {
        _submitClaimState.value = null
    }
}
