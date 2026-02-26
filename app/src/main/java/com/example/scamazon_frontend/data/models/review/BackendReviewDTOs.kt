package com.example.scamazon_frontend.data.models.review

import com.google.gson.annotations.SerializedName

data class BackendProductReviewsDto(
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("totalReviews") val totalReviews: Int,
    @SerializedName("ratingDistribution") val ratingDistribution: Map<String, Int>,
    @SerializedName("reviews") val reviews: List<BackendReviewItemDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class BackendReviewItemDto(
    @SerializedName("reviewId") val reviewId: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("reviewer") val reviewer: String,
    @SerializedName("createdAt") val createdAt: String?
)
