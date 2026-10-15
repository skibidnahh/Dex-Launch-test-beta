package com.example.dexlauncher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "dex_settings")

class SettingsRepository(private val context: Context) {

    object PreferencesKeys {
        val DESKTOP_WALLPAPER_URI = stringPreferencesKey("desktop_wallpaper_uri")
        val DESKTOP_WALLPAPER_TYPE = stringPreferencesKey("desktop_wallpaper_type") // preset, image, video
        val STANDBY_WALLPAPER_URI = stringPreferencesKey("standby_wallpaper_uri")
        val STANDBY_WALLPAPER_TYPE = stringPreferencesKey("standby_wallpaper_type")
        val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
        val WALLPAPER_BLUR = floatPreferencesKey("wallpaper_blur")

        val CLOCK_FONT = stringPreferencesKey("clock_font")
        val CLOCK_COLOR = intPreferencesKey("clock_color")
        val CLOCK_SCALE = floatPreferencesKey("clock_scale")
        val CLOCK_OFFSET_X = floatPreferencesKey("clock_offset_x")
        val CLOCK_OFFSET_Y = floatPreferencesKey("clock_offset_y")
        val CLOCK_OPACITY = floatPreferencesKey("clock_opacity")
        val CLOCK_BLUR = floatPreferencesKey("clock_blur")

        val ICON_SIZE = intPreferencesKey("icon_size")
        val ICON_LABEL_SIZE = intPreferencesKey("icon_label_size")
        val ICON_CORNER_RADIUS = intPreferencesKey("icon_corner_radius")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val SHOW_SHADOW = booleanPreferencesKey("show_shadow")
        val DESKTOP_LAYOUT_MODE = stringPreferencesKey("desktop_layout_mode") // grid, freeform
    }

    val settingsFlow: Flow<LauncherSettings> = context.dataStore.data
        .map { prefs ->
            LauncherSettings(
                desktopWallpaperUri = prefs[PreferencesKeys.DESKTOP_WALLPAPER_URI] ?: "",
                desktopWallpaperType = prefs[PreferencesKeys.DESKTOP_WALLPAPER_TYPE] ?: "preset",
                standbyWallpaperUri = prefs[PreferencesKeys.STANDBY_WALLPAPER_URI] ?: "",
                standbyWallpaperType = prefs[PreferencesKeys.STANDBY_WALLPAPER_TYPE] ?: "preset",
                wallpaperDim = prefs[PreferencesKeys.WALLPAPER_DIM] ?: 0.2f,
                wallpaperBlur = prefs[PreferencesKeys.WALLPAPER_BLUR] ?: 0f,
                clockFont = prefs[PreferencesKeys.CLOCK_FONT] ?: "Default",
                clockColor = prefs[PreferencesKeys.CLOCK_COLOR] ?: android.graphics.Color.WHITE,
                clockScale = prefs[PreferencesKeys.CLOCK_SCALE] ?: 1.0f,
                clockOffsetX = prefs[PreferencesKeys.CLOCK_OFFSET_X] ?: 0.5f,
                clockOffsetY = prefs[PreferencesKeys.CLOCK_OFFSET_Y] ?: 0.3f,
                clockOpacity = prefs[PreferencesKeys.CLOCK_OPACITY] ?: 1.0f,
                clockBlur = prefs[PreferencesKeys.CLOCK_BLUR] ?: 0f,
                iconSize = prefs[PreferencesKeys.ICON_SIZE] ?: 64,
                iconLabelSize = prefs[PreferencesKeys.ICON_LABEL_SIZE] ?: 12,
                iconCornerRadius = prefs[PreferencesKeys.ICON_CORNER_RADIUS] ?: 16,
                showLabels = prefs[PreferencesKeys.SHOW_LABELS] ?: true,
                showShadow = prefs[PreferencesKeys.SHOW_SHADOW] ?: true,
                desktopLayoutMode = prefs[PreferencesKeys.DESKTOP_LAYOUT_MODE] ?: "grid"
            )
        }

    suspend fun updateDesktopWallpaper(uri: String, type: String) {
        context.dataStore.edit {
            it[PreferencesKeys.DESKTOP_WALLPAPER_URI] = uri
            it[PreferencesKeys.DESKTOP_WALLPAPER_TYPE] = type
        }
    }

    suspend fun updateStandbyWallpaper(uri: String, type: String) {
        context.dataStore.edit {
            it[PreferencesKeys.STANDBY_WALLPAPER_URI] = uri
            it[PreferencesKeys.STANDBY_WALLPAPER_TYPE] = type
        }
    }

    suspend fun updateWallpaperDim(dim: Float) {
        context.dataStore.edit { it[PreferencesKeys.WALLPAPER_DIM] = dim }
    }

    suspend fun updateWallpaperBlur(blur: Float) {
        context.dataStore.edit { it[PreferencesKeys.WALLPAPER_BLUR] = blur }
    }

    suspend fun updateClockCustomization(font: String, color: Int, scale: Float, offsetX: Float, offsetY: Float, opacity: Float, blur: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.CLOCK_FONT] = font
            it[PreferencesKeys.CLOCK_COLOR] = color
            it[PreferencesKeys.CLOCK_SCALE] = scale
            it[PreferencesKeys.CLOCK_OFFSET_X] = offsetX
            it[PreferencesKeys.CLOCK_OFFSET_Y] = offsetY
            it[PreferencesKeys.CLOCK_OPACITY] = opacity
            it[PreferencesKeys.CLOCK_BLUR] = blur
        }
    }

    suspend fun updateIconSettings(size: Int, labelSize: Int, cornerRadius: Int, showLabels: Boolean, showShadow: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.ICON_SIZE] = size
            it[PreferencesKeys.ICON_LABEL_SIZE] = labelSize
            it[PreferencesKeys.ICON_CORNER_RADIUS] = cornerRadius
            it[PreferencesKeys.SHOW_LABELS] = showLabels
            it[PreferencesKeys.SHOW_SHADOW] = showShadow
        }
    }

    suspend fun updateDesktopLayoutMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.DESKTOP_LAYOUT_MODE] = mode }
    }
}

data class LauncherSettings(
    val desktopWallpaperUri: String,
    val desktopWallpaperType: String,
    val standbyWallpaperUri: String,
    val standbyWallpaperType: String,
    val wallpaperDim: Float,
    val wallpaperBlur: Float,
    val clockFont: String,
    val clockColor: Int,
    val clockScale: Float,
    val clockOffsetX: Float,
    val clockOffsetY: Float,
    val clockOpacity: Float,
    val clockBlur: Float,
    val iconSize: Int,
    val iconLabelSize: Int,
    val iconCornerRadius: Int,
    val showLabels: Boolean,
    val showShadow: Boolean,
    val desktopLayoutMode: String
)
