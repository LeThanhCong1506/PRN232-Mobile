package com.example.scamazon_frontend.data.models.checkout

data class ValidateCheckoutRequest(
    val couponCode: String?
)

data class ValidateCheckoutResponse(
    val isValid: Boolean,
    val cartItems: List<CheckoutCartItemDto>?,
    val shippingInfo: CheckoutShippingInfoDto?,
    val coupon: CheckoutCouponDto?,
    val summary: CheckoutSummaryDto?,
    val stockErrors: List<StockErrorDto>?
)

data class CheckoutCartItemDto(
    val cartItemId: Int,
    val productId: Int,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val itemTotal: Double,
    val stockQuantity: Int,
    val isAvailable: Boolean
)

data class CheckoutShippingInfoDto(
    val username: String?,
    val email: String?,
    val phone: String?,
    val address: String?
)

data class CheckoutCouponDto(
    val isApplied: Boolean,
    val code: String?,
    val discountAmount: Double
)

data class CheckoutSummaryDto(
    val subtotal: Double,
    val shippingFee: Double,
    val discount: Double,
    val total: Double
)

data class StockErrorDto(
    val productId: Int,
    val productName: String,
    val requestedQuantity: Int,
    val availableQuantity: Int
)

data class ShippingInfoResponse(
    val username: String?,
    val email: String?,
    val phone: String?,
    val address: String?
)

data class PaymentMethodDto(
    val code: String,
    val name: String,
    val description: String?,
    val isActive: Boolean
)
