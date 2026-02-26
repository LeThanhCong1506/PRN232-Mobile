package com.example.scamazon_frontend.ui.screens.admin.chat

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

class AdminChatDetailViewModel(
    private val chatRepo: ChatRepository,
    private val signalR: SignalRChatClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<List<ChatMessageDto>>>(Resource.Loading())
    val messagesState = _messagesState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private var targetUserId: Int? = null
    private val localMessages = mutableListOf<ChatMessageDto>()

    fun loadMessages(userId: Int) {
        targetUserId = userId
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()
            val result = chatRepo.getChatHistoryWithUser(userId)
            if (result is Resource.Success) {
                localMessages.clear()
                localMessages.addAll(result.data ?: emptyList())
                _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
            } else {
                _messagesState.value = result
            }

            // Connect SignalR and listen for new messages
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
        val userId = targetUserId ?: return
        viewModelScope.launch {
            _isSending.value = true
            // Optimistic update: show message immediately before server confirms
            val optimisticMsg = ChatMessageDto(
                id = -(localMessages.size + 1), // negative temp ID
                chatRoomId = 0,
                senderId = null,
                senderName = "Admin",
                messageType = "text",
                content = content,
                imageUrl = null,
                productId = null,
                productName = null,
                productImage = null,
                isFromStore = true, // admin message → right side
                isRead = false,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            )
            localMessages.add(optimisticMsg)
            _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
            
            // Send via HTTP REST API fallback
            val response = chatRepo.sendMessage(userId, content)
            
            if (response is Resource.Error) {
                localMessages.remove(optimisticMsg)
                _messagesState.value = Resource.Success(localMessages.sortedBy { it.createdAt })
                // TODO: Show toast or error message to admin
            }
            
            _isSending.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        signalR.disconnect()
    }
}
