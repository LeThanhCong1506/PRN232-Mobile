package com.example.scamazon_frontend.ui.screens.admin.`return`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.returnrequest.ProcessReturnRequestDto
import com.example.scamazon_frontend.data.models.returnrequest.ReturnRequestResponse
import com.example.scamazon_frontend.data.repository.ReturnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminReturnViewModel(
    private val repository: ReturnRepository
) : ViewModel() {

    private val _returnListState = MutableStateFlow<Resource<BackendPagedResponse<ReturnRequestResponse>>>(Resource.Loading())
    val returnListState: StateFlow<Resource<BackendPagedResponse<ReturnRequestResponse>>> = _returnListState.asStateFlow()

    private val _processState = MutableStateFlow<Resource<ReturnRequestResponse>?>(null)
    val processState: StateFlow<Resource<ReturnRequestResponse>?> = _processState.asStateFlow()

    private val _returnDetailState = MutableStateFlow<Resource<ReturnRequestResponse>?>(null)
    val returnDetailState: StateFlow<Resource<ReturnRequestResponse>?> = _returnDetailState.asStateFlow()

    fun loadAllReturnRequests(page: Int = 1, pageSize: Int = 20) {
        _returnListState.value = Resource.Loading()
        viewModelScope.launch {
            val response = repository.getAllReturnRequests(page, pageSize)
            _returnListState.value = response
        }
    }

    fun loadReturnRequestDetail(id: Int) {
        _returnDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _returnDetailState.value = repository.getReturnRequestById(id)
        }
    }

    fun processReturnRequest(id: Int, status: String, adminNote: String?) {
        _processState.value = Resource.Loading()
        viewModelScope.launch {
            val request = ProcessReturnRequestDto(status, adminNote?.takeIf { it.isNotBlank() })
            val result = repository.processReturnRequest(id, request)
            _processState.value = result
            
            // Reload list if successful
            if (result is Resource.Success) {
                if (_returnListState.value is Resource.Success) {
                    val currentData = (_returnListState.value as Resource.Success).data
                    if (currentData != null) {
                        // Optimistically update the list
                        val updatedItems = currentData.items.map { if (it.returnRequestId == id) result.data else it }
                        _returnListState.value = Resource.Success(
                            BackendPagedResponse(updatedItems, currentData.totalItems, currentData.pageNumber, currentData.pageSize, currentData.totalPages)
                        )
                    }
                }
            }
        }
    }

    fun resetProcessState() {
        _processState.value = null
    }
}
