package com.example.dexlauncher.ui.settings

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.LauncherSettings
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.ui.standby.StandbyClockView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

sealed class SettingsPage {
    object Main : SettingsPage()
    object Standby : SettingsPage()
    object Desktop : SettingsPage()
    object Wallpaper : SettingsPage()
    object Taskbar : SettingsPage()
    object Icons : SettingsPage()
    object Typography : SettingsPage()
    object Animations : SettingsPage()
    object HiddenApps : SettingsPage()
    object ShizukuBeta : SettingsPage()
    object Permissions : SettingsPage()
    object About : SettingsPage()
}

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    settings: LauncherSettings,
    initialPage: SettingsPage = SettingsPage.Main,
    onDismiss: () -> Unit
) {
    var currentPage by remember { mutableStateOf(initialPage) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(750.dp)
                .height(680.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF181818),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentPage !is SettingsPage.Main) {
                            IconButton(onClick = { currentPage = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = when (currentPage) {
                                is SettingsPage.Main -> "Cài đặt"
                                is SettingsPage.Standby -> "Màn hình chờ (Standby)"
                                is SettingsPage.Desktop -> "Màn hình chính"
                                is SettingsPage.Wallpaper -> "Hình nền"
                                is SettingsPage.Taskbar -> "Thanh tác vụ"
                                is SettingsPage.Icons -> "Biểu tượng"
                                is SettingsPage.Typography -> "Phông chữ & Đồng hồ"
                                is SettingsPage.Animations -> "Hiệu ứng chuyển động"
                                is SettingsPage.HiddenApps -> "Ứng dụng đã ẩn"
                                is SettingsPage.ShizukuBeta -> "Shizuku (BETA)"
                                is SettingsPage.Permissions -> "Quyền & Tương thích"
                                is SettingsPage.About -> "Giới thiệu"
                            },
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentPage is SettingsPage.Main) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch { settingsRepository.resetToDefaults() }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                            ) {
                                Text("Khôi phục mặc định")
                            }
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                        ) {
                            Text("Xong", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Pages
                Box(modifier = Modifier.weight(1f)) {
                    when (currentPage) {
                        is SettingsPage.Main -> MainSettingsList(
                            settings = settings,
                            onNavigate = { currentPage = it }
                        )
                        is SettingsPage.Standby -> StandbySettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.Desktop -> DesktopSettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.Wallpaper -> WallpaperSettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.Taskbar -> TaskbarSettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.Icons -> IconsSettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.Typography -> TypographySettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.Animations -> AnimationsSettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.HiddenApps -> HiddenAppsSettingsPage(settings = settings, settingsRepository = settingsRepository)
                        is SettingsPage.ShizukuBeta -> ShizukuSettingsPage()
                        is SettingsPage.Permissions -> PermissionsSettingsPage()
                        is SettingsPage.About -> AboutSettingsPage()
                    }
                }
            }
        }
    }
}

