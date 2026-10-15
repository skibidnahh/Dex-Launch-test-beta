package com.example.dexlauncher.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.example.dexlauncher.data.model.AppInfo
import com.example.dexlauncher.shizuku.ShizukuManager
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

    private val _focusedPackage = MutableStateFlow<String?>(null)
    val focusedPackage: StateFlow<String?> = _focusedPackage.asStateFlow()

    private val _hiddenPackages = MutableStateFlow<Set<String>>(loadHiddenPackages())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages.asStateFlow()

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
        val hiddenSet = _hiddenPackages.value
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

        // Filter out hidden packages from desktop and drawer
        val desktopOrder = getDesktopAppOrder()
        val pinned = apps.filter { it.isPinnedToDesktop && !hiddenSet.contains(it.packageName) }
        _pinnedDesktopApps.value = if (desktopOrder.isNotEmpty()) {
            val orderMap = desktopOrder.withIndex().associate { it.value to it.index }
            pinned.sortedBy { orderMap[it.packageName] ?: 999 }
        } else {
            pinned
        }

        _pinnedTaskbarApps.value = apps.filter { it.isPinnedToTaskbar && !hiddenSet.contains(it.packageName) }
    }

    private fun loadHiddenPackages(): Set<String> {
        return prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet()
    }

    fun hideApp(packageName: String) {
        val current = _hiddenPackages.value.toMutableSet()
        current.add(packageName)
        prefs.edit().putStringSet("hidden_packages", current).apply()
        _hiddenPackages.value = current
        _pinnedDesktopApps.value = _pinnedDesktopApps.value.filter { it.packageName != packageName }
        _pinnedTaskbarApps.value = _pinnedTaskbarApps.value.filter { it.packageName != packageName }
    }

    fun unhideApp(packageName: String) {
        val current = _hiddenPackages.value.toMutableSet()
        current.remove(packageName)
        prefs.edit().putStringSet("hidden_packages", current).apply()
        _hiddenPackages.value = current
    }

    fun toggleHideApp(packageName: String) {
        if (_hiddenPackages.value.contains(packageName)) {
            unhideApp(packageName)
        } else {
            hideApp(packageName)
        }
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

    fun getDesktopAppOrder(): List<String> {
        val raw = prefs.getString("desktop_app_order", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    fun saveDesktopAppOrder(order: List<String>) {
        prefs.edit().putString("desktop_app_order", order.joinToString(",")).apply()
        val orderMap = order.withIndex().associate { it.value to it.index }
        _pinnedDesktopApps.value = _pinnedDesktopApps.value.sortedBy { orderMap[it.packageName] ?: 999 }
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
        _focusedPackage.value = packageName

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

    fun setFocusedApp(packageName: String?) {
        _focusedPackage.value = packageName
    }

    fun markAppClosed(packageName: String) {
        val current = _runningPackages.value.toMutableSet()
        current.remove(packageName)
        _runningPackages.value = current
        if (_focusedPackage.value == packageName) {
            _focusedPackage.value = current.lastOrNull()
        }

        _installedApps.value = _installedApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = false) else it
        }
        _pinnedDesktopApps.value = _pinnedDesktopApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = false) else it
        }
        _pinnedTaskbarApps.value = _pinnedTaskbarApps.value.map {
            if (it.packageName == packageName) it.copy(isRunning = false) else it
        }
    }

    fun syncRunningTasksWithShizuku(shizukuManager: ShizukuManager) {
        if (!shizukuManager.isConnected()) return
        try {
            val output = shizukuManager.executeShell("dumpsys activity recents | grep -E 'realActivity=' | head -n 15")
            val regex = Regex("realActivity=([^/]+)/")
            val matches = regex.findAll(output).map { it.groupValues[1] }.toSet()
            if (matches.isNotEmpty()) {
                val current = _runningPackages.value.toMutableSet()
                current.addAll(matches.filter { it != context.packageName })
                _runningPackages.value = current
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun syncRunningTasksWithUsageStats() {
        try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 1000 * 60 * 30 // last 30 minutes
            val usageEvents = usm.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            val recentPkgs = mutableSetOf<String>()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && event.packageName != context.packageName) {
                    recentPkgs.add(event.packageName)
                }
            }
            if (recentPkgs.isNotEmpty()) {
                val current = _runningPackages.value.toMutableSet()
                current.addAll(recentPkgs)
                _runningPackages.value = current
            }
        } catch (e: Exception) {
            // Ignore if permission not granted
        }
    }
}
