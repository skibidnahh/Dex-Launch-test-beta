package com.example.dexlauncher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.dexlauncher.data.repository.AppRepository
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.service.ScreenStateService

class DeXApplication : Application() {

    lateinit var appRepository: AppRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        appRepository = AppRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)

        createNotificationChannels()
        startScreenMonitorService()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val screenChannel = NotificationChannel(
                CHANNEL_SCREEN_SERVICE,
                getString(R.string.notification_channel_screen),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Theo dõi trạng thái màn hình để chuyển về Standby"
                setShowBadge(false)
            }

            val taskbarChannel = NotificationChannel(
                CHANNEL_TASKBAR_SERVICE,
                getString(R.string.notification_channel_taskbar),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị thanh tác vụ DeX dạng overlay"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(screenChannel)
            notificationManager.createNotificationChannel(taskbarChannel)
        }
    }

    fun startScreenMonitorService() {
        val intent = Intent(this, ScreenStateService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var instance: DeXApplication
            private set
        const val CHANNEL_SCREEN_SERVICE = "dex_screen_state_channel"
        const val CHANNEL_TASKBAR_SERVICE = "dex_taskbar_overlay_channel"
    }
}
