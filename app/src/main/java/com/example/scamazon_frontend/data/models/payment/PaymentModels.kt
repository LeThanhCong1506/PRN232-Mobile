package com.example.scamazon_frontend.data.models.payment

data class PaymentStatusResponse(
    val orderId: Int,
    val orderNumber: String?,
    val paymentMethod: String,
    val paymentStatus: String,
    val amount: Double,
    val receivedAmount: Double?,
    val qrCodeUrl: String?,
    val paymentReference: String?,
    val expiredAt: String?,
    val isPaid: Boolean,
    val remainingSeconds: Int
)
