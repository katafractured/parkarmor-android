package com.katafract.parkarmor.utils

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object LocationUtils {
    /**
     * Calculate distance between two GPS coordinates in meters using Haversine formula.
     */
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadiusM = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * acos(kotlin.math.sqrt(a))
        return (earthRadiusM * c).toFloat()
    }

    /**
     * Calculate bearing from point 1 to point 2 in degrees (0-360, where 0 is North).
     */
    fun getBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        val bearing = Math.toDegrees(kotlin.math.atan2(y, x))
        return ((bearing + 360) % 360).toFloat()
    }

    /**
     * Format distance for display (meters to appropriate unit).
     */
    fun formatDistance(meters: Float): String {
        return when {
            meters < 1000 -> "${meters.toInt()} m"
            else -> String.format("%.2f km", meters / 1000)
        }
    }
}
