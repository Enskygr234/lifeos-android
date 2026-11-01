package com.lifeos.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.data.HabitEntity
import com.lifeos.app.domain.LifeLogic
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.components.ConfirmDialog
import com.lifeos.app.ui.components.EmptyState
import com.lifeos.app.ui.components.LinearMeter
import com.lifeos.app.ui.components.MonthGrid
import com.lifeos.app.ui.components.MonthHeader
import com.lifeos.app.ui.components.WeekDaysShort
import com.lifeos.app.ui.theme.HabitColors
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.ui.theme.Warning
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HabitsScreen(vm: AppViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    val logs by vm.habitLogs.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<HabitEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<HabitEntity?>(null) }
    var expandedId by rememberSaveable { mutableStateOf<Long?>(null) }

    val logsByHabit = remember(logs) { logs.groupBy({ it.habitId }, { it.date }).mapValues { it.value.toSet() } }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (habits.isEmpty()) {
                item { EmptyState(Icons.Filled.Loop, "Henüz alışkanlık yok.\n+ ile ilk alışkanlığını oluştur.") }
            } else {
                item {
                    val doneToday = habits.count { today in (logsByHabit[it.id] ?: emptySet()) }
                    Text(
                        "Bugün $doneToday/${habits.size} alışkanlık tamamlandı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearMeter(if (habits.isEmpty()) 0f else doneToday.toFloat() / habits.size)
                }
            }
            items(habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    days = logsByHabit[habit.id] ?: emptySet(),
                    today = today,
                    expanded = expandedId == habit.id,
                    onExpand = { expandedId = if (expandedId == habit.id) null else habit.id },
                    onToggle = { day -> vm.toggleHabit(habit.id, day) },
                    onEdit = { editing = habit; showEditor = true },
                    onDelete = { toDelete = habit },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showEditor = true },
            containerColor = Purple,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).testTag("fab_add_habit"),
        ) { Icon(Icons.Filled.Add, "Alışkanlık ekle") }
    }

    if (showEditor) {
        HabitEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { name, color ->
                val h = editing?.copy(name = name, colorIndex = color)
                    ?: HabitEntity(name = name, colorIndex = color, createdDate = today)
                vm.saveHabit(h)
                showEditor = false
            },
        )
    }
    toDelete?.let { h ->
        ConfirmDialog(
            title = "Alışkanlığı sil",
            text = "\"${h.name}\" ve tüm geçmiş kayıtları silinsin mi?",
            onConfirm = { vm.deleteHabit(h) },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun HabitCard(
    habit: HabitEntity,
    days: Set<Long>,
    today: Long,
    expanded: Boolean,
    onExpand: () -> Unit,
    onToggle: (Long) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = HabitColors[habit.colorIndex.mod(HabitColors.size)]
    val doneToday = today in days
    val streak = LifeLogic.currentStreak(days, today)
    val best = LifeLogic.bestStreak(days)
    val btnColor by animateColorAsState(if (doneToday) color else Color.Transparent, label = "habitBtn")
    val todayDate = LocalDate.ofEpochDay(today)
    val weekStart = todayDate.minusDays((todayDate.dayOfWeek.value - 1).toLong()).toEpochDay()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable(onClick = onExpand)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = Warning, modifier = Modifier.size(16.dp))
                        Text(" $streak gün seri", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("  •  En iyi: $best", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(btnColor)
                        .border(2.dp, color, CircleShape)
                        .clickable { onToggle(today) }
                        .testTag("habit_toggle_${habit.id}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, if (doneToday) "Yapıldı" else "Bugün yap",
                        tint = if (doneToday) Color.White else color)
                }
            }
            Spacer(Modifier.height(12.dp))
            // Bu hafta
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (0 until 7).forEach { i ->
                    val d = weekStart + i
                    val done = d in days
                    val future = d > today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(WeekDaysShort[i], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.size(28.dp).clip(CircleShape)
                                .background(if (done) color else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .then(if (!future) Modifier.clickable { onToggle(d) } else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().clickable(onClick = onExpand).padding(top = 6.dp), horizontalArrangement = Arrangement.Center) {
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, if (expanded) "Daralt" else "Ayrıntılar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AnimatedVisibility(expanded) {
                var month by remember { mutableStateOf(YearMonth.now()) }
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Stat("Toplam", days.size.toString())
                        Stat("7 gün", "%" + (LifeLogic.habitRate(habit, days, today, 7) * 100).toInt())
                        Stat("30 gün", "%" + (LifeLogic.habitRate(habit, days, today, 30) * 100).toInt())
                    }
                    Spacer(Modifier.height(8.dp))
                    MonthHeader(month, { month = month.minusMonths(1) }, { month = month.plusMonths(1) })
                    MonthGrid(
                        month = month,
                        today = today,
                        selected = null,
                        onDayClick = { d -> if (d <= today) onToggle(d) },
                        fill = { d -> if (d in days) color else null },
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Düzenle")
                        }
                        TextButton(onClick = onDelete) {
                            Icon(Icons.Filled.DeleteOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(4.dp)); Text("Sil", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HabitEditorDialog(
    initial: HabitEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var color by rememberSaveable { mutableStateOf(initial?.colorIndex ?: 0) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Yeni alışkanlık" else "Alışkanlığı düzenle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = false },
                    label = { Text("Ad (ör. 2 L su iç)") },
                    isError = error,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("habit_name_input"),
                )
                Text("Renk", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HabitColors.forEachIndexed { i, c ->
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(c)
                                .border(if (color == i) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .clickable { color = i }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) error = true else onSave(name.trim(), color)
            }, modifier = Modifier.testTag("habit_save")) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}
