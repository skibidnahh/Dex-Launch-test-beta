package com.example.dexlauncher.ui.wallpaper

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dexlauncher.data.model.DeXWallpaperPreset
import com.example.dexlauncher.data.repository.WallpaperSettings

@OptIn(UnstableApi::class)
@Composable
fun WallpaperBackground(
    wallpaperSettings: WallpaperSettings,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        when (wallpaperSettings.type) {
            "IMAGE" -> {
                if (wallpaperSettings.uriString.isNotBlank()) {
                    val blurModifier = if (wallpaperSettings.blurRadius > 0f) {
                        Modifier
                            .fillMaxSize()
                            .blur(wallpaperSettings.blurRadius.dp)
                    } else {
                        Modifier.fillMaxSize()
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(wallpaperSettings.uriString))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Wallpaper Image",
                        contentScale = ContentScale.Crop,
                        modifier = blurModifier
                    )
                } else {
                    DefaultPresetCanvas(presetId = wallpaperSettings.presetId)
                }
            }

            "VIDEO" -> {
                if (wallpaperSettings.uriString.isNotBlank()) {
                    val exoPlayer = remember(wallpaperSettings.uriString) {
                        ExoPlayer.Builder(context).build().apply {
                            repeatMode = Player.REPEAT_MODE_ALL
                            volume = 0f // Muted background video
                            try {
                                val mediaItem = MediaItem.fromUri(Uri.parse(wallpaperSettings.uriString))
                                setMediaItem(mediaItem)
                                prepare()
                                playWhenReady = !isPaused
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    DisposableEffect(isPaused) {
                        if (isPaused) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                        onDispose { }
                    }

                    DisposableEffect(exoPlayer) {
                        onDispose {
                            try {
                                exoPlayer.stop()
                                exoPlayer.release()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    DefaultPresetCanvas(presetId = wallpaperSettings.presetId)
                }
            }

            else -> { // "PRESET"
                DefaultPresetCanvas(presetId = wallpaperSettings.presetId)
            }
        }

        // Dark dim overlay (0% - 60%)
        if (wallpaperSettings.dimOpacity > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = wallpaperSettings.dimOpacity.coerceIn(0f, 0.6f)))
            )
        }
    }
}

@Composable
fun DefaultPresetCanvas(presetId: String) {
    val preset = remember(presetId) {
        DeXWallpaperPreset.entries.find { it.id == presetId } ?: DeXWallpaperPreset.NEBULA_PURPLE
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                preset.topColor,
                preset.midColor,
                preset.bottomColor
            )
        )
        drawRect(brush = gradientBrush)

        // Cosmic stardust
        for (i in 0..80) {
            val x = (Math.sin(i * 353.0) * 0.5 + 0.5) * size.width
            val y = (Math.cos(i * 911.0) * 0.5 + 0.5) * (size.height * 0.65f)
            val alpha = ((Math.sin(i * 77.0) * 0.5 + 0.5) * 0.8f + 0.2f).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = if (i % 9 == 0) 2.0f else 1.0f,
                center = Offset(x.toFloat(), y.toFloat())
            )
        }

        // DeX wave
        val wavePath = Path().apply {
            moveTo(0f, size.height * 0.45f)
            cubicTo(
                size.width * 0.35f, size.height * 0.35f,
                size.width * 0.65f, size.height * 0.55f,
                size.width, size.height * 0.38f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = wavePath, color = preset.accentWaveColor)

        // Horizon dunes
        val dunesPath = Path().apply {
            moveTo(0f, size.height * 0.82f)
            cubicTo(
                size.width * 0.25f, size.height * 0.79f,
                size.width * 0.6f, size.height * 0.85f,
                size.width, size.height * 0.81f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = dunesPath, color = preset.bottomColor.copy(alpha = 0.65f))
    }
}
