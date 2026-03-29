package com.example.scamazon_frontend.data.repository

import android.content.Context
import android.net.Uri
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.chat.BackendChatMessageDto
import com.example.scamazon_frontend.data.models.chat.ChatMessageDto
import com.example.scamazon_frontend.data.models.chat.ChatRoomSummaryDto
import com.example.scamazon_frontend.data.network.api.ChatApi
import com.example.scamazon_frontend.data.network.api.SendMessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository(private val api: ChatApi) {

    /**
     * Customer: get own chat history, mapped to UI DTOs
     */
    suspend fun getChatHistory(
        pageNumber: Int = 1,
        pageSize: Int = 50
    ): Resource<List<ChatMessageDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChatHistory(pageNumber, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val messages = body.data.items.map { it.toUiDto() }
                        Resource.Success(messages)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load chat history")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Admin: get all conversations, mapped to ChatRoomSummaryDto
     */
    suspend fun getConversations(): Resource<List<ChatRoomSummaryDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getConversations()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val conversations = body.data.map { conv ->
                            ChatRoomSummaryDto(
                                id = conv.userId,
                                userId = conv.userId,
                                userName = conv.userName,
                                userAvatar = null,
                                storeId = null,
                                storeName = null,
                                status = null,
                                lastMessage = conv.lastMessage,
                                lastMessageAt = conv.lastMessageAt,
                                unreadCount = conv.unreadCount,
                                createdAt = conv.lastMessageAt
                            )
                        }
                        Resource.Success(conversations)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load conversations")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Admin: get chat history with a specific user
     */
    suspend fun getChatHistoryWithUser(
        targetUserId: Int,
        pageNumber: Int = 1,
        pageSize: Int = 50
    ): Resource<List<ChatMessageDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChatHistoryWithUser(targetUserId, pageNumber, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val messages = body.data.items.map { it.toUiDto() }
                        Resource.Success(messages)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load chat history")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Fallback: Send a message via HTTP POST
     */
    suspend fun sendMessage(
        receiverId: Int?,
        content: String
    ): Resource<ChatMessageDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.sendMessage(SendMessageRequest(receiverId, content))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data.toUiDto())
                    } else {
                        Resource.Error(body?.message ?: "Failed to send message")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Upload an image to Cloudinary via the backend.
     * Returns the public Cloudinary URL on success.
     */
    suspend fun uploadImage(uri: Uri, context: Context): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Resource.Error("Cannot open image file")

                val bytes = inputStream.readBytes()
                inputStream.close()

                val fileName = "chat_image_${System.currentTimeMillis()}.jpg"
                val requestBody = bytes.toRequestBody("image/*".toMediaType())
                val part = MultipartBody.Part.createFormData("image", fileName, requestBody)

                val response = api.uploadImage(part)
                if (response.isSuccessful) {
                    val body = response.body()
                    val imageUrl = body?.data?.imageUrl
                    if (!imageUrl.isNullOrBlank()) {
                        Resource.Success(imageUrl)
                    } else {
                        Resource.Error(body?.message ?: "No image URL returned")
                    }
                } else {
                    Resource.Error("Upload failed (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Upload error")
            }
        }
    }

    /**
     * Mark all messages from a specific user as read (Admin use).
     */
    suspend fun markReadByUserId(userId: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.markReadByUserId(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to mark messages as read")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Mark all messages as read for the authenticated user.
     */
    suspend fun markRead(): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.markRead()
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to mark messages as read")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Get unread message count for the authenticated user.
     */
    suspend fun getUnreadCount(): Resource<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUnreadCount()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(body.data ?: 0)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get unread count")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    companion object {
        /**
         * Map backend DTO → existing UI ChatMessageDto.
         * If content is a Cloudinary/HTTP URL, treat it as an image message:
         *   imageUrl = content, content = "📷 Ảnh"
         */
        fun BackendChatMessageDto.toUiDto(): ChatMessageDto {
            val isImageContent = content.startsWith("https://") || content.startsWith("http://")
            return ChatMessageDto(
                id = messageId,
                chatRoomId = 0,
                senderId = senderId,
                senderName = senderName,
                messageType = if (isImageContent) "image" else "text",
                content = if (isImageContent) "📷 Ảnh" else content,
                imageUrl = if (isImageContent) content else null,
                productId = null,
                productName = null,
                productImage = null,
                isFromStore = isFromAdmin,
                isRead = isRead,
                createdAt = sentAt
            )
        }
    }
}
