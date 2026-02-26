package com.example.scamazon_frontend.data.models.admin

import com.google.gson.annotations.SerializedName

// ==================== Admin Product ====================

data class BackendAdminProductDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String?,
    @SerializedName("productType") val productType: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("brandName") val brandName: String?,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("primaryImage") val primaryImage: String?,
    @SerializedName("lowStock") val lowStock: Boolean
)

data class BackendAdminCreateProductResponseDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String?
)

data class BackendAdminProductImageDto(
    @SerializedName("imageId") val imageId: Int,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("isPrimary") val isPrimary: Boolean
)

data class BackendAdminImagesResponseDto(
    @SerializedName("uploadedImages") val uploadedImages: List<BackendAdminProductImageDto>
)

// ==================== Admin Order ====================

data class BackendAdminOrderDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("status") val status: String?,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("customerEmail") val customerEmail: String?,
    @SerializedName("customerPhone") val customerPhone: String?,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("itemCount") val itemCount: Int,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class BackendAdminOrderDetailDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("status") val status: String?,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("customerEmail") val customerEmail: String?,
    @SerializedName("customerPhone") val customerPhone: String?,
    @SerializedName("shippingAddress") val shippingAddress: String?,
    @SerializedName("province") val province: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("ward") val ward: String?,
    @SerializedName("streetAddress") val streetAddress: String?,
    @SerializedName("subtotalAmount") val subtotalAmount: Double,
    @SerializedName("shippingFee") val shippingFee: Double?,
    @SerializedName("discountAmount") val discountAmount: Double?,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("payment") val payment: BackendAdminPaymentInfoDto?,
    @SerializedName("items") val items: List<BackendAdminOrderItemDto>,
    @SerializedName("allowedStatusTransitions") val allowedStatusTransitions: List<String>
)

data class BackendAdminPaymentInfoDto(
    @SerializedName("paymentId") val paymentId: Int,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("transactionId") val transactionId: String?,
    @SerializedName("paymentDate") val paymentDate: String?
)

data class BackendAdminOrderItemDto(
    @SerializedName("orderItemId") val orderItemId: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("productName") val productName: String?,
    @SerializedName("productSku") val productSku: String?,
    @SerializedName("productImageUrl") val productImageUrl: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("subtotal") val subtotal: Double
)

data class BackendUpdateOrderStatusRequest(
    @SerializedName("newStatus") val newStatus: String,
    @SerializedName("note") val note: String? = null
)

data class BackendUpdatePaymentStatusRequest(
    @SerializedName("newPaymentStatus") val newPaymentStatus: String,
    @SerializedName("note") val note: String? = null
)

// ==================== Dashboard ====================

data class BackendDashboardDto(
    @SerializedName("orders") val orders: BackendOrderStatsDto,
    @SerializedName("products") val products: BackendProductStatsDto,
    @SerializedName("customers") val customers: BackendCustomerStatsDto,
    @SerializedName("recentOrders") val recentOrders: List<BackendRecentOrderDto>
)

data class BackendOrderStatsDto(
    @SerializedName("totalOrders") val totalOrders: Int,
    @SerializedName("pendingOrders") val pendingOrders: Int,
    @SerializedName("confirmedOrders") val confirmedOrders: Int,
    @SerializedName("shippedOrders") val shippedOrders: Int,
    @SerializedName("deliveredOrders") val deliveredOrders: Int,
    @SerializedName("cancelledOrders") val cancelledOrders: Int,
    @SerializedName("totalRevenue") val totalRevenue: Double,
    @SerializedName("monthlyRevenue") val monthlyRevenue: Double
)

data class BackendProductStatsDto(
    @SerializedName("totalProducts") val totalProducts: Int,
    @SerializedName("activeProducts") val activeProducts: Int,
    @SerializedName("lowStockProducts") val lowStockProducts: Int,
    @SerializedName("outOfStockProducts") val outOfStockProducts: Int
)

data class BackendCustomerStatsDto(
    @SerializedName("totalCustomers") val totalCustomers: Int,
    @SerializedName("newCustomersThisMonth") val newCustomersThisMonth: Int
)

data class BackendRecentOrderDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("status") val status: String?,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("createdAt") val createdAt: String?
)
