package com.example.dontstealmyphone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.dontstealmyphone.R

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establecer el layout de la actividad principal
        setContentView(R.layout.activity_main)

        // Iniciar el servicio en segundo plano al abrir la aplicación
        startAntiTheftService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startAntiTheftService() {
        // Crear un intent explícito para iniciar el servicio AntiTheftService
        val serviceIntent = Intent(this, AntiTheftService::class.java)
        // Si estás compilando para API nivel 26 o superior, deberías comenzar el servicio en primer plano para evitar restricciones de fondo
        startForegroundService(serviceIntent)
    }
}
