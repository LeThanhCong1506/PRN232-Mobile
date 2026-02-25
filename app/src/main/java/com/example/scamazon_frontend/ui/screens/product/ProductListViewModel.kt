package com.example.scamazon_frontend.ui.screens.product

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductListViewModel : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<ProductDto>>>(Resource.Loading())
    val productsState: StateFlow<Resource<List<ProductDto>>> = _productsState.asStateFlow()

    private val _currentSort = MutableStateFlow("newest")
    val currentSort: StateFlow<String> = _currentSort.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private var categoryId: Int? = null
    private val allProducts = mutableListOf<ProductDto>()

    fun init(categoryId: Int?) {
        this.categoryId = categoryId
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

        val result = MockData.getProductsPaginated(
            page = _currentPage.value,
            limit = 20,
            categoryId = categoryId
        )
        _totalPages.value = result.pagination.totalPages
        allProducts.addAll(result.items)
        applySortAndEmit()
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
