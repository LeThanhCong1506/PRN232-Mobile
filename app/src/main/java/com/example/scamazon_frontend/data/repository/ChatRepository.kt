package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.chat.BackendChatMessageDto
import com.example.scamazon_frontend.data.models.chat.ChatMessageDto
import com.example.scamazon_frontend.data.models.chat.ChatRoomSummaryDto
import com.example.scamazon_frontend.data.network.api.ChatApi
import com.example.scamazon_frontend.data.network.api.SendMessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    companion object {
        /**
         * Map backend DTO → existing UI ChatMessageDto
         */
        fun BackendChatMessageDto.toUiDto(): ChatMessageDto {
            return ChatMessageDto(
                id = messageId,
                chatRoomId = 0,
                senderId = senderId,
                senderName = senderName,
                messageType = "text",
                content = content,
                imageUrl = null,
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
