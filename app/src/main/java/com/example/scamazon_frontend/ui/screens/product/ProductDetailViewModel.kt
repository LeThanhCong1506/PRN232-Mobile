package com.example.scamazon_frontend.ui.screens.product

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.cart.CartDataDto
import com.example.scamazon_frontend.data.models.product.ProductDetailDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductDetailViewModel : ViewModel() {

    private val _productState = MutableStateFlow<Resource<ProductDetailDto>>(Resource.Loading())
    val productState: StateFlow<Resource<ProductDetailDto>> = _productState.asStateFlow()

    private val _addToCartState = MutableStateFlow<Resource<CartDataDto>?>(null)
    val addToCartState: StateFlow<Resource<CartDataDto>?> = _addToCartState.asStateFlow()

    fun loadProduct(slug: String) {
        _productState.value = Resource.Loading()
        _productState.value = Resource.Success(MockData.getProductDetail(slug))
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        _addToCartState.value = Resource.Loading()
        // Simulate adding to cart
        _addToCartState.value = Resource.Success(MockData.cartData)
    }

    fun resetAddToCartState() {
        _addToCartState.value = null
    }
}
