package com.katafract.parkarmor.services

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geocoder: Geocoder = Geocoder(context)

    suspend fun getCurrentLocation(): LatLng? = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(LatLng(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    suspend fun getAddressFromLocation(lat: Double, lon: Double): String? =
        suspendCancellableCoroutine { continuation ->
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressText = buildString {
                        if (!address.thoroughfare.isNullOrBlank()) {
                            append(address.thoroughfare)
                            if (!address.subThoroughfare.isNullOrBlank()) append(" ${address.subThoroughfare}")
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
