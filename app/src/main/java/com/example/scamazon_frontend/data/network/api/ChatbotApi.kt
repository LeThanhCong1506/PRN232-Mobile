package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApi {

    @POST("chatbot/ask")
    suspend fun ask(@Body request: ChatbotAskRequest): Response<BackendApiResponse<ChatbotAnswerDto>>
}

data class ChatbotAskRequest(
    @SerializedName("question") val question: String
)

data class ChatbotAnswerDto(
    @SerializedName("answer") val answer: String,
    @SerializedName("source") val source: String?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("products") val products: List<ProductSuggestionDto>? = null
)

data class ProductSuggestionDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("imageUrl") val imageUrl: String?
)
