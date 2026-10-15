package com.example.dexlauncher.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.LauncherSettings
import com.example.dexlauncher.data.repository.SettingsRepository
import com.example.dexlauncher.ui.standby.StandbyCustomizeSimulatorDialog
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    settings: LauncherSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showStandbySimulator by remember { mutableStateOf(false) }

    val desktopWallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val type = if (context.contentResolver.getType(uri)?.startsWith("video") == true) "video" else "image"
                scope.launch {
                    settingsRepository.updateDesktopWallpaper(uri.toString(), type)
                }
            } catch (_: Exception) {}
        }
    }

    val standbyWallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val type = if (context.contentResolver.getType(uri)?.startsWith("video") == true) "video" else "image"
                scope.launch {
                    settingsRepository.updateStandbyWallpaper(uri.toString(), type)
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(700.dp)
                .height(700.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF222222),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Cài đặt DeX Launcher", color = Color.White, fontSize = 22.sp)
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Xong / Lưu", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Wallpaper Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Hình nền Màn hình chính (Desktop)", color = Color(0xFF80CBC4), fontSize = 16.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { desktopWallpaperLauncher.launch(arrayOf("image/*", "video/*")) }) {
                                    Text("Chọn Ảnh/Video Desktop")
                                }
                                OutlinedButton(onClick = {
                                    scope.launch { settingsRepository.updateDesktopWallpaper("", "preset") }
                                }) {
                                    Text("Mặc định")
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Hình nền Màn hình chờ (Standby)", color = Color(0xFF80CBC4), fontSize = 16.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { standbyWallpaperLauncher.launch(arrayOf("image/*", "video/*")) }) {
                                    Text("Chọn Ảnh/Video Standby")
                                }
                                OutlinedButton(onClick = {
                                    scope.launch { settingsRepository.updateStandbyWallpaper("", "preset") }
                                }) {
                                    Text("Mặc định")
                                }
                            }
                        }
                    }

                    // Wallpaper Dim & Blur
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Độ tối nền (${(settings.wallpaperDim * 100).toInt()}%)", color = Color.White, fontSize = 14.sp)
                            Slider(
                                value = settings.wallpaperDim,
                                onValueChange = { scope.launch { settingsRepository.updateWallpaperDim(it) } },
                                valueRange = 0f..0.6f
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Độ mờ Blur nền (${settings.wallpaperBlur.toInt()}dp)", color = Color.White, fontSize = 14.sp)
                            Slider(
                                value = settings.wallpaperBlur,
                                onValueChange = { scope.launch { settingsRepository.updateWallpaperBlur(it) } },
                                valueRange = 0f..25f
                            )
                        }
                    }

                    // 2. Icon & Desktop Grid Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Kích thước Icon (${settings.iconSize}dp)", color = Color.White, fontSize = 14.sp)
                            Slider(
                                value = settings.iconSize.toFloat(),
                                onValueChange = {
                                    scope.launch {
                                        settingsRepository.updateIconSettings(
                                            it.toInt(),
                                            settings.iconLabelSize,
                                            settings.iconCornerRadius,
                                            settings.showLabels,
                                            settings.showShadow
                                        )
                                    }
                                },
                                valueRange = 40f..112f
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Bố cục Desktop", color = Color(0xFF80CBC4), fontSize = 16.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { scope.launch { settingsRepository.updateDesktopLayoutMode("grid") } },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (settings.desktopLayoutMode == "grid") Color(0xFF00897B) else Color.DarkGray
                                    )
                                ) {
                                    Text("Lưới (Grid)")
                                }
                                Button(
                                    onClick = { scope.launch { settingsRepository.updateDesktopLayoutMode("freeform") } },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (settings.desktopLayoutMode == "freeform") Color(0xFF00897B) else Color.DarkGray
                                    )
                                ) {
                                    Text("Tự do (Freeform)")
                                }
                            }
                        }
                    }

                    // 3. Standby & Clock Customization Buttons in Settings (as requested)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Tùy chỉnh Màn hình chờ & Đồng hồ", color = Color(0xFF80CBC4), fontSize = 16.sp)
                            Button(
                                onClick = { showStandbySimulator = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                            ) {
                                Text("Mở bảng giả lập & Tùy chỉnh Đồng hồ Standby")
                            }
                        }
                    }

                    // 4. System Permissions Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Quyền hệ thống & Công cụ", color = Color(0xFF80CBC4), fontSize = 16.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                    } catch (_: Exception) {}
                                }, modifier = Modifier.weight(1f)) {
                                    Text("Quyền Usage Stats", fontSize = 12.sp)
                                }
                                Button(onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    } catch (_: Exception) {}
                                }, modifier = Modifier.weight(1f)) {
                                    Text("Quyền Notification", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showStandbySimulator) {
            StandbyCustomizeSimulatorDialog(
                settings = settings,
                settingsRepository = settingsRepository,
                onDismiss = { showStandbySimulator = false }
            )
        }
    }
}
