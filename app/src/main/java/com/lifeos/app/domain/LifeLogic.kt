package com.lifeos.app.domain

import com.lifeos.app.data.GoalEntity
import com.lifeos.app.data.HabitEntity
import com.lifeos.app.data.HabitLogEntity
import com.lifeos.app.data.TaskEntity

data class DayStat(val day: Long, val done: Int, val total: Int) {
    val rate: Float get() = if (total == 0) 0f else done.toFloat() / total
}

/** Uygulamanın saf iş kuralları (birim testleriyle doğrulanır). */
object LifeLogic {

    /** Bugün (veya bugün henüz yapılmadıysa dün) biten ardışık gün sayısı. */
    fun currentStreak(days: Set<Long>, today: Long): Int {
        var d = if (today in days) today else today - 1
        var count = 0
        while (d in days) {
            count++
            d--
        }
        return count
    }

    fun bestStreak(days: Set<Long>): Int {
        if (days.isEmpty()) return 0
        val sorted = days.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i] == sorted[i - 1] + 1) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    fun lastNDays(today: Long, n: Int): List<Long> = (n - 1 downTo 0).map { today - it }

    /** Ana ekranda gösterilecek "bugünün görevleri". */
    fun todayTasks(tasks: List<TaskEntity>, today: Long): List<TaskEntity> = tasks.filter { t ->
        when {
            t.dueDate == today -> true
            t.dueDate != null && t.dueDate < today -> !t.completed // gecikmiş
            t.dueDate == null -> !t.completed || t.completedDate == today
            else -> false
        }
    }

    fun isOverdue(task: TaskEntity, today: Long): Boolean =
        !task.completed && task.dueDate != null && task.dueDate < today

    /** Bir gün için görev + hedef + alışkanlık tamamlanma istatistiği. */
    fun dayStat(
        day: Long,
        tasks: List<TaskEntity>,
        goals: List<GoalEntity>,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
    ): DayStat {
        val dayTasks = tasks.filter { it.dueDate == day || (it.dueDate == null && it.completedDate == day) }
        val taskDone = dayTasks.count { it.completed }
        val dayGoals = goals.filter { it.date == day }
        val goalDone = dayGoals.count { it.completed }
        val activeHabitIds = habits.filter { it.createdDate <= day }.map { it.id }.toSet()
        val habitDone = logs.count { it.date == day && it.habitId in activeHabitIds }
        return DayStat(
            day = day,
            done = taskDone + goalDone + habitDone,
            total = dayTasks.size + dayGoals.size + activeHabitIds.size,
        )
    }

    /** Alışkanlığın son [n] gündeki gerçekleşme oranı (oluşturulma tarihinden itibaren). */
    fun habitRate(habit: HabitEntity, logDays: Set<Long>, today: Long, n: Int): Float {
        val window = lastNDays(today, n).filter { it >= habit.createdDate }
        if (window.isEmpty()) return 0f
        return window.count { it in logDays }.toFloat() / window.size
    }

    fun formatMinutes(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
}
