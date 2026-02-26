package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimDto
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.network.api.WarrantyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarrantyRepository(private val api: WarrantyApi) {

    suspend fun getMyWarranties(): Resource<List<MyWarrantyDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyWarranties()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(body.data ?: emptyList())
                    } else {
                        Resource.Error(body?.message ?: "Failed to load warranties")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun submitClaim(warrantyId: Int, request: SubmitWarrantyClaimRequest): Resource<SubmitWarrantyClaimResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.submitClaim(warrantyId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to submit claim")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getAdminWarrantyClaims(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Resource<List<AdminWarrantyClaimDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAdminWarrantyClaims(status, page, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data.items)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load warranty claims")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun resolveWarrantyClaim(claimId: Int, request: ResolveWarrantyClaimRequest): Resource<ResolveWarrantyClaimResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.resolveWarrantyClaim(claimId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to resolve claim")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
