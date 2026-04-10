package com.katafract.parkarmor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ParkingLocation::class, ParkingTimer::class],
    version = 1,
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun parkingDao(): ParkingDao

    companion object {
        @Volatile
        private var instance: ParkingDatabase? = null

        fun getInstance(context: Context): ParkingDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                ).build().also { instance = it }
            }
        }
    }
}
