package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.product.BackendBrandWithCountDto
import com.example.scamazon_frontend.data.models.product.BackendCategoryWithCountDto
import com.example.scamazon_frontend.data.models.product.BackendProductDetailDto
import com.example.scamazon_frontend.data.models.product.BackendProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("product")
    suspend fun getProducts(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("searchTerm") searchTerm: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("brandId") brandId: Int? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null
    ): Response<BackendApiResponse<BackendPagedResponse<BackendProductDto>>>

    @GET("product/{id}")
    suspend fun getProductDetail(
        @Path("id") productId: Int
    ): Response<BackendApiResponse<BackendProductDetailDto>>

    @GET("product/categories")
    suspend fun getCategories(): Response<BackendApiResponse<List<BackendCategoryWithCountDto>>>

    @GET("product/brands")
    suspend fun getBrands(): Response<BackendApiResponse<List<BackendBrandWithCountDto>>>
}
