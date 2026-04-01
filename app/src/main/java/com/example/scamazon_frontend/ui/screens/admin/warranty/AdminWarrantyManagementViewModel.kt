package com.example.scamazon_frontend.ui.screens.admin.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.CreateWarrantyRequest
import com.example.scamazon_frontend.data.models.warranty.UpdateWarrantyRequest
import com.example.scamazon_frontend.data.repository.WarrantyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminWarrantyManagementViewModel(
    private val warrantyRepo: WarrantyRepository
) : ViewModel() {

    private val _warrantiesState = MutableStateFlow<Resource<List<AdminWarrantyDto>>>(Resource.Loading())
    val warrantiesState: StateFlow<Resource<List<AdminWarrantyDto>>> = _warrantiesState.asStateFlow()

    private val _serialSearchState = MutableStateFlow<Resource<AdminWarrantyDto>?>(null)
    val serialSearchState: StateFlow<Resource<AdminWarrantyDto>?> = _serialSearchState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<AdminWarrantyDto>?>(null)
    val saveState: StateFlow<Resource<AdminWarrantyDto>?> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteState: StateFlow<Resource<Unit>?> = _deleteState.asStateFlow()

    // Holds the warranty being edited (set before navigating to edit screen)
    private val _editingWarranty = MutableStateFlow<AdminWarrantyDto?>(null)
    val editingWarranty: StateFlow<AdminWarrantyDto?> = _editingWarranty.asStateFlow()

    // "all" | "active" | "expired"
    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        loadWarranties("all")
    }

    fun loadWarranties(filter: String = "all") {
        _selectedFilter.value = filter
        viewModelScope.launch {
            _warrantiesState.value = Resource.Loading()
            _warrantiesState.value = when (filter) {
                "active" -> warrantyRepo.getActiveWarranties()
                "expired" -> warrantyRepo.getExpiredWarranties()
                else -> warrantyRepo.getAllWarranties()
            }
        }
    }

    fun searchBySerial(serialNumber: String) {
        if (serialNumber.isBlank()) return
        viewModelScope.launch {
            _serialSearchState.value = Resource.Loading()
            _serialSearchState.value = warrantyRepo.getWarrantyBySerial(serialNumber.trim())
        }
    }

    fun clearSerialSearch() {
        _serialSearchState.value = null
    }

    fun createWarranty(request: CreateWarrantyRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            _saveState.value = warrantyRepo.createWarranty(request)
            if (_saveState.value is Resource.Success) {
                loadWarranties(_selectedFilter.value)
            }
        }
    }

    fun updateWarranty(id: Int, request: UpdateWarrantyRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            _saveState.value = warrantyRepo.updateWarranty(id, request)
            if (_saveState.value is Resource.Success) {
                loadWarranties(_selectedFilter.value)
            }
        }
    }

    fun deleteWarranty(id: Int) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            _deleteState.value = warrantyRepo.deleteWarranty(id)
            if (_deleteState.value is Resource.Success) {
                loadWarranties(_selectedFilter.value)
            }
        }
    }

    /** Gọi khi user nhấn Edit trên list — load warranty vào state để form đọc */
    fun loadWarrantyForEdit(warrantyId: Int) {
        viewModelScope.launch {
            // Ưu tiên tìm từ list đang load sẵn (tránh gọi API thêm)
            val cached = (_warrantiesState.value as? Resource.Success)?.data
                ?.find { it.warrantyId == warrantyId }
            if (cached != null) {
                _editingWarranty.value = cached
                return@launch
            }
            // Fallback: gọi API GET /api/warranty/{id}
            val result = warrantyRepo.getAdminWarrantyById(warrantyId)
            if (result is Resource.Success) {
                _editingWarranty.value = result.data
            }
        }
    }

    fun clearEditingWarranty() { _editingWarranty.value = null }
    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
}
