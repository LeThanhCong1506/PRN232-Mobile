package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimPagedDto
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WarrantyApi {
    // Backend: GET /api/warranties (absolute path on WarrantyController)
    // BASE_URL = .../api/ so just use "warranties"
    @GET("warranties")
    suspend fun getMyWarranties(): Response<BackendApiResponse<List<MyWarrantyDto>>>

    @GET("warranties/policies")
    suspend fun getPolicies(): Response<BackendApiResponse<List<com.example.scamazon_frontend.data.models.product.BackendWarrantyPolicyDto>>>

    @GET("Warranty/{id}")
    suspend fun getWarrantyById(@Path("id") id: Int): Response<BackendApiResponse<MyWarrantyDto>>

    // Backend: GET /api/warranties/claims (customer)
    @GET("warranties/claims")
    suspend fun getMyWarrantyClaims(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BackendApiResponse<AdminWarrantyClaimPagedDto>>

    // Backend: POST /api/warranties/{warrantyId}/claims (absolute path on WarrantyClaimController)
    @POST("warranties/{warrantyId}/claims")
    suspend fun submitWarrantyClaim(
        @Path("warrantyId") warrantyId: Int,
        @Body request: SubmitWarrantyClaimRequest
    ): Response<BackendApiResponse<SubmitWarrantyClaimResponseDto>>

    // Backend: GET /api/admin/warranty-claims (absolute path on WarrantyClaimController)
    @GET("admin/warranty-claims")
    suspend fun getAdminWarrantyClaims(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("status") status: String? = null
    ): Response<BackendApiResponse<AdminWarrantyClaimPagedDto>>

    // Backend: PUT /api/admin/warranty-claims/{claimId}/resolve
    @PUT("admin/warranty-claims/{claimId}/resolve")
    suspend fun resolveWarrantyClaim(
        @Path("claimId") claimId: Int,
        @Body request: ResolveWarrantyClaimRequest
    ): Response<BackendApiResponse<ResolveWarrantyClaimResponseDto>>
}
