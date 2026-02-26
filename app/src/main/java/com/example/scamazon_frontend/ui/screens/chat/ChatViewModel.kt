package com.example.scamazon_frontend.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.TokenManager
import com.example.scamazon_frontend.data.models.chat.ChatMessageDto
import com.example.scamazon_frontend.data.network.SignalRChatClient
import com.example.scamazon_frontend.data.repository.ChatRepository
import com.example.scamazon_frontend.data.repository.ChatRepository.Companion.toUiDto
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepo: ChatRepository,
    private val signalR: SignalRChatClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<List<ChatMessageDto>>>(Resource.Loading())
    val messagesState = _messagesState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

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

            // Connect SignalR for real-time messages
            val token = tokenManager.getToken()
            if (token != null) {
                signalR.connect(token)
                signalR.onReceiveMessage { backendMsg ->
                    val uiMsg = backendMsg.toUiDto()
                    localMessages.add(uiMsg)
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
            // Customer sends to admin: receiverId = null
            // We use standard REST API instead of SignalR to bypass SSL Handshake blocks on self-signed local certs
            val response = chatRepo.sendMessage(null, content)
            
            // If the REST call succeeds, the optimistic update stays.
            // If it fails, we should ideally remove the optimistic update or show an error state.
            if (response is Resource.Error) {
                localMessages.remove(optimisticMsg)
                _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
                // TODO: Show toast or error message to user
            }
            
            _isSending.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        signalR.disconnect()
    }
}
