package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.store.StoreBranchDto
import com.example.scamazon_frontend.data.models.store.StoreLocationDto
import retrofit2.Response
import retrofit2.http.GET

interface StoreApi {

    @GET("store/location")
    suspend fun getStoreLocation(): Response<BackendApiResponse<StoreLocationDto>>

    @GET("store/branches")
    suspend fun getStoreBranches(): Response<BackendApiResponse<List<StoreBranchDto>>>
}
