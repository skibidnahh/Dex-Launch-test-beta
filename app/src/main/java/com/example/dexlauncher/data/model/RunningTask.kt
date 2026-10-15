package com.example.dexlauncher.data.model

import android.graphics.Rect

data class RunningTask(
    val taskId: Int,
    val packageName: String,
    val title: String,
    val windowingMode: Int = 5, // 5 = WINDOWING_MODE_FREEFORM in Android framework
    val bounds: Rect = Rect()
)