@Composable
fun MainSettingsList(
    settings: LauncherSettings,
    onNavigate: (SettingsPage) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsCard(title = "Hiển thị & Giao diện") {
                SettingsItem(icon = Icons.Default.Lock, iconBg = Color(0xFFE91E63), title = "Màn hình chờ (Standby)", desc = "Đồng hồ, vị trí, hiệu ứng màn hình chờ") { onNavigate(SettingsPage.Standby) }
                SettingsItem(icon = Icons.Default.Home, iconBg = Color(0xFF4CAF50), title = "Màn hình chính", desc = "Bố cục lưới, kích thước icon, lề") { onNavigate(SettingsPage.Desktop) }
                SettingsItem(icon = Icons.Default.Wallpaper, iconBg = Color(0xFF2196F3), title = "Hình nền", desc = "Ảnh/Video màn hình chính và standby, độ mờ") { onNavigate(SettingsPage.Wallpaper) }
                SettingsItem(icon = Icons.Default.Star, iconBg = Color(0xFFFF9800), title = "Biểu tượng", desc = "Độ bo góc, nhãn icon, kích thước") { onNavigate(SettingsPage.Icons) }
            }
        }

        item {
            SettingsCard(title = "Hệ thống & Tác vụ") {
                SettingsItem(icon = Icons.Default.Menu, iconBg = Color(0xFF9C27B0), title = "Thanh tác vụ (Taskbar)", desc = "Tự động ẩn, ghim ứng dụng") { onNavigate(SettingsPage.Taskbar) }
                SettingsItem(icon = Icons.Default.Edit, iconBg = Color(0xFF00BCD4), title = "Phông chữ & Đồng hồ", desc = "Tùy chỉnh font .ttf, trọng lượng, màu sắc") { onNavigate(SettingsPage.Typography) }
                SettingsItem(icon = Icons.Default.PlayArrow, iconBg = Color(0xFF795548), title = "Hiệu ứng chuyển động", desc = "Tốc độ animation, fullscreen ngang") { onNavigate(SettingsPage.Animations) }
                SettingsItem(icon = Icons.Default.Delete, iconBg = Color(0xFF607D8B), title = "Ứng dụng đã ẩn", desc = "Quản lý các app ẩn trong app drawer") { onNavigate(SettingsPage.HiddenApps) }
            }
        }

        item {
            SettingsCard(title = "Nâng cao & Tương thích") {
                SettingsItem(icon = Icons.Default.Build, iconBg = Color(0xFFE65100), title = "Shizuku (BETA)", desc = "Kết nối Shizuku để chạy freeform cao cấp") { onNavigate(SettingsPage.ShizukuBeta) }
                SettingsItem(icon = Icons.Default.Settings, iconBg = Color(0xFF3F51B5), title = "Quyền & Tương thích", desc = "Usage stats, overlay, WRITE_SECURE_SETTINGS lệnh ADB") { onNavigate(SettingsPage.Permissions) }
                SettingsItem(icon = Icons.Default.Info, iconBg = Color(0xFF009688), title = "Giới thiệu", desc = "DeX Launcher phiên bản Pro v2.0") { onNavigate(SettingsPage.About) }
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF242424),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, color = Color(0xFF80CBC4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = desc, color = Color.Gray, fontSize = 13.sp)
        }

        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun StandbySettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    var clockScale by remember { mutableFloatStateOf(settings.clockScale) }
    var clockOpacity by remember { mutableFloatStateOf(settings.clockOpacity) }
    var clockBlur by remember { mutableFloatStateOf(settings.clockBlur) }
    var is24H by remember { mutableStateOf(settings.clockIs24H) }
    var showDate by remember { mutableStateOf(settings.clockShowDate) }
    var showSwipeUp by remember { mutableStateOf(settings.clockShowSwipeUp) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 16:9 Preview Frame
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                val previewSettings = settings.copy(
                    clockScale = clockScale,
                    clockOpacity = clockOpacity,
                    clockBlur = clockBlur,
                    clockIs24H = is24H,
                    clockShowDate = showDate,
                    clockShowSwipeUp = showSwipeUp
                )
                StandbyClockView(settings = previewSettings, onClick = {})
            }
        }

        item {
            SettingsCard(title = "Tùy chỉnh Đồng hồ") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Định dạng 24 giờ", color = Color.White)
                        Switch(checked = is24H, onCheckedChange = {
                            is24H = it
                            scope.launch {
                                settingsRepository.updateClockCustomization(settings.clockFont, settings.clockFontPath, settings.clockColor, clockScale, settings.clockOffsetX, settings.clockOffsetY, clockOpacity, clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, it, showDate, showSwipeUp, settings.clockAlignment)
                            }
                        })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hiển thị Ngày", color = Color.White)
                        Switch(checked = showDate, onCheckedChange = {
                            showDate = it
                            scope.launch {
                                settingsRepository.updateClockCustomization(settings.clockFont, settings.clockFontPath, settings.clockColor, clockScale, settings.clockOffsetX, settings.clockOffsetY, clockOpacity, clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, is24H, it, showSwipeUp, settings.clockAlignment)
                            }
                        })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hiển thị 'Vuốt lên để mở khóa'", color = Color.White)
                        Switch(checked = showSwipeUp, onCheckedChange = {
                            showSwipeUp = it
                            scope.launch {
                                settingsRepository.updateClockCustomization(settings.clockFont, settings.clockFontPath, settings.clockColor, clockScale, settings.clockOffsetX, settings.clockOffsetY, clockOpacity, clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, is24H, showDate, it, settings.clockAlignment)
                            }
                        })
                    }

                    Text("Kích thước đồng hồ (${String.format("%.1f", clockScale)}x)", color = Color.LightGray)
                    Slider(
                        value = clockScale,
                        onValueChange = {
                            clockScale = it
                            scope.launch {
                                settingsRepository.updateClockCustomization(settings.clockFont, settings.clockFontPath, settings.clockColor, it, settings.clockOffsetX, settings.clockOffsetY, clockOpacity, clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, is24H, showDate, showSwipeUp, settings.clockAlignment)
                            }
                        },
                        valueRange = 0.5f..2.0f
                    )

                    Text("Độ trong suốt (${(clockOpacity * 100).toInt()}%)", color = Color.LightGray)
                    Slider(
                        value = clockOpacity,
                        onValueChange = {
                            clockOpacity = it
                            scope.launch {
                                settingsRepository.updateClockCustomization(settings.clockFont, settings.clockFontPath, settings.clockColor, clockScale, settings.clockOffsetX, settings.clockOffsetY, it, clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, is24H, showDate, showSwipeUp, settings.clockAlignment)
                            }
                        },
                        valueRange = 0.2f..1.0f
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopSettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Bố cục Màn hình chính") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { scope.launch { settingsRepository.updateDesktopLayoutMode("grid") } },
                        colors = ButtonDefaults.buttonColors(containerColor = if (settings.desktopLayoutMode == "grid") Color(0xFF00897B) else Color.DarkGray)
                    ) {
                        Text("Lưới (Grid)")
                    }
                    Button(
                        onClick = { scope.launch { settingsRepository.updateDesktopLayoutMode("freeform") } },
                        colors = ButtonDefaults.buttonColors(containerColor = if (settings.desktopLayoutMode == "freeform") Color(0xFF00897B) else Color.DarkGray)
                    ) {
                        Text("Tự do (Freeform)")
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperSettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val desktopWallpaperLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val type = if (context.contentResolver.getType(it)?.startsWith("video") == true) "video" else "image"
                scope.launch { settingsRepository.updateDesktopWallpaper(it.toString(), type) }
            } catch (_: Exception) {}
        }
    }

    val standbyWallpaperLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val type = if (context.contentResolver.getType(it)?.startsWith("video") == true) "video" else "image"
                scope.launch { settingsRepository.updateStandbyWallpaper(it.toString(), type) }
            } catch (_: Exception) {}
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Hình nền Màn hình chính") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { desktopWallpaperLauncher.launch(arrayOf("image/*", "video/*")) }) {
                        Text("Chọn Ảnh / Video Desktop")
                    }
                    OutlinedButton(onClick = { scope.launch { settingsRepository.updateDesktopWallpaper("", "preset") } }) {
                        Text("Mặc định")
                    }
                }
            }
        }

        item {
            SettingsCard(title = "Hình nền Màn hình chờ") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { standbyWallpaperLauncher.launch(arrayOf("image/*", "video/*")) }) {
                        Text("Chọn Ảnh / Video Standby")
                    }
                    OutlinedButton(onClick = { scope.launch { settingsRepository.updateStandbyWallpaper("", "preset") } }) {
                        Text("Mặc định")
                    }
                }
            }
        }

        item {
            SettingsCard(title = "Hiệu ứng nền") {
                Text("Độ tối nền (${(settings.wallpaperDim * 100).toInt()}%)", color = Color.White)
                Slider(value = settings.wallpaperDim, onValueChange = { scope.launch { settingsRepository.updateWallpaperDim(it) } }, valueRange = 0f..0.6f)

                Text("Độ mờ Blur nền (${settings.wallpaperBlur.toInt()}dp)", color = Color.White)
                Slider(value = settings.wallpaperBlur, onValueChange = { scope.launch { settingsRepository.updateWallpaperBlur(it) } }, valueRange = 0f..25f)
            }
        }
    }
}

@Composable
fun TaskbarSettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Tác vụ & Taskbar") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tự động ẩn Thanh tác vụ", color = Color.White)
                    Switch(checked = settings.taskbarAutoHide, onCheckedChange = {
                        scope.launch { settingsRepository.updateTaskbarAutoHide(it) }
                    })
                }
            }
        }
    }
}

@Composable
fun IconsSettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Kích thước & Kiểu dáng Icon") {
                Text("Kích thước Icon (${settings.iconSize}dp)", color = Color.White)
                Slider(
                    value = settings.iconSize.toFloat(),
                    onValueChange = {
                        scope.launch { settingsRepository.updateIconSettings(it.toInt(), settings.iconLabelSize, settings.iconCornerRadius, settings.showLabels, settings.showShadow) }
                    },
                    valueRange = 40f..112f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hiển thị nhãn ứng dụng", color = Color.White)
                    Switch(checked = settings.showLabels, onCheckedChange = {
                        scope.launch { settingsRepository.updateIconSettings(settings.iconSize, settings.iconLabelSize, settings.iconCornerRadius, it, settings.showShadow) }
                    })
                }
            }
        }
    }
}

@Composable
fun TypographySettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fontName by remember { mutableStateOf(settings.clockFont) }
    var fontPath by remember { mutableStateOf(settings.clockFontPath) }

    val fontPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val fontsDir = File(context.filesDir, "fonts").apply { mkdirs() }
                val destFile = File(fontsDir, "custom_font_${System.currentTimeMillis()}.ttf")
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
                fontPath = destFile.absolutePath
                fontName = destFile.name
                scope.launch {
                    settingsRepository.updateClockCustomization(fontName, fontPath, settings.clockColor, settings.clockScale, settings.clockOffsetX, settings.clockOffsetY, settings.clockOpacity, settings.clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, settings.clockIs24H, settings.clockShowDate, settings.clockShowSwipeUp, settings.clockAlignment)
                }
            } catch (_: Exception) {}
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Phông chữ đồng hồ tùy chỉnh") {
                Text("Font hiện tại: $fontName", color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { fontPickerLauncher.launch(arrayOf("font/ttf", "font/otf", "*/*")) }) {
                        Text("Nhập font .ttf / .otf")
                    }
                    if (fontPath.isNotEmpty()) {
                        OutlinedButton(onClick = {
                            fontPath = ""
                            fontName = "Default"
                            scope.launch {
                                settingsRepository.updateClockCustomization(fontName, fontPath, settings.clockColor, settings.clockScale, settings.clockOffsetX, settings.clockOffsetY, settings.clockOpacity, settings.clockBlur, settings.clockWeight, settings.clockLetterSpacing, settings.clockShadow, settings.clockIs24H, settings.clockShowDate, settings.clockShowSwipeUp, settings.clockAlignment)
                            }
                        }) {
                            Text("Xóa font tùy chỉnh")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimationsSettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Hiệu ứng & Fullscreen") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fullscreen Ngang (Ẩn Status/Nav Bar)", color = Color.White)
                    Switch(checked = settings.fullscreenEnabled, onCheckedChange = {
                        scope.launch { settingsRepository.updateFullscreenEnabled(it) }
                    })
                }
            }
        }
    }
}

