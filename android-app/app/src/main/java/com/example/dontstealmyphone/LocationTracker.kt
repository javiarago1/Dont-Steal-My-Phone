package com.example.dontstealmyphone


import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class LocationTracker(private val context: Context) : LocationListener {

    private var locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun startLocationTracking() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this
            )
        } catch (e: SecurityException) {
            // Necesitas manejar la excepción de seguridad, posiblemente solicitando permisos de ubicación.
        }
    }

    override fun onLocationChanged(location: Location) {
        // Lógica para manejar una nueva ubicación.
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // Lógica para manejar cambios en el estado del proveedor de ubicación.
    }

    override fun onProviderEnabled(provider: String) {
        // Lógica para manejar el proveedor de ubicación cuando está habilitado.
    }

    override fun onProviderDisabled(provider: String) {
        // Lógica para manejar el proveedor de ubicación cuando está deshabilitado.
    }

    companion object {
        private const val MIN_TIME_BW_UPDATES: Long = 1000 // 1 segundo
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 1f // 1 metro
    }
}
