package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.admin.BackendAdminCreateProductResponseDto
import com.example.scamazon_frontend.data.models.admin.BackendAdminImagesResponseDto
import com.example.scamazon_frontend.data.models.admin.BackendAdminProductDto
import com.example.scamazon_frontend.data.models.admin.CreateProductRequest
import com.example.scamazon_frontend.data.models.admin.UpdateProductRequest
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AdminProductApi {

    @GET("admin/products")
    suspend fun getAdminProducts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("brandId") brandId: Int? = null,
        @Query("productType") productType: String? = null,
        @Query("lowStock") lowStock: Boolean? = null
    ): Response<BackendApiResponse<BackendPagedResponse<BackendAdminProductDto>>>

    @POST("admin/products")
    suspend fun createProduct(
        @Body request: CreateProductRequest
    ): Response<BackendApiResponse<BackendAdminCreateProductResponseDto>>

    @PUT("admin/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: UpdateProductRequest
    ): Response<BackendApiResponse<Any>>

    @DELETE("admin/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): Response<BackendApiResponse<Any>>

    @Multipart
    @POST("admin/products/{id}/images")
    suspend fun uploadImages(
        @Path("id") productId: Int,
        @Part images: List<MultipartBody.Part>,
        @Part("setPrimaryIndex") setPrimaryIndex: RequestBody
    ): Response<BackendApiResponse<BackendAdminImagesResponseDto>>

    @DELETE("admin/products/{id}/images/{imageId}")
    suspend fun deleteImage(
        @Path("id") productId: Int,
        @Path("imageId") imageId: Int
    ): Response<BackendApiResponse<Any>>
}
