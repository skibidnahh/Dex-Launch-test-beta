package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.dexlauncher.data.repository.LauncherSettings
import com.example.dexlauncher.service.StandbyStateManager
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground

@Composable
fun StandbyScreen(
    settings: LauncherSettings,
    onOpenSettings: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        offsetY += dragAmount
                        if (offsetY < -100f) {
                            StandbyStateManager.isInStandby = false
                        }
                    },
                    onDragEnd = {
                        if (offsetY > -100f) {
                            offsetY = 0f
                        }
                    }
                )
            }
    ) {
        WallpaperBackground(
            wallpaperUri = settings.standbyWallpaperUri,
            wallpaperType = settings.standbyWallpaperType,
            dim = settings.wallpaperDim,
            blur = settings.wallpaperBlur
        )

        StandbyClockView(
            settings = settings,
            onClick = onOpenSettings
        )
    }
}
