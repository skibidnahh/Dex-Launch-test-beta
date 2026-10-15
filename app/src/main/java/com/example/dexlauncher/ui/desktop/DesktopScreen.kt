package com.example.dexlauncher.ui.desktop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Color
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
import com.example.dexlauncher.data.repository.DesktopAppearance
import com.example.dexlauncher.data.repository.TaskbarAppearance
import com.example.dexlauncher.data.repository.WallpaperSettings
import com.example.dexlauncher.shizuku.ShizukuManager
import com.example.dexlauncher.ui.taskbar.TaskbarView
import com.example.dexlauncher.ui.taskbar.drawableToBitmap
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import com.example.dexlauncher.ui.wallpaper.WallpaperBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopScreen(
    wallpaperSettings: WallpaperSettings,
    desktopAppearance: DesktopAppearance,
    taskbarAppearance: TaskbarAppearance,
    pinnedDesktopApps: List<AppInfo>,
    pinnedTaskbarApps: List<AppInfo>,
    allApps: List<AppInfo>,
    runningPackages: Set<String>,
    focusedPackage: String?,
    shizukuManager: ShizukuManager,
    isStandbyActive: Boolean,
    onAppClicked: (AppInfo) -> Unit,
    onTogglePinDesktop: (AppInfo) -> Unit,
    onTogglePinTaskbar: (AppInfo) -> Unit,
    onHideApp: (AppInfo) -> Unit,
    onCloseApp: (AppInfo) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onRecentsClicked: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDesktopMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth

        // Wallpaper (Preset / Image / Video with optional dim and blur)
        WallpaperBackground(
            wallpaperSettings = wallpaperSettings,
            isPaused = isStandbyActive
        )

        // Desktop Context Menu (when long pressed on background)
        DropdownMenu(
            expanded = showDesktopMenu,
            onDismissRequest = { showDesktopMenu = false },
            modifier = Modifier.background(DeXDarkPalette.CardBackground)
        ) {
            DropdownMenuItem(
                text = { Text("Đổi hình nền Desktop", color = Color.White, fontSize = 13.sp) },
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

        // Desktop Background Tap Handler
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDesktopMenu = false },
                    onLongClick = { showDesktopMenu = true }
                )
        )

        // Desktop Pinned Icons Grid
        val columns = if (desktopAppearance.gridColumns > 0) {
            GridCells.Fixed(desktopAppearance.gridColumns)
        } else {
            GridCells.Adaptive(minSize = (desktopAppearance.iconSizeDp + 24).dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = taskbarAppearance.heightDp.dp, top = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            LazyVerticalGrid(
                columns = columns,
                contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = if (screenWidth > 600.dp) 480.dp else screenWidth)
            ) {
                itemsIndexed(pinnedDesktopApps, key = { _, it -> it.packageName }) { index, app ->
                    // Staggered entrance animation
                    AnimatedVisibility(
                        visible = !isStandbyActive,
                        enter = fadeIn(animationSpec = tween(250, delayMillis = index * 35)) +
                                scaleIn(initialScale = 0.88f, animationSpec = tween(250, delayMillis = index * 35))
                    ) {
                        DesktopAppIcon(
                            app = app,
                            iconSizeDp = desktopAppearance.iconSizeDp,
                            labelSizeSp = desktopAppearance.labelSizeSp,
                            onClick = { onAppClicked(app) },
                            onUnpin = { onTogglePinDesktop(app) },
                            onTogglePinTaskbar = { onTogglePinTaskbar(app) },
                            onHide = { onHideApp(app) }
                        )
                    }
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
                allApps = allApps,
                runningPackages = runningPackages,
                focusedPackage = focusedPackage,
                taskbarAppearance = taskbarAppearance,
                shizukuManager = shizukuManager,
                onAppsClicked = onOpenAppDrawer,
                onHomeClicked = { },
                onRecentsClicked = onRecentsClicked,
                onBackClicked = onBackClicked,
                onAppClicked = { app, _ -> onAppClicked(app) },
                onCloseApp = onCloseApp,
                onTogglePinTaskbar = onTogglePinTaskbar,
                onSettingsClicked = onOpenSettings
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopAppIcon(
    app: AppInfo,
    iconSizeDp: Int,
    labelSizeSp: Int,
    onClick: () -> Unit,
    onUnpin: () -> Unit,
    onTogglePinTaskbar: () -> Unit,
    onHide: () -> Unit
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
                .width((iconSizeDp + 22).dp)
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
                        .size(iconSizeDp.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(iconSizeDp.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = Color.White,
                        fontSize = (iconSizeDp * 0.38f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = app.label,
                color = Color.White,
                fontSize = labelSizeSp.sp,
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
                text = { Text("Bỏ khỏi Desktop", color = Color(0xFFEF4444), fontSize = 13.sp) },
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
                        if (app.isPinnedToTaskbar) "Bỏ ghim Taskbar" else "Ghim vào Taskbar",
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
                text = { Text("Ẩn ứng dụng", color = Color(0xFFFBBF24), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                },
                onClick = {
                    onHide()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = { Text("Gỡ cài đặt", color = Color(0xFFEF4444), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                },
                onClick = {
                    showMenu = false
                    val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(uninstallIntent)
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
