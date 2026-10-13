package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.BakriDatabase
import com.example.data.repository.BakriRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BakriApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { BakriDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { BakriRepository(database.bakriDao()) }
    val preferences by lazy { com.example.data.local.AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تنبيهات مواعيد التسليم وورشة بكر برنت"
            val descriptionText = "إشعارات المواعيد النهائية لتسليم الطلبات والتصاميم"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_DEADLINES, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_DEADLINES = "bakri_deadlines_channel"
        lateinit var instance: BakriApplication
            private set
    }
}
