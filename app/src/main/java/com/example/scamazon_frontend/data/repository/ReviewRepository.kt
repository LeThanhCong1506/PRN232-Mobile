package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.review.BackendProductReviewsDto
import com.example.scamazon_frontend.data.models.review.ReviewDto
import com.example.scamazon_frontend.data.models.review.ReviewListDataDto
import com.example.scamazon_frontend.data.models.review.ReviewPaginationDto
import com.example.scamazon_frontend.data.models.review.ReviewRequestDto
import com.example.scamazon_frontend.data.models.review.ReviewUserDto
import com.example.scamazon_frontend.data.network.api.ReviewApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewRepository(private val api: ReviewApi) {

    suspend fun getProductReviews(
        productId: Int,
        page: Int = 1,
        pageSize: Int = 10,
        rating: Int? = null
    ): Resource<Pair<BackendProductReviewsDto, ReviewListDataDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getProductReviews(productId, page, pageSize, rating)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val backendData = body.data
                        val reviewList = ReviewListDataDto(
                            reviews = backendData.reviews.map { item ->
                                ReviewDto(
                                    id = item.reviewId,
                                    rating = item.rating,
                                    comment = item.comment,
                                    user = ReviewUserDto(
                                        id = 0,
                                        username = item.reviewer,
                                        fullName = item.reviewer,
                                        avatarUrl = null
                                    ),
                                    createdAt = item.createdAt
                                )
                            },
                            pagination = ReviewPaginationDto(
                                currentPage = backendData.page,
                                totalPages = backendData.totalPages,
                                totalItems = backendData.totalReviews,
                                limit = backendData.pageSize,
                                hasNext = backendData.page < backendData.totalPages,
                                hasPrev = backendData.page > 1
                            )
                        )
                        Resource.Success(Pair(backendData, reviewList))
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

    suspend fun createReview(productId: Int, request: ReviewRequestDto): Resource<ReviewDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createReview(productId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
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
