package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.BackendUpdateOrderStatusRequest
import com.example.scamazon_frontend.data.models.admin.BackendUpdatePaymentStatusRequest
import com.example.scamazon_frontend.data.models.admin.CustomerStatsDto
import com.example.scamazon_frontend.data.models.admin.DashboardStatsDto
import com.example.scamazon_frontend.data.models.admin.OrderStatsDto
import com.example.scamazon_frontend.data.models.admin.ProductStatsDto
import com.example.scamazon_frontend.data.models.admin.RevenueStatsDto
import com.example.scamazon_frontend.data.models.admin.ChatStatsDto
import com.example.scamazon_frontend.data.models.order.AdminOrderListDataDto
import com.example.scamazon_frontend.data.models.order.AdminOrderSummaryDto
import com.example.scamazon_frontend.data.models.order.OrderDetailDataDto
import com.example.scamazon_frontend.data.models.order.OrderItemDto
import com.example.scamazon_frontend.data.models.order.PaymentInfoDto
import com.example.scamazon_frontend.data.network.api.AdminOrderApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminOrderRepository(private val api: AdminOrderApi) {

    suspend fun getAdminOrders(
        page: Int = 1,
        pageSize: Int = 20,
        status: String? = null,
        paymentStatus: String? = null,
        search: String? = null
    ): Resource<List<AdminOrderSummaryDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAdminOrders(page, pageSize, status, paymentStatus, search)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val orders = body.data.items.map { backend ->
                            AdminOrderSummaryDto(
                                id = backend.orderId,
                                orderCode = backend.orderNumber,
                                customerName = backend.customerName,
                                customerPhone = backend.customerPhone,
                                total = backend.totalAmount,
                                status = backend.status,
                                paymentMethod = backend.paymentMethod,
                                paymentStatus = backend.paymentStatus,
                                itemCount = backend.itemCount,
                                createdAt = backend.createdAt
                            )
                        }
                        Resource.Success(orders)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load orders")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getAdminOrderDetail(orderId: Int): Resource<OrderDetailDataDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAdminOrderDetail(orderId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val detail = body.data
                        val orderDetail = OrderDetailDataDto(
                            id = detail.orderId,
                            orderCode = detail.orderNumber,
                            shippingName = detail.customerName ?: "",
                            shippingPhone = detail.customerPhone ?: "",
                            shippingAddress = detail.shippingAddress ?: "",
                            shippingCity = detail.province,
                            shippingDistrict = detail.district,
                            shippingWard = detail.ward,
                            subtotal = detail.subtotalAmount,
                            shippingFee = detail.shippingFee ?: 0.0,
                            discount = detail.discountAmount ?: 0.0,
                            total = detail.totalAmount,
                            status = detail.status,
                            note = detail.notes,
                            createdAt = detail.createdAt,
                            items = detail.items.map { item ->
                                OrderItemDto(
                                    id = item.orderItemId,
                                    productId = item.productId,
                                    productName = item.productName ?: "",
                                    productImage = item.productImageUrl,
                                    price = item.unitPrice,
                                    quantity = item.quantity,
                                    subtotal = item.subtotal
                                )
                            },
                            payment = detail.payment?.let { p ->
                                PaymentInfoDto(
                                    paymentMethod = p.paymentMethod ?: "",
                                    amount = p.amount,
                                    status = p.paymentStatus,
                                    transactionId = p.transactionId,
                                    paidAt = p.paymentDate
                                )
                            }
                        )
                        Resource.Success(orderDetail)
                    } else {
                        Resource.Error(body?.message ?: "Order not found")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String, note: String? = null): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateOrderStatus(orderId, BackendUpdateOrderStatusRequest(newStatus, note))
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to update status")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updatePaymentStatus(orderId: Int, newPaymentStatus: String, note: String? = null): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updatePaymentStatus(orderId, BackendUpdatePaymentStatusRequest(newPaymentStatus, note))
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to update payment status")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getDashboard(): Resource<DashboardStatsDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getDashboard()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val d = body.data
                        val stats = DashboardStatsDto(
                            customers = CustomerStatsDto(
                                total = d.customers.totalCustomers,
                                new7Days = d.customers.newCustomersThisMonth
                            ),
                            products = ProductStatsDto(
                                total = d.products.totalProducts,
                                lowStock = d.products.lowStockProducts
                            ),
                            orders = OrderStatsDto(
                                pending = d.orders.pendingOrders,
                                confirmed = d.orders.confirmedOrders,
                                shipping = d.orders.shippedOrders,
                                delivered = d.orders.deliveredOrders,
                                today = d.orders.totalOrders
                            ),
                            revenue = RevenueStatsDto(
                                today = if (d.orders.monthlyRevenue > 0) d.orders.monthlyRevenue / 30 else 0.0,
                                week = if (d.orders.monthlyRevenue > 0) d.orders.monthlyRevenue / 4 else 0.0,
                                month = d.orders.monthlyRevenue
                            ),
                            chats = ChatStatsDto(active = 0)
                        )
                        Resource.Success(stats)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load dashboard")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
