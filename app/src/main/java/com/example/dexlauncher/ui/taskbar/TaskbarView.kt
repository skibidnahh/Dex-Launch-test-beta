package com.example.dexlauncher.ui.taskbar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.R
import com.example.dexlauncher.data.model.AppInfo
import com.example.dexlauncher.data.repository.TaskbarAppearance
import com.example.dexlauncher.shizuku.ShizukuManager
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskbarView(
    pinnedApps: List<AppInfo>,
    allApps: List<AppInfo>,
    runningPackages: Set<String>,
    focusedPackage: String?,
    taskbarAppearance: TaskbarAppearance,
    shizukuManager: ShizukuManager,
    onAppsClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    onRecentsClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onAppClicked: (AppInfo, Boolean) -> Unit, // app, isRunning
    onCloseApp: (AppInfo) -> Unit,
    onTogglePinTaskbar: (AppInfo) -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, d MMM", Locale.getDefault()) }

    // Combined taskbar list: pinned apps + running unpinned apps
    val pinnedPackageSet = remember(pinnedApps) { pinnedApps.map { it.packageName }.toSet() }
    val unpinnedRunningApps = remember(allApps, runningPackages, pinnedPackageSet) {
        allApps.filter { runningPackages.contains(it.packageName) && !pinnedPackageSet.contains(it.packageName) }
    }
    val combinedTaskbarApps = remember(pinnedApps, unpinnedRunningApps) {
        pinnedApps + unpinnedRunningApps
    }

    val taskbarBg = remember(taskbarAppearance.transparency) {
        DeXDarkPalette.TaskbarBackground.copy(alpha = taskbarAppearance.transparency.coerceIn(0.1f, 1.0f))
    }

    val shape = remember(taskbarAppearance.cornerRadiusDp) {
        if (taskbarAppearance.cornerRadiusDp > 0) RoundedCornerShape(topStart = taskbarAppearance.cornerRadiusDp.dp, topEnd = taskbarAppearance.cornerRadiusDp.dp)
        else RoundedCornerShape(0.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(taskbarAppearance.heightDp.dp)
            .clip(shape)
            .background(taskbarBg)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Side: DeX Navigation & Apps
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Apps (9 dots)
            TaskbarIconButton(onClick = onAppsClicked, contentDescription = "Apps") {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_apps),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Recents (III)
            TaskbarIconButton(onClick = onRecentsClicked, contentDescription = "Recents") {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_recents),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Home
            TaskbarIconButton(onClick = onHomeClicked, contentDescription = "Home") {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_home),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Back
            TaskbarIconButton(onClick = onBackClicked, contentDescription = "Back") {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_back),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Separator
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .width(1.dp)
                    .height(24.dp)
                    .background(DeXDarkPalette.TaskbarDivider)
            )

            // Pinned & Running Apps in Taskbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                combinedTaskbarApps.take(12).forEach { app ->
                    val isRunning = runningPackages.contains(app.packageName)
                    val isFocused = focusedPackage == app.packageName

                    TaskbarAppItem(
                        app = app,
                        isRunning = isRunning,
                        isFocused = isFocused,
                        shizukuManager = shizukuManager,
                        onClick = { onAppClicked(app, isRunning) },
                        onCloseApp = { onCloseApp(app) },
                        onTogglePin = { onTogglePinTaskbar(app) }
                    )
                }
            }
        }

        // Right Side: DeX Status Tray
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100%",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSettingsClicked),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clickable(onClick = onSettingsClicked)
            ) {
                Text(
                    text = timeFormat.format(currentTime),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = dateFormat.format(currentTime),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun TaskbarIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskbarAppItem(
    app: AppInfo,
    isRunning: Boolean,
    isFocused: Boolean,
    shizukuManager: ShizukuManager,
    onClick: () -> Unit,
    onCloseApp: () -> Unit,
    onTogglePin: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .size(width = 46.dp, height = 50.dp)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 4.dp)
        ) {
            val bitmap = remember(app.icon) {
                app.icon?.let { drawableToBitmap(it) }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Running indicator
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .size(width = if (isFocused) 22.dp else 12.dp, height = 3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(if (isFocused) DeXDarkPalette.ActiveCyan else Color(0xFF60A5FA))
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }

        // Long Press Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(DeXDarkPalette.CardBackground)
        ) {
            DropdownMenuItem(
                text = { Text("Mở cửa sổ mới", color = Color.White, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = DeXDarkPalette.PrimaryBlue, modifier = Modifier.size(16.dp))
                },
                onClick = {
                    onClick()
                    showMenu = false
                }
            )

            if (isRunning) {
                DropdownMenuItem(
                    text = { Text("Đóng ứng dụng", color = Color(0xFFEF4444), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    },
                    onClick = {
                        if (shizukuManager.isConnected()) {
                            shizukuManager.forceStopApp(app.packageName)
                        } else {
                            Toast.makeText(context, "Đã đóng khỏi Taskbar", Toast.LENGTH_SHORT).show()
                        }
                        onCloseApp()
                        showMenu = false
                    }
                )
            }

            DropdownMenuItem(
                text = {
                    Text(
                        if (app.isPinnedToTaskbar) "Bỏ ghim khỏi Taskbar" else "Ghim vào Taskbar",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = DeXDarkPalette.ActiveCyan, modifier = Modifier.size(16.dp))
                },
                onClick = {
                    onTogglePin()
                    showMenu = false
                }
            )
        }
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 64
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 64
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
