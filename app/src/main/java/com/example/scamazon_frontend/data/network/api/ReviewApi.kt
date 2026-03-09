package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.review.CreateReviewRequest
import com.example.scamazon_frontend.data.models.review.ProductReviewSummaryResponse
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {

    // Backend: GET /api/Product/{productId}/reviews (absolute path on ReviewController)
    // BASE_URL = .../api/ so just use "Product/{productId}/reviews"
    @GET("Product/{productId}/reviews")
    suspend fun getProductReviews(
        @Path("productId") productId: Int,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<BackendApiResponse<ProductReviewSummaryResponse>>

    // Backend: POST /api/Product/{productId}/reviews
    @POST("Product/{productId}/reviews")
    suspend fun createReview(
        @Path("productId") productId: Int,
        @Body request: CreateReviewRequest
    ): Response<BackendApiResponse<Any>>

    // Backend: DELETE /api/admin/reviews/{reviewId}
    @DELETE("admin/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Int
    ): Response<BackendApiResponse<Any>>
}
