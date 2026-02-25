package com.example.scamazon_frontend.ui.screens.cart

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.cart.CartDataDto
import com.example.scamazon_frontend.data.models.cart.CartItemDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {

    private val _cartState = MutableStateFlow<Resource<CartDataDto>>(Resource.Loading())
    val cartState: StateFlow<Resource<CartDataDto>> = _cartState.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private val cartItems = MockData.cartData.items.toMutableList()

    init {
        fetchCart()
    }

    fun fetchCart() {
        rebuildCart()
    }

    fun updateQuantity(itemId: Int, quantity: Int) {
        val index = cartItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = cartItems[index]
            cartItems[index] = item.copy(quantity = quantity, itemTotal = (item.salePrice ?: item.price) * quantity)
            rebuildCart()
        }
    }

    fun removeItem(itemId: Int) {
        cartItems.removeAll { it.id == itemId }
        rebuildCart()
    }

    fun clearMessage() {
        _operationMessage.value = null
    }

    private fun rebuildCart() {
        val subtotal = cartItems.sumOf { it.itemTotal }
        val totalItems = cartItems.sumOf { it.quantity }
        _cartState.value = Resource.Success(
            CartDataDto(cartId = 1, items = cartItems.toList(), subtotal = subtotal, totalItems = totalItems)
        )
    }
}
