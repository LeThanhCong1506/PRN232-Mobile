package com.example.scamazon_frontend.ui.screens.admin.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.chat.ChatRoomSummaryDto
import com.example.scamazon_frontend.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminChatListViewModel(
    private val chatRepo: ChatRepository
) : ViewModel() {

    private val _conversationsState = MutableStateFlow<Resource<List<ChatRoomSummaryDto>>>(Resource.Loading())
    val conversationsState = _conversationsState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.value = Resource.Loading()
            _conversationsState.value = chatRepo.getConversations()
        }
    }
}
