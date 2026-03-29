package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.network.api.ChatbotAnswerDto
import com.example.scamazon_frontend.data.network.api.ChatbotApi
import com.example.scamazon_frontend.data.network.api.ChatbotAskRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatbotRepository(private val api: ChatbotApi) {

    suspend fun ask(question: String): Resource<ChatbotAnswerDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.ask(ChatbotAskRequest(question))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "No response from chatbot")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
