package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.SubmitWarrantyClaimResponseDto
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimDto
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimPagedDto
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimRequest
import com.example.scamazon_frontend.data.models.warranty.ResolveWarrantyClaimResponseDto
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
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get warranties")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    Resource.Error("Failed: $err")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getWarrantyById(id: Int): Resource<MyWarrantyDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getWarrantyById(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get warranty detail")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    Resource.Error("Failed: $err")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getMyWarrantyClaims(
        page: Int = 1,
        pageSize: Int = 20
    ): Resource<AdminWarrantyClaimPagedDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyWarrantyClaims(page, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load claims")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    Resource.Error("Failed: $err")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun submitWarrantyClaim(
        warrantyId: Int,
        request: SubmitWarrantyClaimRequest
    ): Resource<SubmitWarrantyClaimResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.submitWarrantyClaim(warrantyId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to submit claim")
                    }
                } else {
                    val err = response.errorBody()?.string() ?: response.message()
                    Resource.Error("Failed: $err")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getAdminWarrantyClaims(
        page: Int = 1,
        pageSize: Int = 20,
        status: String? = null
    ): Resource<List<AdminWarrantyClaimDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAdminWarrantyClaims(page, pageSize, status)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data.items)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load claims")
                    }
                } else {
                    Resource.Error("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun resolveWarrantyClaim(
        claimId: Int,
        request: ResolveWarrantyClaimRequest
    ): Resource<ResolveWarrantyClaimResponseDto> {
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
                    Resource.Error("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
