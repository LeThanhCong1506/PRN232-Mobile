package com.example.scamazon_frontend.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<ProductDto>>>(Resource.Loading())
    val productsState: StateFlow<Resource<List<ProductDto>>> = _productsState.asStateFlow()

    private val _currentSort = MutableStateFlow("newest")
    val currentSort: StateFlow<String> = _currentSort.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private var categoryId: Int? = null
    private var searchTerm: String? = null
    private val allProducts = mutableListOf<ProductDto>()

    fun init(categoryId: Int?, searchTerm: String? = null) {
        this.categoryId = categoryId
        this.searchTerm = searchTerm
        fetchProducts(reset = true)
    }

    fun setSort(sort: String) {
        if (_currentSort.value != sort) {
            _currentSort.value = sort
            applySortAndEmit()
        }
    }

    fun loadNextPage() {
        if (_currentPage.value < _totalPages.value) {
            _currentPage.value += 1
            fetchProducts(reset = false)
        }
    }

    fun refresh() {
        fetchProducts(reset = true)
    }

    private fun fetchProducts(reset: Boolean) {
        if (reset) {
            _currentPage.value = 1
            allProducts.clear()
            _productsState.value = Resource.Loading()
        }

        viewModelScope.launch {
            val result = productRepository.getProducts(
                pageNumber = _currentPage.value,
                pageSize = 20,
                categoryId = categoryId,
                searchTerm = searchTerm
            )
            when (result) {
                is Resource.Success -> {
                    result.data?.let { paginationResponse ->
                        _totalPages.value = paginationResponse.pagination.totalPages
                        allProducts.addAll(paginationResponse.items)
                        applySortAndEmit()
                    }
                }
                is Resource.Error -> {
                    _productsState.value = Resource.Error(result.message ?: "Failed to load products")
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun applySortAndEmit() {
        val sorted = when (_currentSort.value) {
            "price" -> allProducts.sortedBy { it.salePrice ?: it.price }
            "name" -> allProducts.sortedBy { it.name.lowercase() }
            "rating" -> allProducts.sortedByDescending { it.avgRating ?: 0f }
            else -> allProducts.sortedByDescending { it.id } // "newest"
        }
        _productsState.value = Resource.Success(sorted)
    }
}
