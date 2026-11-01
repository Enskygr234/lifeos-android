package com.lifeos.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.data.TaskEntity
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.components.EmptyState
import com.lifeos.app.ui.components.MonthGrid
import com.lifeos.app.ui.components.MonthHeader
import com.lifeos.app.ui.components.SectionCard
import com.lifeos.app.ui.components.WeekDaysShort
import com.lifeos.app.ui.components.formatDay
import com.lifeos.app.ui.theme.Purple
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(vm: AppViewModel) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    var weekly by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(today) }
    var monthStr by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val month = YearMonth.parse(monthStr)
    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    val byDay = remember(tasks) { tasks.filter { it.dueDate != null }.groupBy { it.dueDate!! } }
    val selDate = LocalDate.ofEpochDay(selected)
    val weekStart = selDate.minusDays((selDate.dayOfWeek.value - 1).toLong()).toEpochDay()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = !weekly, onClick = { weekly = false }, label = { Text("Aylık") },
                        modifier = Modifier.testTag("cal_month"))
                    FilterChip(selected = weekly, onClick = { weekly = true }, label = { Text("Haftalık") },
                        modifier = Modifier.testTag("cal_week"))
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { selected = today; monthStr = YearMonth.now().toString() }) { Text("Bugün") }
                }
            }
            item {
                SectionCard {
                    AnimatedContent(weekly, label = "calMode") { isWeekly ->
                        if (!isWeekly) {
                            Column {
                                MonthHeader(
                                    month,
                                    onPrev = { monthStr = month.minusMonths(1).toString() },
                                    onNext = { monthStr = month.plusMonths(1).toString() },
                                )
                                MonthGrid(
                                    month = month,
                                    today = today,
                                    selected = selected,
                                    onDayClick = { selected = it },
                                    dots = { d -> byDay[d]?.count { !it.completed } ?: 0 },
                                )
                            }
                        } else {
                            Column {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { selected -= 7 }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Önceki hafta") }
                                    Text(
                                        formatDay(weekStart, "d MMM") + " – " + formatDay(weekStart + 6, "d MMM yyyy"),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    IconButton(onClick = { selected += 7 }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sonraki hafta") }
                                }
                                (0 until 7).forEach { i ->
                                    val d = weekStart + i
                                    val dayTasks = byDay[d].orEmpty()
                                    val isSel = d == selected
                                    Row(
                                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                            .then(if (isSel) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier)
                                            .clickable { selected = d }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(Modifier.width(52.dp)) {
                                            Text(WeekDaysShort[i], style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatDay(d, "d"), fontWeight = FontWeight.Bold,
                                                color = if (d == today) Purple else MaterialTheme.colorScheme.onSurface)
                                        }
                                        Text(
                                            if (dayTasks.isEmpty()) "—"
                                            else dayTasks.joinToString(" • ") { it.title },
                                            maxLines = 2,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (dayTasks.isNotEmpty()) {
                                            Text("${dayTasks.count { it.completed }}/${dayTasks.size}",
                                                style = MaterialTheme.typography.labelMedium, color = Purple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text(formatDay(selected, "d MMMM yyyy, EEEE"), style = MaterialTheme.typography.titleMedium)
            }
            val dayTasks = byDay[selected].orEmpty()
            if (dayTasks.isEmpty()) {
                item { EmptyState(Icons.Filled.EventAvailable, "Bu gün için görev yok.") }
            }
            items(dayTasks, key = { it.id }) { t ->
                TaskRow(
                    task = t, today = today,
                    onToggle = { vm.setTaskCompleted(t, !t.completed) },
                    onClick = { editing = t; showEditor = true },
                    onDelete = null,
                )
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showEditor = true },
            containerColor = Purple,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) { Icon(Icons.Filled.Add, "Bu güne görev ekle") }
    }

    if (showEditor) {
        TaskEditorDialog(
            initial = editing,
            defaultDate = selected,
            categories = (DefaultCategories + tasks.map { it.category }).distinct(),
            onDismiss = { showEditor = false },
            onSave = { vm.saveTask(it); showEditor = false },
        )
    }
}
