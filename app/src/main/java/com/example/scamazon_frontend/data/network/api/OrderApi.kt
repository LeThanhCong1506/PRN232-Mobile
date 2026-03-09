package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.order.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("order/checkout")
    suspend fun checkout(@Body request: CreateOrderRequest): Response<BackendApiResponse<CheckoutResponse>>

    @GET("order/my-orders")
    suspend fun getMyOrders(
        @Query("status") status: String?,
        @Query("paymentStatus") paymentStatus: String?,
        @Query("searchTerm") search: String?,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<BackendApiResponse<BackendPagedResponse<OrderResponse>>>

    @GET("order/{id}")
    suspend fun getOrderDetail(@Path("id") id: Int): Response<BackendApiResponse<OrderDetailResponse>>

    @PUT("order/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: Int,
        @Body request: CancelOrderRequest
    ): Response<BackendApiResponse<Any>>
}
