package com.example.dontstealmyphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 101
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 102
        const val PREFS_NAME = "MyApp_Settings"
        const val DEVICE_ID_KEY = "device_id"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val deviceIdTextView = findViewById<TextView>(R.id.deviceIdTextView)

        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val deviceId = settings.getString(DEVICE_ID_KEY, null)

        if (deviceId == null) {
            promptForDeviceId()
        } else {
            deviceIdTextView.text = getString(R.string.tu_id_es, deviceId)
            requestLocationPermission()
        }

        val stopAnarchyButton = findViewById<Button>(R.id.stopAnarchyButton)
        stopAnarchyButton.setOnClickListener {
            stopAnarchyService()
        }

    }

    private fun stopAnarchyService() {
        val stopServiceIntent = Intent(this, AntiTheftService::class.java).apply {
            action = "STOP_ANARCHY"
        }
        startService(stopServiceIntent)
    }

    private fun promptForDeviceId() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Device ID")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newDeviceId = input.text.toString()
            if (newDeviceId.isNotEmpty()) {
                val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                val editor = settings.edit()
                editor.putString(DEVICE_ID_KEY, newDeviceId)
                editor.apply()

                val deviceIdTextView = findViewById<TextView>(R.id.deviceIdTextView)
                deviceIdTextView.text = getString(R.string.tu_id_es, newDeviceId)

                requestLocationPermission()
            } else {
                Toast.makeText(this, "Device ID can't be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION)
        } else {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                PERMISSIONS_REQUEST_BACKGROUND_LOCATION)
        } else {
            startAntiTheftService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestBackgroundLocationPermission()
                } else {
                    Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startAntiTheftService()
                } else {
                    Toast.makeText(this, "Background location permission is required.", Toast.LENGTH_SHORT).show()
                }
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
