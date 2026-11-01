package com.lifeos.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.LifeOSApp
import com.lifeos.app.data.AppSettings
import com.lifeos.app.data.BackupCodec
import com.lifeos.app.data.GoalEntity
import com.lifeos.app.data.HabitEntity
import com.lifeos.app.data.HabitLogEntity
import com.lifeos.app.data.NoteEntity
import com.lifeos.app.data.QUICK_NOTE_CATEGORY
import com.lifeos.app.data.TaskEntity
import com.lifeos.app.work.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val lifeApp = application as LifeOSApp
    private val repo = lifeApp.repository
    private val store = lifeApp.settingsStore

    private val _today = MutableStateFlow(LocalDate.now().toEpochDay())
    val today: StateFlow<Long> = _today.asStateFlow()

    private fun <T> kotlinx.coroutines.flow.Flow<T>.state(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.Eagerly, initial)

    val tasks: StateFlow<List<TaskEntity>> = repo.tasks.state(emptyList())
    val goals: StateFlow<List<GoalEntity>> = repo.goals.state(emptyList())
    val habits: StateFlow<List<HabitEntity>> = repo.habits.state(emptyList())
    val habitLogs: StateFlow<List<HabitLogEntity>> = repo.habitLogs.state(emptyList())
    val notes: StateFlow<List<NoteEntity>> = repo.notes.state(emptyList())
    val settings: StateFlow<AppSettings> = store.settings.state(AppSettings())

    fun refreshDay() {
        _today.value = LocalDate.now().toEpochDay()
    }

    // ---- Görevler ----
    fun saveTask(task: TaskEntity) = viewModelScope.launch { repo.saveTask(task) }
    fun setTaskCompleted(task: TaskEntity, completed: Boolean) =
        viewModelScope.launch { repo.setTaskCompleted(task, completed, _today.value) }
    fun deleteTask(task: TaskEntity) = viewModelScope.launch { repo.deleteTask(task) }

    // ---- Hedefler ----
    fun addGoal(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch { repo.addGoal(t, _today.value) }
    }
    fun toggleGoal(goal: GoalEntity) =
        viewModelScope.launch { repo.updateGoal(goal.copy(completed = !goal.completed)) }
    fun deleteGoal(goal: GoalEntity) = viewModelScope.launch { repo.deleteGoal(goal) }

    // ---- Alışkanlıklar ----
    fun saveHabit(habit: HabitEntity) = viewModelScope.launch { repo.saveHabit(habit) }
    fun deleteHabit(habit: HabitEntity) = viewModelScope.launch { repo.deleteHabit(habit) }
    fun toggleHabit(habitId: Long, day: Long) = viewModelScope.launch { repo.toggleHabit(habitId, day) }

    // ---- Notlar ----
    fun saveNote(note: NoteEntity) = viewModelScope.launch { repo.saveNote(note) }
    fun deleteNote(note: NoteEntity) = viewModelScope.launch { repo.deleteNote(note) }
    fun setFavorite(note: NoteEntity, favorite: Boolean) =
        viewModelScope.launch { repo.setFavorite(note, favorite) }

    fun addQuickNote(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        val title = t.lineSequence().first().take(40)
        viewModelScope.launch {
            repo.saveNote(NoteEntity(title = title, content = t, category = QUICK_NOTE_CATEGORY))
        }
    }

    // ---- Ayarlar ----
    fun setTheme(mode: String) = viewModelScope.launch { store.setTheme(mode) }

    fun setTaskReminders(enabled: Boolean) = viewModelScope.launch {
        store.setTaskReminders(enabled)
        repo.rescheduleAllReminders()
    }

    fun setDailySummary(enabled: Boolean, hour: Int) = viewModelScope.launch {
        store.setDailySummary(enabled, hour)
        ReminderScheduler.scheduleDailySummary(getApplication(), enabled, hour)
    }

    // ---- Yedekleme ----
    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        BackupCodec.encode(repo.snapshot())
    }

    /** Başarılıysa null, aksi halde hata mesajı döner. */
    suspend fun importJson(json: String): String? = withContext(Dispatchers.IO) {
        try {
            val data = BackupCodec.decode(json)
            repo.replaceAll(data)
            null
        } catch (e: IllegalArgumentException) {
            e.message ?: "Geçersiz yedek dosyası."
        } catch (e: Exception) {
            "Geri yükleme başarısız: ${e.message}"
        }
    }
}
