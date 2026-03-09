package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.review.ReviewResponse
import com.example.scamazon_frontend.data.models.review.CreateReviewRequest
import com.example.scamazon_frontend.data.models.review.ProductReviewSummaryResponse
import com.example.scamazon_frontend.data.network.api.ReviewApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewRepository(private val api: ReviewApi) {

    suspend fun getProductReviews(
        productId: Int,
        page: Int = 1,
        pageSize: Int = 10
    ): Resource<ProductReviewSummaryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getProductReviews(productId, page, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load reviews")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun createReview(productId: Int, request: CreateReviewRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createReview(productId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(Unit)
                    } else {
                        Resource.Error(body?.message ?: "Failed to create review")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun deleteReview(reviewId: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteReview(reviewId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to delete review")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
