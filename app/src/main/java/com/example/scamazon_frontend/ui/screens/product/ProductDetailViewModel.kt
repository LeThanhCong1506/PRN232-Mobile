package com.example.scamazon_frontend.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.KitAvailableStockDto
import com.example.scamazon_frontend.data.models.product.ProductDetailDto
import com.example.scamazon_frontend.data.models.product.RelatedProductDto
import com.example.scamazon_frontend.data.repository.CartRepository
import com.example.scamazon_frontend.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _productState = MutableStateFlow<Resource<ProductDetailDto>>(Resource.Loading())
    val productState: StateFlow<Resource<ProductDetailDto>> = _productState.asStateFlow()

    private val _addToCartState = MutableStateFlow<Resource<String>?>(null)
    val addToCartState: StateFlow<Resource<String>?> = _addToCartState.asStateFlow()

    private val _kitAvailableStock = MutableStateFlow<Resource<KitAvailableStockDto>?>(null)
    val kitAvailableStock: StateFlow<Resource<KitAvailableStockDto>?> = _kitAvailableStock.asStateFlow()

    private val _relatedProducts = MutableStateFlow<Resource<List<RelatedProductDto>>?>(null)
    val relatedProducts: StateFlow<Resource<List<RelatedProductDto>>?> = _relatedProducts.asStateFlow()

    fun loadProduct(productId: Int) {
        _productState.value = Resource.Loading()
        viewModelScope.launch {
            _productState.value = productRepository.getProductDetail(productId)
        }
    }

    // Keep backward compatibility for slug-based navigation
    fun loadProduct(slug: String) {
        // If slug is actually a numeric ID, parse it
        val id = slug.toIntOrNull()
        if (id != null) {
            loadProduct(id)
        } else {
            _productState.value = Resource.Error("Invalid product identifier")
        }
    }

    fun loadKitAvailableStock(kitId: Int) {
        viewModelScope.launch {
            _kitAvailableStock.value = Resource.Loading()
            _kitAvailableStock.value = productRepository.getKitAvailableStock(kitId)
        }
    }

    fun loadRelatedProducts(productId: Int) {
        viewModelScope.launch {
            _relatedProducts.value = Resource.Loading()
            _relatedProducts.value = productRepository.getRelatedProducts(productId)
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        _addToCartState.value = Resource.Loading()
        viewModelScope.launch {
            _addToCartState.value = cartRepository.addToCart(productId, quantity)
        }
    }

    fun resetAddToCartState() {
        _addToCartState.value = null
    }
}
