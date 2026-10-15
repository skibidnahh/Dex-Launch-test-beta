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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.StandbySettings
import com.example.dexlauncher.data.repository.WallpaperSettings
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground

@Composable
fun StandbyPreviewView(
    standbySettings: StandbySettings,
    wallpaperSettings: WallpaperSettings,
    onPositionChanged: (gridPos: Int, offsetX: Float, offsetY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DeXDarkPalette.Border, RoundedCornerShape(16.dp))
    ) {
        val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // Wallpaper in preview
        WallpaperBackground(
            wallpaperSettings = wallpaperSettings,
            isPaused = true
        )

        // 3x3 Grid of position buttons overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..2) {
                        val pos = row * 3 + col
                        val isSelected = standbySettings.gridPosition == pos
                        val targetX = when (col) { 0 -> 0.18f; 1 -> 0.5f; else -> 0.82f }
                        val targetY = when (row) { 0 -> 0.22f; 1 -> 0.5f; else -> 0.78f }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DeXDarkPalette.PrimaryBlue else Color.White.copy(alpha = 0.2f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) DeXDarkPalette.ActiveCyan else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { onPositionChanged(pos, targetX, targetY) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Clock rendered at custom offset / grid position with drag capability
        val biasX = (standbySettings.customOffsetX * 2f - 1f).coerceIn(-0.95f, 0.95f)
        val biasY = (standbySettings.customOffsetY * 2f - 1f).coerceIn(-0.95f, 0.95f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(boxWidthPx, boxHeightPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (standbySettings.customOffsetX + dragAmount.x / boxWidthPx).coerceIn(0.1f, 0.9f)
                        val newY = (standbySettings.customOffsetY + dragAmount.y / boxHeightPx).coerceIn(0.15f, 0.85f)
                        // Determine nearest grid position if close
                        val col = if (newX < 0.35f) 0 else if (newX > 0.65f) 2 else 1
                        val row = if (newY < 0.35f) 0 else if (newY > 0.65f) 2 else 1
                        val nearestPos = row * 3 + col
                        onPositionChanged(nearestPos, newX, newY)
                    }
                },
            contentAlignment = BiasAlignment(biasX, biasY)
        ) {
            // Scaled down preview clock
            StandbyClockView(
                settings = standbySettings.copy(
                    clockSizeSp = (standbySettings.clockSizeSp * 0.42f).toInt().coerceAtLeast(22)
                )
            )
        }

        // Hint label at bottom center
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Kéo thả tự do hoặc chạm 1 trong 9 nút vị trí",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}
