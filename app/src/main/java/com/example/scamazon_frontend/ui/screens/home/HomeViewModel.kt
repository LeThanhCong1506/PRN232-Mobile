package com.example.scamazon_frontend.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.models.product.ProductPaginationResponse
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _categoriesState = MutableStateFlow<Resource<List<CategoryDto>>>(Resource.Loading())
    val categoriesState: StateFlow<Resource<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _flashSaleState = MutableStateFlow<Resource<ProductPaginationResponse>>(Resource.Loading())
    val flashSaleState: StateFlow<Resource<ProductPaginationResponse>> = _flashSaleState.asStateFlow()

    private val _megaSaleState = MutableStateFlow<Resource<ProductPaginationResponse>>(Resource.Loading())
    val megaSaleState: StateFlow<Resource<ProductPaginationResponse>> = _megaSaleState.asStateFlow()
    
    private val _recommendedState = MutableStateFlow<Resource<ProductPaginationResponse>>(Resource.Loading())
    val recommendedState: StateFlow<Resource<ProductPaginationResponse>> = _recommendedState.asStateFlow()

    init {
        fetchHomeData()
    }

    fun fetchHomeData() {
        _categoriesState.value = Resource.Success(MockData.categories)
        _flashSaleState.value = Resource.Success(MockData.getProductsPaginated(limit = 5, sort = "newest"))
        _megaSaleState.value = Resource.Success(MockData.getProductsPaginated(limit = 5, sort = "price_asc"))
        _recommendedState.value = Resource.Success(MockData.getProductsPaginated(limit = 10, sort = "popular"))
    }
}
