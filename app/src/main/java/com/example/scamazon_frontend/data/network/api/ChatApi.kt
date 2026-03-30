package com.example.scamazon_frontend.data.network.api

import com.example.scamazon_frontend.data.models.chat.BackendChatMessageDto
import com.example.scamazon_frontend.data.models.chat.BackendConversationDto
import com.example.scamazon_frontend.data.models.common.BackendApiResponse
import com.example.scamazon_frontend.data.models.common.BackendPagedResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    /**
     * Upload a chat image to Cloudinary.
     * Returns { imageUrl } wrapped in ApiResponse.
     */
    @Multipart
    @POST("chat/upload-image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<BackendApiResponse<UploadImageResponse>>

    /**
     * Mark all messages from a specific user as read (Admin use).
     */
    @POST("chat/mark-read/{userId}")
    suspend fun markReadByUserId(
        @Path("userId") userId: Int
    ): Response<BackendApiResponse<Any>>

    /**
     * Mark all messages as read for the authenticated user (Customer use).
     */
    @POST("chat/mark-read")
    suspend fun markRead(): Response<BackendApiResponse<Any>>

    /**
     * Get unread message count for the authenticated user.
     */
    @GET("chat/unread-count")
    suspend fun getUnreadCount(): Response<BackendApiResponse<Int>>
}

data class SendMessageRequest(
    val receiverId: Int?,
    val content: String
)

data class UploadImageResponse(
    @SerializedName("imageUrl") val imageUrl: String?
)
