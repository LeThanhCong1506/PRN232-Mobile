package com.example.scamazon_frontend.data.network

import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState

class SignalRNotificationClient {

    private var hubConnection: HubConnection? = null
    private val gson = Gson()

    fun connect(token: String) {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        val hubUrl = "https://prn232-backend-production.up.railway.app/hubs/notification?access_token=\$token"

        hubConnection = HubConnectionBuilder
            .create(hubUrl)
            .build()

        try {
            hubConnection?.start()?.blockingAwait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            hubConnection?.stop()?.blockingAwait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        hubConnection = null
    }

    fun onAdminOrderUpdated(callback: (Any) -> Unit) {
        hubConnection?.on("AdminOrderUpdated", { data ->
            try {
                callback(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Object::class.java)
    }
}
