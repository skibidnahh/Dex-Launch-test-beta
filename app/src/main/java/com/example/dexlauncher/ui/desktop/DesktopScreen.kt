package com.example.dexlauncher.ui.desktop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ripple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.R
import com.example.dexlauncher.data.model.AppInfo
import com.example.dexlauncher.data.model.DeXWallpaperPreset
import com.example.dexlauncher.ui.taskbar.TaskbarView
import com.example.dexlauncher.ui.taskbar.drawableToBitmap
import com.example.dexlauncher.ui.theme.DeXDarkPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopScreen(
    wallpaper: DeXWallpaperPreset,
    pinnedDesktopApps: List<AppInfo>,
    pinnedTaskbarApps: List<AppInfo>,
    runningPackages: Set<String>,
    onAppClicked: (AppInfo) -> Unit,
    onTogglePinDesktop: (AppInfo) -> Unit,
    onTogglePinTaskbar: (AppInfo) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onRecentsClicked: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDesktopMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Dynamic Samsung DeX Wallpaper Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDesktopMenu = false },
                    onLongClick = { showDesktopMenu = true }
                )
        ) {
            // Gradient Sky / Background
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    wallpaper.topColor,
                    wallpaper.midColor,
                    wallpaper.bottomColor
                )
            )
            drawRect(brush = gradientBrush)

            // Starfield (subtle cosmic stardust)
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

            // Diagonal streaks / DeX Nebula wave as in Samsung DeX wallpapers
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
            drawPath(
                path = wavePath,
                color = wallpaper.accentWaveColor
            )

            // Lower dunes / horizon
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
            drawPath(
                path = dunesPath,
                color = wallpaper.bottomColor.copy(alpha = 0.65f)
            )
        }

        // Desktop Context Menu (when long pressed on background)
        DropdownMenu(
            expanded = showDesktopMenu,
            onDismissRequest = { showDesktopMenu = false },
            modifier = Modifier.background(DeXDarkPalette.CardBackground)
        ) {
            DropdownMenuItem(
                text = { Text("Đổi hình nền DeX", color = Color.White, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = DeXDarkPalette.ActiveCyan)
                },
                onClick = {
                    showDesktopMenu = false
                    onOpenSettings()
                }
            )
            DropdownMenuItem(
                text = { Text("Mở toàn bộ ứng dụng", color = Color.White, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = DeXDarkPalette.PrimaryBlue)
                },
                onClick = {
                    showDesktopMenu = false
                    onOpenAppDrawer()
                }
            )
            DropdownMenuItem(
                text = { Text("Cài đặt Samsung DeX", color = Color.White, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = DeXDarkPalette.TextSecondary)
                },
                onClick = {
                    showDesktopMenu = false
                    onOpenSettings()
                }
            )
        }

        // Left Side: Desktop Pinned Apps Grid / Column (matching Samsung DeX screenshots)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 54.dp, top = 20.dp, start = 12.dp, end = 12.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 74.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = if (screenWidth > 600.dp) 320.dp else screenWidth)
            ) {
                items(pinnedDesktopApps, key = { it.packageName }) { app ->
                    DesktopAppIcon(
                        app = app,
                        onClick = { onAppClicked(app) },
                        onUnpin = { onTogglePinDesktop(app) },
                        onTogglePinTaskbar = { onTogglePinTaskbar(app) }
                    )
                }
            }
        }

        // Bottom Taskbar (pinned to bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            TaskbarView(
                pinnedApps = pinnedTaskbarApps,
                runningPackages = runningPackages,
                onAppsClicked = onOpenAppDrawer,
                onHomeClicked = { /* already on home desktop */ },
                onRecentsClicked = onRecentsClicked,
                onBackClicked = onBackClicked,
                onAppClicked = onAppClicked,
                onSettingsClicked = onOpenSettings
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopAppIcon(
    app: AppInfo,
    onClick: () -> Unit,
    onUnpin: () -> Unit,
    onTogglePinTaskbar: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val bitmap = remember(app.icon) {
        app.icon?.let { drawableToBitmap(it) }
    }

    Box(contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(74.dp)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = app.label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(1f, 1f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(DeXDarkPalette.CardBackground)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.unpin_from_desktop), color = Color.White, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                },
                onClick = {
                    onUnpin()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        if (app.isPinnedToTaskbar) stringResource(R.string.unpin_from_taskbar)
                        else stringResource(R.string.pin_to_taskbar),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = DeXDarkPalette.ActiveCyan, modifier = Modifier.size(16.dp))
                },
                onClick = {
                    onTogglePinTaskbar()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.app_info), color = Color.White, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = DeXDarkPalette.TextSecondary, modifier = Modifier.size(16.dp))
                },
                onClick = {
                    showMenu = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", app.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}
