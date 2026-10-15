package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.LauncherSettings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StandbyClockView(
    settings: LauncherSettings,
    onClick: () -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val delay = (60 - cal.get(Calendar.SECOND)) * 1000L - cal.get(Calendar.MILLISECOND)
            kotlinx.coroutines.delay(if (delay > 0) delay else 1000L)
        }
    }

    val timeStr = remember(currentTime) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTime))
    }
    val dateStr = remember(currentTime) {
        SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date(currentTime))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(settings.clockOpacity)
                .let { if (settings.clockBlur > 0f) it.blur(settings.clockBlur.dp) else it }
        ) {
            Text(
                text = timeStr,
                color = Color(settings.clockColor),
                fontSize = (72 * settings.clockScale).sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateStr,
                color = Color(settings.clockColor).copy(alpha = 0.8f),
                fontSize = (18 * settings.clockScale).sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
