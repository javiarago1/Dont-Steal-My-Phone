package com.example.dontstealmyphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 101
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Solicitar permisos de ubicación en primer plano al iniciar la aplicación
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            // Si los permisos de ubicación en primer plano ya están concedidos, verifica si es necesario pedir el permiso en segundo plano
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            startAntiTheftService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Ahora que los permisos de ubicación en primer plano han sido concedidos, solicita el permiso de ubicación en segundo plano
                    requestBackgroundLocationPermission()
                } else {
                    // Manejar el caso donde el usuario rechaza la solicitud de permiso.
                }
                return
            }
            PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso de ubicación en segundo plano concedido, iniciar el servicio
                    startAntiTheftService()
                } else {
                    // Manejar el caso donde el usuario rechaza la solicitud de permiso.
                }
                return
            }
        }
    }

    private fun startAntiTheftService() {
        val serviceIntent = Intent(this, AntiTheftService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
