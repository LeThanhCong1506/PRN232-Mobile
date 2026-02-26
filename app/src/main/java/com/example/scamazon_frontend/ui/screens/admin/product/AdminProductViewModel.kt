package com.example.scamazon_frontend.ui.screens.admin.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.*
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.models.product.ProductDetailDto
import com.example.scamazon_frontend.data.models.product.ProductPaginationResponse
import com.example.scamazon_frontend.data.repository.AdminProductRepository
import com.example.scamazon_frontend.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminProductViewModel(
    private val adminProductRepo: AdminProductRepository,
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<ProductPaginationResponse>>(Resource.Loading())
    val productsState: StateFlow<Resource<ProductPaginationResponse>> = _productsState.asStateFlow()

    private val _productDetailState = MutableStateFlow<Resource<ProductDetailDto>?>(null)
    val productDetailState: StateFlow<Resource<ProductDetailDto>?> = _productDetailState.asStateFlow()

    private val _categoriesState = MutableStateFlow<Resource<List<CategoryDto>>>(Resource.Loading())
    val categoriesState: StateFlow<Resource<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _brandsState = MutableStateFlow<Resource<List<BrandDto>>>(Resource.Loading())
    val brandsState: StateFlow<Resource<List<BrandDto>>> = _brandsState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Any>?>(null)
    val saveState: StateFlow<Resource<Any>?> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Any>?>(null)
    val deleteState: StateFlow<Resource<Any>?> = _deleteState.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<UploadDataDto>?>(null)
    val uploadState: StateFlow<Resource<UploadDataDto>?> = _uploadState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
        loadBrands()
    }

    fun loadProducts(page: Int = 1, search: String? = null) {
        viewModelScope.launch {
            _productsState.value = Resource.Loading()
            _productsState.value = adminProductRepo.getAdminProducts(page = page, search = search)
        }
    }

    fun loadProductDetail(productIdOrSlug: String) {
        viewModelScope.launch {
            _productDetailState.value = Resource.Loading()
            val productId = productIdOrSlug.toIntOrNull()
            if (productId != null) {
                _productDetailState.value = productRepo.getProductDetail(productId)
            } else {
                _productDetailState.value = Resource.Error("Invalid product ID")
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val result = productRepo.getCategories()
            when (result) {
                is Resource.Success -> _categoriesState.value = Resource.Success(result.data ?: emptyList())
                is Resource.Error -> _categoriesState.value = Resource.Error(result.message ?: "Error loading categories")
                is Resource.Loading -> _categoriesState.value = Resource.Loading()
            }
        }
    }

    fun loadBrands() {
        viewModelScope.launch {
            _brandsState.value = Resource.Success(emptyList())
        }
    }

    fun createProduct(request: CreateProductRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = adminProductRepo.createProduct(request)
            _saveState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to create product")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun updateProduct(id: Int, request: UpdateProductRequest) {
        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = adminProductRepo.updateProduct(id, request)
            _saveState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to update product")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            val result = adminProductRepo.deleteProduct(id)
            _deleteState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to delete product")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun uploadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            // Store URI locally; batch upload will be called with uploadImages()
            _uploadState.value = Resource.Success(
                UploadDataDto(url = uri.toString(), fileName = uri.lastPathSegment ?: "image.jpg")
            )
        }
    }

    fun uploadImages(context: Context, productId: Int, uris: List<Uri>) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            val result = adminProductRepo.uploadImages(context, productId, uris)
            _uploadState.value = when (result) {
                is Resource.Success -> Resource.Success(UploadDataDto(url = "", fileName = "uploaded"))
                is Resource.Error -> Resource.Error(result.message ?: "Upload failed")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
    fun resetUploadState() { _uploadState.value = null }
}
