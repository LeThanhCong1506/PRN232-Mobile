package com.example.scamazon_frontend.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.store.StoreBranchDto
import com.example.scamazon_frontend.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val storeRepo: StoreRepository) : ViewModel() {

    private val _branchesState = MutableStateFlow<Resource<List<StoreBranchDto>>>(Resource.Loading())
    val branchesState = _branchesState.asStateFlow()

    init {
        loadBranches()
    }

    fun loadBranches() {
        viewModelScope.launch {
            _branchesState.value = Resource.Loading()
            _branchesState.value = storeRepo.getStoreBranches()
        }
    }
}
