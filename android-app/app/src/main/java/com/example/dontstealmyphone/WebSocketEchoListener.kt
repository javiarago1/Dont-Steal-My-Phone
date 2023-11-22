package com.example.dontstealmyphone

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.WebSocketListener
import okhttp3.WebSocket
import okio.ByteString

class WebSocketEchoListener(private val service: AntiTheftService) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        Log.d("WebSocket", "WebSocket connected")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessage(webSocket: WebSocket, text: String) {
        service.handleReceivedCommand(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        Log.e("WebSocket", "Error on WebSocket", t)
        Handler(Looper.getMainLooper()).postDelayed({
            service.initializeWebSocket()
        }, 3000)
    }
}
