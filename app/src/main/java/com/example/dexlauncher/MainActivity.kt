package com.example.dexlauncher

import android.app.ActivityOptions
import android.content.Intent
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

        enableEdgeToEdge()

        appRepository = AppRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        shizukuManager = ShizukuManager(applicationContext)

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
        shizukuManager.checkStatus()
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
        val currentWallpaper by settingsRepository.currentWallpaper.collectAsState()
        val installedApps by appRepository.installedApps.collectAsState()
        val pinnedDesktopApps by appRepository.pinnedDesktopApps.collectAsState()
        val pinnedTaskbarApps by appRepository.pinnedTaskbarApps.collectAsState()
        val runningPackages by appRepository.runningPackages.collectAsState()
        val isOverlayEnabled by settingsRepository.isTaskbarOverlayEnabled.collectAsState()

        var isAppDrawerOpen by remember { mutableStateOf(false) }
        var isSettingsOpen by remember { mutableStateOf(false) }

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
                wallpaper = currentWallpaper,
                pinnedDesktopApps = pinnedDesktopApps,
                pinnedTaskbarApps = pinnedTaskbarApps,
                runningPackages = runningPackages,
                onAppClicked = { app -> launchAppExternalFreeform(app) },
                onTogglePinDesktop = { app ->
                    coroutineScope.launch { appRepository.togglePinToDesktop(app.packageName) }
                },
                onTogglePinTaskbar = { app ->
                    coroutineScope.launch { appRepository.togglePinToTaskbar(app.packageName) }
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
                    wallpaper = currentWallpaper,
                    onUnlocked = {
                        StandbyStateManager.unlockToDesktop()
                    }
                )
            }

            // 3. App Drawer Dialog
            AppDrawerDialog(
                isOpen = isAppDrawerOpen && !isStandby,
                apps = installedApps,
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
                shizukuManager.launchFreeformWindow(app.packageName, bounds)
                appRepository.markAppStarted(app.packageName)
                return
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
