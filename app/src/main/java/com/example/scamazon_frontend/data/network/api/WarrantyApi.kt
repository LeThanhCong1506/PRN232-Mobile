package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimPagedDto
import com.example.scamazon_frontend.data.models.warranty.CustomerWarrantyClaimPagedDto
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.CreateWarrantyRequest
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.UpdateWarrantyRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WarrantyApi {
    // ==================== Customer ====================

    @GET("warranties")
    suspend fun getMyWarranties(): Response<BackendApiResponse<List<MyWarrantyDto>>>

    @GET("warranties/policies")
    suspend fun getPolicies(): Response<BackendApiResponse<List<com.example.scamazon_frontend.data.models.product.BackendWarrantyPolicyDto>>>

    @GET("Warranty/{id}")
    suspend fun getWarrantyById(@Path("id") id: Int): Response<BackendApiResponse<MyWarrantyDto>>

    @GET("warranties/claims")
    suspend fun getMyWarrantyClaims(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BackendApiResponse<CustomerWarrantyClaimPagedDto>>

    @POST("warranties/{warrantyId}/claims")
    suspend fun submitWarrantyClaim(
        @Path("warrantyId") warrantyId: Int,
        @Body request: SubmitWarrantyClaimRequest
    ): Response<BackendApiResponse<SubmitWarrantyClaimResponseDto>>

    // ==================== Admin - Warranty Claims ====================

    @GET("admin/warranty-claims")
    suspend fun getAdminWarrantyClaims(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String? = null
    ): Response<BackendApiResponse<AdminWarrantyClaimPagedDto>>

    @PUT("admin/warranty-claims/{claimId}/resolve")
    suspend fun resolveWarrantyClaim(
        @Path("claimId") claimId: Int,
        @Body request: ResolveWarrantyClaimRequest
    ): Response<BackendApiResponse<ResolveWarrantyClaimResponseDto>>

    // ==================== Admin - Warranty CRUD ====================

    // GET /api/warranty/{id} - single warranty by ID (Admin)
    @GET("warranty/{id}")
    suspend fun getAdminWarrantyById(
        @Path("id") id: Int
    ): Response<BackendApiResponse<AdminWarrantyDto>>

    // GET /api/warranty (all warranties)
    @GET("warranty")
    suspend fun getAllWarranties(): Response<BackendApiResponse<List<AdminWarrantyDto>>>

    // GET /api/warranty/serial/{serialNumber}
    @GET("warranty/serial/{serialNumber}")
    suspend fun getWarrantyBySerial(
        @Path("serialNumber") serialNumber: String
    ): Response<BackendApiResponse<AdminWarrantyDto>>

    // GET /api/warranty/product/{productId}
    @GET("warranty/product/{productId}")
    suspend fun getWarrantiesByProduct(
        @Path("productId") productId: Int
    ): Response<BackendApiResponse<List<AdminWarrantyDto>>>

    // GET /api/warranty/active
    @GET("warranty/active")
    suspend fun getActiveWarranties(): Response<BackendApiResponse<List<AdminWarrantyDto>>>

    // GET /api/warranty/expired
    @GET("warranty/expired")
    suspend fun getExpiredWarranties(): Response<BackendApiResponse<List<AdminWarrantyDto>>>

    // POST /api/warranty
    @POST("warranty")
    suspend fun createWarranty(
        @Body request: CreateWarrantyRequest
    ): Response<BackendApiResponse<AdminWarrantyDto>>

    // PUT /api/warranty/{id}
    @PUT("warranty/{id}")
    suspend fun updateWarranty(
        @Path("id") id: Int,
        @Body request: UpdateWarrantyRequest
    ): Response<BackendApiResponse<AdminWarrantyDto>>

    // DELETE /api/warranty/{id}
    @DELETE("warranty/{id}")
    suspend fun deleteWarranty(
        @Path("id") id: Int
    ): Response<BackendApiResponse<Any>>
}
