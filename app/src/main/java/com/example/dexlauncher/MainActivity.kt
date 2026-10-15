package com.example.dexlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dexlauncher.data.model.AppInfo
import com.example.dexlauncher.ui.desktop.DesktopScreen
import com.example.dexlauncher.ui.drawer.AppDrawerOverlay
import com.example.dexlauncher.ui.quickpanel.QuickPanelOverlay
import com.example.dexlauncher.ui.settings.SettingsScreen
import com.example.dexlauncher.ui.standby.StandbyScreen
import com.example.dexlauncher.ui.taskbar.TaskbarView
import com.example.dexlauncher.ui.theme.DeXLauncherTheme
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeXLauncherTheme {
                DeXLauncherApp()
            }
        }
    }
}

@Composable
fun DeXLauncherApp() {
    val appCtx = DeXApplication.instance
    val appRepository = appCtx.appRepository
    val settingsRepository = appCtx.settingsRepository

    val settingsFlow = settingsRepository.settingsFlow.collectAsState(
        initial = com.example.dexlauncher.data.repository.LauncherSettings(
            desktopWallpaperUri = "",
            desktopWallpaperType = "preset",
            standbyWallpaperUri = "",
            standbyWallpaperType = "preset",
            wallpaperDim = 0.2f,
            wallpaperBlur = 0f,
            clockFont = "Default",
            clockColor = android.graphics.Color.WHITE,
            clockScale = 1.0f,
            clockOffsetX = 0.5f,
            clockOffsetY = 0.3f,
            clockOpacity = 1.0f,
            clockBlur = 0f,
            iconSize = 64,
            iconLabelSize = 12,
            iconCornerRadius = 16,
            showLabels = true,
            showShadow = true,
            desktopLayoutMode = "grid"
        )
    ).value

    var isAppDrawerOpen by remember { mutableStateOf(false) }
    var isQuickPanelOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isStandbyOpen by remember { mutableStateOf(false) }

    // Drag and drop floating icon state
    var draggedApp by remember { mutableStateOf<AppInfo?>(null) }
    var dragOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        appRepository.refreshApps()
        while (true) {
            appRepository.syncRunningTasksWithUsageStats()
            delay(1500L)
        }
    }

    // BackHandler to close overlays
    BackHandler(enabled = isAppDrawerOpen || isQuickPanelOpen || isSettingsOpen || isStandbyOpen) {
        if (isStandbyOpen) isStandbyOpen = false
        else if (isAppDrawerOpen) isAppDrawerOpen = false
        else if (isQuickPanelOpen) isQuickPanelOpen = false
        else if (isSettingsOpen) isSettingsOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isStandbyOpen) {
            StandbyScreen(
                settings = settingsFlow,
                onDismiss = { isStandbyOpen = false }
            )
        } else {
            // Main Desktop & Wallpaper
            WallpaperBackground(
                wallpaperUri = settingsFlow.desktopWallpaperUri,
                wallpaperType = settingsFlow.desktopWallpaperType,
                dim = settingsFlow.wallpaperDim,
                blur = settingsFlow.wallpaperBlur
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    DesktopScreen(
                        appRepository = appRepository,
                        iconSize = settingsFlow.iconSize,
                        iconLabelSize = settingsFlow.iconLabelSize,
                        iconCornerRadius = settingsFlow.iconCornerRadius,
                        showLabels = settingsFlow.showLabels,
                        showShadow = settingsFlow.showShadow,
                        layoutMode = settingsFlow.desktopLayoutMode
                    )
                }

                // Taskbar at bottom
                TaskbarView(
                    appRepository = appRepository,
                    onToggleAppDrawer = { isAppDrawerOpen = !isAppDrawerOpen },
                    onToggleQuickPanel = { isQuickPanelOpen = !isQuickPanelOpen },
                    onOpenSettings = { isSettingsOpen = true },
                    onToggleStandby = { isStandbyOpen = true }
                )
            }

            // App Drawer Overlay
            AppDrawerOverlay(
                visible = isAppDrawerOpen,
                appRepository = appRepository,
                onDismiss = { isAppDrawerOpen = false },
                onStartDragIcon = { app, offset ->
                    draggedApp = app
                    dragOffset = offset
                    scope.launch {
                        appRepository.pinToDesktop(app.packageName)
                    }
                }
            )

            // Quick Panel Overlay
            if (isQuickPanelOpen) {
                QuickPanelOverlay(
                    onDismiss = { isQuickPanelOpen = false },
                    onOpenSettings = {
                        isQuickPanelOpen = false
                        isSettingsOpen = true
                    }
                )
            }

            // Settings Overlay
            if (isSettingsOpen) {
                SettingsScreen(
                    settingsRepository = settingsRepository,
                    settings = settingsFlow,
                    onDismiss = { isSettingsOpen = false }
                )
            }
        }
    }
}
