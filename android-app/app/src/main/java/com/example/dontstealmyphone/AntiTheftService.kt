package com.example.dontstealmyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class AntiTheftService : Service() {
    private var webSocket: WebSocket? = null
    private val webSocketListener = WebSocketEchoListener(this)
    private val objectMapper = jacksonObjectMapper()

    override fun onBind(intent: Intent): IBinder? {
        // Este es un servicio iniciado, por lo que no proporcionamos binding.
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initializeWebSocket()

        // Iniciar servicio en primer plano para evitar RemoteServiceException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            // Para versiones anteriores a Android Oreo
            startForeground(1, Notification())
        }
    }

    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.example.dontstealmyphone"
        val channelName = "Anti Theft Service Background"
        val chan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        } else {
            null
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan!!)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    private fun initializeWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://10.0.2.2:5000/socket.io/?EIO=4&transport=websocket").build()
        webSocket = client.newWebSocket(request, webSocketListener)
        print("connected?")
    }

     fun handleReceivedCommand(command: String) {
        when (command) {
            "start_theft_alarm" -> startAnarchy()
            // Ejemplo: Comando para iniciar la grabación de la ubicación.
            "START_LOCATION_TRACKING" -> startLocationTracking()
            // Ejemplo: Comando para reproducir un sonido de alarma.
            "PLAY_ALARM_SOUND" -> playAlarmSound()
            // Agrega más comandos según sea necesario.
        }
    }

    fun startAnarchy(){
        
    }

    private fun startLocationTracking() {
        // Implementar la lógica para comenzar el seguimiento de la ubicación.
    }

    private fun playAlarmSound() {
        // Implementar la lógica para reproducir un sonido.
    }

    fun registerDevice() {
        // Suponiendo que ya tienes un `device_id` generado y almacenado
        val deviceId = getDeviceIdd()

        // Crear un objeto JSON para el registro
        val registerMessage = objectMapper.writeValueAsString(mapOf("type" to "register_device", "device_id" to deviceId))

        // Enviar el mensaje de registro al servidor como texto
        webSocket?.send(registerMessage)
    }


     private fun getDeviceIdd(): String {
        // Aquí obtendrías el ID del dispositivo de SharedPreferences o lo generarías si no existe
        // Este es un ejemplo, necesitas implementar esta función según tus necesidades
        return "YOUR_DEVICE_ID"
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.cancel() // Asegúrate de cerrar el WebSocket al destruir el servicio.
    }
}
