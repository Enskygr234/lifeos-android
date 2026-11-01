package com.lifeos.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tarih alanları LocalDate.toEpochDay() değeri olarak saklanır. */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    /** 0 = Düşük, 1 = Orta, 2 = Yüksek */
    val priority: Int = 1,
    val category: String = "Genel",
    val dueDate: Long? = null,
    /** Günün dakikası (ör. 14:30 -> 870) */
    val dueTime: Int? = null,
    val reminder: Boolean = false,
    val completed: Boolean = false,
    val completedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,
    val completed: Boolean = false,
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorIndex: Int = 0,
    val createdDate: Long,
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("habitId")],
)
data class HabitLogEntity(
    val habitId: Long,
    val date: Long,
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Genel",
    val favorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

const val QUICK_NOTE_CATEGORY = "Hızlı"
