package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.returnrequest.CreateReturnRequestDto
import com.example.scamazon_frontend.data.models.returnrequest.ProcessReturnRequestDto
import com.example.scamazon_frontend.data.models.returnrequest.ReturnRequestResponse
import com.example.scamazon_frontend.data.network.api.ReturnApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReturnRepository(private val api: ReturnApi) {

    suspend fun createReturnRequest(request: CreateReturnRequestDto): Resource<ReturnRequestResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createReturnRequest(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to create return request")
                    }
                } else {
                    Resource.Error("API call failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    suspend fun getMyReturnRequests(
        page: Int = 1,
        pageSize: Int = 10
    ): Resource<BackendPagedResponse<ReturnRequestResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyReturnRequests(page, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to fetch return requests")
                    }
                } else {
                    Resource.Error("API call failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching requests")
            }
        }
    }

    suspend fun getAllReturnRequests(
        page: Int = 1,
        pageSize: Int = 20
    ): Resource<BackendPagedResponse<ReturnRequestResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllReturnRequests(page, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to fetch return requests")
                    }
                } else {
                    Resource.Error("API call failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching requests")
            }
        }
    }

    suspend fun getReturnRequestById(id: Int): Resource<ReturnRequestResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getReturnRequestById(id)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to fetch return request target")
                    }
                } else {
                    Resource.Error("API call failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred retrieving request details")
            }
        }
    }

    suspend fun processReturnRequest(
        id: Int,
        request: ProcessReturnRequestDto
    ): Resource<ReturnRequestResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.processReturnRequest(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to process return request")
                    }
                } else {
                    Resource.Error("API error: ${response.errorBody()?.string() ?: response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred processing request")
            }
        }
    }
}
