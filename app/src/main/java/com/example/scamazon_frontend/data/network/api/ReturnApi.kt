package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.example.scamazon_frontend.data.models.returnrequest.CreateReturnRequestDto
import com.example.scamazon_frontend.data.models.returnrequest.ProcessReturnRequestDto
import com.example.scamazon_frontend.data.models.returnrequest.ReturnRequestResponse
import retrofit2.Response
import retrofit2.http.*

interface ReturnApi {
    @POST("return-request")
    suspend fun createReturnRequest(@Body request: CreateReturnRequestDto): Response<BackendApiResponse<ReturnRequestResponse>>

    @GET("return-request/my")
    suspend fun getMyReturnRequests(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<BackendApiResponse<BackendPagedResponse<ReturnRequestResponse>>>

    @GET("return-request")
    suspend fun getAllReturnRequests(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BackendApiResponse<BackendPagedResponse<ReturnRequestResponse>>>

    @GET("return-request/{id}")
    suspend fun getReturnRequestById(@Path("id") id: Int): Response<BackendApiResponse<ReturnRequestResponse>>

    @PUT("return-request/{id}/process")
    suspend fun processReturnRequest(
        @Path("id") id: Int,
        @Body request: ProcessReturnRequestDto
    ): Response<BackendApiResponse<ReturnRequestResponse>>
}
