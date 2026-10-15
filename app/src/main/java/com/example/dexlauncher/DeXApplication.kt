package com.example.dexlauncher

import android.app.Application
import com.example.dexlauncher.data.repository.AppRepository
import com.example.dexlauncher.data.repository.SettingsRepository

class DeXApplication : Application() {

    val appRepository by lazy { AppRepository(this) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DeXApplication
            private set
    }
}
