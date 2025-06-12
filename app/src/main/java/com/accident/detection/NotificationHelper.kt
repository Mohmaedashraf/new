
package com.accident.detection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "bt_channel"
    const val NOTIFICATION_ID = 1

    private fun ensure(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Bluetooth Messages", NotificationManager.IMPORTANCE_HIGH))
            }
        }
    }

    fun baseNotification(ctx: Context): Notification {
        ensure(ctx)
        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle("Accident Detection")
            .setContentText("Listening for messages…")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
    }

    fun pushMessage(ctx: Context, msg: String) {
        ensure(ctx)
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle("Accident Detection")
            .setContentText(msg)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), n)
    }
}
