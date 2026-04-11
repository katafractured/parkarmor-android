package com.katafract.parkarmor.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.LocalBroadcastManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.katafract.parkarmor.R

class ParkingTimerService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "parking_timer"
        const val ACTION_TIMER_EXPIRED = "com.katafract.parkarmor.TIMER_EXPIRED"
        const val ACTION_TIMER_UPDATE = "com.katafract.parkarmor.TIMER_UPDATE"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_ADDRESS = "address"
        const val EXTRA_LOCATION_ID = "location_id"
        const val EXTRA_REMAINING_MINUTES = "remaining_minutes"
    }

    private var countDownTimer: CountDownTimer? = null
    private var locationId: String = ""
    private var address: String = ""
    private val broadcastManager by lazy { LocalBroadcastManager.getInstance(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        when (intent.action) {
            ACTION_TIMER_EXPIRED -> {
                val expiredLocationId = intent.getStringExtra(EXTRA_LOCATION_ID) ?: ""
                handleTimerExpired(expiredLocationId)
            }
            else -> {
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 60)
                address = intent.getStringExtra(EXTRA_ADDRESS) ?: "Your parked car"
                locationId = intent.getStringExtra(EXTRA_LOCATION_ID) ?: ""

                startForegroundService()
                startCountdownTimer(durationMinutes.toLong() * 60 * 1000)
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parking Timer Active")
            .setContentText(address)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        createNotificationChannel()

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

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = android.app.NotificationManager.IMPORTANCE_LOW
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Parking Timer",
                importance
            ).apply {
                description = "Notifications for parking timers"
            }
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startCountdownTimer(durationMillis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMillis, 30_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minutesRemaining = millisUntilFinished / 1000 / 60
                updateNotification(minutesRemaining)
                broadcastUpdate(minutesRemaining)
            }

            override fun onFinish() {
                handleTimerExpired(locationId)
            }
        }
        countDownTimer?.start()
    }

    private fun updateNotification(minutesRemaining: Long) {
        val timeText = if (minutesRemaining > 0) {
            val hours = minutesRemaining / 60
            val mins = minutesRemaining % 60
            when {
                hours > 0 -> "${hours}h ${mins}m remaining"
                else -> "${mins}m remaining"
            }
        } else {
            "Timer expired"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parking Timer: $timeText")
            .setContentText(address)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, 100, false)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun broadcastUpdate(minutesRemaining: Long) {
        val intent = Intent(ACTION_TIMER_UPDATE).apply {
            putExtra(EXTRA_REMAINING_MINUTES, minutesRemaining)
            putExtra(EXTRA_LOCATION_ID, locationId)
        }
        broadcastManager.sendBroadcast(intent)
    }

    private fun handleTimerExpired(expiredLocationId: String) {
        countDownTimer?.cancel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parking Time Expired!")
            .setContentText("Your parking timer has finished. $address")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(expiredLocationId.hashCode(), notification)

        val broadcastIntent = Intent(ACTION_TIMER_EXPIRED).apply {
            putExtra(EXTRA_LOCATION_ID, expiredLocationId)
        }
        broadcastManager.sendBroadcast(broadcastIntent)

        stopSelf()
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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
