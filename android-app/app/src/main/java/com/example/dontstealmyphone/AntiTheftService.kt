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
    private var flashHandler: Handler? = null
    private var ttsHandler: Handler? = null




    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initializeWebSocket()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
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
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(notificationSoundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setOngoing(true)
            setContentTitle("App is running in background")
            setContentText("Touch to open.")
            setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia a un ícono existente en tu carpeta drawable
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(Notification.CATEGORY_ALARM)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        }

        val notification = notificationBuilder.build()
        startForeground(2, notification)
    }

    fun initializeWebSocket() {
        val deviceId = getDeviceIdentifier() // Asegúrate de que este método devuelva el ID correcto del dispositivo
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://192.168.1.132:5000/android/socket.io/?EIO=4&transport=websocket&device_id=$deviceId").build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

     @RequiresApi(Build.VERSION_CODES.O)
     fun handleReceivedCommand(command: String) {
        when (command) {
            "start_theft_alarm" -> startAnarchy()
            "stop_effects" -> stopAnarchy()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localeSpanish = Locale("es", "ES")
            val result = textToSpeech?.setLanguage(localeSpanish)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Este idioma no es soportado.")
            } else {
                val spanishVoices = textToSpeech?.voices?.filter { it.locale == localeSpanish }
                if (spanishVoices.isNullOrEmpty()) {
                    Log.e("TTS", "No hay voces en español disponibles.")
                } else {
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
        // Establecer volumen máximo.
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)

        // TextToSpeech.
        val speechRate = 1.5f
        textToSpeech!!.setSpeechRate(speechRate)
        val utteranceId = "AntiTheftMessage"
        textToSpeech?.speak("Este móvil ha sido robado.", TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        // Preparar el Handler para la repetición.
        ttsHandler = Handler(Looper.getMainLooper())
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { }
            override fun onDone(utteranceId: String?) {
                ttsHandler?.postDelayed({
                    playEstridenteSound()
                    textToSpeech?.speak("Este móvil ha sido robado.", TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                }, 500)
            }
            override fun onError(utteranceId: String?) { }
        })

        // Vibración.
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createWaveform(longArrayOf(0, 500, 250), intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0), 0)
        vibrator.vibrate(vibrationEffect)

        // Luz del flash.
        flashHandler = Handler(Looper.getMainLooper())
        flashLightOnOff()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP_ANARCHY" -> stopAnarchy()
        }
        return START_STICKY
    }


    private fun stopAnarchy() {
        // Detener TextToSpeech.
        textToSpeech?.stop()
        ttsHandler?.removeCallbacksAndMessages(null)

        // Detener vibración.
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()

        // Detener flash.
        flashHandler?.removeCallbacksAndMessages(null)
        try {
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
        } catch (e: CameraAccessException) {
            Log.e("AntiTheftService", "Error al acceder a la cámara para el flash", e)
        }
    }

    private fun flashLightOnOff() {
        flashHandler?.post(object : Runnable {
            var isFlashOn = false
            override fun run() {
                isFlashOn = !isFlashOn
                try {
                    val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    cameraManager.setTorchMode(cameraManager.cameraIdList[0], isFlashOn)
                } catch (e: CameraAccessException) {
                    Log.e("AntiTheftService", "Error al acceder a la cámara para el flash", e)
                }
                flashHandler?.postDelayed(this, 500)
            }
        })
    }


    private fun playEstridenteSound() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
    }


    private fun getDeviceIdentifier(): String {
        val settings = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        return settings.getString(MainActivity.DEVICE_ID_KEY, "UNREGISTERED_DEVICE") ?: "UNREGISTERED_DEVICE"
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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        stopAnarchy()
        textToSpeech?.shutdown()
        webSocket?.cancel()
        super.onDestroy()
    }

}
