package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.review.BackendProductReviewsDto
import com.example.scamazon_frontend.data.models.review.ReviewDto
import com.example.scamazon_frontend.data.models.review.ReviewRequestDto
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {

    @GET("products/{productId}/reviews")
    suspend fun getProductReviews(
        @Path("productId") productId: Int,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("rating") rating: Int? = null
    ): Response<BackendApiResponse<BackendProductReviewsDto>>

    @POST("products/{productId}/reviews")
    suspend fun createReview(
        @Path("productId") productId: Int,
        @Body request: ReviewRequestDto
    ): Response<BackendApiResponse<ReviewDto>>

    @DELETE("admin/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Int
    ): Response<BackendApiResponse<Any>>
}
