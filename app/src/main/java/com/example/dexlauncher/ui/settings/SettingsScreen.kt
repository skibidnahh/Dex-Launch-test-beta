package com.example.dexlauncher.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dexlauncher.R
import com.example.dexlauncher.data.model.DeXWallpaperPreset
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.shizuku.ShizukuManager
import com.example.dexlauncher.shizuku.ShizukuStatus
import com.example.dexlauncher.ui.standby.StandbyPreviewView
import com.example.dexlauncher.ui.standby.StandbyCustomizeSimulatorDialog
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    isOpen: Boolean,
    settingsRepository: SettingsRepository,
    shizukuManager: ShizukuManager,
    onDismissRequest: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val alwaysLandscape by settingsRepository.alwaysLandscape.collectAsState()
    val hideSystemBars by settingsRepository.hideSystemBars.collectAsState()
    val isOverlayEnabled by settingsRepository.isTaskbarOverlayEnabled.collectAsState()
    val standbySettings by settingsRepository.standbySettings.collectAsState()
    val desktopWallpaper by settingsRepository.desktopWallpaper.collectAsState()
    val standbyWallpaper by settingsRepository.standbyWallpaper.collectAsState()
    val taskbarAppearance by settingsRepository.taskbarAppearance.collectAsState()
    val currentWallpaperPreset by settingsRepository.currentWallpaper.collectAsState()
    val shizukuStatus by shizukuManager.status.collectAsState()

    var hasOverlayPermission by remember { mutableStateOf(settingsRepository.hasOverlayPermission()) }
    var hasSecureSettings by remember { mutableStateOf(settingsRepository.hasWriteSecureSettings()) }
    var isFreeformActive by remember { mutableStateOf(settingsRepository.isFreeformGlobalEnabled()) }
    var showStandbySimulator by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 36.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DeXDarkPalette.SurfaceFloating)
                    .border(1.dp, DeXDarkPalette.Border, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.settings),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Samsung DeX Simulation & Windowing Engine",
                            color = DeXDarkPalette.TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: Display & Fullscreen Orientation
                    SettingsCard(title = "Chế độ hiển thị & Toàn màn hình") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Luôn chạy màn hình ngang (Landscape)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Cố định hướng ngang như màn hình Samsung DeX chuẩn",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = alwaysLandscape,
                                onCheckedChange = { settingsRepository.setAlwaysLandscape(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeXDarkPalette.PrimaryBlue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ẩn thanh hệ thống (Status & Navigation bars)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tràn viền tối đa, chỉ hiện tạm thời khi vuốt từ mép",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = hideSystemBars,
                                onCheckedChange = { settingsRepository.setHideSystemBars(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeXDarkPalette.PrimaryBlue
                                )
                            )
                        }
                    }

                    // Section 2: Standby Screen Customization
                    SettingsCard(title = "Màn hình chờ (Standby) tùy chỉnh") {
                        Text(
                            text = "Xem trước & chọn 1 trong 9 vị trí đồng hồ (chạm vào các nút tròn để đổi vị trí):",
                            color = DeXDarkPalette.TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Preview Box with 9 grid positions
                        StandbyPreviewView(
                            standbySettings = standbySettings,
                            wallpaperSettings = standbyWallpaper,
                            onPositionChanged = { newPos, offsetX, offsetY ->
                                settingsRepository.updateStandbySettings(
                                    standbySettings.copy(
                                        gridPosition = newPos,
                                        customOffsetX = offsetX,
                                        customOffsetY = offsetY
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showStandbySimulator = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue)
                        ) {
                            Text("Mở Giả lập Màn hình chờ (Interactive Simulator)", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clock Size Slider
                        Text(
                            text = "Kích cỡ đồng hồ: ${standbySettings.clockSizeSp} sp (40sp - 160sp)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = standbySettings.clockSizeSp.toFloat(),
                            onValueChange = { size ->
                                settingsRepository.updateStandbySettings(standbySettings.copy(clockSizeSp = size.roundToInt()))
                            },
                            valueRange = 40f..160f,
                            colors = SliderDefaults.colors(
                                thumbColor = DeXDarkPalette.PrimaryBlue,
                                activeTrackColor = DeXDarkPalette.ActiveCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Font Family Selector
                        Text(
                            text = "Phông chữ đồng hồ:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val fonts = listOf("Default", "Sans-serif", "Serif", "Monospace", "Cursive")
                            fonts.forEach { fontName ->
                                val isSelected = standbySettings.fontFamilyName == fontName
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        settingsRepository.updateStandbySettings(standbySettings.copy(fontFamilyName = fontName))
                                    },
                                    label = { Text(fontName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeXDarkPalette.PrimaryBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = DeXDarkPalette.SurfaceFloating,
                                        labelColor = DeXDarkPalette.TextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Font Weight Slider
                        Text(
                            text = "Độ đậm phông chữ: ${standbySettings.fontWeightValue} (100 - 900)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = standbySettings.fontWeightValue.toFloat(),
                            onValueChange = { weight ->
                                val step = (weight / 100).roundToInt() * 100
                                settingsRepository.updateStandbySettings(standbySettings.copy(fontWeightValue = step.coerceIn(100, 900)))
                            },
                            valueRange = 100f..900f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = DeXDarkPalette.PrimaryBlue,
                                activeTrackColor = DeXDarkPalette.ActiveCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color Palette for Clock
                        Text(
                            text = "Màu sắc đồng hồ:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val clockColors = listOf(
                            0xFFFFFFFF to "Trắng tinh",
                            0xCCFFFFFF to "Kính mờ",
                            0xFF38BDF8 to "Xanh Cyan",
                            0xFFA855F7 to "Tím Nebula",
                            0xFFFACC15 to "Vàng Neon",
                            0xFFFB923C to "Cam Hoàng hôn",
                            0xFF34D399 to "Xanh Bạc hà",
                            0xFFF472B6 to "Hồng Pastel"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            clockColors.forEach { (colorLong, name) ->
                                val isSelected = standbySettings.colorHex == colorLong
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DeXDarkPalette.PrimaryBlue.copy(alpha = 0.3f) else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) DeXDarkPalette.ActiveCyan else DeXDarkPalette.Border,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            settingsRepository.updateStandbySettings(standbySettings.copy(colorHex = colorLong))
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorLong))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else DeXDarkPalette.TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Extra Toggles (24h, date, swipe hint)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Định dạng 24 giờ", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = standbySettings.is24Hour,
                                onCheckedChange = {
                                    settingsRepository.updateStandbySettings(standbySettings.copy(is24Hour = it))
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = DeXDarkPalette.PrimaryBlue)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hiển thị ngày tháng", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = standbySettings.showDate,
                                onCheckedChange = {
                                    settingsRepository.updateStandbySettings(standbySettings.copy(showDate = it))
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = DeXDarkPalette.PrimaryBlue)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dòng gợi ý 'Vuốt lên để mở'", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = standbySettings.showSwipeHint,
                                onCheckedChange = {
                                    settingsRepository.updateStandbySettings(standbySettings.copy(showSwipeHint = it))
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = DeXDarkPalette.PrimaryBlue)
                            )
                        }
                    }

                    // Section 3: Wallpaper Presets & Adjustments
                    SettingsCard(title = "Hình nền Samsung DeX & Hiệu ứng") {
                        Text(
                            text = "Chọn giao diện chủ đề DeX:",
                            color = DeXDarkPalette.TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DeXWallpaperPreset.entries.forEach { preset ->
                                val isSelected = currentWallpaperPreset == preset
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) DeXDarkPalette.ActiveCyan else DeXDarkPalette.Border,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { settingsRepository.setWallpaper(preset) }
                                        .padding(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(height = 46.dp, width = 60.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(preset.midColor)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.title.take(9),
                                        color = if (isSelected) DeXDarkPalette.ActiveCyan else DeXDarkPalette.TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dim Opacity Slider
                        Text(
                            text = "Độ tối hình nền: ${(desktopWallpaper.dimOpacity * 100).roundToInt()}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = desktopWallpaper.dimOpacity,
                            onValueChange = { dim ->
                                settingsRepository.updateDesktopWallpaper(desktopWallpaper.copy(dimOpacity = dim))
                            },
                            valueRange = 0f..0.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = DeXDarkPalette.PrimaryBlue,
                                activeTrackColor = DeXDarkPalette.ActiveCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Blur Radius Slider
                        Text(
                            text = "Độ mờ làm mịn nền: ${desktopWallpaper.blurRadius.roundToInt()} dp",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = desktopWallpaper.blurRadius,
                            onValueChange = { blur ->
                                settingsRepository.updateDesktopWallpaper(desktopWallpaper.copy(blurRadius = blur))
                            },
                            valueRange = 0f..25f,
                            colors = SliderDefaults.colors(
                                thumbColor = DeXDarkPalette.PrimaryBlue,
                                activeTrackColor = DeXDarkPalette.ActiveCyan
                            )
                        )
                    }

                    // Section 4: Taskbar Customization
                    SettingsCard(title = "Thanh tác vụ (Taskbar)") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Luôn hiển thị thanh tác vụ DeX trên mọi ứng dụng (Overlay)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Yêu cầu quyền hiển thị trên ứng dụng khác",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isOverlayEnabled,
                                onCheckedChange = { checked ->
                                    if (checked && !settingsRepository.hasOverlayPermission()) {
                                        Toast.makeText(context, "Vui lòng cấp quyền Overlay trước!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } else {
                                        settingsRepository.setTaskbarOverlayEnabled(checked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeXDarkPalette.PrimaryBlue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Chiều cao Taskbar: ${taskbarAppearance.heightDp} dp (44dp - 68dp)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = taskbarAppearance.heightDp.toFloat(),
                            onValueChange = { h ->
                                settingsRepository.updateTaskbarAppearance(taskbarAppearance.copy(heightDp = h.roundToInt()))
                            },
                            valueRange = 44f..68f,
                            colors = SliderDefaults.colors(
                                thumbColor = DeXDarkPalette.PrimaryBlue,
                                activeTrackColor = DeXDarkPalette.ActiveCyan
                            )
                        )
                    }

                    // Section 5: Shizuku Connection (4 States) - BETA Mode
                    SettingsCard(title = "Kết nối Shizuku [BETA - Thử nghiệm]") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF854D0E).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFFEAB308).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Lưu ý: Chế độ Shizuku đang ở giai đoạn thử nghiệm (BETA). Launcher mở app đa nhiệm cửa sổ chuẩn qua ActivityOptions.setLaunchBounds tự động kể cả khi không có Shizuku.",
                                color = Color(0xFFFDE047),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        when (val status = shizukuStatus) {
                            is ShizukuStatus.NotInstalled -> {
                                StatusBadge(
                                    text = "Trạng thái: Chưa cài đặt Shizuku",
                                    color = Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cài đặt Shizuku để bật tính năng thay đổi kích thước cửa sổ và shell đa nhiệm mở rộng.",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { shizukuManager.openShizukuApp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue)
                                ) {
                                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cài đặt / Tải Shizuku", fontSize = 12.sp)
                                }
                            }
                            is ShizukuStatus.NotRunning -> {
                                StatusBadge(
                                    text = "Trạng thái: Shizuku chưa chạy",
                                    color = Color(0xFFF59E0B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mở app Shizuku và khởi động dịch vụ thông qua Ghép nối Wi-Fi (Wireless Debugging) hoặc qua ADB máy tính.",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { shizukuManager.openShizukuApp() },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue)
                                    ) {
                                        Text("Mở app Shizuku", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { shizukuManager.checkStatus() }
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kiểm tra lại", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                            is ShizukuStatus.PermissionNeeded -> {
                                StatusBadge(
                                    text = "Trạng thái: Cần cấp quyền Shizuku",
                                    color = Color(0xFF3B82F6)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Shizuku đang chạy. Bấm nút dưới để yêu cầu cấp quyền cho DeX Launcher.",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (context is android.app.Activity) {
                                            shizukuManager.requestPermission(context)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue)
                                ) {
                                    Text("Yêu cầu quyền Shizuku", fontSize = 12.sp)
                                }
                            }
                            is ShizukuStatus.Connected -> {
                                StatusBadge(
                                    text = "Trạng thái: Đã kết nối thành công (UID: ${status.uid}, Root: ${status.isRoot})",
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Dịch vụ Shizuku UserService đang hoạt động. Có thể điều khiển cửa sổ đa nhiệm tự do.",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Section 6: Android System Permissions & Settings
                    SettingsCard(title = "Quyền hệ thống & Bật Freeform") {
                        PermissionRow(
                            title = "Quyền hiển thị trên ứng dụng khác (SYSTEM_ALERT_WINDOW)",
                            isGranted = hasOverlayPermission,
                            onActionClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(intent)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        PermissionRow(
                            title = "Quyền ghi cài đặt hệ thống (WRITE_SECURE_SETTINGS)",
                            isGranted = hasSecureSettings,
                            onActionClick = null
                        )

                        if (!hasSecureSettings) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val adbCmd = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = adbCmd,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCmd))
                                        Toast.makeText(context, "Đã sao chép lệnh ADB!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sao chép lệnh", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Auto Enable Freeform Button
                        Button(
                            onClick = {
                                val success = settingsRepository.tryEnableFreeformViaSecureSettings()
                                if (success) {
                                    isFreeformActive = true
                                    Toast.makeText(context, "Đã kích hoạt Freeform thành công!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Cần cấp quyền WRITE_SECURE_SETTINGS qua ADB trước!", Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = hasSecureSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tự động bật Freeform & Force Resizable", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Battery Optimizations
                        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                        val isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        PermissionRow(
                            title = "Bỏ tối ưu hóa pin (Giữ Standby chạy ổn định)",
                            isGranted = isIgnoringBattery,
                            onActionClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }

                    // Section 7: Set as Default Home
                    SettingsCard(title = "Màn hình chính mặc định") {
                        Text(
                            text = "Đặt DeX Launcher làm ứng dụng Home mặc định để khởi động giao diện máy tính mỗi khi mở máy.",
                            color = DeXDarkPalette.TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đặt làm Home mặc định", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    StandbyCustomizeSimulatorDialog(
        isOpen = showStandbySimulator,
        standbySettings = standbySettings,
        wallpaperSettings = standbyWallpaper,
        onSettingsChanged = { settingsRepository.updateStandbySettings(it) },
        onDismiss = { showStandbySimulator = false }
    )
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeXDarkPalette.CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DeXDarkPalette.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onActionClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = DeXDarkPalette.TextPrimary,
                fontSize = 12.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đã cấp",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Not granted",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Chưa cấp",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                if (onActionClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onActionClick,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Cấp", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
