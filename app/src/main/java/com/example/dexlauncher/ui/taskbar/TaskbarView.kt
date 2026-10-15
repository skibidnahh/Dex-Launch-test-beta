package com.example.dexlauncher.ui.taskbar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.dexlauncher.ui.theme.DeXDarkPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskbarView(
    pinnedApps: List<AppInfo>,
    runningPackages: Set<String>,
    onAppsClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    onRecentsClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onAppClicked: (AppInfo) -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Clock for status tray
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, d MMM", Locale.getDefault()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(DeXDarkPalette.TaskbarBackground)
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
            TaskbarIconButton(
                onClick = onAppsClicked,
                contentDescription = "Apps"
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_apps),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Recents (III)
            TaskbarIconButton(
                onClick = onRecentsClicked,
                contentDescription = "Recents"
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_recents),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Home
            TaskbarIconButton(
                onClick = onHomeClicked,
                contentDescription = "Home"
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_home),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Back
            TaskbarIconButton(
                onClick = onBackClicked,
                contentDescription = "Back"
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dex_back),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Divider
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
                pinnedApps.take(7).forEach { app ->
                    val isRunning = runningPackages.contains(app.packageName)
                    TaskbarAppItem(
                        app = app,
                        isRunning = isRunning,
                        onClick = { onAppClicked(app) }
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
            // Pill status container
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Chevron expander
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Expand Tray",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Wi-Fi
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Volume
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Battery 100%
                Text(
                    text = "100%",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Settings quick trigger
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

            // Clock & Date Stack
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

@Composable
fun TaskbarAppItem(
    app: AppInfo,
    isRunning: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 46.dp, height = 50.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .padding(vertical = 4.dp)
    ) {
        // App icon
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

        // Running indicator: glowing blue/cyan pill under icon
        if (isRunning) {
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(DeXDarkPalette.ActiveCyan)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
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
