package com.lifeos.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeos.app.ui.theme.Blue
import com.lifeos.app.ui.theme.Danger
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.ui.theme.Success
import com.lifeos.app.ui.theme.Warning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val TR: Locale = Locale.forLanguageTag("tr-TR")

fun formatDay(epochDay: Long, pattern: String = "d MMM yyyy"): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern(pattern, TR))

fun relativeDay(epochDay: Long, today: Long): String = when (epochDay - today) {
    0L -> "Bugün"
    1L -> "Yarın"
    -1L -> "Dün"
    else -> formatDay(epochDay, "d MMM")
}

val PriorityLabels = listOf("Düşük", "Orta", "Yüksek")
fun priorityColor(p: Int): Color = when (p) {
    2 -> Danger
    1 -> Warning
    else -> Success
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Box(
                            Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
                                .background(Brush.linearGradient(listOf(Purple, Blue))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    action?.invoke()
                }
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    stroke: Dp = 12.dp,
    centerText: String? = null,
    subText: String? = null,
    textColor: Color = Color.Unspecified,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "ring",
    )
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val s = stroke.toPx()
            val arcSize = Size(this.size.width - s, this.size.height - s)
            val topLeft = Offset(s / 2, s / 2)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
            drawArc(
                brush = Brush.sweepGradient(listOf(Purple, Blue, Purple)),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(s, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                centerText ?: "%${(animated * 100).toInt()}",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
            )
            if (subText != null) {
                Text(
                    subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else textColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
fun LinearMeter(progress: Float, modifier: Modifier = Modifier, color: Color = Purple) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "meter")
    Box(
        modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            Modifier.fillMaxWidth(animated).height(8.dp).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color, Blue)))
        )
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun EmptyState(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Animasyonlu çubuk grafik. values 0..1 aralığında. */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    highlightLast: Boolean = true,
) {
    val anim = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { anim.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            if (values.isEmpty()) return@Canvas
            val slot = size.width / values.size
            val barW = slot * 0.55f
            values.forEachIndexed { i, v ->
                val x = i * slot + (slot - barW) / 2
                drawRoundRect(track, Offset(x, 0f), Size(barW, size.height), CornerRadius(barW / 2))
                val h = size.height * v.coerceIn(0f, 1f) * anim.value
                if (h > 0f) {
                    val brush = if (highlightLast && i == values.lastIndex)
                        Brush.verticalGradient(listOf(Blue, Purple), startY = size.height - h, endY = size.height)
                    else Brush.verticalGradient(
                        listOf(Purple.copy(alpha = 0.85f), Purple.copy(alpha = 0.45f)),
                        startY = size.height - h, endY = size.height,
                    )
                    drawRoundRect(brush, Offset(x, size.height - h), Size(barW, h), CornerRadius(barW / 2))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            labels.forEach {
                Text(it, color = labelColor, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "Sil",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}
