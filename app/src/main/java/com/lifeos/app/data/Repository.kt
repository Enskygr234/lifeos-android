package com.lifeos.app.data

import android.content.Context
import androidx.room.withTransaction
import com.lifeos.app.work.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class Repository(
    private val context: Context,
    private val db: AppDatabase,
    private val settingsStore: SettingsStore,
) {
    private val taskDao = db.taskDao()
    private val goalDao = db.goalDao()
    private val habitDao = db.habitDao()
    private val noteDao = db.noteDao()

    val tasks: Flow<List<TaskEntity>> = taskDao.observeAll()
    val goals: Flow<List<GoalEntity>> = goalDao.observeAll()
    val habits: Flow<List<HabitEntity>> = habitDao.observeAll()
    val habitLogs: Flow<List<HabitLogEntity>> = habitDao.observeLogs()
    val notes: Flow<List<NoteEntity>> = noteDao.observeAll()

    // ---- Görevler ----
    suspend fun getTask(id: Long): TaskEntity? = taskDao.get(id)
    suspend fun getAllTasks(): List<TaskEntity> = taskDao.getAll()

    suspend fun saveTask(task: TaskEntity): TaskEntity {
        val saved = if (task.id == 0L) {
            task.copy(id = taskDao.insert(task))
        } else {
            taskDao.update(task); task
        }
        scheduleIfAllowed(saved)
        return saved
    }

    suspend fun setTaskCompleted(task: TaskEntity, completed: Boolean, today: Long) {
        val updated = task.copy(completed = completed, completedDate = if (completed) today else null)
        taskDao.update(updated)
        scheduleIfAllowed(updated)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.delete(task)
        ReminderScheduler.cancelTask(context, task.id)
    }

    private suspend fun scheduleIfAllowed(task: TaskEntity) {
        if (settingsStore.current().taskReminders) {
            ReminderScheduler.scheduleTask(context, task)
        } else {
            ReminderScheduler.cancelTask(context, task.id)
        }
    }

    suspend fun rescheduleAllReminders() {
        val enabled = settingsStore.current().taskReminders
        taskDao.getAll().forEach {
            if (enabled) ReminderScheduler.scheduleTask(context, it)
            else ReminderScheduler.cancelTask(context, it.id)
        }
    }

    // ---- Hedefler ----
    suspend fun addGoal(title: String, date: Long) {
        goalDao.insert(GoalEntity(title = title, date = date))
    }

    suspend fun updateGoal(goal: GoalEntity) = goalDao.update(goal)
    suspend fun deleteGoal(goal: GoalEntity) = goalDao.delete(goal)

    // ---- Alışkanlıklar ----
    suspend fun saveHabit(habit: HabitEntity) {
        if (habit.id == 0L) habitDao.insert(habit) else habitDao.update(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.delete(habit)

    suspend fun toggleHabit(habitId: Long, date: Long) {
        if (habitDao.countLog(habitId, date) > 0) habitDao.deleteLog(habitId, date)
        else habitDao.insertLog(HabitLogEntity(habitId, date))
    }

    // ---- Notlar ----
    suspend fun saveNote(note: NoteEntity) {
        val stamped = note.copy(updatedAt = System.currentTimeMillis())
        if (stamped.id == 0L) noteDao.insert(stamped) else noteDao.update(stamped)
    }

    suspend fun setFavorite(note: NoteEntity, favorite: Boolean) {
        noteDao.update(note.copy(favorite = favorite))
    }

    suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)

    // ---- Yedekleme ----
    suspend fun snapshot(): BackupData = BackupData(
        tasks = taskDao.getAll(),
        goals = goalDao.getAll(),
        habits = habitDao.getAll(),
        habitLogs = habitDao.getAllLogs(),
        notes = noteDao.getAll(),
        settings = settingsStore.current(),
    )

    suspend fun replaceAll(data: BackupData) {
        val oldTasks = taskDao.getAll()
        db.withTransaction {
            habitDao.clearLogs()
            habitDao.clear()
            taskDao.clear()
            goalDao.clear()
            noteDao.clear()
            taskDao.insertAll(data.tasks)
            goalDao.insertAll(data.goals)
            habitDao.insertAll(data.habits)
            habitDao.insertLogs(data.habitLogs)
            noteDao.insertAll(data.notes)
        }
        oldTasks.forEach { ReminderScheduler.cancelTask(context, it.id) }
        data.settings?.let { settingsStore.restore(it) }
        rescheduleAllReminders()
        val s = settingsStore.current()
        ReminderScheduler.scheduleDailySummary(context, s.dailySummary, s.summaryHour)
    }
}
