package com.example.scamazon_frontend.data.models.product

import com.google.gson.annotations.SerializedName

// === Product List Item (GET /api/product) ===

data class BackendProductDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("sku") val sku: String?,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("productType") val productType: String?,
    @SerializedName("brand") val brand: BackendBrandRef?,
    @SerializedName("primaryImage") val primaryImage: String?,
    @SerializedName("categories") val categories: List<BackendCategoryRef>?,
    @SerializedName("inStock") val inStock: Boolean
)

data class BackendBrandRef(
    @SerializedName("brandId") val brandId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logoUrl") val logoUrl: String? = null
)

data class BackendCategoryRef(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("name") val name: String
)

// === Product Detail (GET /api/product/{id}) ===

data class BackendProductDetailDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("sku") val sku: String?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("productType") val productType: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("availableQuantity") val availableQuantity: Int?,
    @SerializedName("hasSerialTracking") val hasSerialTracking: Boolean?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("brand") val brand: BackendBrandRef?,
    @SerializedName("warrantyPolicy") val warrantyPolicy: BackendWarrantyPolicyDto?,
    @SerializedName("categories") val categories: List<BackendCategoryRef>?,
    @SerializedName("images") val images: List<BackendProductImageDto>?,
    @SerializedName("reviews") val reviews: BackendReviewSummaryDto?,
    @SerializedName("bundleComponents") val bundleComponents: List<BackendBundleComponentDto>?
)

data class BackendWarrantyPolicyDto(
    @SerializedName("policyId") val policyId: Int,
    @SerializedName("policyName") val policyName: String,
    @SerializedName("durationMonths") val durationMonths: Int,
    @SerializedName("description") val description: String?
)

data class BackendProductImageDto(
    @SerializedName("imageId") val imageId: Int,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("createdAt") val createdAt: String?
)

data class BackendReviewSummaryDto(
    @SerializedName("totalReviews") val totalReviews: Int,
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("fiveStarCount") val fiveStarCount: Int,
    @SerializedName("fourStarCount") val fourStarCount: Int,
    @SerializedName("threeStarCount") val threeStarCount: Int,
    @SerializedName("twoStarCount") val twoStarCount: Int,
    @SerializedName("oneStarCount") val oneStarCount: Int
)

data class BackendBundleComponentDto(
    @SerializedName("bundleId") val bundleId: Int,
    @SerializedName("childProductId") val childProductId: Int,
    @SerializedName("childProductName") val childProductName: String,
    @SerializedName("childProductSku") val childProductSku: String,
    @SerializedName("childProductPrice") val childProductPrice: Double,
    @SerializedName("quantity") val quantity: Int
)

// === Category with product count (GET /api/product/categories) ===

data class BackendCategoryWithCountDto(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("productCount") val productCount: Int
)

// === Brand with product count (GET /api/product/brands) ===

data class BackendBrandWithCountDto(
    @SerializedName("brandId") val brandId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("productCount") val productCount: Int
)
