package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.order.CancelOrderRequest
import com.example.scamazon_frontend.data.models.order.CheckoutResponse
import com.example.scamazon_frontend.data.models.order.CreateOrderRequest
import com.example.scamazon_frontend.data.models.order.OrderDetailResponse
import com.example.scamazon_frontend.data.models.order.OrderResponse
import com.example.scamazon_frontend.data.network.api.OrderApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(
    private val orderApi: OrderApi
) {
    suspend fun checkout(request: CreateOrderRequest): Resource<CheckoutResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.checkout(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Checkout failed without a message")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Checkout request failed (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during checkout")
            }
        }
    }

    suspend fun getMyOrders(
        status: String? = null,
        paymentStatus: String? = null,
        search: String? = null,
        pageNumber: Int = 1,
        pageSize: Int = 10
    ): Resource<BackendPagedResponse<OrderResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.getMyOrders(status, paymentStatus, search, pageNumber, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get orders")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to get orders (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching orders")
            }
        }
    }

    suspend fun getOrderDetail(id: Int): Resource<OrderDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.getOrderDetail(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get order details")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to get order details (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching order details")
            }
        }
    }

    suspend fun cancelOrder(id: Int, reason: String): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.cancelOrder(id, CancelOrderRequest(cancelReason = reason))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(true)
                    } else {
                        Resource.Error(body?.message ?: "Failed to cancel order")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to cancel order (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred canceling order")
            }
        }
    }
}
