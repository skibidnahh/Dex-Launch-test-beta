package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dexlauncher.data.repository.StandbySettings
import com.example.dexlauncher.data.repository.WallpaperSettings
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground
import kotlin.math.roundToInt

@Composable
fun StandbyCustomizeSimulatorDialog(
    isOpen: Boolean,
    standbySettings: StandbySettings,
    wallpaperSettings: WallpaperSettings,
    onSettingsChanged: (StandbySettings) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var showEditorSheet by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

            // Wallpaper with blur & opacity applied if customized
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(standbySettings.backgroundOpacity)
                    .blur(standbySettings.backgroundBlurDp.dp)
            ) {
                WallpaperBackground(
                    wallpaperSettings = wallpaperSettings,
                    isPaused = false
                )
            }

            // Top Toolbar inside simulator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Giả lập Màn hình chờ (Standby Simulator)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Giữ & kéo đồng hồ đến vị trí bất kỳ. Chạm vào đồng hồ để đổi Font & Màu.",
                        color = DeXDarkPalette.TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showEditorSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tùy chỉnh Font/Màu", fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Draggable Clock
            val biasX = (standbySettings.customOffsetX * 2f - 1f).coerceIn(-0.95f, 0.95f)
            val biasY = (standbySettings.customOffsetY * 2f - 1f).coerceIn(-0.95f, 0.95f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(36.dp)
                    .pointerInput(boxWidthPx, boxHeightPx) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = (standbySettings.customOffsetX + dragAmount.x / boxWidthPx).coerceIn(0.05f, 0.95f)
                            val newY = (standbySettings.customOffsetY + dragAmount.y / boxHeightPx).coerceIn(0.1f, 0.9f)
                            onSettingsChanged(standbySettings.copy(customOffsetX = newX, customOffsetY = newY))
                        }
                    },
                contentAlignment = BiasAlignment(biasX, biasY)
            ) {
                Box(
                    modifier = Modifier
                        .clickable { showEditorSheet = true }
                        .padding(8.dp)
                ) {
                    StandbyClockView(settings = standbySettings)
                }
            }

            // Floating Editor Sheet / Dialog when clicking clock or customize button
            if (showEditorSheet) {
                Dialog(onDismissRequest = { showEditorSheet = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DeXDarkPalette.SurfaceFloating)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tùy chỉnh Đồng hồ & Nền", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { showEditorSheet = false }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Font Family
                            Text("Phông chữ:", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Default", "Sans-serif", "Serif", "Monospace", "Cursive").forEach { font ->
                                    FilterChip(
                                        selected = standbySettings.fontFamilyName == font,
                                        onClick = { onSettingsChanged(standbySettings.copy(fontFamilyName = font)) },
                                        label = { Text(font, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Color Preset / Custom
                            Text("Màu sắc đồng hồ:", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val colors = listOf(0xFFFFFFFFL, 0xFF00E5FFL, 0xFFFFD700L, 0xFFFF5252L, 0xFF76FF03L)
                                colors.forEach { colorVal ->
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorVal))
                                            .border(
                                                width = if (standbySettings.colorHex == colorVal) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable { onSettingsChanged(standbySettings.copy(colorHex = colorVal)) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Background Opacity & Blur sliders for corners/blur
                            Text("Độ mờ nền (Opacity): ${(standbySettings.backgroundOpacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = standbySettings.backgroundOpacity,
                                onValueChange = { onSettingsChanged(standbySettings.copy(backgroundOpacity = it)) },
                                valueRange = 0.2f..1.0f
                            )

                            Text("Độ mờ góc / Blur nền: ${standbySettings.backgroundBlurDp} dp", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = standbySettings.backgroundBlurDp.toFloat(),
                                onValueChange = { onSettingsChanged(standbySettings.copy(backgroundBlurDp = it.toInt())) },
                                valueRange = 0f..25f
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { showEditorSheet = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DeXDarkPalette.PrimaryBlue)
                            ) {
                                Text("Áp dụng & Lưu", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
