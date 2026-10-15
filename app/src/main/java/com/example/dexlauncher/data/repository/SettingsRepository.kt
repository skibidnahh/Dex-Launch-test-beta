package com.example.dexlauncher.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.provider.Settings
import com.example.dexlauncher.data.model.DeXWallpaperPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

class SettingsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("dex_settings_pref", Context.MODE_PRIVATE)

    private val _currentWallpaper = MutableStateFlow(loadWallpaperPreset())
    val currentWallpaper: StateFlow<DeXWallpaperPreset> = _currentWallpaper.asStateFlow()

    private val _isTaskbarOverlayEnabled = MutableStateFlow(
        prefs.getBoolean("taskbar_overlay_enabled", false)
    )
    val isTaskbarOverlayEnabled: StateFlow<Boolean> = _isTaskbarOverlayEnabled.asStateFlow()

    private val _iconScale = MutableStateFlow(
        prefs.getFloat("icon_scale", 1.0f)
    )
    val iconScale: StateFlow<Float> = _iconScale.asStateFlow()

    private val cascadeStep = AtomicInteger(0)

    fun setWallpaper(preset: DeXWallpaperPreset) {
        prefs.edit().putString("wallpaper_preset_id", preset.id).apply()
        _currentWallpaper.value = preset
    }

    private fun loadWallpaperPreset(): DeXWallpaperPreset {
        val id = prefs.getString("wallpaper_preset_id", DeXWallpaperPreset.NEBULA_PURPLE.id)
        return DeXWallpaperPreset.entries.find { it.id == id } ?: DeXWallpaperPreset.NEBULA_PURPLE
    }

    fun setTaskbarOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("taskbar_overlay_enabled", enabled).apply()
        _isTaskbarOverlayEnabled.value = enabled
    }

    fun setIconScale(scale: Float) {
        prefs.edit().putFloat("icon_scale", scale).apply()
        _iconScale.value = scale
    }

    fun calculateNextWindowBounds(screenWidth: Int, screenHeight: Int): Rect {
        val windowWidth = (screenWidth * 0.72).toInt().coerceAtLeast(480)
        val windowHeight = (screenHeight * 0.70).toInt().coerceAtLeast(600)

        val maxOffsetX = (screenWidth - windowWidth).coerceAtLeast(0)
        val maxOffsetY = (screenHeight - windowHeight - 80).coerceAtLeast(0) // Leave space for bottom taskbar

        val step = cascadeStep.getAndIncrement() % 6
        val offsetX = (step * 55).coerceAtMost(maxOffsetX)
        val offsetY = (step * 55 + 40).coerceAtMost(maxOffsetY)

        return Rect(offsetX, offsetY, offsetX + windowWidth, offsetY + windowHeight)
    }

    fun hasWriteSecureSettings(): Boolean {
        return context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") ==
                PackageManager.PERMISSION_GRANTED
    }

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isFreeformGlobalEnabled(): Boolean {
        return try {
            val freeform = Settings.Global.getInt(context.contentResolver, "enable_freeform_support", 0)
            val resizable = Settings.Global.getInt(context.contentResolver, "force_resizable_activities", 0)
            freeform == 1 && resizable == 1
        } catch (e: Exception) {
            false
        }
    }

    fun tryEnableFreeformViaSecureSettings(): Boolean {
        if (!hasWriteSecureSettings()) return false
        return try {
            Settings.Global.putInt(context.contentResolver, "enable_freeform_support", 1)
            Settings.Global.putInt(context.contentResolver, "force_resizable_activities", 1)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
