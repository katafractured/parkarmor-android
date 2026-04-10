package com.katafract.parkarmor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "parking_locations")
data class ParkingLocation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val notes: String = "",
    val savedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val nickname: String? = null,
    val photoUris: String = "[]" // JSON array of content URIs
)
