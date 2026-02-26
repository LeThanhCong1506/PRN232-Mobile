package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.models.product.*
import com.example.scamazon_frontend.data.network.api.ProductApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(private val api: ProductApi) {

    suspend fun getProducts(
        pageNumber: Int = 1,
        pageSize: Int = 10,
        searchTerm: String? = null,
        categoryId: Int? = null,
        brandId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ): Resource<ProductPaginationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getProducts(pageNumber, pageSize, searchTerm, categoryId, brandId, minPrice, maxPrice)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val pagedData = body.data
                        val products = pagedData.items.map { it.toProductDto() }
                        val pagination = PaginationMetadata(
                            currentPage = pagedData.pagination.currentPage,
                            totalPages = pagedData.pagination.totalPages,
                            totalItems = pagedData.pagination.totalItems
                        )
                        Resource.Success(ProductPaginationResponse(items = products, pagination = pagination))
                    } else {
                        Resource.Error(body?.message ?: "Failed to load products")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getProductDetail(productId: Int): Resource<ProductDetailDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getProductDetail(productId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data.toProductDetailDto())
                    } else {
                        Resource.Error(body?.message ?: "Product not found")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getCategories(): Resource<List<CategoryDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getCategories()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val categories = body.data.map {
                            CategoryDto(
                                id = it.categoryId,
                                name = it.name,
                                slug = "",
                                description = null,
                                imageUrl = null,
                                parentId = null,
                                isActive = true,
                                createdAt = ""
                            )
                        }
                        Resource.Success(categories)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load categories")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    // === Mapping functions ===

    private fun BackendProductDto.toProductDto(): ProductDto {
        return ProductDto(
            id = productId,
            name = name,
            slug = "",
            description = null,
            detailDescription = null,
            price = price,
            salePrice = null,
            stockQuantity = stockQuantity,
            categoryId = categories?.firstOrNull()?.categoryId,
            brandId = brand?.brandId,
            primaryImage = primaryImage,
            images = null,
            avgRating = null,
            soldCount = null,
            isActive = inStock,
            isFeatured = false,
            createdAt = null
        )
    }

    private fun BackendProductDetailDto.toProductDetailDto(): ProductDetailDto {
        return ProductDetailDto(
            id = productId,
            name = name,
            slug = "",
            description = description,
            detailDescription = null,
            specifications = null,
            price = price,
            salePrice = null,
            discountPercent = null,
            stockQuantity = stockQuantity,
            stockStatus = if (stockQuantity > 0) "In Stock" else "Out of Stock",
            category = categories?.firstOrNull()?.let {
                CategoryInfoDto(id = it.categoryId, name = it.name, slug = "")
            },
            brand = brand?.let {
                BrandInfoDto(id = it.brandId, name = it.name, slug = "", logoUrl = it.logoUrl)
            },
            images = images?.map {
                ProductImageDto(id = it.imageId, imageUrl = it.imageUrl, isPrimary = false, sortOrder = 0)
            } ?: emptyList(),
            ratingSummary = reviews?.let {
                RatingSummaryDto(
                    avgRating = it.averageRating.toFloat(),
                    totalReviews = it.totalReviews,
                    ratingBreakdown = RatingBreakdownDto(
                        five = it.fiveStarCount,
                        four = it.fourStarCount,
                        three = it.threeStarCount,
                        two = it.twoStarCount,
                        one = it.oneStarCount
                    )
                )
            },
            viewCount = null,
            soldCount = null,
            isFeatured = null,
            createdAt = createdAt,
            updatedAt = null
        )
    }
}
