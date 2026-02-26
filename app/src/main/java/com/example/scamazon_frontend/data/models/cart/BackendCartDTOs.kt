package com.example.scamazon_frontend.data.models.cart

import com.google.gson.annotations.SerializedName

// === Cart Response (GET /api/cart) ===

data class BackendCartResponseDto(
    @SerializedName("cartId") val cartId: Int,
    @SerializedName("items") val items: List<BackendCartItemDto>,
    @SerializedName("summary") val summary: BackendCartSummaryDto
)

data class BackendCartItemDto(
    @SerializedName("cartItemId") val cartItemId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("product") val product: BackendCartProductDto,
    @SerializedName("itemTotal") val itemTotal: Double
)

data class BackendCartProductDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("primaryImage") val primaryImage: String?,
    @SerializedName("inStock") val inStock: Boolean
)

data class BackendCartSummaryDto(
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("shippingFee") val shippingFee: Double,
    @SerializedName("discount") val discount: Double,
    @SerializedName("total") val total: Double
)

// === Coupon ===

data class ValidateCouponRequest(
    @SerializedName("couponCode") val couponCode: String
)

data class ValidateCouponResponseDto(
    @SerializedName("couponId") val couponId: Int,
    @SerializedName("code") val code: String,
    @SerializedName("discountValue") val discountValue: Double,
    @SerializedName("calculatedDiscount") val calculatedDiscount: Double,
    @SerializedName("cartSubtotal") val cartSubtotal: Double,
    @SerializedName("newTotal") val newTotal: Double,
    @SerializedName("message") val message: String?
)
