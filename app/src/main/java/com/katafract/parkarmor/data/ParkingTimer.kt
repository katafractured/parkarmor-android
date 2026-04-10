package com.katafract.parkarmor.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "parking_timers",
    foreignKeys = [
        ForeignKey(
            entity = ParkingLocation::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("locationId")]
)
data class ParkingTimer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val locationId: String,
    val expiresAt: Long,
    val warningMinutes: Int = 10,
    val isActive: Boolean = true
)
