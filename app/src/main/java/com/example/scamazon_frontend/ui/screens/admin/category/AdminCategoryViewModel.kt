package com.example.scamazon_frontend.ui.screens.admin.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.*
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.repository.AdminCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AdminCategoryViewModel(
    private val adminCategoryRepo: AdminCategoryRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<Resource<List<CategoryDto>>>(Resource.Loading())
    val categoriesState: StateFlow<Resource<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _brandsState = MutableStateFlow<Resource<List<BrandDto>>>(Resource.Loading())
    val brandsState: StateFlow<Resource<List<BrandDto>>> = _brandsState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Any>?>(null)
    val saveState: StateFlow<Resource<Any>?> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Any>?>(null)
    val deleteState: StateFlow<Resource<Any>?> = _deleteState.asStateFlow()

    private val _uploadImageState = MutableStateFlow<Resource<Any>?>(null)
    val uploadImageState: StateFlow<Resource<Any>?> = _uploadImageState.asStateFlow()

    init {
        loadCategories()
        loadBrands()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = Resource.Loading()
            val result = adminCategoryRepo.getCategories()
            _categoriesState.value = when (result) {
                is Resource.Success -> Resource.Success(result.data ?: emptyList())
                is Resource.Error -> Resource.Error(result.message ?: "Error loading categories")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun loadBrands() {
        viewModelScope.launch {
            _brandsState.value = Resource.Loading()
            val result = adminCategoryRepo.getBrands()
            _brandsState.value = when (result) {
                is Resource.Success -> Resource.Success(result.data ?: emptyList())
                is Resource.Error -> Resource.Error(result.message ?: "Error loading brands")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    // ===== Category CRUD =====

    fun createCategory(request: CreateCategoryRequest, imagePart: MultipartBody.Part? = null) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = adminCategoryRepo.createCategory(request)
            if (result is Resource.Success && result.data != null) {
                if (imagePart != null) {
                    val uploadResult = adminCategoryRepo.uploadCategoryImage(result.data.id, imagePart)
                    _saveState.value = uploadResult as Resource<Any>?
                } else {
                    _saveState.value = Resource.Success(Unit)
                }
            } else {
                _saveState.value = when (result) {
                    is Resource.Success -> Resource.Success(Unit) // fallback
                    is Resource.Error -> Resource.Error(result.message ?: "Failed to create category")
                    is Resource.Loading -> Resource.Loading()
                }
            }
        }
    }

    fun updateCategory(id: Int, request: UpdateCategoryRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = adminCategoryRepo.updateCategory(id, request)
            _saveState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to update category")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            val result = adminCategoryRepo.deleteCategory(id)
            _deleteState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to delete category")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    // ===== Brand CRUD =====

    fun createBrand(request: CreateBrandRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = adminCategoryRepo.createBrand(request)
            _saveState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to create brand")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun updateBrand(id: Int, request: UpdateBrandRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = adminCategoryRepo.updateBrand(id, request)
            _saveState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to update brand")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun deleteBrand(id: Int) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            val result = adminCategoryRepo.deleteBrand(id)
            _deleteState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to delete brand")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun uploadCategoryImage(id: Int, filePart: MultipartBody.Part) {
        viewModelScope.launch {
            _uploadImageState.value = Resource.Loading()
            val result = adminCategoryRepo.uploadCategoryImage(id, filePart)
            _uploadImageState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to upload image")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
    fun resetUploadImageState() { _uploadImageState.value = null }
}
