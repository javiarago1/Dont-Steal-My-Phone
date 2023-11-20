package com.example.dontstealmyphone

import android.util.Log
import okhttp3.WebSocketListener
import okhttp3.WebSocket
import okio.ByteString

class WebSocketEchoListener(private val service: AntiTheftService) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        Log.d("WebSocket", "WebSocket connected")
        service.registerDevice()
    }



    override fun onMessage(webSocket: WebSocket, text: String) {
        service.handleReceivedCommand(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Manejar mensajes binarios recibidos.
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        Log.e("WebSocket", "Error on WebSocket", t)

    }
}