@Composable
fun HiddenAppsSettingsPage(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            SettingsCard(title = "Danh sách ứng dụng đã ẩn") {
                if (settings.hiddenApps.isEmpty()) {
                    Text("Không có ứng dụng nào bị ẩn.", color = Color.Gray)
                } else {
                    settings.hiddenApps.forEach { pkg ->
                        Text(pkg, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ShizukuSettingsPage() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Trạng thái Shizuku (BETA)") {
                Text("Shizuku cho phép khởi chạy ứng dụng dạng cửa sổ nổi freeform chuẩn xác mà không cần root.", color = Color.LightGray)
                Button(onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))
                        // context.startActivity(intent)
                    } catch (_: Exception) {}
                }) {
                    Text("Cài đặt / Kiểm tra Shizuku")
                }
            }
        }
    }
}

@Composable
fun PermissionsSettingsPage() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val adbCommand = "adb shell pm grant com.example.dexlauncher android.permission.WRITE_SECURE_SETTINGS"

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Quyền hệ thống & ADB") {
                Button(onClick = {
                    try { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) } catch (_: Exception) {}
                }) {
                    Text("Cấp quyền Usage Access")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Lệnh ADB cấp quyền bảo mật:", color = Color.LightGray)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(adbCommand, color = Color.Green, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Button(onClick = { clipboardManager.setText(AnnotatedString(adbCommand)) }) {
                            Text("Sao chép")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSettingsPage() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SettingsCard(title = "Thông tin phiên bản") {
                Text("DeX Launcher Pro", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Phiên bản: 2.0.0 (Android 10+ Optimized)", color = Color.Gray)
                Text("Giao diện Samsung DeX & One UI kết hợp hoàn hảo.", color = Color.LightGray)
            }
        }
    }
}
