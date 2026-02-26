package com.example.scamazon_frontend.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.cart.CartDataDto
import com.example.scamazon_frontend.data.models.cart.ValidateCouponResponseDto
import com.example.scamazon_frontend.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<Resource<CartDataDto>>(Resource.Loading())
    val cartState: StateFlow<Resource<CartDataDto>> = _cartState.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private val _couponState = MutableStateFlow<Resource<ValidateCouponResponseDto>?>(null)
    val couponState: StateFlow<Resource<ValidateCouponResponseDto>?> = _couponState.asStateFlow()

    init {
        fetchCart()
    }

    fun fetchCart() {
        _cartState.value = Resource.Loading()
        viewModelScope.launch {
            _cartState.value = cartRepository.getCart()
        }
    }

    fun updateQuantity(itemId: Int, quantity: Int) {
        viewModelScope.launch {
            val result = cartRepository.updateCartItem(itemId, quantity)
            if (result is Resource.Error) {
                _operationMessage.value = result.message
            }
            fetchCart()
        }
    }

    fun removeItem(itemId: Int) {
        viewModelScope.launch {
            val result = cartRepository.removeCartItem(itemId)
            if (result is Resource.Error) {
                _operationMessage.value = result.message
            }
            fetchCart()
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val result = cartRepository.clearCart()
            if (result is Resource.Error) {
                _operationMessage.value = result.message
            }
            fetchCart()
        }
    }

    fun applyCoupon(code: String) {
        _couponState.value = Resource.Loading()
        viewModelScope.launch {
            val result = cartRepository.validateCoupon(code)
            _couponState.value = result
            when (result) {
                is Resource.Success -> {
                    _operationMessage.value = result.data?.message ?: "Coupon applied!"
                    fetchCart()
                }
                is Resource.Error -> {
                    _operationMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    fun clearMessage() {
        _operationMessage.value = null
    }
}
