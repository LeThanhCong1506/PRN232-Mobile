package com.example.scamazon_frontend.data.models.order

data class CreateOrderRequest(
    val paymentMethod: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val province: String,
    val district: String,
    val ward: String,
    val streetAddress: String,
    val couponCode: String?,
    val notes: String?
)

data class CheckoutResponse(
    val orderId: Int,
    val orderNumber: String,
    val status: String?,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String?,
    val paymentReference: String?,
    val qrCodeUrl: String?,
    val checkoutUrl: String?,
    val paymentExpiredAt: String?,
    val message: String?
)

data class OrderFilterRequest(
    val status: String? = null,
    val paymentStatus: String? = null,
    val searchTerm: String? = null,
    val pageNumber: Int = 1,
    val pageSize: Int = 10
)

data class OrderResponse(
    val orderId: Int,
    val orderNumber: String,
    val customerName: String?,
    val customerPhone: String?,
    val subtotalAmount: Double?,
    val shippingFee: Double?,
    val discountAmount: Double?,
    val totalAmount: Double,
    val paymentMethod: String?,
    val paymentStatus: String?,
    val status: String,
    val itemCount: Int,
    val createdAt: String?,
    val updatedAt: String?
)

data class OrderDetailResponse(
    val orderId: Int,
    val orderNumber: String,
    val status: String,
    val customerName: String?,
    val customerEmail: String?,
    val customerPhone: String?,
    val province: String?,
    val district: String?,
    val ward: String?,
    val streetAddress: String?,
    val shippingAddress: String,
    val subtotalAmount: Double,
    val shippingFee: Double?,
    val discountAmount: Double?,
    val totalAmount: Double,
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val confirmedAt: String?,
    val shippedAt: String?,
    val deliveredAt: String?,
    val cancelledAt: String?,
    val cancelReason: String?,
    val trackingNumber: String?,
    val carrier: String?,
    val expectedDeliveryDate: String?,
    val items: List<OrderItemResponse>,
    val payment: OrderPaymentResponse?,
    val coupon: OrderCouponResponse?
)

data class OrderItemResponse(
    val orderItemId: Int,
    val productId: Int,
    val productName: String?,
    val productSku: String?,
    val productImageUrl: String?,
    val quantity: Int,
    val unitPrice: Double,
    val discountAmount: Double?,
    val subtotal: Double
)

data class OrderPaymentResponse(
    val paymentId: Int,
    val paymentMethod: String,
    val status: String,
    val amount: Double,
    val receivedAmount: Double?,
    val paymentReference: String?,
    val transactionId: String?,
    val qrCodeUrl: String?,
    val paymentDate: String?,
    val expiredAt: String?
)

data class OrderCouponResponse(
    val couponId: Int,
    val code: String,
    val discountValue: Double
)

data class CancelOrderRequest(
    val cancelReason: String
)
