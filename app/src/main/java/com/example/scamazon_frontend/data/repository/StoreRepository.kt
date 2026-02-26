package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.store.StoreBranchDto
import com.example.scamazon_frontend.data.models.store.StoreLocationDto
import com.example.scamazon_frontend.data.network.api.StoreApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository(private val api: StoreApi) {

    suspend fun getStoreLocation(): Resource<StoreLocationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStoreLocation()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load store location")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getStoreBranches(): Resource<List<StoreBranchDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStoreBranches()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load store branches")
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
