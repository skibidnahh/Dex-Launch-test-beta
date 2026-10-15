package com.example.dexlauncher.ui.settings

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.dexlauncher.ui.theme.DeXDarkPalette

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    settingsRepository: SettingsRepository,
    shizukuManager: ShizukuManager,
    onDismissRequest: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val currentWallpaper by settingsRepository.currentWallpaper.collectAsState()
    val isOverlayEnabled by settingsRepository.isTaskbarOverlayEnabled.collectAsState()
    val shizukuStatus by shizukuManager.status.collectAsState()

    var hasOverlayPermission by remember { mutableStateOf(settingsRepository.hasOverlayPermission()) }
    var hasSecureSettings by remember { mutableStateOf(settingsRepository.hasWriteSecureSettings()) }
    var isFreeformActive by remember { mutableStateOf(settingsRepository.isFreeformGlobalEnabled()) }

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
                .padding(top = 36.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
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
                    // Section 1: Shizuku Connection (4 States) - BETA Mode
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
                                text = "Lưu ý: Chế độ Shizuku đang ở giai đoạn thử nghiệm (BETA) và có thể phát sinh lỗi tùy thuộc vào ROM/hãng máy. Launcher mặc định sử dụng đa nhiệm dạng tab độc lập mà không cần Shizuku.",
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
                                    text = "Cài đặt Shizuku để bật tính năng thay đổi kích thước cửa sổ và am start --windowingMode 5 chính xác.",
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
                                    text = "Mở app Shizuku và khởi động dịch vụ thông qua Ghép nối Wi-Fi (Wireless Debugging) hoặc qua máy tính (ADB).",
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
                                    text = "Trạng thái: Chưa cấp quyền Shizuku",
                                    color = Color(0xFFF97316)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cấp quyền cho DeX Launcher để cho phép chạy lệnh quản lý cửa sổ đa nhiệm dạng tự do.",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        (context as? Activity)?.let { act ->
                                            shizukuManager.requestPermission(act)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("Cấp quyền Shizuku", fontSize = 12.sp)
                                }
                            }
                            is ShizukuStatus.Connected -> {
                                StatusBadge(
                                    text = "Trạng thái: Đã kết nối Shizuku (UID: ${status.uid}, Root: ${status.isRoot})",
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Đã sẵn sàng điều khiển cửa sổ thông qua Shizuku UserService AIDL.",
                                    color = DeXDarkPalette.ActiveCyan,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Section 2: Permissions Status & ADB Commands
                    SettingsCard(title = "Trạng thái quyền hệ thống") {
                        // 1. Overlay Permission
                        PermissionRow(
                            title = "Hiển thị trên ứng dụng khác (SYSTEM_ALERT_WINDOW)",
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

                        // 2. WRITE_SECURE_SETTINGS
                        PermissionRow(
                            title = "Ghi thiết lập bảo mật (WRITE_SECURE_SETTINGS)",
                            isGranted = hasSecureSettings,
                            onActionClick = null
                        )

                        val adbCmd = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F1420))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Lệnh ADB cấp quyền một lần:",
                                    color = DeXDarkPalette.TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = adbCmd,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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

                        // 3. Battery Optimizations
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

                    // Section 3: Taskbar Overlay Switch
                    SettingsCard(title = "Thanh tác vụ đè (Taskbar Overlay)") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Luôn hiển thị thanh tác vụ DeX trên mọi ứng dụng",
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
                    }

                    // Section 4: Wallpaper Chooser
                    SettingsCard(title = "Hình nền Samsung DeX") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DeXWallpaperPreset.entries.forEach { preset ->
                                val isSelected = currentWallpaper == preset
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
                                            .size(height = 50.dp, width = 60.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(preset.midColor)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.title.take(8),
                                        color = if (isSelected) DeXDarkPalette.ActiveCyan else DeXDarkPalette.TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Section 5: Set as Default Home
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
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeXDarkPalette.CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeXDarkPalette.Border),
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
