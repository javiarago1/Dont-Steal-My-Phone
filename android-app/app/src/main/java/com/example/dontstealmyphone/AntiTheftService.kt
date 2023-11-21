package com.example.dontstealmyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.Locale


class AntiTheftService : Service(), TextToSpeech.OnInitListener, LocationListener {
    private var webSocket: WebSocket? = null
    private val webSocketListener = WebSocketEchoListener(this)
    private val objectMapper = jacksonObjectMapper()
    private var textToSpeech: TextToSpeech? = null
    private lateinit var locationManager: LocationManager



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
        textToSpeech = TextToSpeech(this, this)
        initializeLocationManager()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.example.dontstealmyphone"
        val channelName = "Anti Theft Service Background"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación con alta importancia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(notificationSoundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Asegúrate de que el ícono que usarás existe en la carpeta drawable de tu proyecto.
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setOngoing(true)
            setContentTitle("App is running in background")
            setContentText("Touch to open.")
            setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia a un ícono existente en tu carpeta drawable
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(Notification.CATEGORY_ALARM)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        }

        val notification = notificationBuilder.build()
        startForeground(2, notification)
    }

    private fun initializeWebSocket() {
        val deviceId = getDeviceIdd() // Asegúrate de que este método devuelva el ID correcto del dispositivo
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://192.168.1.132:5000/android/socket.io/?EIO=4&transport=websocket&device_id=$deviceId").build()
        webSocket = client.newWebSocket(request, webSocketListener)
        print("connected?")
    }

     @RequiresApi(Build.VERSION_CODES.O)
     fun handleReceivedCommand(command: String) {
        when (command) {
            "start_theft_alarm" -> startAnarchy()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localeSpanish = Locale("es", "ES")
            val result = textToSpeech?.setLanguage(localeSpanish)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Este idioma no es soportado.")
            } else {
                // Filtrar por voces en español y seleccionar una
                val spanishVoices = textToSpeech?.voices?.filter { it.locale == localeSpanish }
                if (spanishVoices.isNullOrEmpty()) {
                    Log.e("TTS", "No hay voces en español disponibles.")
                } else {
                    // Esto es un ejemplo, deberías verificar qué voz prefieres usar.
                    val maleVoice = spanishVoices.find { voice -> voice.name.contains("es_ES") }
                    if (maleVoice != null) {
                        textToSpeech?.voice = maleVoice
                    } else {
                        Log.e("TTS", "Voz masculina en español no encontrada, se usará la predeterminada.")
                    }
                }
            }
        } else {
            Log.e("TTS", "Inicialización fallida.")
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun startAnarchy() {
        // Configurar el volumen del TTS al máximo
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

        // Configurar TextToSpeech para repetir el mensaje
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Nada que hacer aquí
            }

            override fun onDone(utteranceId: String?) {
                // Cuando termina de hablar, repetir el mensaje después de un breve intervalo
                Handler(Looper.getMainLooper()).postDelayed({
                    textToSpeech?.speak("Este móvil ha sido robado.", TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                }, 1000) // Retrasar la próxima locución en 1 segundo
            }

            override fun onError(utteranceId: String?) {
                Log.e("TTS", "Error en la locución")
            }
        })

        flashLightOnOff()

        // Configurar la vibración
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)

        // Configurar TextToSpeech para reproducir un sonido estridente al final de cada frase
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
            }

            override fun onDone(utteranceId: String?) {
                // Reproducir un sonido estridente al final de la frase
                //playEstridenteSound()

                // Volver a hablar después de un breve intervalo
                Handler(Looper.getMainLooper()).postDelayed({
                    // Asegúrate de no iniciar un nuevo mensaje de voz si el servicio se está deteniendo
                    val isStopping = false
                    if (!isStopping) {
                        textToSpeech?.speak("Este móvil ha sido robado.", TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                    }
                }, 1000) // Retrasar la próxima locución en 1 segundo
            }

            override fun onError(utteranceId: String?) {
                TODO("Not yet implemented")
            }
        })


        // Hablar por primera vez sin retraso
        val utteranceId = "AntiTheftMessage"
        textToSpeech?.speak("Este móvil ha sido robado.", TextToSpeech.QUEUE_FLUSH, null, utteranceId)

    }

    private fun flashLightOnOff() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Suponiendo que el flash está en la cámara principal
        var isFlashOn = false
        val runnable = object : Runnable {
            override fun run() {
                try {
                    isFlashOn = !isFlashOn
                    cameraManager.setTorchMode(cameraId, isFlashOn) // Encender o apagar el flash
                    Handler(Looper.getMainLooper()).postDelayed(this, 500) // Cambiar el estado cada 500ms
                } catch (e: CameraAccessException) {
                    Log.e("AntiTheftService", "Error al acceder a la cámara para el flash", e)
                }
            }
        }
        Handler(Looper.getMainLooper()).post(runnable)
    }

    private fun playEstridenteSound() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000) // Duración de 1 segundo
    }



    fun registerDevice() {
        val deviceId = getDeviceIdd()
        val registerMessage = objectMapper.writeValueAsString(mapOf("type" to "register_device", "device_id" to deviceId))
        webSocket?.send(registerMessage)
    }


     private fun getDeviceIdd(): String {
        return "YOUR_DEVICE_ID"
    }

    private fun initializeLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // 1 second
                0f, // 0 meters
                this
            )
        } catch (ex: SecurityException) {
            // Log any security exceptions
            println("Security Exception when requesting location updates: ${ex.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("AntiTheftService", "Location changed: Lat ${location.latitude}, Lon ${location.longitude}")
        val locationUpdate = objectMapper.writeValueAsString(mapOf(
            "type" to "location_update",
            "latitude" to location.latitude,
            "longitude" to location.longitude
        ))
        if (webSocket != null) {
            webSocket?.send(locationUpdate)
            Log.d("AntiTheftService", "Location sent: $locationUpdate")
        } else {
            Log.d("AntiTheftService", "WebSocket not connected.")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.shutdown()
        webSocket?.cancel() // Asegúrate de cerrar el WebSocket al destruir el servicio.
    }
}
