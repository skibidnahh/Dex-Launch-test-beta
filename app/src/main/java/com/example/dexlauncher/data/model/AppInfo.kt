package com.example.dexlauncher.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
    val isPinnedToDesktop: Boolean = false,
    val isPinnedToTaskbar: Boolean = false,
    val isRunning: Boolean = false,
    val launchCount: Int = 0
)
