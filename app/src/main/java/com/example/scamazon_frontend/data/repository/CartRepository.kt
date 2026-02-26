package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.cart.*
import com.example.scamazon_frontend.data.network.api.CartApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(private val api: CartApi) {

    suspend fun getCart(): Resource<CartDataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getCart()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data.toCartDataDto())
                    } else {
                        Resource.Error(body?.message ?: "Failed to load cart")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun addToCart(productId: Int, quantity: Int): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.addToCart(AddToCartRequest(productId, quantity))
                if (response.isSuccessful) {
                    Resource.Success("Added to cart")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to add to cart")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateCartItem(cartItemId: Int, quantity: Int): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateCartItem(cartItemId, UpdateCartItemRequest(quantity))
                if (response.isSuccessful) {
                    Resource.Success("Updated")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to update item")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun removeCartItem(cartItemId: Int): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.removeCartItem(cartItemId)
                if (response.isSuccessful) {
                    Resource.Success("Removed")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to remove item")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun clearCart(): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.clearCart()
                if (response.isSuccessful) {
                    Resource.Success("Cart cleared")
                } else {
                    Resource.Error("Failed to clear cart")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun validateCoupon(couponCode: String): Resource<ValidateCouponResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.validateCoupon(ValidateCouponRequest(couponCode))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Invalid coupon")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Invalid coupon")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    // === Mapping functions ===

    private fun BackendCartResponseDto.toCartDataDto(): CartDataDto {
        return CartDataDto(
            cartId = cartId,
            items = items.map { it.toCartItemDto() },
            subtotal = summary.subtotal,
            totalItems = summary.totalItems,
            shippingFee = summary.shippingFee,
            discount = summary.discount,
            total = summary.total
        )
    }

    private fun BackendCartItemDto.toCartItemDto(): CartItemDto {
        return CartItemDto(
            id = cartItemId,
            productId = product.productId,
            productName = product.name,
            productImage = product.primaryImage,
            price = product.price,
            salePrice = null,
            quantity = quantity,
            itemTotal = itemTotal,
            stockQuantity = product.stockQuantity,
            isActive = product.inStock
        )
    }
}
