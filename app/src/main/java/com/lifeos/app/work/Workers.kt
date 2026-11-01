package com.lifeos.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.app.LifeOSApp
import com.lifeos.app.domain.LifeLogic
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as LifeOSApp
        if (!app.settingsStore.current().taskReminders) return Result.success()
        val id = inputData.getLong(KEY_TASK_ID, -1L)
        val task = app.repository.getTask(id) ?: return Result.success()
        if (task.completed) return Result.success()
        val time = task.dueTime?.let { " • " + LifeLogic.formatMinutes(it) } ?: ""
        ReminderScheduler.notify(
            applicationContext,
            ReminderScheduler.CHANNEL_REMINDERS,
            (1000 + id).toInt(),
            "Görev hatırlatıcısı$time",
            task.title,
        )
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "taskId"
    }
}

class DailySummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as LifeOSApp
        val settings = app.settingsStore.current()
        if (!settings.dailySummary) return Result.success()
        val repo = app.repository
        val today = LocalDate.now().toEpochDay()
        val tasks = repo.tasks.first()
        val habits = repo.habits.first()
        val logs = repo.habitLogs.first()
        val goals = repo.goals.first()
        val open = LifeLogic.todayTasks(tasks, today).count { !it.completed }
        val habitsLeft = habits.count { h -> logs.none { it.habitId == h.id && it.date == today } }
        val stat = LifeLogic.dayStat(today, tasks, goals, habits, logs)
        val percent = (stat.rate * 100).toInt()
        ReminderScheduler.notify(
            applicationContext,
            ReminderScheduler.CHANNEL_SUMMARY,
            1,
            "Günün özeti • %$percent",
            "$open açık görev, $habitsLeft alışkanlık kaldı. ${stat.done}/${stat.total} öğe tamamlandı.",
        )
        return Result.success()
    }
}
