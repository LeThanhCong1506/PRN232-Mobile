package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.checkout.*
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheckoutApi {
    @POST("checkout/validate")
    suspend fun validateCheckout(@Body request: ValidateCheckoutRequest): Response<BackendApiResponse<ValidateCheckoutResponse>>

    @GET("checkout/shipping-info")
    suspend fun getShippingInfo(): Response<BackendApiResponse<ShippingInfoResponse>>

    @GET("checkout/payment-methods")
    suspend fun getPaymentMethods(): Response<BackendApiResponse<List<PaymentMethodDto>>>
}
