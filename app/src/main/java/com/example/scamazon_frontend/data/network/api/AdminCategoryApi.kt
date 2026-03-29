package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.admin.CreateBrandRequest
import com.example.scamazon_frontend.data.models.admin.CreateCategoryRequest
import com.example.scamazon_frontend.data.models.admin.UpdateBrandRequest
import com.example.scamazon_frontend.data.models.admin.UpdateCategoryRequest
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AdminCategoryApi {

    // ===== Category CRUD =====

    @POST("categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): Response<BackendApiResponse<com.example.scamazon_frontend.data.models.category.CategoryDto>>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: UpdateCategoryRequest
    ): Response<BackendApiResponse<Any>>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<BackendApiResponse<Any>>

    @Multipart
    @POST("categories/{id}/image")
    suspend fun uploadCategoryImage(
        @Path("id") id: Int,
        @Part image: MultipartBody.Part
    ): Response<BackendApiResponse<Any>>

    // ===== Brand CRUD =====

    @POST("brands")
    suspend fun createBrand(
        @Body request: CreateBrandRequest
    ): Response<BackendApiResponse<Any>>

    @PUT("brands/{id}")
    suspend fun updateBrand(
        @Path("id") id: Int,
        @Body request: UpdateBrandRequest
    ): Response<BackendApiResponse<Any>>

    @DELETE("brands/{id}")
    suspend fun deleteBrand(
        @Path("id") id: Int
    ): Response<BackendApiResponse<Any>>
}
