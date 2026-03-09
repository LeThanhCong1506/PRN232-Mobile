package com.example.scamazon_frontend.data.models.review

import com.google.gson.annotations.SerializedName

// ==================== Response ====================

/**
 * Maps to backend ReviewItemResponse:
 *   reviewId, rating, comment, reviewer, createdAt
 * Extra fields are nullable to handle gracefully.
 */
data class ReviewResponse(
    @SerializedName("reviewId") val reviewId: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("reviewer") val reviewer: String?,
    @SerializedName("createdAt") val createdAt: String?
)

/**
 * Maps to backend ProductReviewsResponse:
 *   averageRating, totalReviews, ratingDistribution, reviews (flat list), page, pageSize, totalPages
 * Backend puts reviews/page/pageSize/totalPages at the TOP level (not nested).
 */
data class ProductReviewSummaryResponse(
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("totalReviews") val totalReviews: Int,
    @SerializedName("ratingDistribution") val ratingDistribution: Map<String, Int>?,
    @SerializedName("reviews") val reviews: List<ReviewResponse>?,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int
)

// ==================== Request ====================

data class CreateReviewRequest(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?
)
