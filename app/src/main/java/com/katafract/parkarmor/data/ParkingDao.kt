package com.katafract.parkarmor.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: ParkingLocation)

    @Update
    suspend fun updateLocation(location: ParkingLocation)

    @Delete
    suspend fun deleteLocation(location: ParkingLocation)

    @Query("SELECT * FROM parking_locations WHERE isActive = 1 LIMIT 1")
    fun getActiveLocation(): Flow<ParkingLocation?>

    @Query("SELECT * FROM parking_locations ORDER BY savedAt DESC")
    fun getAllLocations(): Flow<List<ParkingLocation>>

    @Query("SELECT * FROM parking_timers WHERE locationId = :locationId LIMIT 1")
    fun getTimerForLocation(locationId: String): Flow<ParkingTimer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: ParkingTimer)

    @Update
    suspend fun updateTimer(timer: ParkingTimer)

    @Delete
    suspend fun deleteTimer(timer: ParkingTimer)

    @Query("DELETE FROM parking_timers WHERE locationId = :locationId")
    suspend fun deleteTimerForLocation(locationId: String)
}
