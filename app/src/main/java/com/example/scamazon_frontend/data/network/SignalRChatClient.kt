package com.example.scamazon_frontend.data.network

import com.example.scamazon_frontend.data.models.chat.BackendChatMessageDto
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState

/**
 * SignalR client for real-time chat communication.
 * Connects to the backend chat hub and provides methods for sending/receiving messages.
 */
class SignalRChatClient {

    private var hubConnection: HubConnection? = null
    private val gson = Gson()

    /**
     * Connect to the SignalR chat hub with the given JWT token.
     */
    fun connect(token: String) {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        hubConnection = HubConnectionBuilder
            .create("https://10.0.2.2:7295/hubs/chat?access_token=$token")
            .build()

        try {
            hubConnection?.start()?.blockingAwait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Disconnect from the SignalR hub.
     */
    fun disconnect() {
        try {
            hubConnection?.stop()?.blockingAwait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        hubConnection = null
    }

    /**
     * Send a message via SignalR.
     * @param receiverId null for customer (sends to admin), userId for admin (sends to specific customer)
     * @param content the message text
     */
    fun sendMessage(receiverId: Int?, content: String) {
        try {
            hubConnection?.send("SendMessage", receiverId, content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Mark a message as read.
     */
    fun markAsRead(messageId: Int) {
        try {
            hubConnection?.send("MarkAsRead", messageId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Listen for incoming messages from the server.
     */
    fun onReceiveMessage(callback: (BackendChatMessageDto) -> Unit) {
        hubConnection?.on("ReceiveMessage", { jsonMsg ->
            try {
                val msg = gson.fromJson(jsonMsg.toString(), BackendChatMessageDto::class.java)
                callback(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Object::class.java)
    }

    /**
     * Listen for message read acknowledgements.
     */
    fun onMessageRead(callback: (Int) -> Unit) {
        hubConnection?.on("MessageRead", { messageId ->
            try {
                val id = (messageId as? Number)?.toInt() ?: messageId.toString().toIntOrNull()
                if (id != null) {
                    callback(id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Object::class.java)
    }
}
