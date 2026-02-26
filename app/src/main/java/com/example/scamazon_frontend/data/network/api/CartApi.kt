package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.cart.AddToCartRequest
import com.example.scamazon_frontend.data.models.cart.BackendCartResponseDto
import com.example.scamazon_frontend.data.models.cart.UpdateCartItemRequest
import com.example.scamazon_frontend.data.models.cart.ValidateCouponRequest
import com.example.scamazon_frontend.data.models.cart.ValidateCouponResponseDto
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApi {

    @GET("cart")
    suspend fun getCart(): Response<BackendApiResponse<BackendCartResponseDto>>

    @POST("cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<BackendApiResponse<Any>>

    @PUT("cart/items/{cartItemId}")
    suspend fun updateCartItem(
        @Path("cartItemId") cartItemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<BackendApiResponse<Any>>

    @DELETE("cart/items/{cartItemId}")
    suspend fun removeCartItem(@Path("cartItemId") cartItemId: Int): Response<BackendApiResponse<Any>>

    @DELETE("cart")
    suspend fun clearCart(): Response<BackendApiResponse<Any>>

    @POST("cart/validate-coupon")
    suspend fun validateCoupon(@Body request: ValidateCouponRequest): Response<BackendApiResponse<ValidateCouponResponseDto>>
}
