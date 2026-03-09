package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.payment.PaymentStatusResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PaymentApi {
    @GET("payment/{orderId}/status")
    suspend fun getPaymentStatus(@Path("orderId") orderId: Int): Response<BackendApiResponse<PaymentStatusResponse>>
}
