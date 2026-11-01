package com.lifeos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeos.app.ui.theme.Purple
import java.time.YearMonth
import java.time.format.DateTimeFormatter

val WeekDaysShort = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

fun monthTitle(month: YearMonth): String =
    month.format(DateTimeFormatter.ofPattern("MMMM yyyy", TR)).replaceFirstChar { it.titlecase(TR) }

@Composable
fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Önceki ay") }
        Text(
            monthTitle(month),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNext) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sonraki ay") }
    }
}

/**
 * Pazartesi ile başlayan aylık takvim ızgarası.
 * [fill]: günü tamamen boyamak için renk (ör. alışkanlık yapıldı),
 * [dots]: gün altında gösterilecek nokta sayısı (ör. görev sayısı).
 */
@Composable
fun MonthGrid(
    month: YearMonth,
    today: Long,
    selected: Long?,
    onDayClick: ((Long) -> Unit)?,
    fill: (Long) -> Color? = { null },
    dots: (Long) -> Int = { 0 },
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - 1
    val days = month.lengthOfMonth()
    val cells = leading + days
    val rows = (cells + 6) / 7
    Column {
        Row(Modifier.fillMaxWidth()) {
            WeekDaysShort.forEach {
                Text(
                    it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val index = r * 7 + c
                    val dayNum = index - leading + 1
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..days) {
                            val epoch = first.plusDays((dayNum - 1).toLong()).toEpochDay()
                            val f = fill(epoch)
                            val isSel = selected == epoch
                            val isToday = today == epoch
                            var m = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                            m = when {
                                f != null -> m.background(f)
                                isSel -> m.background(MaterialTheme.colorScheme.primaryContainer)
                                else -> m
                            }
                            if (isToday) m = m.border(1.5.dp, Purple, RoundedCornerShape(10.dp))
                            if (onDayClick != null) m = m.clickable { onDayClick(epoch) }
                            Column(m, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    "$dayNum",
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday || isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (f != null) Color.White else MaterialTheme.colorScheme.onSurface,
                                )
                                val n = dots(epoch).coerceAtMost(3)
                                if (n > 0) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(n) {
                                            Box(Modifier.size(4.dp).clip(CircleShape).background(if (f != null) Color.White else Purple))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
