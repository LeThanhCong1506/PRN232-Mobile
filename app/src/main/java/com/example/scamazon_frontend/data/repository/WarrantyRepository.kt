package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimDto
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

    suspend fun getPolicies(): Resource<List<com.example.scamazon_frontend.data.models.product.BackendWarrantyPolicyDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPolicies()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get warranty policies")
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
    ): Resource<CustomerWarrantyClaimPagedDto> {
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

    // ==================== Admin Warranty CRUD ====================

    suspend fun getAdminWarrantyById(id: Int): Resource<AdminWarrantyDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAdminWarrantyById(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Warranty not found")
                } else Resource.Error("Error ${response.code()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun getAllWarranties(): Resource<List<AdminWarrantyDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllWarranties()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Failed to load warranties")
                } else Resource.Error("Error ${response.code()}: ${response.message()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun getWarrantyBySerial(serialNumber: String): Resource<AdminWarrantyDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getWarrantyBySerial(serialNumber)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Warranty not found")
                } else Resource.Error("Error ${response.code()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun getActiveWarranties(): Resource<List<AdminWarrantyDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getActiveWarranties()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Failed")
                } else Resource.Error("Error ${response.code()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun getExpiredWarranties(): Resource<List<AdminWarrantyDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getExpiredWarranties()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Failed")
                } else Resource.Error("Error ${response.code()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun createWarranty(request: CreateWarrantyRequest): Resource<AdminWarrantyDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createWarranty(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Failed to create warranty")
                } else Resource.Error("Error ${response.code()}: ${response.message()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun updateWarranty(id: Int, request: UpdateWarrantyRequest): Resource<AdminWarrantyDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateWarranty(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) Resource.Success(body.data)
                    else Resource.Error(body?.message ?: "Failed to update warranty")
                } else Resource.Error("Error ${response.code()}: ${response.message()}")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }

    suspend fun deleteWarranty(id: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteWarranty(id)
                if (response.isSuccessful && response.body()?.success == true) Resource.Success(Unit)
                else Resource.Error(response.body()?.message ?: "Failed to delete warranty")
            } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
        }
    }
}
