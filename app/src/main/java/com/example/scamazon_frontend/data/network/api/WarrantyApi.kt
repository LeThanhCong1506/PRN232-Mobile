package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimPagedDto
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import retrofit2.Response
import retrofit2.http.*

interface WarrantyApi {

    // Customer: Get my warranties
    @GET("warranties")
    suspend fun getMyWarranties(): Response<BackendApiResponse<List<MyWarrantyDto>>>

    // Customer: Submit warranty claim
    @POST("warranties/{warrantyId}/claims")
    suspend fun submitClaim(
        @Path("warrantyId") warrantyId: Int,
        @Body request: SubmitWarrantyClaimRequest
    ): Response<BackendApiResponse<SubmitWarrantyClaimResponseDto>>

    // Admin: Get all warranty claims with filters
    @GET("admin/warranty-claims")
    suspend fun getAdminWarrantyClaims(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BackendApiResponse<AdminWarrantyClaimPagedDto>>

    // Admin: Resolve warranty claim
    @PUT("admin/warranty-claims/{claimId}/resolve")
    suspend fun resolveWarrantyClaim(
        @Path("claimId") claimId: Int,
        @Body request: ResolveWarrantyClaimRequest
    ): Response<BackendApiResponse<ResolveWarrantyClaimResponseDto>>
}
