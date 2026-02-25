package com.example.scamazon_frontend.ui.screens.search

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _products = MutableStateFlow<Resource<List<ProductDto>>>(Resource.Loading())
    val products: StateFlow<Resource<List<ProductDto>>> = _products.asStateFlow()

    private val _sortBy = MutableStateFlow("newest")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val rawProducts = mutableListOf<ProductDto>()

    init {
        searchProducts()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchProducts()
    }

    fun onSortChanged(sort: String) {
        _sortBy.value = sort
        applySortAndEmit()
    }

    fun searchProducts() {
        _products.value = Resource.Loading()
        val query = _searchQuery.value.lowercase()
        rawProducts.clear()
        rawProducts.addAll(
            if (query.isBlank()) MockData.products
            else MockData.products.filter { it.name.lowercase().contains(query) }
        )
        applySortAndEmit()
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
