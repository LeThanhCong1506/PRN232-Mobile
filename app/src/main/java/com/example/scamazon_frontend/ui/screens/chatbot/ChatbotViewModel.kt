package com.example.scamazon_frontend.ui.screens.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.network.api.ProductSuggestionDto
import com.example.scamazon_frontend.data.repository.CartRepository
import com.example.scamazon_frontend.data.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatbotMessage(
    val content: String,
    val isUser: Boolean,
    val source: String? = null,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
    val products: List<ProductSuggestionDto>? = null
)

class ChatbotViewModel(
    private val chatbotRepository: ChatbotRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatbotMessage>>(
        listOf(ChatbotMessage("👋 Hello! I'm STEM Bot, your AI assistant for STEM Store. I can help you with products, pricing, shipping, warranties, and more. Ask me anything!", isUser = false))
    )
    val messages: StateFlow<List<ChatbotMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _addToCartEvent = MutableStateFlow<String?>(null)
    val addToCartEvent: StateFlow<String?> = _addToCartEvent.asStateFlow()

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
                        content = result.data?.answer ?: "Sorry, I don't have an answer for that.",
                        isUser = false,
                        source = result.data?.source,
                        products = result.data?.products
                    )
                    _messages.value = _messages.value + botMsg
                }
                is Resource.Error -> {
                    val errMsg = ChatbotMessage(
                        content = "Sorry, something went wrong: ${result.message}",
                        isUser = false,
                        source = "error"
                    )
                    _messages.value = _messages.value + errMsg
                }
                else -> {}
            }
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            val result = cartRepository.addToCart(productId, 1)
            _addToCartEvent.value = when (result) {
                is Resource.Success -> "Added to cart! 🛒"
                is Resource.Error -> result.message ?: "Failed to add to cart"
                else -> null
            }
        }
    }

    fun clearAddToCartEvent() {
        _addToCartEvent.value = null
    }
}
