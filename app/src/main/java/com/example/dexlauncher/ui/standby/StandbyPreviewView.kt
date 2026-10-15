package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onGridPositionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DeXDarkPalette.Border, RoundedCornerShape(16.dp))
    ) {
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
                                .clickable { onGridPositionSelected(pos) },
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

        // Clock rendered at selected position
        val clockAlignment = when (standbySettings.gridPosition) {
            0 -> Alignment.TopStart
            1 -> Alignment.TopCenter
            2 -> Alignment.TopEnd
            3 -> Alignment.CenterStart
            4 -> Alignment.Center
            5 -> Alignment.CenterEnd
            6 -> Alignment.BottomStart
            7 -> Alignment.BottomCenter
            8 -> Alignment.BottomEnd
            else -> Alignment.Center
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = clockAlignment
        ) {
            // Scaled down preview clock
            StandbyClockView(
                settings = standbySettings.copy(clockSizeSp = (standbySettings.clockSizeSp * 0.45f).toInt().coerceAtLeast(24))
            )
        }
    }
}
