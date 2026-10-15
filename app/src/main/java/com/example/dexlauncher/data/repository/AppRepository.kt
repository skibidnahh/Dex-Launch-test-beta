package com.example.dexlauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.example.dexlauncher.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("dex_apps_pref", Context.MODE_PRIVATE)

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _pinnedDesktopApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val pinnedDesktopApps: StateFlow<List<AppInfo>> = _pinnedDesktopApps.asStateFlow()

    private val _pinnedTaskbarApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val pinnedTaskbarApps: StateFlow<List<AppInfo>> = _pinnedTaskbarApps.asStateFlow()

    private val _runningPackages = MutableStateFlow<Set<String>>(emptySet())
    val runningPackages: StateFlow<Set<String>> = _runningPackages.asStateFlow()

    suspend fun refreshApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        val desktopPinnedSet = getPinnedDesktopPackages()
        val taskbarPinnedSet = getPinnedTaskbarPackages()
        val runningSet = _runningPackages.value
        val myPackageName = context.packageName

        val apps = resolveInfos
            .filter { it.activityInfo.packageName != myPackageName }
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                val activity = resolveInfo.activityInfo.name

                AppInfo(
                    label = label,
                    packageName = pkg,
                    activityName = activity,
                    icon = icon,
                    isPinnedToDesktop = desktopPinnedSet.contains(pkg),
                    isPinnedToTaskbar = taskbarPinnedSet.contains(pkg),
                    isRunning = runningSet.contains(pkg)
                )
            }
            .sortedBy { it.label.lowercase() }

        _installedApps.value = apps

        // Update pinned lists
        _pinnedDesktopApps.value = apps.filter { it.isPinnedToDesktop }
        _pinnedTaskbarApps.value = apps.filter { it.isPinnedToTaskbar }
    }

    private fun getPinnedDesktopPackages(): Set<String> {
        val defaultPinned = setOf(
            "com.sec.android.app.myfiles",
            "com.android.settings",
            "com.sec.android.app.sbrowser",
            "com.android.chrome",
            "com.google.android.apps.photos",
            "com.sec.android.gallery3d",
            "com.google.android.gm",
            "com.spotify.music"
        )
        return prefs.getStringSet("pinned_desktop_packages", null) ?: run {
            // Seed defaults on first run
            prefs.edit().putStringSet("pinned_desktop_packages", defaultPinned).apply()
            defaultPinned
        }
    }

    private fun getPinnedTaskbarPackages(): Set<String> {
        val defaultPinned = setOf(
            "com.google.android.gm",
            "com.android.settings",
            "com.google.android.apps.docs",
            "com.android.chrome"
        )
        return prefs.getStringSet("pinned_taskbar_packages", null) ?: run {
            prefs.edit().putStringSet("pinned_taskbar_packages", defaultPinned).apply()
            defaultPinned
        }
    }

    suspend fun togglePinToDesktop(packageName: String) {
        val currentSet = getPinnedDesktopPackages().toMutableSet()
        if (currentSet.contains(packageName)) {
            currentSet.remove(packageName)
        } else {
            currentSet.add(packageName)
        }
        prefs.edit().putStringSet("pinned_desktop_packages", currentSet).apply()
        refreshApps()
    }

    suspend fun togglePinToTaskbar(packageName: String) {
        val currentSet = getPinnedTaskbarPackages().toMutableSet()
        if (currentSet.contains(packageName)) {
            currentSet.remove(packageName)
        } else {
            currentSet.add(packageName)
        }
        prefs.edit().putStringSet("pinned_taskbar_packages", currentSet).apply()
        refreshApps()
    }

    fun markAppStarted(packageName: String) {
        val current = _runningPackages.value.toMutableSet()
        current.add(packageName)
        _runningPackages.value = current
        _installedApps.value = _installedApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = true) else it
        }
        _pinnedDesktopApps.value = _pinnedDesktopApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = true) else it
        }
        _pinnedTaskbarApps.value = _pinnedTaskbarApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = true) else it
        }
    }

    fun markAppClosed(packageName: String) {
        val current = _runningPackages.value.toMutableSet()
        current.remove(packageName)
        _runningPackages.value = current
        _installedApps.value = _installedApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = false) else it
        }
    }
}
