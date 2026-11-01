package com.lifeos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.data.QUICK_NOTE_CATEGORY
import com.lifeos.app.data.TaskEntity
import com.lifeos.app.domain.LifeLogic
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.Dest
import com.lifeos.app.ui.components.EmptyState
import com.lifeos.app.ui.components.ProgressRing
import com.lifeos.app.ui.components.SectionCard
import com.lifeos.app.ui.components.formatDay
import com.lifeos.app.ui.theme.Blue
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.ui.theme.Warning
import java.time.LocalTime

@Composable
fun HomeScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val goals by vm.goals.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val logs by vm.habitLogs.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()

    val todayTasks = LifeLogic.todayTasks(tasks, today)
    val todayGoals = goals.filter { it.date == today }
    val stat = LifeLogic.dayStat(today, tasks, goals, habits, logs)
    val habitsDone = habits.count { h -> logs.any { it.habitId == h.id && it.date == today } }
    val overdue = tasks.count { LifeLogic.isOverdue(it, today) }
    val bestStreak = habits.maxOfOrNull { h ->
        LifeLogic.currentStreak(logs.filter { it.habitId == h.id }.map { it.date }.toSet(), today)
    } ?: 0
    val quickNotes = notes.filter { it.category == QUICK_NOTE_CATEGORY }.sortedByDescending { it.updatedAt }.take(3)

    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var goalText by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.testTag("home_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ---- Hero kart ----
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF1E3A8A))))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        val hour = LocalTime.now().hour
                        val greet = when (hour) {
                            in 5..11 -> "Günaydın"
                            in 12..17 -> "İyi günler"
                            else -> "İyi akşamlar"
                        }
                        Text(greet, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        Text(formatDay(today, "d MMMM yyyy, EEEE"), color = Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${stat.done}/${stat.total} öğe tamamlandı",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    ProgressRing(stat.rate, size = 104.dp, stroke = 10.dp, subText = "bugün", textColor = Color.White)
                }
            }
        }
        // ---- Günün özeti ----
        item {
            SectionCard(title = "Günün özeti", icon = Icons.Filled.Insights) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryTile("Açık görev", todayTasks.count { !it.completed }.toString(), Purple, Modifier.weight(1f))
                    SummaryTile("Alışkanlık", "$habitsDone/${habits.size}", Blue, Modifier.weight(1f))
                    SummaryTile("Hedef", "${todayGoals.count { it.completed }}/${todayGoals.size}", Color(0xFF22D3EE), Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                val lines = buildList {
                    if (overdue > 0) add("⚠️ $overdue gecikmiş görevin var.")
                    if (bestStreak > 0) add("🔥 En uzun aktif serin: $bestStreak gün.")
                    if (habits.isNotEmpty() && habitsDone == habits.size) add("✅ Bugünkü tüm alışkanlıklar tamam!")
                    if (isEmpty()) add("Güne bir görev veya hedef ekleyerek başla.")
                }
                lines.forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // ---- Günlük görevler ----
        item {
            SectionCard(
                title = "Günlük görevler",
                icon = Icons.Filled.TaskAlt,
                action = {
                    IconButton(onClick = { editing = null; showEditor = true }, modifier = Modifier.testTag("home_add_task")) {
                        Icon(Icons.Filled.Add, "Görev ekle")
                    }
                },
            ) {
                if (todayTasks.isEmpty()) {
                    EmptyState(Icons.Filled.TaskAlt, "Bugün için görev yok.")
                } else {
                    todayTasks.take(6).forEach { t ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .clickable { editing = t; showEditor = true },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = t.completed,
                                onCheckedChange = { vm.setTaskCompleted(t, it) },
                                colors = CheckboxDefaults.colors(checkedColor = Purple),
                            )
                            Text(
                                t.title,
                                Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (t.completed) TextDecoration.LineThrough else null,
                            )
                            if (LifeLogic.isOverdue(t, today)) {
                                Text("Gecikmiş", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            } else t.dueTime?.let {
                                Text(LifeLogic.formatMinutes(it), style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (todayTasks.size > 6) {
                        TextButton(onClick = { onOpen(Dest.Tasks.route) }) { Text("Tümünü gör (${todayTasks.size})") }
                    }
                }
            }
        }
        // ---- Günlük hedefler ----
        item {
            SectionCard(title = "Günlük hedefler", icon = Icons.Filled.Flag) {
                todayGoals.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = g.completed,
                            onCheckedChange = { vm.toggleGoal(g) },
                            colors = CheckboxDefaults.colors(checkedColor = Blue),
                        )
                        Text(
                            g.title, Modifier.weight(1f),
                            textDecoration = if (g.completed) TextDecoration.LineThrough else null,
                        )
                        IconButton(onClick = { vm.deleteGoal(g) }) {
                            Icon(Icons.Filled.Close, "Hedefi sil", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        placeholder = { Text("Bugünün hedefi…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { vm.addGoal(goalText); goalText = "" }),
                        modifier = Modifier.weight(1f).testTag("goal_input"),
                    )
                    IconButton(onClick = { vm.addGoal(goalText); goalText = "" }, modifier = Modifier.testTag("goal_add")) {
                        Icon(Icons.Filled.Add, "Hedef ekle")
                    }
                }
            }
        }
        // ---- Hızlı notlar ----
        item {
            SectionCard(
                title = "Hızlı notlar",
                icon = Icons.Filled.EditNote,
                action = { TextButton(onClick = { onOpen(Dest.Notes.route) }) { Text("Tümü") } },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("Aklındakini yaz…") },
                        maxLines = 3,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { vm.addQuickNote(noteText); noteText = "" }) {
                        Icon(Icons.Filled.Add, "Not kaydet")
                    }
                }
                quickNotes.forEach { n ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.padding(top = 7.dp).size(6.dp).clip(RoundedCornerShape(3.dp)).background(Warning))
                        Spacer(Modifier.width(8.dp))
                        Text(n.content, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = Warning, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Tüm veriler yalnızca bu cihazda saklanır. İnternet gerekmez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showEditor) {
        TaskEditorDialog(
            initial = editing,
            defaultDate = today,
            categories = (DefaultCategories + tasks.map { it.category }).distinct(),
            onDismiss = { showEditor = false },
            onSave = { vm.saveTask(it); showEditor = false },
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.12f)).padding(12.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
