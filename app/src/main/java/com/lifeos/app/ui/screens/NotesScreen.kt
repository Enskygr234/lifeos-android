package com.lifeos.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.data.NoteEntity
import com.lifeos.app.data.QUICK_NOTE_CATEGORY
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.components.ConfirmDialog
import com.lifeos.app.ui.components.EmptyState
import com.lifeos.app.ui.components.Pill
import com.lifeos.app.ui.components.TR
import com.lifeos.app.ui.theme.Blue
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date

private val NoteCategories = listOf("Genel", "Kişisel", "İş", "Fikirler", QUICK_NOTE_CATEGORY)
private const val FAVORITES = "★ Favoriler"

@Composable
fun NotesScreen(vm: AppViewModel) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<NoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val categories = remember(notes) { (NoteCategories + notes.map { it.category }).distinct() }
    val q = query.trim().lowercase(TR)
    val visible = notes.filter { n ->
        val catOk = when (filter) {
            null -> true
            FAVORITES -> n.favorite
            else -> n.category == filter
        }
        catOk && (q.isEmpty() || n.title.lowercase(TR).contains(q) || n.content.lowercase(TR).contains(q))
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    placeholder = { Text("Notlarda ara…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("note_search"),
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Tümü") })
                    FilterChip(selected = filter == FAVORITES, onClick = { filter = if (filter == FAVORITES) null else FAVORITES }, label = { Text(FAVORITES) })
                    categories.forEach { c ->
                        FilterChip(selected = filter == c, onClick = { filter = if (filter == c) null else c }, label = { Text(c) })
                    }
                }
            }
            if (visible.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.StickyNote2,
                        if (notes.isEmpty()) "Henüz not yok. + ile ilk notunu oluştur." else "Aramayla eşleşen not bulunamadı.",
                    )
                }
            }
            items(visible, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { editing = note; showEditor = true },
                    onFavorite = { vm.setFavorite(note, !note.favorite) },
                    onDelete = { toDelete = note },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showEditor = true },
            containerColor = Purple,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).testTag("fab_add_note"),
        ) { Icon(Icons.Filled.Add, "Not ekle") }
    }

    if (showEditor) {
        NoteEditor(
            initial = editing,
            categories = categories,
            onDismiss = { showEditor = false },
            onSave = { vm.saveNote(it); showEditor = false },
        )
    }
    toDelete?.let { n ->
        ConfirmDialog(
            title = "Notu sil",
            text = "\"${n.title.ifBlank { "Başlıksız not" }}\" kalıcı olarak silinsin mi?",
            onConfirm = { vm.deleteNote(n) },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fmt = remember { SimpleDateFormat("d MMM yyyy HH:mm", TR) }
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    note.title.ifBlank { "Başlıksız not" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (note.favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        if (note.favorite) "Favoriden çıkar" else "Favorilere ekle",
                        tint = if (note.favorite) Warning else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.DeleteOutline, "Sil", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (note.content.isNotBlank()) {
                Text(
                    note.content,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(note.category, Blue)
                Spacer(Modifier.weight(1f))
                Text(
                    fmt.format(Date(note.updatedAt)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteEditor(
    initial: NoteEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(initial?.content ?: "") }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: "Genel") }
    var favorite by rememberSaveable { mutableStateOf(initial?.favorite ?: false) }
    var newCat by rememberSaveable { mutableStateOf("") }

    fun save() {
        if (title.isBlank() && content.isBlank()) {
            onDismiss(); return
        }
        val base = initial ?: NoteEntity(title = "", content = "")
        onSave(base.copy(title = title.trim(), content = content.trim(), category = category, favorite = favorite))
    }

    Dialog(onDismissRequest = { save() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kaydetmeden kapat") }
                    Text(
                        if (initial == null) "Yeni not" else "Notu düzenle",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { favorite = !favorite }) {
                        Icon(if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder, "Favori",
                            tint = if (favorite) Warning else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { save() }, modifier = Modifier.testTag("note_save")) {
                        Icon(Icons.Filled.Check, "Kaydet", tint = Purple)
                    }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (categories + category).distinct().forEach { c ->
                            FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newCat, onValueChange = { newCat = it },
                            label = { Text("Yeni kategori") }, singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            if (newCat.isNotBlank()) { category = newCat.trim(); newCat = "" }
                        }) { Icon(Icons.Filled.Add, "Kategori ekle") }
                    }
                    val colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Başlık", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        colors = colors,
                        modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
                    )
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Notunu yaz…") },
                        colors = colors,
                        modifier = Modifier.fillMaxWidth().height(400.dp).testTag("note_content_input"),
                    )
                }
            }
        }
    }
}
