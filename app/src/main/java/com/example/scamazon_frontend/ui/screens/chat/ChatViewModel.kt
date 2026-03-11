package com.example.scamazon_frontend.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.TokenManager
import com.example.scamazon_frontend.data.models.chat.ChatMessageDto
import com.example.scamazon_frontend.data.network.SignalRChatClient
import com.example.scamazon_frontend.data.repository.ChatRepository
import com.example.scamazon_frontend.data.repository.ChatRepository.Companion.toUiDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val chatRepo: ChatRepository,
    private val signalR: SignalRChatClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<List<ChatMessageDto>>>(Resource.Loading())
    val messagesState = _messagesState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    // Error message for toast (null = no error)
    private val _sendError = MutableStateFlow<String?>(null)
    val sendError = _sendError.asStateFlow()

    private val localMessages = mutableListOf<ChatMessageDto>()

    fun startOrLoadChat(storeId: Int? = null) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()

            // Load chat history from REST API
            val result = chatRepo.getChatHistory()
            if (result is Resource.Success) {
                localMessages.clear()
                localMessages.addAll(result.data ?: emptyList())
                _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
            } else {
                _messagesState.value = result
            }

            // Connect SignalR on IO thread to avoid blocking main thread
            val token = tokenManager.getToken()
            if (token != null) {
                withContext(Dispatchers.IO) {
                    signalR.connect(token)
                }
                signalR.onReceiveMessage { backendMsg ->
                    val uiMsg = backendMsg.toUiDto()
                    // Replace matching optimistic message (same content, negative ID) to avoid duplicate
                    val optimisticIdx = localMessages.indexOfFirst {
                        it.id < 0 && it.content == uiMsg.content && it.isFromStore == uiMsg.isFromStore
                    }
                    if (optimisticIdx >= 0) {
                        localMessages[optimisticIdx] = uiMsg
                    } else {
                        localMessages.add(uiMsg)
                    }
                    _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
                }
                signalR.onMessageRead { messageId ->
                    val idx = localMessages.indexOfFirst { it.id == messageId }
                    if (idx >= 0) {
                        localMessages[idx] = localMessages[idx].copy(isRead = true)
                        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
                    }
                }
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            _isSending.value = true

            // Optimistic update: show message immediately before server confirms
            val optimisticMsg = ChatMessageDto(
                id = -(localMessages.size + 1), // negative temp ID
                chatRoomId = 0,
                senderId = null,
                senderName = null,
                messageType = "text",
                content = content,
                imageUrl = null,
                productId = null,
                productName = null,
                productImage = null,
                isFromStore = false, // customer message → right side
                isRead = false,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            )
            localMessages.add(optimisticMsg)
            _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })

            val response = chatRepo.sendMessage(null, content)

            when (response) {
                is Resource.Success -> {
                    // Replace optimistic with real server message (correct ID)
                    val realMsg = response.data
                    if (realMsg != null) {
                        val idx = localMessages.indexOf(optimisticMsg)
                        if (idx >= 0) {
                            localMessages[idx] = realMsg
                        }
                        _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
                    }
                }
                is Resource.Error -> {
                    // Keep optimistic message visible so user sees their message
                    // Show error toast to notify failure
                    _sendError.value = response.message ?: "Không thể gửi tin nhắn. Vui lòng thử lại."
                }
                else -> { /* Loading - no-op */ }
            }

            _isSending.value = false
        }
    }

    fun clearSendError() {
        _sendError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        signalR.disconnect()
    }
}
