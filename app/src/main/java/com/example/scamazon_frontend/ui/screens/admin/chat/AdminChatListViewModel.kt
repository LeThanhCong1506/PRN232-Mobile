package com.example.scamazon_frontend.ui.screens.admin.chat

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.chat.ChatRoomSummaryDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminChatListViewModel : ViewModel() {

    private val _conversationsState = MutableStateFlow<Resource<List<ChatRoomSummaryDto>>>(Resource.Loading())
    val conversationsState = _conversationsState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        _conversationsState.value = Resource.Success(MockData.chatRooms)
    }
}
