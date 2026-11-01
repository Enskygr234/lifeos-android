package com.lifeos.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    /** "dark", "light" veya "system" */
    val themeMode: String = "dark",
    val taskReminders: Boolean = true,
    val dailySummary: Boolean = false,
    val summaryHour: Int = 20,
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val REMINDERS = booleanPreferencesKey("task_reminders")
        val SUMMARY = booleanPreferencesKey("daily_summary")
        val SUMMARY_HOUR = intPreferencesKey("summary_hour")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            AppSettings(
                themeMode = p[Keys.THEME] ?: "dark",
                taskReminders = p[Keys.REMINDERS] ?: true,
                dailySummary = p[Keys.SUMMARY] ?: false,
                summaryHour = (p[Keys.SUMMARY_HOUR] ?: 20).coerceIn(0, 23),
            )
        }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setTheme(mode: String) {
        context.dataStore.edit { it[Keys.THEME] = mode }
    }

    suspend fun setTaskReminders(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS] = enabled }
    }

    suspend fun setDailySummary(enabled: Boolean, hour: Int) {
        context.dataStore.edit {
            it[Keys.SUMMARY] = enabled
            it[Keys.SUMMARY_HOUR] = hour.coerceIn(0, 23)
        }
    }

    suspend fun restore(s: AppSettings) {
        context.dataStore.edit {
            it[Keys.THEME] = s.themeMode
            it[Keys.REMINDERS] = s.taskReminders
            it[Keys.SUMMARY] = s.dailySummary
            it[Keys.SUMMARY_HOUR] = s.summaryHour.coerceIn(0, 23)
        }
    }
}
