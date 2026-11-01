package com.lifeos.app

import com.lifeos.app.data.GoalEntity
import com.lifeos.app.data.HabitEntity
import com.lifeos.app.data.HabitLogEntity
import com.lifeos.app.data.TaskEntity
import com.lifeos.app.domain.LifeLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeLogicTest {
    private val today = 20_000L

    @Test
    fun currentStreak_countsUntilToday() {
        assertEquals(3, LifeLogic.currentStreak(setOf(today, today - 1, today - 2, today - 4), today))
    }

    @Test
    fun currentStreak_allowsTodayNotYetDone() {
        assertEquals(2, LifeLogic.currentStreak(setOf(today - 1, today - 2), today))
    }

    @Test
    fun currentStreak_zeroWhenBroken() {
        assertEquals(0, LifeLogic.currentStreak(setOf(today - 2, today - 3), today))
        assertEquals(0, LifeLogic.currentStreak(emptySet(), today))
    }

    @Test
    fun bestStreak_findsLongestRun() {
        val days = setOf(1L, 2L, 3L, 7L, 8L, 9L, 10L, 15L)
        assertEquals(4, LifeLogic.bestStreak(days))
        assertEquals(0, LifeLogic.bestStreak(emptySet()))
        assertEquals(1, LifeLogic.bestStreak(setOf(5L)))
    }

    @Test
    fun todayTasks_includesDueOverdueAndUndated() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "bugün", dueDate = today),
            TaskEntity(id = 2, title = "gecikmiş", dueDate = today - 2),
            TaskEntity(id = 3, title = "gecikmiş-bitmiş", dueDate = today - 2, completed = true, completedDate = today - 1),
            TaskEntity(id = 4, title = "yarın", dueDate = today + 1),
            TaskEntity(id = 5, title = "tarihsiz"),
            TaskEntity(id = 6, title = "tarihsiz-bugün-bitti", completed = true, completedDate = today),
            TaskEntity(id = 7, title = "tarihsiz-dün-bitti", completed = true, completedDate = today - 1),
        )
        val ids = LifeLogic.todayTasks(tasks, today).map { it.id }.toSet()
        assertEquals(setOf(1L, 2L, 5L, 6L), ids)
    }

    @Test
    fun isOverdue() {
        assertTrue(LifeLogic.isOverdue(TaskEntity(title = "a", dueDate = today - 1), today))
        assertFalse(LifeLogic.isOverdue(TaskEntity(title = "a", dueDate = today), today))
        assertFalse(LifeLogic.isOverdue(TaskEntity(title = "a", dueDate = today - 1, completed = true), today))
        assertFalse(LifeLogic.isOverdue(TaskEntity(title = "a"), today))
    }

    @Test
    fun dayStat_combinesTasksGoalsHabits() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "a", dueDate = today, completed = true, completedDate = today),
            TaskEntity(id = 2, title = "b", dueDate = today),
        )
        val goals = listOf(GoalEntity(1, "g", today, completed = true), GoalEntity(2, "g2", today - 1, true))
        val habits = listOf(
            HabitEntity(1, "su", 0, today - 10),
            HabitEntity(2, "spor", 0, today - 3),
            HabitEntity(3, "yeni", 0, today + 1), // henüz aktif değil
        )
        val logs = listOf(HabitLogEntity(1, today), HabitLogEntity(2, today - 1))
        val s = LifeLogic.dayStat(today, tasks, goals, habits, logs)
        assertEquals(3, s.done)  // 1 görev + 1 hedef + 1 alışkanlık
        assertEquals(5, s.total) // 2 görev + 1 hedef + 2 aktif alışkanlık
        assertEquals(0.6f, s.rate, 0.0001f)
    }

    @Test
    fun dayStat_emptyIsZero() {
        val s = LifeLogic.dayStat(today, emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(0f, s.rate, 0f)
    }

    @Test
    fun habitRate_respectsCreationDate() {
        val h = HabitEntity(1, "x", 0, createdDate = today - 1)
        assertEquals(0.5f, LifeLogic.habitRate(h, setOf(today), today, 7), 0.0001f)
    }

    @Test
    fun lastNDays_isChronological() {
        assertEquals(listOf(today - 2, today - 1, today), LifeLogic.lastNDays(today, 3))
    }

    @Test
    fun formatMinutes() {
        assertEquals("09:05", LifeLogic.formatMinutes(545))
        assertEquals("00:00", LifeLogic.formatMinutes(0))
    }
}
