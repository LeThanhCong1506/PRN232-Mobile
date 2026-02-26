package com.example.scamazon_frontend.ui.screens.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.repository.WarrantyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WarrantyViewModel(
    private val warrantyRepo: WarrantyRepository
) : ViewModel() {

    private val _warrantiesState = MutableStateFlow<Resource<List<MyWarrantyDto>>>(Resource.Loading())
    val warrantiesState: StateFlow<Resource<List<MyWarrantyDto>>> = _warrantiesState.asStateFlow()

    private val _submitClaimState = MutableStateFlow<Resource<SubmitWarrantyClaimResponseDto>?>(null)
    val submitClaimState: StateFlow<Resource<SubmitWarrantyClaimResponseDto>?> = _submitClaimState.asStateFlow()

    private val _issueDescription = MutableStateFlow("")
    val issueDescription: StateFlow<String> = _issueDescription.asStateFlow()

    private val _contactPhone = MutableStateFlow("")
    val contactPhone: StateFlow<String> = _contactPhone.asStateFlow()

    init {
        loadMyWarranties()
    }

    fun loadMyWarranties() {
        viewModelScope.launch {
            _warrantiesState.value = Resource.Loading()
            _warrantiesState.value = warrantyRepo.getMyWarranties()
        }
    }

    fun submitClaim(warrantyId: Int) {
        if (_issueDescription.value.length < 10) return
        viewModelScope.launch {
            _submitClaimState.value = Resource.Loading()
            val request = SubmitWarrantyClaimRequest(
                issueDescription = _issueDescription.value,
                contactPhone = _contactPhone.value.ifBlank { null }
            )
            _submitClaimState.value = warrantyRepo.submitClaim(warrantyId, request)
        }
    }

    fun setIssueDescription(text: String) { _issueDescription.value = text }
    fun setContactPhone(phone: String) { _contactPhone.value = phone }

    fun resetSubmitState() {
        _submitClaimState.value = null
        _issueDescription.value = ""
        _contactPhone.value = ""
    }
}
