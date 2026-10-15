package com.example.dexlauncher.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.provider.Settings
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dexlauncher.data.model.DeXWallpaperPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

private val Context.dataStore by preferencesDataStore(name = "dex_settings_prefs")

data class StandbySettings(
    val gridPosition: Int = 4, // 0..8 (0: TL, 1: TC, 2: TR, 3: ML, 4: Center, 5: MR, 6: BL, 7: BC, 8: BR)
    val customOffsetX: Float = 0.5f,
    val customOffsetY: Float = 0.45f,
    val clockSizeSp: Int = 78,
    val fontFamilyName: String = "Default",
    val fontWeightValue: Int = 300,
    val colorHex: Long = 0xFFFFFFFF,
    val is24Hour: Boolean = true,
    val showDate: Boolean = true,
    val showSwipeHint: Boolean = true,
    val textAlignment: String = "AUTO",
    val backgroundOpacity: Float = 1.0f,
    val backgroundBlurDp: Int = 0
)

data class WallpaperSettings(
    val type: String = "PRESET", // PRESET, IMAGE, VIDEO
    val presetId: String = "nebula_purple",
    val uriString: String = "",
    val dimOpacity: Float = 0f,
    val blurRadius: Float = 0f
)

data class DesktopAppearance(
    val iconSizeDp: Int = 54,
    val labelSizeSp: Int = 11,
    val gridColumns: Int = 0 // 0 = Auto
)

data class TaskbarAppearance(
    val heightDp: Int = 54,
    val position: String = "BOTTOM", // BOTTOM, LEFT, RIGHT
    val alignment: String = "START", // START, CENTER
    val transparency: Float = 0.85f,
    val cornerRadiusDp: Int = 0
)

class SettingsRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataStore = context.dataStore

    // Keys
    private val KEY_ALWAYS_LANDSCAPE = booleanPreferencesKey("always_landscape")
    private val KEY_HIDE_SYSTEM_BARS = booleanPreferencesKey("hide_system_bars")
    private val KEY_OVERLAY_TASKBAR = booleanPreferencesKey("taskbar_overlay_enabled")

    // Standby Keys
    private val KEY_STANDBY_GRID_POS = intPreferencesKey("standby_grid_pos")
    private val KEY_STANDBY_OFFSET_X = floatPreferencesKey("standby_offset_x")
    private val KEY_STANDBY_OFFSET_Y = floatPreferencesKey("standby_offset_y")
    private val KEY_STANDBY_CLOCK_SIZE = intPreferencesKey("standby_clock_size")
    private val KEY_STANDBY_FONT = stringPreferencesKey("standby_font")
    private val KEY_STANDBY_WEIGHT = intPreferencesKey("standby_weight")
    private val KEY_STANDBY_COLOR = longPreferencesKey("standby_color")
    private val KEY_STANDBY_24H = booleanPreferencesKey("standby_24h")
    private val KEY_STANDBY_SHOW_DATE = booleanPreferencesKey("standby_show_date")
    private val KEY_STANDBY_SHOW_HINT = booleanPreferencesKey("standby_show_hint")
    private val KEY_STANDBY_ALIGNMENT = stringPreferencesKey("standby_alignment")

    // Wallpaper Keys
    private val KEY_STANDBY_WP_TYPE = stringPreferencesKey("standby_wp_type")
    private val KEY_STANDBY_WP_PRESET = stringPreferencesKey("standby_wp_preset")
    private val KEY_STANDBY_WP_URI = stringPreferencesKey("standby_wp_uri")

    private val KEY_DESKTOP_WP_TYPE = stringPreferencesKey("desktop_wp_type")
    private val KEY_DESKTOP_WP_PRESET = stringPreferencesKey("desktop_wp_preset")
    private val KEY_DESKTOP_WP_URI = stringPreferencesKey("desktop_wp_uri")
    private val KEY_DESKTOP_WP_DIM = floatPreferencesKey("desktop_wp_dim")
    private val KEY_DESKTOP_WP_BLUR = floatPreferencesKey("desktop_wp_blur")

    // Desktop & Taskbar Appearance Keys
    private val KEY_DESKTOP_ICON_SIZE = intPreferencesKey("desktop_icon_size")
    private val KEY_DESKTOP_LABEL_SIZE = intPreferencesKey("desktop_label_size")
    private val KEY_DESKTOP_COLUMNS = intPreferencesKey("desktop_columns")

    private val KEY_TASKBAR_HEIGHT = intPreferencesKey("taskbar_height")
    private val KEY_TASKBAR_POSITION = stringPreferencesKey("taskbar_position")
    private val KEY_TASKBAR_ALIGNMENT = stringPreferencesKey("taskbar_alignment")
    private val KEY_TASKBAR_TRANSPARENCY = floatPreferencesKey("taskbar_transparency")
    private val KEY_TASKBAR_CORNER_RADIUS = intPreferencesKey("taskbar_corner_radius")

    // Motion & Theme Keys
    private val KEY_ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
    private val KEY_ANIMATION_SPEED = floatPreferencesKey("animation_speed")
    private val KEY_ACCENT_COLOR = longPreferencesKey("accent_color")
    private val KEY_APP_THEME = stringPreferencesKey("app_theme")
    private val KEY_WINDOW_WIDTH_PCT = intPreferencesKey("window_width_pct")
    private val KEY_WINDOW_HEIGHT_PCT = intPreferencesKey("window_height_pct")

    // StateFlows
    private val _alwaysLandscape = MutableStateFlow(true)
    val alwaysLandscape: StateFlow<Boolean> = _alwaysLandscape.asStateFlow()

    private val _hideSystemBars = MutableStateFlow(true)
    val hideSystemBars: StateFlow<Boolean> = _hideSystemBars.asStateFlow()

    private val _isTaskbarOverlayEnabled = MutableStateFlow(false)
    val isTaskbarOverlayEnabled: StateFlow<Boolean> = _isTaskbarOverlayEnabled.asStateFlow()

    private val _standbySettings = MutableStateFlow(StandbySettings())
    val standbySettings: StateFlow<StandbySettings> = _standbySettings.asStateFlow()

    private val _standbyWallpaper = MutableStateFlow(WallpaperSettings(type = "PRESET", presetId = "nebula_purple"))
    val standbyWallpaper: StateFlow<WallpaperSettings> = _standbyWallpaper.asStateFlow()

    private val _desktopWallpaper = MutableStateFlow(WallpaperSettings(type = "PRESET", presetId = "nebula_purple"))
    val desktopWallpaper: StateFlow<WallpaperSettings> = _desktopWallpaper.asStateFlow()

    private val _currentWallpaper = MutableStateFlow(DeXWallpaperPreset.NEBULA_PURPLE)
    val currentWallpaper: StateFlow<DeXWallpaperPreset> = _currentWallpaper.asStateFlow()

    private val _desktopAppearance = MutableStateFlow(DesktopAppearance())
    val desktopAppearance: StateFlow<DesktopAppearance> = _desktopAppearance.asStateFlow()

    private val _taskbarAppearance = MutableStateFlow(TaskbarAppearance())
    val taskbarAppearance: StateFlow<TaskbarAppearance> = _taskbarAppearance.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _animationSpeed = MutableStateFlow(1.0f)
    val animationSpeed: StateFlow<Float> = _animationSpeed.asStateFlow()

    private val _accentColor = MutableStateFlow(0xFF2B66FF)
    val accentColor: StateFlow<Long> = _accentColor.asStateFlow()

    private val _appTheme = MutableStateFlow("DARK")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _windowWidthPercent = MutableStateFlow(72)
    val windowWidthPercent: StateFlow<Int> = _windowWidthPercent.asStateFlow()

    private val _windowHeightPercent = MutableStateFlow(70)
    val windowHeightPercent: StateFlow<Int> = _windowHeightPercent.asStateFlow()

    private val cascadeStep = AtomicInteger(0)

    init {
        scope.launch {
            loadInitialSettings()
        }
    }

    private suspend fun loadInitialSettings() {
        val prefs = dataStore.data.first()

        _alwaysLandscape.value = prefs[KEY_ALWAYS_LANDSCAPE] ?: true
        _hideSystemBars.value = prefs[KEY_HIDE_SYSTEM_BARS] ?: true
        _isTaskbarOverlayEnabled.value = prefs[KEY_OVERLAY_TASKBAR] ?: false

        _standbySettings.value = StandbySettings(
            gridPosition = prefs[KEY_STANDBY_GRID_POS] ?: 4,
            customOffsetX = prefs[KEY_STANDBY_OFFSET_X] ?: 0.5f,
            customOffsetY = prefs[KEY_STANDBY_OFFSET_Y] ?: 0.45f,
            clockSizeSp = prefs[KEY_STANDBY_CLOCK_SIZE] ?: 78,
            fontFamilyName = prefs[KEY_STANDBY_FONT] ?: "Default",
            fontWeightValue = prefs[KEY_STANDBY_WEIGHT] ?: 300,
            colorHex = prefs[KEY_STANDBY_COLOR] ?: 0xFFFFFFFF,
            is24Hour = prefs[KEY_STANDBY_24H] ?: true,
            showDate = prefs[KEY_STANDBY_SHOW_DATE] ?: true,
            showSwipeHint = prefs[KEY_STANDBY_SHOW_HINT] ?: true,
            textAlignment = prefs[KEY_STANDBY_ALIGNMENT] ?: "AUTO"
        )

        _standbyWallpaper.value = WallpaperSettings(
            type = prefs[KEY_STANDBY_WP_TYPE] ?: "PRESET",
            presetId = prefs[KEY_STANDBY_WP_PRESET] ?: "nebula_purple",
            uriString = prefs[KEY_STANDBY_WP_URI] ?: ""
        )

        _desktopWallpaper.value = WallpaperSettings(
            type = prefs[KEY_DESKTOP_WP_TYPE] ?: "PRESET",
            presetId = prefs[KEY_DESKTOP_WP_PRESET] ?: "nebula_purple",
            uriString = prefs[KEY_DESKTOP_WP_URI] ?: "",
            dimOpacity = prefs[KEY_DESKTOP_WP_DIM] ?: 0f,
            blurRadius = prefs[KEY_DESKTOP_WP_BLUR] ?: 0f
        )
        _currentWallpaper.value = DeXWallpaperPreset.entries.find { it.id == _desktopWallpaper.value.presetId } ?: DeXWallpaperPreset.NEBULA_PURPLE

        _desktopAppearance.value = DesktopAppearance(
            iconSizeDp = prefs[KEY_DESKTOP_ICON_SIZE] ?: 54,
            labelSizeSp = prefs[KEY_DESKTOP_LABEL_SIZE] ?: 11,
            gridColumns = prefs[KEY_DESKTOP_COLUMNS] ?: 0
        )

        _taskbarAppearance.value = TaskbarAppearance(
            heightDp = prefs[KEY_TASKBAR_HEIGHT] ?: 54,
            position = prefs[KEY_TASKBAR_POSITION] ?: "BOTTOM",
            alignment = prefs[KEY_TASKBAR_ALIGNMENT] ?: "START",
            transparency = prefs[KEY_TASKBAR_TRANSPARENCY] ?: 0.85f,
            cornerRadiusDp = prefs[KEY_TASKBAR_CORNER_RADIUS] ?: 0
        )

        _animationsEnabled.value = prefs[KEY_ANIMATIONS_ENABLED] ?: true
        _animationSpeed.value = prefs[KEY_ANIMATION_SPEED] ?: 1.0f
        _accentColor.value = prefs[KEY_ACCENT_COLOR] ?: 0xFF2B66FF
        _appTheme.value = prefs[KEY_APP_THEME] ?: "DARK"
        _windowWidthPercent.value = prefs[KEY_WINDOW_WIDTH_PCT] ?: 72
        _windowHeightPercent.value = prefs[KEY_WINDOW_HEIGHT_PCT] ?: 70
    }

    // Setters that persist immediately
    fun setAlwaysLandscape(value: Boolean) {
        _alwaysLandscape.value = value
        scope.launch { dataStore.edit { it[KEY_ALWAYS_LANDSCAPE] = value } }
    }

    fun setHideSystemBars(value: Boolean) {
        _hideSystemBars.value = value
        scope.launch { dataStore.edit { it[KEY_HIDE_SYSTEM_BARS] = value } }
    }

    fun setTaskbarOverlayEnabled(value: Boolean) {
        _isTaskbarOverlayEnabled.value = value
        scope.launch { dataStore.edit { it[KEY_OVERLAY_TASKBAR] = value } }
    }

    fun updateStandbySettings(newSettings: StandbySettings) {
        _standbySettings.value = newSettings
        scope.launch {
            dataStore.edit {
                it[KEY_STANDBY_GRID_POS] = newSettings.gridPosition
                it[KEY_STANDBY_OFFSET_X] = newSettings.customOffsetX
                it[KEY_STANDBY_OFFSET_Y] = newSettings.customOffsetY
                it[KEY_STANDBY_CLOCK_SIZE] = newSettings.clockSizeSp
                it[KEY_STANDBY_FONT] = newSettings.fontFamilyName
                it[KEY_STANDBY_WEIGHT] = newSettings.fontWeightValue
                it[KEY_STANDBY_COLOR] = newSettings.colorHex
                it[KEY_STANDBY_24H] = newSettings.is24Hour
                it[KEY_STANDBY_SHOW_DATE] = newSettings.showDate
                it[KEY_STANDBY_SHOW_HINT] = newSettings.showSwipeHint
                it[KEY_STANDBY_ALIGNMENT] = newSettings.textAlignment
            }
        }
    }

    fun updateStandbyWallpaper(newWp: WallpaperSettings) {
        _standbyWallpaper.value = newWp
        scope.launch {
            dataStore.edit {
                it[KEY_STANDBY_WP_TYPE] = newWp.type
                it[KEY_STANDBY_WP_PRESET] = newWp.presetId
                it[KEY_STANDBY_WP_URI] = newWp.uriString
            }
        }
    }

    fun updateDesktopWallpaper(newWp: WallpaperSettings) {
        _desktopWallpaper.value = newWp
        _currentWallpaper.value = DeXWallpaperPreset.entries.find { it.id == newWp.presetId } ?: DeXWallpaperPreset.NEBULA_PURPLE
        scope.launch {
            dataStore.edit {
                it[KEY_DESKTOP_WP_TYPE] = newWp.type
                it[KEY_DESKTOP_WP_PRESET] = newWp.presetId
                it[KEY_DESKTOP_WP_URI] = newWp.uriString
                it[KEY_DESKTOP_WP_DIM] = newWp.dimOpacity
                it[KEY_DESKTOP_WP_BLUR] = newWp.blurRadius
            }
        }
    }

    fun setWallpaper(preset: DeXWallpaperPreset) {
        _currentWallpaper.value = preset
        updateDesktopWallpaper(_desktopWallpaper.value.copy(type = "PRESET", presetId = preset.id))
    }

    fun updateDesktopAppearance(newAppearance: DesktopAppearance) {
        _desktopAppearance.value = newAppearance
        scope.launch {
            dataStore.edit {
                it[KEY_DESKTOP_ICON_SIZE] = newAppearance.iconSizeDp
                it[KEY_DESKTOP_LABEL_SIZE] = newAppearance.labelSizeSp
                it[KEY_DESKTOP_COLUMNS] = newAppearance.gridColumns
            }
        }
    }

    fun updateTaskbarAppearance(newAppearance: TaskbarAppearance) {
        _taskbarAppearance.value = newAppearance
        scope.launch {
            dataStore.edit {
                it[KEY_TASKBAR_HEIGHT] = newAppearance.heightDp
                it[KEY_TASKBAR_POSITION] = newAppearance.position
                it[KEY_TASKBAR_ALIGNMENT] = newAppearance.alignment
                it[KEY_TASKBAR_TRANSPARENCY] = newAppearance.transparency
                it[KEY_TASKBAR_CORNER_RADIUS] = newAppearance.cornerRadiusDp
            }
        }
    }

    fun setAnimationsEnabled(value: Boolean) {
        _animationsEnabled.value = value
        scope.launch { dataStore.edit { it[KEY_ANIMATIONS_ENABLED] = value } }
    }

    fun setAnimationSpeed(value: Float) {
        _animationSpeed.value = value
        scope.launch { dataStore.edit { it[KEY_ANIMATION_SPEED] = value } }
    }

    fun setAccentColor(value: Long) {
        _accentColor.value = value
        scope.launch { dataStore.edit { it[KEY_ACCENT_COLOR] = value } }
    }

    fun setAppTheme(value: String) {
        _appTheme.value = value
        scope.launch { dataStore.edit { it[KEY_APP_THEME] = value } }
    }

    fun setWindowSizes(widthPct: Int, heightPct: Int) {
        _windowWidthPercent.value = widthPct
        _windowHeightPercent.value = heightPct
        scope.launch {
            dataStore.edit {
                it[KEY_WINDOW_WIDTH_PCT] = widthPct
                it[KEY_WINDOW_HEIGHT_PCT] = heightPct
            }
        }
    }

    fun resetToDefaults() {
        scope.launch {
            dataStore.edit { it.clear() }
            loadInitialSettings()
        }
    }

    fun calculateNextWindowBounds(screenWidth: Int, screenHeight: Int): Rect {
        val widthPct = _windowWidthPercent.value / 100.0
        val heightPct = _windowHeightPercent.value / 100.0

        val windowWidth = (screenWidth * widthPct).toInt().coerceAtLeast(480)
        val windowHeight = (screenHeight * heightPct).toInt().coerceAtLeast(400)

        val taskbarH = _taskbarAppearance.value.heightDp
        val maxOffsetX = (screenWidth - windowWidth).coerceAtLeast(0)
        val maxOffsetY = (screenHeight - windowHeight - taskbarH).coerceAtLeast(0)

        val step = cascadeStep.getAndIncrement() % 6
        val offsetX = (step * 50).coerceAtMost(maxOffsetX)
        val offsetY = (step * 40 + 20).coerceAtMost(maxOffsetY)

        return Rect(offsetX, offsetY, offsetX + windowWidth, offsetY + windowHeight)
    }

    fun hasWriteSecureSettings(): Boolean {
        return context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") ==
                PackageManager.PERMISSION_GRANTED
    }

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
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
