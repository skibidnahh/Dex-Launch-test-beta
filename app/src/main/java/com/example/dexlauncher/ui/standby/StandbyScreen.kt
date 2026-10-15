package com.example.dexlauncher.ui.standby

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.R
import com.example.dexlauncher.data.model.DeXWallpaperPreset
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StandbyScreen(
    wallpaper: DeXWallpaperPreset,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    // Live clock ticker
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    // Pulsing chevron animation
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
                                // Animate all the way up offscreen
                                offsetY.animateTo(
                                    targetValue = -size.height.toFloat(),
                                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                                )
                                onUnlocked()
                            } else {
                                // Snap back
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
                            // Only allow upward dragging
                            val newOffset = (offsetY.value + dragAmount).coerceAtMost(0f)
                            offsetY.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        val density = LocalDensity.current

        // Wallpaper Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    wallpaper.topColor,
                    wallpaper.midColor,
                    wallpaper.bottomColor
                )
            )
            drawRect(brush = gradientBrush)

            // Star field dots
            val starCount = 65
            for (i in 0 until starCount) {
                val x = (Math.sin(i * 997.0) * 0.5 + 0.5) * size.width
                val y = (Math.cos(i * 733.0) * 0.5 + 0.5) * (size.height * 0.7f)
                val alpha = ((Math.sin(i * 123.0) * 0.5 + 0.5) * 0.7f + 0.3f).toFloat()
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = if (i % 7 == 0) 2.2f else 1.2f,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }

        // Dark dimming overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )

        // Main Standby Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top lock icon & status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Samsung DeX",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Big Clock & Date in center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeFormat.format(currentTime),
                    color = Color.White,
                    fontSize = 78.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1.5).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dateFormat.format(currentTime)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Bottom "Vuốt lên để mở" indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = bounceOffset.dp)
                    .alpha(pulseAlpha)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
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
