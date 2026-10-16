package com.example.dexlauncher.ui.standby

import android.graphics.Typeface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.LauncherSettings
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StandbyClockView(
    settings: LauncherSettings,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val delay = (60 - cal.get(Calendar.SECOND)) * 1000L - cal.get(Calendar.MILLISECOND)
            kotlinx.coroutines.delay(if (delay > 0) delay else 1000L)
        }
    }

    val timeFormat = if (settings.clockIs24H) "HH:mm" else "hh:mm a"
    val timeStr = remember(currentTime, settings.clockIs24H) {
        SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date(currentTime))
    }
    val dateStr = remember(currentTime) {
        SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date(currentTime))
    }

    // Resolve custom typeface if fontPath exists
    val customFontFamily = remember(settings.clockFontPath) {
        if (settings.clockFontPath.isNotEmpty()) {
            try {
                val file = File(settings.clockFontPath)
                if (file.exists()) {
                    FontFamily(Typeface.createFromFile(file))
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val alignment = when (settings.clockAlignment) {
        "start" -> Alignment.Start
        "end" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val textAlign = when (settings.clockAlignment) {
        "start" -> TextAlign.Start
        "end" -> TextAlign.End
        else -> TextAlign.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                .alpha(settings.clockOpacity)
                .let { if (settings.clockBlur > 0f) it.blur(settings.clockBlur.dp) else it }
        ) {
            Text(
                text = timeStr,
                color = Color(settings.clockColor),
                fontSize = (72 * settings.clockScale).sp,
                fontWeight = FontWeight(settings.clockWeight),
                letterSpacing = settings.clockLetterSpacing.sp,
                fontFamily = customFontFamily,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (settings.clockShowDate) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateStr,
                    color = Color(settings.clockColor).copy(alpha = 0.8f),
                    fontSize = (18 * settings.clockScale).sp,
                    fontFamily = customFontFamily,
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (settings.clockShowSwipeUp) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Vuốt lên để mở khóa",
                    color = Color(settings.clockColor).copy(alpha = 0.6f),
                    fontSize = (14 * settings.clockScale).sp,
                    textAlign = textAlign
                )
            }
        }
    }
}
