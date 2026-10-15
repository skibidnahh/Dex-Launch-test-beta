package com.example.dexlauncher

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.dexlauncher.data.model.AppInfo
import com.example.dexlauncher.data.repository.AppRepository
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.service.ScreenStateService
import com.example.dexlauncher.service.StandbyStateManager
import com.example.dexlauncher.service.TaskbarOverlayService
import com.example.dexlauncher.shizuku.ShizukuManager
import com.example.dexlauncher.ui.desktop.DesktopScreen
import com.example.dexlauncher.ui.drawer.AppDrawerDialog
import com.example.dexlauncher.ui.settings.SettingsDialog
import com.example.dexlauncher.ui.standby.StandbyScreen
import com.example.dexlauncher.ui.theme.DeXLauncherTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appRepository: AppRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var shizukuManager: ShizukuManager

    private var openDrawerOnStart by mutableStateOf(false)
    private var openSettingsOnStart by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        enableEdgeToEdge()

        appRepository = AppRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        shizukuManager = ShizukuManager(applicationContext)

        applySystemBarVisibility()
        applyOrientation()

        val screenServiceIntent = Intent(this, ScreenStateService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, screenServiceIntent)
            } else {
                startService(screenServiceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handleExtraIntents(intent)

        setContent {
            DeXLauncherTheme {
                MainContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applySystemBarVisibility()
        applyOrientation()
        shizukuManager.checkStatus()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applySystemBarVisibility()
        }
    }

    private fun applySystemBarVisibility() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (settingsRepository.hideSystemBars.value) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun applyOrientation() {
        requestedOrientation = if (settingsRepository.alwaysLandscape.value) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        StandbyStateManager.onHomeButtonPressed()
        handleExtraIntents(intent)
    }

    private fun handleExtraIntents(intent: Intent?) {
        if (intent?.getBooleanExtra("OPEN_APP_DRAWER", false) == true) {
            openDrawerOnStart = true
        }
        if (intent?.getBooleanExtra("OPEN_SETTINGS", false) == true) {
            openSettingsOnStart = true
        }
    }

    @Composable
    private fun MainContent() {
        val coroutineScope = rememberCoroutineScope()
        val isStandby by StandbyStateManager.isStandbyActive.collectAsState()
        val desktopWallpaper by settingsRepository.desktopWallpaper.collectAsState()
        val standbyWallpaper by settingsRepository.standbyWallpaper.collectAsState()
        val standbySettings by settingsRepository.standbySettings.collectAsState()
        val desktopAppearance by settingsRepository.desktopAppearance.collectAsState()
        val taskbarAppearance by settingsRepository.taskbarAppearance.collectAsState()
        val alwaysLandscape by settingsRepository.alwaysLandscape.collectAsState()
        val hideSystemBars by settingsRepository.hideSystemBars.collectAsState()

        val installedApps by appRepository.installedApps.collectAsState()
        val pinnedDesktopApps by appRepository.pinnedDesktopApps.collectAsState()
        val pinnedTaskbarApps by appRepository.pinnedTaskbarApps.collectAsState()
        val runningPackages by appRepository.runningPackages.collectAsState()
        val focusedPackage by appRepository.focusedPackage.collectAsState()
        val hiddenPackages by appRepository.hiddenPackages.collectAsState()
        val isOverlayEnabled by settingsRepository.isTaskbarOverlayEnabled.collectAsState()

        var isAppDrawerOpen by remember { mutableStateOf(false) }
        var isSettingsOpen by remember { mutableStateOf(false) }

        LaunchedEffect(alwaysLandscape) {
            applyOrientation()
        }

        LaunchedEffect(hideSystemBars) {
            applySystemBarVisibility()
        }

        LaunchedEffect(openDrawerOnStart) {
            if (openDrawerOnStart) {
                isAppDrawerOpen = true
                openDrawerOnStart = false
            }
        }

        LaunchedEffect(openSettingsOnStart) {
            if (openSettingsOnStart) {
                isSettingsOpen = true
                openSettingsOnStart = false
            }
        }

        LaunchedEffect(isOverlayEnabled) {
            val serviceIntent = Intent(this@MainActivity, TaskbarOverlayService::class.java)
            if (isOverlayEnabled && settingsRepository.hasOverlayPermission()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                stopService(serviceIntent)
            }
        }

        LaunchedEffect(Unit) {
            appRepository.refreshApps()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Desktop Screen
            DesktopScreen(
                wallpaperSettings = desktopWallpaper,
                desktopAppearance = desktopAppearance,
                taskbarAppearance = taskbarAppearance,
                pinnedDesktopApps = pinnedDesktopApps,
                pinnedTaskbarApps = pinnedTaskbarApps,
                allApps = installedApps,
                runningPackages = runningPackages,
                focusedPackage = focusedPackage,
                shizukuManager = shizukuManager,
                isStandbyActive = isStandby,
                onAppClicked = { app -> launchAppExternalFreeform(app) },
                onTogglePinDesktop = { app ->
                    coroutineScope.launch { appRepository.togglePinToDesktop(app.packageName) }
                },
                onTogglePinTaskbar = { app ->
                    coroutineScope.launch { appRepository.togglePinToTaskbar(app.packageName) }
                },
                onHideApp = { app ->
                    coroutineScope.launch { appRepository.toggleHideApp(app.packageName) }
                },
                onCloseApp = { app ->
                    if (shizukuManager.isConnected()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            shizukuManager.forceStopApp(app.packageName)
                        }
                    }
                },
                onOpenAppDrawer = { isAppDrawerOpen = true },
                onOpenSettings = { isSettingsOpen = true },
                onRecentsClicked = { isAppDrawerOpen = true },
                onBackClicked = { }
            )

            // 2. Standby (Lock) Overlay Layer
            AnimatedVisibility(
                visible = isStandby,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it }
            ) {
                StandbyScreen(
                    wallpaperSettings = standbyWallpaper,
                    standbySettings = standbySettings,
                    onUnlocked = {
                        StandbyStateManager.unlockToDesktop()
                    }
                )
            }

            // 3. App Drawer Dialog
            AppDrawerDialog(
                isOpen = isAppDrawerOpen && !isStandby,
                apps = installedApps,
                hiddenPackages = hiddenPackages,
                onDismissRequest = { isAppDrawerOpen = false },
                onAppClicked = { app ->
                    isAppDrawerOpen = false
                    launchAppExternalFreeform(app)
                },
                onTogglePinDesktop = { app ->
                    coroutineScope.launch { appRepository.togglePinToDesktop(app.packageName) }
                },
                onTogglePinTaskbar = { app ->
                    coroutineScope.launch { appRepository.togglePinToTaskbar(app.packageName) }
                },
                onHideApp = { app ->
                    coroutineScope.launch { appRepository.toggleHideApp(app.packageName) }
                }
            )

            // 4. Settings Dialog
            SettingsDialog(
                isOpen = isSettingsOpen && !isStandby,
                settingsRepository = settingsRepository,
                shizukuManager = shizukuManager,
                onDismissRequest = { isSettingsOpen = false }
            )
        }
    }

    private fun launchAppExternalFreeform(app: AppInfo) {
        val displayMetrics = resources.displayMetrics
        val bounds: Rect = settingsRepository.calculateNextWindowBounds(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels
        )

        // Shizuku Windowing Mode 5 (if connected in BETA)
        if (shizukuManager.isConnected()) {
            try {
                val launched = shizukuManager.launchFreeformWindow(app.packageName, bounds)
                if (launched) {
                    appRepository.markAppStarted(app.packageName)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Standard ActivityOptions freeform bounds launch
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            val options = ActivityOptions.makeBasic()
            options.setLaunchBounds(bounds)

            try {
                startActivity(launchIntent, options.toBundle())
                appRepository.markAppStarted(app.packageName)
            } catch (e: Exception) {
                try {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(launchIntent)
                    appRepository.markAppStarted(app.packageName)
                } catch (ex: Exception) {
                    Toast.makeText(this, "Không thể khởi chạy: ${app.label}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Không tìm thấy intent của ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }
}
