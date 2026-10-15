package com.example.dexlauncher.ui.standby

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.R
import com.example.dexlauncher.data.repository.StandbySettings
import com.example.dexlauncher.data.repository.WallpaperSettings
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun StandbyScreen(
    wallpaperSettings: WallpaperSettings,
    standbySettings: StandbySettings,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "standby_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                val dragThreshold = size.height * 0.22f
                detectVerticalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetY.value < -dragThreshold) {
                                offsetY.animateTo(
                                    targetValue = -size.height.toFloat(),
                                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                                )
                                onUnlocked()
                            } else {
                                offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f))
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f))
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val newOffset = (offsetY.value + dragAmount).coerceAtMost(0f)
                            offsetY.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        // Wallpaper background: preset / photo / video
        WallpaperBackground(
            wallpaperSettings = wallpaperSettings,
            isPaused = false
        )

        // 9-position alignment
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
                .padding(horizontal = 48.dp, vertical = 36.dp)
        ) {
            // Customizable clock
            StandbyClockView(
                settings = standbySettings,
                modifier = Modifier.align(clockAlignment)
            )

            // Bottom "Vuốt lên để mở" indicator
            if (standbySettings.showSwipeHint) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .offset(y = bounceOffset.dp)
                        .alpha(pulseAlpha)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.swipe_up_to_unlock),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
