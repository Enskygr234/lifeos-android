package com.lifeos.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query(
        "SELECT * FROM tasks ORDER BY completed ASC, " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate ASC, priority DESC, id DESC"
    )
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clear()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY date DESC, id ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals")
    suspend fun getAll(): List<GoalEntity>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun clear()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAll(): List<HabitEntity>

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits")
    suspend fun clear()

    @Query("SELECT * FROM habit_logs")
    fun observeLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllLogs(): List<HabitLogEntity>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun countLog(habitId: Long, date: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun clearLogs()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY favorite DESC, updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun clear()
}
