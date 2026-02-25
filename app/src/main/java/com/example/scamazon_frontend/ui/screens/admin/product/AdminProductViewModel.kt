package com.example.scamazon_frontend.ui.screens.admin.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.*
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.models.product.ProductDetailDto
import com.example.scamazon_frontend.data.models.product.ProductPaginationResponse
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminProductViewModel : ViewModel() {

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
        _productsState.value = Resource.Loading()
        val filtered = if (!search.isNullOrBlank()) {
            val items = MockData.products.filter { it.name.lowercase().contains(search.lowercase()) }
            ProductPaginationResponse(
                items = items,
                pagination = com.example.scamazon_frontend.data.models.product.PaginationMetadata(1, 1, items.size)
            )
        } else {
            MockData.getProductsPaginated(page = page, limit = 20)
        }
        _productsState.value = Resource.Success(filtered)
    }

    fun loadProductDetail(slug: String) {
        _productDetailState.value = Resource.Loading()
        _productDetailState.value = Resource.Success(MockData.getProductDetail(slug))
    }

    fun loadCategories() {
        _categoriesState.value = Resource.Success(MockData.categories)
    }

    fun loadBrands() {
        _brandsState.value = Resource.Success(MockData.brands)
    }

    fun createProduct(request: CreateProductRequest) {
        _saveState.value = Resource.Loading()
        _saveState.value = Resource.Success(Unit)
    }

    fun updateProduct(id: Int, request: UpdateProductRequest) {
        _saveState.value = Resource.Loading()
        _saveState.value = Resource.Success(Unit)
    }

    fun deleteProduct(id: Int) {
        _deleteState.value = Resource.Loading()
        _deleteState.value = Resource.Success(Unit)
    }

    fun uploadImage(context: Context, uri: Uri) {
        _uploadState.value = Resource.Loading()
        _uploadState.value = Resource.Success(
            UploadDataDto(url = "https://picsum.photos/seed/upload${System.currentTimeMillis()}/400/400", fileName = "uploaded.jpg")
        )
    }

    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
    fun resetUploadState() { _uploadState.value = null }
}
