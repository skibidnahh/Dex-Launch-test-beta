package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.LauncherSettings
import com.example.dexlauncher.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun StandbyCustomizeSimulatorDialog(
    settings: LauncherSettings,
    settingsRepository: SettingsRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var clockScale by remember { mutableFloatStateOf(settings.clockScale) }
    var clockOpacity by remember { mutableFloatStateOf(settings.clockOpacity) }
    var clockBlur by remember { mutableFloatStateOf(settings.clockBlur) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(680.dp)
                .height(550.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF1E1E1E),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar with OK button at top-left as requested ("nút ok đồng ý thoát ở góc trái")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                settingsRepository.updateClockCustomization(
                                    font = settings.clockFont,
                                    color = settings.clockColor,
                                    scale = clockScale,
                                    offsetX = settings.clockOffsetX,
                                    offsetY = settings.clockOffsetY,
                                    opacity = clockOpacity,
                                    blur = clockBlur
                                }
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("OK (Đồng ý & Thoát)", color = Color.White)
                    }

                    Text(
                        text = "Tùy chỉnh Màn hình chờ & Đồng hồ",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    OutlinedButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Box simulating Standby clock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF121212), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val previewSettings = settings.copy(
                        clockScale = clockScale,
                        clockOpacity = clockOpacity,
                        clockBlur = clockBlur
                    )
                    StandbyClockView(
                        settings = previewSettings,
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Kích thước đồng hồ (${String.format("%.1f", clockScale)}x)", color = Color.LightGray, fontSize = 14.sp)
                    Slider(
                        value = clockScale,
                        onValueChange = { clockScale = it },
                        valueRange = 0.5f..2.5f
                    )

                    Text(text = "Độ trong suốt (${(clockOpacity * 100).toInt()}%)", color = Color.LightGray, fontSize = 14.sp)
                    Slider(
                        value = clockOpacity,
                        onValueChange = { clockOpacity = it },
                        valueRange = 0.2f..1.0f
                    )

                    Text(text = "Độ mờ Blur (${clockBlur.toInt()}dp)", color = Color.LightGray, fontSize = 14.sp)
                    Slider(
                        value = clockBlur,
                        onValueChange = { clockBlur = it },
                        valueRange = 0f..20f
                    )
                }
            }
        }
    }
}
