package com.example.scamazon_frontend.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.network.ApiResponse
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.data.models.product.ProductPaginationResponse
import com.example.scamazon_frontend.data.remote.ProductService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class SearchViewModel(private val productService: ProductService) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _products = MutableStateFlow<Resource<ProductPaginationResponse>>(Resource.Loading())
    val products: StateFlow<Resource<ProductPaginationResponse>> = _products.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _sortBy = MutableStateFlow("created_at")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("desc")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private var searchJob: Job? = null
    private var currentPage = 1

    init {
        searchProducts()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            currentPage = 1
            searchProducts()
        }
    }

    fun onCategorySelected(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        currentPage = 1
        searchProducts()
    }

    fun onSortChanged(sortBy: String, sortOrder: String) {
        _sortBy.value = sortBy
        _sortOrder.value = sortOrder
        currentPage = 1
        searchProducts()
    }

    fun searchProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            try {
                val response = productService.getProducts(
                    page = currentPage,
                    limit = 20,
                    categoryId = _selectedCategoryId.value,
                    search = _searchQuery.value.ifBlank { null },
                    sortBy = _sortBy.value,
                    sortOrder = _sortOrder.value
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        _products.value = Resource.Success(body.data)
                    } else {
                        _products.value = Resource.Error(body?.message ?: "No products found")
                    }
                } else {
                    _products.value = Resource.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _products.value = Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
