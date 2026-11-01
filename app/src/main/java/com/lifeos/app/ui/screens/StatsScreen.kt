package com.lifeos.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.domain.LifeLogic
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.components.BarChart
import com.lifeos.app.ui.components.EmptyState
import com.lifeos.app.ui.components.LinearMeter
import com.lifeos.app.ui.components.ProgressRing
import com.lifeos.app.ui.components.SectionCard
import com.lifeos.app.ui.components.formatDay
import com.lifeos.app.ui.theme.Blue
import com.lifeos.app.ui.theme.Cyan
import com.lifeos.app.ui.theme.HabitColors
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.ui.theme.Success

@Composable
fun StatsScreen(vm: AppViewModel) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val goals by vm.goals.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val logs by vm.habitLogs.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()

    val week = LifeLogic.lastNDays(today, 7).map { LifeLogic.dayStat(it, tasks, goals, habits, logs) }
    val weekRates = week.map { it.rate }
    val weekLabels = week.map { formatDay(it.day, "EEE") }
    val todayStat = week.last()
    val withData = week.filter { it.total > 0 }
    val weekAvg = if (withData.isEmpty()) 0f else withData.map { it.rate }.average().toFloat()

    // Son 4 hafta ortalaması
    val fourWeeks = (3 downTo 0).map { w ->
        val days = (0 until 7).map { today - w * 7 - 6 + it }
        val stats = days.map { LifeLogic.dayStat(it, tasks, goals, habits, logs) }.filter { it.total > 0 }
        val avg = if (stats.isEmpty()) 0f else stats.map { it.rate }.average().toFloat()
        avg to formatDay(days.first(), "d MMM")
    }

    val completedTasks = tasks.count { it.completed }
    val weekCompleted = tasks.count { it.completed && it.completedDate != null && it.completedDate > today - 7 }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionCard(title = "Günlük başarı oranı", icon = Icons.Filled.ShowChart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(todayStat.rate, size = 110.dp, subText = "bugün")
                    Spacer(Modifier.padding(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Metric("Haftalık ortalama", "%" + (weekAvg * 100).toInt(), Purple)
                        Metric("Bugün tamamlanan", "${todayStat.done}/${todayStat.total}", Blue)
                        Metric("Bu hafta biten görev", weekCompleted.toString(), Success)
                    }
                }
            }
        }
        item {
            SectionCard(title = "Haftalık ilerleme", icon = Icons.Filled.BarChart) {
                Text("Son 7 gün – günlük tamamlanma yüzdesi", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                BarChart(weekRates, weekLabels)
            }
        }
        item {
            SectionCard(title = "Son 4 hafta", icon = Icons.Filled.ShowChart) {
                BarChart(fourWeeks.map { it.first }, fourWeeks.map { it.second }, height = 120.dp)
            }
        }
        item {
            SectionCard(title = "Görevler", icon = Icons.Filled.TaskAlt) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    BigNumber("Toplam", tasks.size.toString(), Purple)
                    BigNumber("Tamamlanan", completedTasks.toString(), Success)
                    BigNumber("Gecikmiş", tasks.count { LifeLogic.isOverdue(it, today) }.toString(), MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(10.dp))
                LinearMeter(if (tasks.isEmpty()) 0f else completedTasks.toFloat() / tasks.size)
                Spacer(Modifier.height(10.dp))
                val byCat = tasks.groupBy { it.category }.toList().sortedByDescending { it.second.size }.take(5)
                byCat.forEach { (cat, list) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat, Modifier.weight(0.35f), style = MaterialTheme.typography.bodyMedium)
                        LinearMeter(list.count { it.completed }.toFloat() / list.size, Modifier.weight(0.5f), color = Cyan)
                        Text("  ${list.count { it.completed }}/${list.size}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        item {
            SectionCard(title = "Alışkanlıklar (son 7 gün)", icon = Icons.Filled.Loop) {
                if (habits.isEmpty()) {
                    EmptyState(Icons.Filled.Loop, "İstatistik için alışkanlık ekleyin.")
                }
                habits.forEach { h ->
                    val days = logs.filter { it.habitId == h.id }.map { it.date }.toSet()
                    val rate = LifeLogic.habitRate(h, days, today, 7)
                    Column(Modifier.padding(vertical = 5.dp)) {
                        Row {
                            Text(h.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("%" + (rate * 100).toInt() + " • 🔥" + LifeLogic.currentStreak(days, today),
                                style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearMeter(rate, color = HabitColors[h.colorIndex.mod(HabitColors.size)])
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color) {
    Column(
        Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BigNumber(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
