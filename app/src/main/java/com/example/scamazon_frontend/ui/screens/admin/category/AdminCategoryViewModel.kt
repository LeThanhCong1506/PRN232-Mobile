package com.example.scamazon_frontend.ui.screens.admin.category

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.*
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminCategoryViewModel : ViewModel() {

    private val _categoriesState = MutableStateFlow<Resource<List<CategoryDto>>>(Resource.Loading())
    val categoriesState: StateFlow<Resource<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _brandsState = MutableStateFlow<Resource<List<BrandDto>>>(Resource.Loading())
    val brandsState: StateFlow<Resource<List<BrandDto>>> = _brandsState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Any>?>(null)
    val saveState: StateFlow<Resource<Any>?> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Any>?>(null)
    val deleteState: StateFlow<Resource<Any>?> = _deleteState.asStateFlow()

    init {
        loadCategories()
        loadBrands()
    }

    fun loadCategories() {
        _categoriesState.value = Resource.Success(MockData.categories)
    }

    fun loadBrands() {
        _brandsState.value = Resource.Success(MockData.brands)
    }

    // Category CRUD
    fun createCategory(request: CreateCategoryRequest) {
        _saveState.value = Resource.Success(Unit)
    }

    fun updateCategory(id: Int, request: UpdateCategoryRequest) {
        _saveState.value = Resource.Success(Unit)
    }

    fun deleteCategory(id: Int) {
        _deleteState.value = Resource.Success(Unit)
    }

    // Brand CRUD
    fun createBrand(request: CreateBrandRequest) {
        _saveState.value = Resource.Success(Unit)
    }

    fun updateBrand(id: Int, request: UpdateBrandRequest) {
        _saveState.value = Resource.Success(Unit)
    }

    fun deleteBrand(id: Int) {
        _deleteState.value = Resource.Success(Unit)
    }

    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
}
