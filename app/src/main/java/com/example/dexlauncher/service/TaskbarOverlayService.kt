package com.example.dexlauncher.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.dexlauncher.DeXApplication
import com.example.dexlauncher.MainActivity
import com.example.dexlauncher.R
import com.example.dexlauncher.data.repository.AppRepository
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.shizuku.ShizukuManager
import com.example.dexlauncher.ui.taskbar.TaskbarView
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import com.example.dexlauncher.ui.theme.DeXLauncherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TaskbarOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private lateinit var appRepository: AppRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var shizukuManager: ShizukuManager

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        appRepository = AppRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        shizukuManager = ShizukuManager(applicationContext)

        serviceScope.launch {
            appRepository.refreshApps()
        }

        startForegroundNotification()

        if (Settings.canDrawOverlays(this)) {
            showTaskbarOverlay()
        }
    }

    private fun startForegroundNotification() {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, DeXApplication.CHANNEL_TASKBAR_SERVICE)
            .setContentTitle("Samsung DeX Taskbar")
            .setContentText(getString(R.string.taskbar_service_running))
            .setSmallIcon(R.drawable.ic_dex_apps)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1002, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1002, notification)
            }
        } else {
            startForeground(1002, notification)
        }
    }

    private fun showTaskbarOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.FILL_HORIZONTAL
            height = (54 * resources.displayMetrics.density).toInt()
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@TaskbarOverlayService)
            setViewTreeViewModelStoreOwner(this@TaskbarOverlayService)
            setViewTreeSavedStateRegistryOwner(this@TaskbarOverlayService)

            setContent {
                DeXLauncherTheme {
                    val pinnedApps by appRepository.pinnedTaskbarApps.collectAsState()
                    val allApps by appRepository.installedApps.collectAsState()
                    val runningPackages by appRepository.runningPackages.collectAsState()
                    val taskbarAppearance by settingsRepository.taskbarAppearance.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(taskbarAppearance.heightDp.dp)
                            .background(DeXDarkPalette.TaskbarBackground)
                    ) {
                        TaskbarView(
                            pinnedApps = pinnedApps,
                            allApps = allApps,
                            runningPackages = runningPackages,
                            focusedPackage = null,
                            taskbarAppearance = taskbarAppearance,
                            shizukuManager = shizukuManager,
                            onAppsClicked = {
                                val intent = Intent(this@TaskbarOverlayService, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    putExtra("OPEN_APP_DRAWER", true)
                                }
                                startActivity(intent)
                            },
                            onHomeClicked = {
                                val intent = Intent(this@TaskbarOverlayService, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                }
                                startActivity(intent)
                            },
                            onRecentsClicked = {
                                try {
                                    if (shizukuManager.isConnected()) {
                                        serviceScope.launch(Dispatchers.IO) {
                                            shizukuManager.executeShell("input keyevent KEYCODE_APP_SWITCH")
                                        }
                                    } else {
                                        val intent = Intent(this@TaskbarOverlayService, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                        }
                                        startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            onBackClicked = {
                                if (shizukuManager.isConnected()) {
                                    serviceScope.launch(Dispatchers.IO) {
                                        shizukuManager.executeShell("input keyevent KEYCODE_BACK")
                                    }
                                }
                            },
                            onAppClicked = { app, isRunning ->
                                val intent = packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                if (intent != null) {
                                    startActivity(intent)
                                    appRepository.markAppStarted(app.packageName)
                                }
                            },
                            onCloseApp = { app ->
                                if (shizukuManager.isConnected()) {
                                    serviceScope.launch(Dispatchers.IO) {
                                        shizukuManager.forceStopApp(app.packageName)
                                    }
                                }
                            },
                            onTogglePinTaskbar = { app ->
                                serviceScope.launch {
                                    appRepository.togglePinToTaskbar(app.packageName)
                                }
                            },
                            onSettingsClicked = {
                                val intent = Intent(this@TaskbarOverlayService, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    putExtra("OPEN_SETTINGS", true)
                                }
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        try {
            windowManager?.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
