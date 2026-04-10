package com.katafract.parkarmor.services

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCancellableCoroutine

class LocationService(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder: Geocoder = Geocoder(context)

    /**
     * Get current device location.
     */
    suspend fun getCurrentLocation(): LatLng? = suspendCancellableCoroutine { continuation ->
        try {
            val task = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            )
            Tasks.addOnSuccessListener(task) { location ->
                if (location != null) {
                    continuation.resume(LatLng(location.latitude, location.longitude))
                } else {
                    continuation.resume(null)
                }
            }
            Tasks.addOnFailureListener(task) { exception ->
                continuation.resumeWithException(exception)
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Reverse geocode a location to get address.
     */
    suspend fun getAddressFromLocation(lat: Double, lon: Double): String? = suspendCancellableCoroutine { continuation ->
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    if (!address.thoroughfare.isNullOrBlank()) append(address.thoroughfare)
                    if (!address.subThoroughfare.isNullOrBlank()) {
                        if (isNotEmpty()) append(" ")
                        append(address.subThoroughfare)
                    }
                    if (!address.locality.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(address.locality)
                    }
                    if (!address.adminArea.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(address.adminArea)
                    }
                    if (!address.postalCode.isNullOrBlank()) {
                        if (isNotEmpty()) append(" ")
                        append(address.postalCode)
                    }
                }
                continuation.resume(addressText.ifBlank { null })
            } else {
                continuation.resume(null)
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}
