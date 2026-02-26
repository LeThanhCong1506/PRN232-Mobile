package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.admin.BackendAdminOrderDetailDto
import com.example.scamazon_frontend.data.models.admin.BackendAdminOrderDto
import com.example.scamazon_frontend.data.models.admin.BackendDashboardDto
import com.example.scamazon_frontend.data.models.admin.BackendUpdateOrderStatusRequest
import com.example.scamazon_frontend.data.models.admin.BackendUpdatePaymentStatusRequest
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import retrofit2.Response
import retrofit2.http.*

interface AdminOrderApi {

    @GET("admin/orders")
    suspend fun getAdminOrders(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String? = null,
        @Query("paymentStatus") paymentStatus: String? = null,
        @Query("search") search: String? = null
    ): Response<BackendApiResponse<BackendPagedResponse<BackendAdminOrderDto>>>

    @GET("admin/orders/{id}")
    suspend fun getAdminOrderDetail(
        @Path("id") orderId: Int
    ): Response<BackendApiResponse<BackendAdminOrderDetailDto>>

    @PUT("admin/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Body request: BackendUpdateOrderStatusRequest
    ): Response<BackendApiResponse<Any>>

    @PUT("admin/orders/{id}/payment-status")
    suspend fun updatePaymentStatus(
        @Path("id") orderId: Int,
        @Body request: BackendUpdatePaymentStatusRequest
    ): Response<BackendApiResponse<Any>>

    @GET("admin/dashboard")
    suspend fun getDashboard(): Response<BackendApiResponse<BackendDashboardDto>>
}
