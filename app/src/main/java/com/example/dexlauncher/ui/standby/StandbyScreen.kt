package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.dexlauncher.data.repository.LauncherSettings
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground

@Composable
fun StandbyScreen(
    settings: LauncherSettings,
    onDismiss: () -> Unit
) {
    var showSimulator by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        WallpaperBackground(
            wallpaperUri = settings.standbyWallpaperUri,
            wallpaperType = settings.standbyWallpaperType,
            dim = settings.wallpaperDim,
            blur = settings.wallpaperBlur
        )

        StandbyClockView(
            settings = settings,
            onClick = { showSimulator = true }
        )

        if (showSimulator) {
            StandbyCustomizeSimulatorDialog(
                settings = settings,
                onDismiss = { showSimulator = false }
            )
        }
    }
}
