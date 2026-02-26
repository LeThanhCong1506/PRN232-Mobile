package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.chat.BackendChatMessageDto
import com.example.scamazon_frontend.data.models.chat.BackendConversationDto
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {

    /**
     * Customer: get own chat history (authenticated)
     */
    @GET("chat/history")
    suspend fun getChatHistory(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<BackendApiResponse<BackendPagedResponse<BackendChatMessageDto>>>

    /**
     * Admin: get all conversations
     */
    @GET("chat/conversations")
    suspend fun getConversations(): Response<BackendApiResponse<List<BackendConversationDto>>>

    /**
     * Admin: get chat history with a specific user
     */
    @GET("chat/history/{targetUserId}")
    suspend fun getChatHistoryWithUser(
        @Path("targetUserId") targetUserId: Int,
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<BackendApiResponse<BackendPagedResponse<BackendChatMessageDto>>>

    /**
     * Fallback HTTP REST API for sending messages
     */
    @POST("chat/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<BackendApiResponse<BackendChatMessageDto>>
}

data class SendMessageRequest(
    val receiverId: Int?,
    val content: String
)
