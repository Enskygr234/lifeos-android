package com.lifeos.app

import com.lifeos.app.data.AppSettings
import com.lifeos.app.data.BackupCodec
import com.lifeos.app.data.BackupData
import com.lifeos.app.data.GoalEntity
import com.lifeos.app.data.HabitEntity
import com.lifeos.app.data.HabitLogEntity
import com.lifeos.app.data.NoteEntity
import com.lifeos.app.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCodecTest {

    private val sample = BackupData(
        tasks = listOf(
            TaskEntity(1, "Rapor yaz", "ayrıntı", 2, "İş", 20_000L, 600, true, false, null, 123L),
            TaskEntity(2, "Süt al", "", 0, "Alışveriş", null, null, false, true, 19_999L, 456L),
        ),
        goals = listOf(GoalEntity(1, "10 sayfa oku", 20_000L, true)),
        habits = listOf(HabitEntity(5, "Su iç", 3, 19_990L)),
        habitLogs = listOf(HabitLogEntity(5, 19_995L), HabitLogEntity(5, 19_996L)),
        notes = listOf(NoteEntity(9, "Fikir", "Çok satırlı\nnot ğüşiöç", "Fikirler", true, 789L)),
        settings = AppSettings("light", false, true, 7),
    )

    @Test
    fun roundTrip_preservesEverything() {
        val json = BackupCodec.encode(sample, exportedAt = 1L)
        val decoded = BackupCodec.decode(json)
        assertEquals(sample, decoded)
    }

    @Test
    fun decode_rejectsForeignJson() {
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode("{\"foo\": 1}") }
    }

    @Test
    fun decode_rejectsGarbage() {
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode("bu json değil") }
    }

    @Test
    fun decode_dropsOrphanHabitLogs() {
        val json = BackupCodec.encode(sample.copy(habitLogs = sample.habitLogs + HabitLogEntity(99, 1L)))
        assertEquals(2, BackupCodec.decode(json).habitLogs.size)
    }

    @Test
    fun decode_clampsInvalidValues() {
        val json = """
            {"format":"lifeos-backup","version":1,
             "settings":{"themeMode":"pembe","summaryHour":99},
             "tasks":[{"id":1,"title":"x","priority":7}]}
        """.trimIndent()
        val d = BackupCodec.decode(json)
        assertEquals("dark", d.settings!!.themeMode)
        assertEquals(23, d.settings!!.summaryHour)
        assertEquals(2, d.tasks.single().priority)
    }
}
