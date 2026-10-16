package com.example.dexlauncher

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.dexlauncher.data.repository.AppRepository
import com.example.dexlauncher.data.repository.LauncherSettings
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.service.StandbyStateManager
import com.example.dexlauncher.ui.desktop.DesktopScreen
import com.example.dexlauncher.ui.drawer.AppDrawerOverlay
import com.example.dexlauncher.ui.quickpanel.QuickPanelOverlay
import com.example.dexlauncher.ui.settings.SettingsPage
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
        updateFullscreenMode()

        setContent {
            DeXLauncherTheme {
                DeXLauncherApp(
                    onLaunchApp = { packageName -> launchFreeformApp(packageName) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateFullscreenMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            updateFullscreenMode()
        }
    }

    private fun updateFullscreenMode() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun launchFreeformApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val options = ActivityOptions.makeBasic()
                val bounds = Rect(150, 100, 1150, 800)
                options.launchBounds = bounds
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
            DeXApplication.instance.appRepository.markAppStarted(packageName)
        } catch (_: Exception) {}
    }
}

@Composable
fun DeXLauncherApp(
    onLaunchApp: (String) -> Unit
) {
    val appCtx = DeXApplication.instance
    val appRepository = appCtx.appRepository
    val settingsRepository = appCtx.settingsRepository

    val settingsFlow by settingsRepository.settingsFlow.collectAsState(
        initial = LauncherSettings(
            desktopWallpaperUri = "",
            desktopWallpaperType = "preset",
            standbyWallpaperUri = "",
            standbyWallpaperType = "preset",
            wallpaperDim = 0.2f,
            wallpaperBlur = 0f,
            clockFont = "Default",
            clockFontPath = "",
            clockColor = android.graphics.Color.WHITE,
            clockScale = 1.0f,
            clockOffsetX = 0.5f,
            clockOffsetY = 0.4f,
            clockOpacity = 1.0f,
            clockBlur = 0f,
            clockWeight = 400,
            clockLetterSpacing = 0f,
            clockShadow = true,
            clockIs24H = true,
            clockShowDate = true,
            clockShowSwipeUp = true,
            clockAlignment = "center",
            iconSize = 64,
            iconLabelSize = 12,
            iconCornerRadius = 16,
            showLabels = true,
            showShadow = true,
            desktopLayoutMode = "grid",
            fullscreenEnabled = true,
            taskbarAutoHide = false,
            animationSpeed = 1.0f,
            hiddenApps = emptySet()
        )
    )

    var isAppDrawerOpen by remember { mutableStateOf(false) }
    var isQuickPanelOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var settingsInitialPage by remember { mutableStateOf<SettingsPage>(SettingsPage.Main) }
    val isInStandby by StandbyStateManager.isStandbyActive.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        appRepository.refreshApps()
        while (true) {
            appRepository.syncRunningTasksWithUsageStats()
            delay(1500L)
        }
    }

    BackHandler(enabled = isAppDrawerOpen || isQuickPanelOpen || isSettingsOpen || isInStandby) {
        if (isInStandby) StandbyStateManager.isInStandby = false
        else if (isAppDrawerOpen) isAppDrawerOpen = false
        else if (isQuickPanelOpen) isQuickPanelOpen = false
        else if (isSettingsOpen) isSettingsOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isInStandby) {
            StandbyScreen(
                settings = settingsFlow,
                onOpenSettings = {
                    settingsInitialPage = SettingsPage.Standby
                    isSettingsOpen = true
                }
            )
        } else {
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

                TaskbarView(
                    appRepository = appRepository,
                    onToggleAppDrawer = { isAppDrawerOpen = !isAppDrawerOpen },
                    onToggleQuickPanel = { isQuickPanelOpen = !isQuickPanelOpen },
                    onOpenSettings = {
                        settingsInitialPage = SettingsPage.Main
                        isSettingsOpen = true
                    },
                    onToggleStandby = { StandbyStateManager.isInStandby = true }
                )
            }

            AppDrawerOverlay(
                visible = isAppDrawerOpen,
                appRepository = appRepository,
                onDismiss = { isAppDrawerOpen = false },
                onStartDragIcon = { app, offset ->
                    scope.launch { appRepository.pinToDesktop(app.packageName) }
                }
            )

            if (isQuickPanelOpen) {
                QuickPanelOverlay(
                    onDismiss = { isQuickPanelOpen = false },
                    onOpenSettings = {
                        isQuickPanelOpen = false
                        settingsInitialPage = SettingsPage.Main
                        isSettingsOpen = true
                    }
                )
            }

            if (isSettingsOpen) {
                SettingsScreen(
                    settingsRepository = settingsRepository,
                    settings = settingsFlow,
                    initialPage = settingsInitialPage,
                    onDismiss = { isSettingsOpen = false }
                )
            }
        }
    }
}
