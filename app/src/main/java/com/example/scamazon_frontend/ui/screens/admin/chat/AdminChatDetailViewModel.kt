package com.example.scamazon_frontend.ui.screens.admin.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.chat.ChatMessageDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminChatDetailViewModel : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<List<ChatMessageDto>>>(Resource.Loading())
    val messagesState = _messagesState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private var currentRoomId: Int? = null
    private val localMessages = MockData.chatMessages.toMutableList()

    fun loadMessages(roomId: Int) {
        currentRoomId = roomId
        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
    }

    fun sendMessage(content: String) {
        val newMsg = ChatMessageDto(
            id = localMessages.size + 1,
            chatRoomId = currentRoomId ?: 1,
            senderId = null,
            senderName = "Admin",
            messageType = "text",
            content = content,
            imageUrl = null,
            productId = null,
            productName = null,
            isFromStore = true,
            isRead = false,
            createdAt = "2025-02-25T${String.format("%02d", 12 + localMessages.size)}:00:00"
        )
        localMessages.add(newMsg)
        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
    }

    fun sendImageMessage(context: Context, uri: Uri) {
        _isSending.value = true
        val newMsg = ChatMessageDto(
            id = localMessages.size + 1,
            chatRoomId = currentRoomId ?: 1,
            senderId = null,
            senderName = "Admin",
            messageType = "image",
            content = "📷 Ảnh",
            imageUrl = "https://picsum.photos/seed/admin${localMessages.size}/300/300",
            productId = null,
            productName = null,
            isFromStore = true,
            isRead = false,
            createdAt = "2025-02-25T${String.format("%02d", 12 + localMessages.size)}:00:00"
        )
        localMessages.add(newMsg)
        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
        _isSending.value = false
    }
}
