package com.example.scamazon_frontend.data.models.returnrequest

import com.google.gson.annotations.SerializedName

data class ReturnRequestResponse(
    @SerializedName("returnRequestId") val returnRequestId: Int,
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("type") val type: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String,
    @SerializedName("adminNote") val adminNote: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("processedAt") val processedAt: String? = null,
    @SerializedName("processedByName") val processedByName: String? = null
)

data class CreateReturnRequestDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("type") val type: String, // "RETURN" or "EXCHANGE"
    @SerializedName("reason") val reason: String
)

data class ProcessReturnRequestDto(
    @SerializedName("status") val status: String, // "APPROVED", "REJECTED", "COMPLETED"
    @SerializedName("adminNote") val adminNote: String? = null
)
