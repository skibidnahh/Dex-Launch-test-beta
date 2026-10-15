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
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    settings: LauncherSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(640.dp)
                .height(650.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = Color(0xFF2C2C2C),
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
                    Text(text = "Cài đặt DeX Launcher", color = Color.White, fontSize = 20.sp)
                    Button(onClick = onDismiss) {
                        Text("Xong")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Wallpaper section
                    item {
                        Text(text = "Hình nền Màn hình chính", color = Color.Cyan, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { desktopWallpaperLauncher.launch(arrayOf("image/*", "video/*")) }) {
                                Text("Chọn Ảnh / Video Desktop")
                            }
                            Button(onClick = {
                                scope.launch { settingsRepository.updateDesktopWallpaper("", "preset") }
                            }) {
                                Text("Mặc định")
                            }
                        }
                    }

                    item {
                        Text(text = "Hình nền Màn hình chờ (Standby)", color = Color.Cyan, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { standbyWallpaperLauncher.launch(arrayOf("image/*", "video/*")) }) {
                                Text("Chọn Ảnh / Video Standby")
                            }
                            Button(onClick = {
                                scope.launch { settingsRepository.updateStandbyWallpaper("", "preset") }
                            }) {
                                Text("Mặc định")
                            }
                        }
                    }

                    // Wallpaper Dim & Blur
                    item {
                        Text(text = "Độ tối nền (${(settings.wallpaperDim * 100).toInt()}%)", color = Color.White, fontSize = 14.sp)
                        Slider(
                            value = settings.wallpaperDim,
                            onValueChange = { scope.launch { settingsRepository.updateWallpaperDim(it) } },
                            valueRange = 0f..0.6f
                        )
                    }

                    item {
                        Text(text = "Độ mờ (Blur) nền (${settings.wallpaperBlur.toInt()}dp)", color = Color.White, fontSize = 14.sp)
                        Slider(
                            value = settings.wallpaperBlur,
                            onValueChange = { scope.launch { settingsRepository.updateWallpaperBlur(it) } },
                            valueRange = 0f..25f
                        )
                    }

                    // Icon settings
                    item {
                        Text(text = "Kích thước Icon (${settings.iconSize}dp)", color = Color.White, fontSize = 14.sp)
                        Slider(
                            value = settings.iconSize.toFloat(),
                            onValueChange = { scope.launch { settingsRepository.updateIconSettings(it.toInt(), settings.iconLabelSize, settings.iconCornerRadius, settings.showLabels, settings.showShadow) } },
                            valueRange = 40f..112f
                        )
                    }

                    // System Permissions
                    item {
                        Text(text = "Quyền hệ thống", color = Color.Cyan, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            } catch (_: Exception) {}
                        }) {
                            Text("Cấp quyền truy cập Usage Stats (cho Taskbar)")
                        }
                    }
                }
            }
        }
    }
}
