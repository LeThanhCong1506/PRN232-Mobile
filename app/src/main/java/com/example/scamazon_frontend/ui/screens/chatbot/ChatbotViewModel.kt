package com.example.scamazon_frontend.ui.screens.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatbotMessage(
    val content: String,
    val isUser: Boolean,
    val source: String? = null
)

class ChatbotViewModel(
    private val chatbotRepository: ChatbotRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatbotMessage>>(
        listOf(ChatbotMessage("Xin chào! Tôi là trợ lý AI của STEM Store. Tôi có thể giúp bạn tìm hiểu về sản phẩm, giá cả và nhiều thông tin khác. Hỏi tôi bất cứ điều gì!", isUser = false))
    )
    val messages: StateFlow<List<ChatbotMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(question: String) {
        if (question.isBlank()) return

        val userMsg = ChatbotMessage(content = question, isUser = true)
        _messages.value = _messages.value + userMsg

        _isLoading.value = true
        viewModelScope.launch {
            val result = chatbotRepository.ask(question)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    val botMsg = ChatbotMessage(
                        content = result.data?.answer ?: "Xin lỗi, tôi không có câu trả lời.",
                        isUser = false,
                        source = result.data?.source
                    )
                    _messages.value = _messages.value + botMsg
                }
                is Resource.Error -> {
                    val errMsg = ChatbotMessage(
                        content = "Xin lỗi, đã có lỗi xảy ra: ${result.message}",
                        isUser = false
                    )
                    _messages.value = _messages.value + errMsg
                }
                else -> {}
            }
        }
    }
}
