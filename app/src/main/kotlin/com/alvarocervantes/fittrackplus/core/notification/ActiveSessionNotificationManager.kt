package com.alvarocervantes.fittrackplus.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alvarocervantes.fittrackplus.MainActivity
import com.alvarocervantes.fittrackplus.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "active_session"
private const val NOTIFICATION_ID = 1001

@Singleton
class ActiveSessionNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sesion en curso",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificacion mientras entrenas"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun show(routineName: String, dayName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_tab", "workout")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Sesion en curso")
            .setContentText("$routineName – $dayName")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasPermission) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
