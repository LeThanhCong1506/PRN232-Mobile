package com.example.scamazon_frontend.ui.screens.`return`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.returnrequest.CreateReturnRequestDto
import com.example.scamazon_frontend.data.models.returnrequest.ReturnRequestResponse
import com.example.scamazon_frontend.data.repository.ReturnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReturnViewModel(
    private val repository: ReturnRepository
) : ViewModel() {

    private val _returnListState = MutableStateFlow<Resource<BackendPagedResponse<ReturnRequestResponse>>>(Resource.Loading())
    val returnListState: StateFlow<Resource<BackendPagedResponse<ReturnRequestResponse>>> = _returnListState.asStateFlow()

    private val _createReturnState = MutableStateFlow<Resource<ReturnRequestResponse>?>(null)
    val createReturnState: StateFlow<Resource<ReturnRequestResponse>?> = _createReturnState.asStateFlow()

    private val _returnDetailState = MutableStateFlow<Resource<ReturnRequestResponse>?>(null)
    val returnDetailState: StateFlow<Resource<ReturnRequestResponse>?> = _returnDetailState.asStateFlow()

    fun loadMyReturnRequests(page: Int = 1, pageSize: Int = 20) {
        _returnListState.value = Resource.Loading()
        viewModelScope.launch {
            _returnListState.value = repository.getMyReturnRequests(page, pageSize)
        }
    }

    fun loadReturnRequestDetail(id: Int) {
        _returnDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _returnDetailState.value = repository.getReturnRequestById(id)
        }
    }

    fun submitReturnRequest(orderId: Int, type: String, reason: String) {
        if (reason.isBlank()) return
        _createReturnState.value = Resource.Loading()
        viewModelScope.launch {
            val request = CreateReturnRequestDto(orderId, type, reason)
            _createReturnState.value = repository.createReturnRequest(request)
        }
    }

    fun resetCreateState() {
        _createReturnState.value = null
    }
}
