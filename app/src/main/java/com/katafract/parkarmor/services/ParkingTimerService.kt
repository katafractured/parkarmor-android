package com.katafract.parkarmor.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.katafract.parkarmor.R

class ParkingTimerService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "parking_timer"
        const val ACTION_TIMER_EXPIRED = "com.katafract.parkarmor.TIMER_EXPIRED"
        const val EXTRA_LOCATION_ID = "location_id"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_TIMER_EXPIRED) {
            val locationId = intent.getStringExtra(EXTRA_LOCATION_ID) ?: ""
            handleTimerExpired(locationId)
        } else {
            startForegroundService()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parking Timer")
            .setContentText("Monitoring parking meter")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
    }

    private fun handleTimerExpired(locationId: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parking Meter Expiring")
            .setContentText("Your parking meter will expire soon")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(locationId.hashCode(), notification)
    }

    fun setTimer(context: Context, locationId: String, expiresAt: Long, warningMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ParkingTimerService::class.java).apply {
            action = ACTION_TIMER_EXPIRED
            putExtra(EXTRA_LOCATION_ID, locationId)
        }
        val pendingIntent = PendingIntent.getService(
            context,
            locationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val warningTime = expiresAt - (warningMinutes * 60 * 1000)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, warningTime, pendingIntent)
        } catch (e: Exception) {
            // Fallback if setExactAndAllowWhileIdle not available
            alarmManager.set(AlarmManager.RTC_WAKEUP, warningTime, pendingIntent)
        }
    }

    fun cancelTimer(context: Context, locationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ParkingTimerService::class.java)
        val pendingIntent = PendingIntent.getService(
            context,
            locationId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
