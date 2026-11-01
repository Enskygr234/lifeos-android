package com.lifeos.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.data.TaskEntity
import com.lifeos.app.domain.LifeLogic
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.components.ConfirmDialog
import com.lifeos.app.ui.components.EmptyState
import com.lifeos.app.ui.components.Pill
import com.lifeos.app.ui.components.PriorityLabels
import com.lifeos.app.ui.components.formatDay
import com.lifeos.app.ui.components.priorityColor
import com.lifeos.app.ui.components.relativeDay
import com.lifeos.app.ui.theme.Blue
import com.lifeos.app.ui.theme.Danger
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.work.ReminderScheduler

val DefaultCategories = listOf("Genel", "İş", "Kişisel", "Sağlık", "Okul", "Alışveriş")

private enum class TaskFilter(val label: String) {
    All("Tümü"), Today("Bugün"), Upcoming("Yaklaşan"), Overdue("Gecikmiş"), Done("Tamamlanan")
}

@Composable
fun TasksScreen(vm: AppViewModel) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(TaskFilter.All) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val categories = remember(tasks) { (DefaultCategories + tasks.map { it.category }).distinct() }
    val visible = tasks.filter { t ->
        val f = when (filter) {
            TaskFilter.All -> true
            TaskFilter.Today -> t.dueDate == today
            TaskFilter.Upcoming -> !t.completed && t.dueDate != null && t.dueDate > today
            TaskFilter.Overdue -> LifeLogic.isOverdue(t, today)
            TaskFilter.Done -> t.completed
        }
        f && (category == null || t.category == category)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskFilter.entries.forEach { f ->
                        FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.label) })
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = category == null, onClick = { category = null }, label = { Text("Tüm kategoriler") })
                    categories.forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = if (category == c) null else c }, label = { Text(c) })
                    }
                }
            }
            if (visible.isEmpty()) {
                item { EmptyState(Icons.Filled.TaskAlt, "Bu filtrede görev yok.\nSağ alttaki + ile yeni görev ekleyin.") }
            }
            items(visible, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    today = today,
                    onToggle = { vm.setTaskCompleted(task, !task.completed) },
                    onClick = { editing = task; showEditor = true },
                    onDelete = { toDelete = task },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showEditor = true },
            containerColor = Purple,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).testTag("fab_add_task"),
        ) { Icon(Icons.Filled.Add, "Görev ekle") }
    }

    if (showEditor) {
        TaskEditorDialog(
            initial = editing,
            defaultDate = if (filter == TaskFilter.Today) today else null,
            categories = categories,
            onDismiss = { showEditor = false },
            onSave = { vm.saveTask(it); showEditor = false },
        )
    }
    toDelete?.let { t ->
        ConfirmDialog(
            title = "Görevi sil",
            text = "\"${t.title}\" kalıcı olarak silinsin mi?",
            onConfirm = { vm.deleteTask(t) },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
fun TaskRow(
    task: TaskEntity,
    today: Long,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val overdue = LifeLogic.isOverdue(task, today)
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Purple),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(PriorityLabels[task.priority.coerceIn(0, 2)], priorityColor(task.priority))
                    Pill(task.category, Blue)
                    task.dueDate?.let { d ->
                        val time = task.dueTime?.let { " " + LifeLogic.formatMinutes(it) } ?: ""
                        Text(
                            relativeDay(d, today) + time,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (overdue) Danger else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (task.reminder && task.dueDate != null) {
                        Icon(Icons.Filled.Alarm, "Hatırlatıcı", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.DeleteOutline, "Sil", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    initial: TaskEntity?,
    defaultDate: Long?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit,
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(initial?.description ?: "") }
    var priority by rememberSaveable { mutableStateOf(initial?.priority ?: 1) }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: "Genel") }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf(initial?.dueDate ?: defaultDate) }
    var dueTime by rememberSaveable { mutableStateOf(initial?.dueTime) }
    var reminder by rememberSaveable { mutableStateOf(initial?.reminder ?: false) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Yeni görev" else "Görevi düzenle") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Başlık") },
                    isError = titleError,
                    supportingText = { if (titleError) Text("Başlık boş olamaz") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (isteğe bağlı)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Öncelik", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityLabels.forEachIndexed { i, l ->
                        FilterChip(selected = priority == i, onClick = { priority = i }, label = { Text(l) })
                    }
                }
                Text("Kategori", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (categories + listOf(category)).distinct().forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Yeni kategori") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        val c = customCategory.trim()
                        if (c.isNotEmpty()) { category = c; customCategory = "" }
                    }) { Icon(Icons.Filled.Add, "Kategori ekle") }
                }
                Text("Son tarih", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDate = true }) {
                        Icon(Icons.Filled.Event, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(dueDate?.let { formatDay(it) } ?: "Tarih seç")
                    }
                    if (dueDate != null) {
                        OutlinedButton(onClick = { showTime = true }) {
                            Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(dueTime?.let { LifeLogic.formatMinutes(it) } ?: "Saat")
                        }
                        IconButton(onClick = { dueDate = null; dueTime = null; reminder = false }) {
                            Icon(Icons.Filled.Close, "Tarihi temizle")
                        }
                    }
                }
                AnimatedVisibility(dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Hatırlatıcı", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Saat seçilmezse 09:00'da bildirim gönderilir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = reminder, onCheckedChange = {
                            reminder = it
                            if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !ReminderScheduler.canNotify(context)
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        val base = initial ?: TaskEntity(title = "")
                        onSave(
                            base.copy(
                                title = title.trim(),
                                description = description.trim(),
                                priority = priority,
                                category = category,
                                dueDate = dueDate,
                                dueTime = if (dueDate == null) null else dueTime,
                                reminder = dueDate != null && reminder,
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("task_save"),
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )

    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = (dueDate ?: java.time.LocalDate.now().toEpochDay()) * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dueDate = Math.floorDiv(it, 86_400_000L) }
                    showDate = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Vazgeç") } },
        ) { DatePicker(state = state) }
    }
    if (showTime) {
        val m = dueTime ?: ReminderScheduler.DEFAULT_REMINDER_MINUTES
        val state = rememberTimePickerState(initialHour = m / 60, initialMinute = m % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Saat seç") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = { dueTime = state.hour * 60 + state.minute; showTime = false }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { dueTime = null; showTime = false }) { Text("Saati kaldır") }
            },
        )
    }
}
