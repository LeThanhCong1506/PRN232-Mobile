package com.example.scamazon_frontend.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.chat.ChatMessageDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<List<ChatMessageDto>>>(Resource.Loading())
    val messagesState = _messagesState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private val localMessages = MockData.chatMessages.toMutableList()

    fun startOrLoadChat(storeId: Int? = null) {
        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
    }

    fun sendMessage(content: String) {
        val newMsg = ChatMessageDto(
            id = localMessages.size + 1,
            chatRoomId = 1,
            senderId = 1,
            senderName = "Nguyễn Văn A",
            messageType = "text",
            content = content,
            imageUrl = null,
            productId = null,
            productName = null,
            isFromStore = false,
            isRead = false,
            createdAt = "2025-02-25T${String.format("%02d", 11 + localMessages.size)}:00:00"
        )
        localMessages.add(newMsg)
        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
    }

    fun sendImageMessage(context: Context, uri: Uri) {
        _isSending.value = true
        val newMsg = ChatMessageDto(
            id = localMessages.size + 1,
            chatRoomId = 1,
            senderId = 1,
            senderName = "Nguyễn Văn A",
            messageType = "image",
            content = "📷 Ảnh",
            imageUrl = "https://picsum.photos/seed/img${localMessages.size}/300/300",
            productId = null,
            productName = null,
            isFromStore = false,
            isRead = false,
            createdAt = "2025-02-25T${String.format("%02d", 11 + localMessages.size)}:00:00"
        )
        localMessages.add(newMsg)
        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
        _isSending.value = false
    }
}
