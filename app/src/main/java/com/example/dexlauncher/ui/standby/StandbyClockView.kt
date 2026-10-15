package com.example.dexlauncher.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dexlauncher.data.repository.StandbySettings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StandbyClockView(
    settings: StandbySettings,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val pattern = if (settings.is24Hour) "HH:mm" else "h:mm"
    val timeFormat = remember(settings.is24Hour) { SimpleDateFormat(pattern, Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    val fontFamily = remember(settings.fontFamilyName) {
        when (settings.fontFamilyName) {
            "Sans-serif" -> FontFamily.SansSerif
            "Serif" -> FontFamily.Serif
            "Monospace" -> FontFamily.Monospace
            "Cursive" -> FontFamily.Cursive
            else -> FontFamily.Default
        }
    }

    val fontWeight = remember(settings.fontWeightValue) {
        FontWeight(settings.fontWeightValue.coerceIn(100, 900))
    }

    val baseColor = remember(settings.colorHex) {
        Color(settings.colorHex)
    }

    val alignment = remember(settings.gridPosition) {
        when (settings.gridPosition % 3) {
            0 -> Alignment.Start
            2 -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    }

    val textAlign = remember(settings.gridPosition) {
        when (settings.gridPosition % 3) {
            0 -> TextAlign.Start
            2 -> TextAlign.End
            else -> TextAlign.Center
        }
    }

    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = alignment
    ) {
        // Small lock badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Samsung DeX",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large customizable clock
        Text(
            text = timeFormat.format(currentTime),
            color = baseColor,
            fontSize = settings.clockSizeSp.sp,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textAlign = textAlign,
            style = TextStyle(
                shadow = if (settings.colorHex != 0L) {
                    Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(2f, 3f), blurRadius = 8f)
                } else null
            )
        )

        // Date
        if (settings.showDate) {
            Text(
                text = dateFormat.format(currentTime)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                color = baseColor.copy(alpha = 0.9f),
                fontSize = (settings.clockSizeSp * 0.22f).coerceIn(12f, 24f).sp,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily,
                textAlign = textAlign,
                style = TextStyle(
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(1f, 2f), blurRadius = 4f)
                )
            )
        }
    }
}
