package com.example.scamazon_frontend.data.models.chat

import com.google.gson.annotations.SerializedName

/**
 * Backend Chat DTOs - maps to backend API responses.
 * These get transformed to existing UI DTOs (ChatMessageDto, ChatRoomSummaryDto)
 * in ChatRepository.
 */

data class BackendChatMessageDto(
    @SerializedName("messageId") val messageId: Int,
    @SerializedName("senderId") val senderId: Int?,
    @SerializedName("senderName") val senderName: String?,
    @SerializedName("receiverId") val receiverId: Int?,
    @SerializedName("content") val content: String,
    @SerializedName("isFromAdmin") val isFromAdmin: Boolean,
    @SerializedName("sentAt") val sentAt: String?,
    @SerializedName("isRead") val isRead: Boolean
)

data class BackendConversationDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String?,
    @SerializedName("lastMessage") val lastMessage: String?,
    @SerializedName("lastMessageAt") val lastMessageAt: String?,
    @SerializedName("unreadCount") val unreadCount: Int
)
