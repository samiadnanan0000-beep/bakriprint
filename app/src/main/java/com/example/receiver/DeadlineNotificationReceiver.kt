package com.example.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.BakriApplication
import com.example.MainActivity
import com.example.R

class DeadlineNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "تذكير موعد التسليم - بكري برنت"
        val client = intent.getStringExtra(EXTRA_CLIENT) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_ID, System.currentTimeMillis().toInt())

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messageText = if (client.isNotBlank()) {
            "حان موعد تسليم طلب العميل: $client ($title)"
        } else {
            "حان موعد إنجاز المهمة: $title"
        }

        val notification = NotificationCompat.Builder(context, BakriApplication.CHANNEL_DEADLINES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ تذكير موعد تسليم: بكري برنت")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not granted or restricted
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_task_title"
        const val EXTRA_CLIENT = "extra_client_name"
        const val EXTRA_ID = "extra_notification_id"
    }
}
