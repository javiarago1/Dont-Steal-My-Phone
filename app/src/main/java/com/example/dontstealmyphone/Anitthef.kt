package com.example.dontstealmyphone

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.database.tubesock.WebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class AntiTheftService : Service() {
    private var webSocket: WebSocket? = null

    override fun onBind(intent: Intent): IBinder? {
        // Este es un servicio iniciado, por lo que no proporcionamos binding.
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initializeWebSocket()
    }

    private fun initializeWebSocket() {
        val client = OkHttpClient()
        // Asume que tienes una URL válida para tu WebSocket.
        val request = Request.Builder().url("ws://jaragone.dev/notification").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                // La conexión se ha establecido.
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Manejar mensajes de texto recibidos.
                handleReceivedCommand(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Manejar mensajes binarios recibidos.
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                // Manejar errores o reconexiones.
            }
        })

        // Para evitar fugas de memoria, no olvides cerrar el cliente y el WebSocket cuando el servicio sea destruido.
        client.dispatcher.executorService.shutdown()
    }

    private fun handleReceivedCommand(command: String) {
        when (command) {
            // Ejemplo: Comando para iniciar la grabación de la ubicación.
            "START_LOCATION_TRACKING" -> startLocationTracking()
            // Ejemplo: Comando para reproducir un sonido de alarma.
            "PLAY_ALARM_SOUND" -> playAlarmSound()
            // Agrega más comandos según sea necesario.
        }
    }

    private fun startLocationTracking() {
        // Implementar la lógica para comenzar el seguimiento de la ubicación.
    }

    private fun playAlarmSound() {
        // Implementar la lógica para reproducir un sonido.
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.cancel() // Asegúrate de cerrar el WebSocket al destruir el servicio.
    }
}
