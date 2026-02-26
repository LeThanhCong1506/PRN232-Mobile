package com.example.scamazon_frontend.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _products = MutableStateFlow<Resource<List<ProductDto>>>(Resource.Loading())
    val products: StateFlow<Resource<List<ProductDto>>> = _products.asStateFlow()

    private val _sortBy = MutableStateFlow("newest")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val rawProducts = mutableListOf<ProductDto>()
    private var searchJob: Job? = null

    init {
        searchProducts()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Debounce search - wait 300ms after user stops typing
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchProducts()
        }
    }

    fun onSortChanged(sort: String) {
        _sortBy.value = sort
        applySortAndEmit()
    }

    fun searchProducts() {
        _products.value = Resource.Loading()
        viewModelScope.launch {
            val query = _searchQuery.value.ifBlank { null }
            val result = productRepository.getProducts(
                pageNumber = 1,
                pageSize = 50,
                searchTerm = query
            )
            when (result) {
                is Resource.Success -> {
                    rawProducts.clear()
                    rawProducts.addAll(result.data?.items ?: emptyList())
                    applySortAndEmit()
                }
                is Resource.Error -> {
                    _products.value = Resource.Error(result.message ?: "Failed to load products")
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun applySortAndEmit() {
        val sorted = when (_sortBy.value) {
            "price" -> rawProducts.sortedBy { it.salePrice ?: it.price }
            "name" -> rawProducts.sortedBy { it.name.lowercase() }
            "rating" -> rawProducts.sortedByDescending { it.avgRating ?: 0f }
            else -> rawProducts.sortedByDescending { it.id } // "newest"
        }
        _products.value = Resource.Success(sorted)
    }
}
