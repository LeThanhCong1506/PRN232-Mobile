package com.example.scamazon_frontend.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.profile.ProfileDataDto
import com.example.scamazon_frontend.data.models.profile.UpdateProfileRequest
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<ProfileDataDto>>(Resource.Loading())
    val profileState: StateFlow<Resource<ProfileDataDto>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<Resource<ProfileDataDto>?>(null)
    val updateState: StateFlow<Resource<ProfileDataDto>?> = _updateState.asStateFlow()

    private var currentProfile = MockData.profileData

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        _profileState.value = Resource.Success(currentProfile)
    }

    fun updateProfile(request: UpdateProfileRequest) {
        _updateState.value = Resource.Loading()
        currentProfile = currentProfile.copy(
            email = request.email ?: currentProfile.email,
            phone = request.phone ?: currentProfile.phone,
            fullName = request.fullName ?: currentProfile.fullName,
            avatarUrl = request.avatarUrl ?: currentProfile.avatarUrl,
            address = request.address ?: currentProfile.address,
            city = request.city ?: currentProfile.city,
            district = request.district ?: currentProfile.district,
            ward = request.ward ?: currentProfile.ward
        )
        _profileState.value = Resource.Success(currentProfile)
        _updateState.value = Resource.Success(currentProfile)
    }

    fun resetUpdateState() {
        _updateState.value = null
    }
}
