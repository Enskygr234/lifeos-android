package com.lifeos.app.data

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class BackupData(
    val tasks: List<TaskEntity>,
    val goals: List<GoalEntity>,
    val habits: List<HabitEntity>,
    val habitLogs: List<HabitLogEntity>,
    val notes: List<NoteEntity>,
    val settings: AppSettings?,
)

/** LifeOS verisini JSON'a çevirir ve geri okur. Saf (Android'e bağlı olmayan) mantık. */
object BackupCodec {
    const val FORMAT = "lifeos-backup"
    const val VERSION = 1

    fun encode(data: BackupData, exportedAt: Long = System.currentTimeMillis()): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("exportedAt", exportedAt)
        data.settings?.let { s ->
            root.put("settings", JSONObject().apply {
                put("themeMode", s.themeMode)
                put("taskReminders", s.taskReminders)
                put("dailySummary", s.dailySummary)
                put("summaryHour", s.summaryHour)
            })
        }
        root.put("tasks", JSONArray().apply {
            data.tasks.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("description", t.description)
                    put("priority", t.priority)
                    put("category", t.category)
                    put("dueDate", t.dueDate ?: JSONObject.NULL)
                    put("dueTime", t.dueTime ?: JSONObject.NULL)
                    put("reminder", t.reminder)
                    put("completed", t.completed)
                    put("completedDate", t.completedDate ?: JSONObject.NULL)
                    put("createdAt", t.createdAt)
                })
            }
        })
        root.put("goals", JSONArray().apply {
            data.goals.forEach { g ->
                put(JSONObject().apply {
                    put("id", g.id); put("title", g.title)
                    put("date", g.date); put("completed", g.completed)
                })
            }
        })
        root.put("habits", JSONArray().apply {
            data.habits.forEach { h ->
                put(JSONObject().apply {
                    put("id", h.id); put("name", h.name)
                    put("colorIndex", h.colorIndex); put("createdDate", h.createdDate)
                })
            }
        })
        root.put("habitLogs", JSONArray().apply {
            data.habitLogs.forEach { l ->
                put(JSONObject().apply { put("habitId", l.habitId); put("date", l.date) })
            }
        })
        root.put("notes", JSONArray().apply {
            data.notes.forEach { n ->
                put(JSONObject().apply {
                    put("id", n.id); put("title", n.title); put("content", n.content)
                    put("category", n.category); put("favorite", n.favorite)
                    put("updatedAt", n.updatedAt)
                })
            }
        })
        return root.toString(2)
    }

    /** Geçersiz dosyada IllegalArgumentException fırlatır. */
    fun decode(json: String): BackupData {
        try {
            val root = JSONObject(json)
            require(root.optString("format") == FORMAT) { "Bu dosya bir LifeOS yedeği değil." }
            val version = root.optInt("version", 0)
            require(version in 1..VERSION) { "Desteklenmeyen yedek sürümü: $version" }

            val settings = root.optJSONObject("settings")?.let { s ->
                AppSettings(
                    themeMode = s.optString("themeMode", "dark")
                        .takeIf { it in setOf("dark", "light", "system") } ?: "dark",
                    taskReminders = s.optBoolean("taskReminders", true),
                    dailySummary = s.optBoolean("dailySummary", false),
                    summaryHour = s.optInt("summaryHour", 20).coerceIn(0, 23),
                )
            }
            val tasks = root.optJSONArray("tasks").objects().map { o ->
                TaskEntity(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    description = o.optString("description", ""),
                    priority = o.optInt("priority", 1).coerceIn(0, 2),
                    category = o.optString("category", "Genel"),
                    dueDate = o.optLongOrNull("dueDate"),
                    dueTime = o.optLongOrNull("dueTime")?.toInt(),
                    reminder = o.optBoolean("reminder", false),
                    completed = o.optBoolean("completed", false),
                    completedDate = o.optLongOrNull("completedDate"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                )
            }
            val goals = root.optJSONArray("goals").objects().map { o ->
                GoalEntity(o.getLong("id"), o.getString("title"), o.getLong("date"), o.optBoolean("completed"))
            }
            val habits = root.optJSONArray("habits").objects().map { o ->
                HabitEntity(o.getLong("id"), o.getString("name"), o.optInt("colorIndex", 0), o.getLong("createdDate"))
            }
            val habitIds = habits.map { it.id }.toSet()
            val logs = root.optJSONArray("habitLogs").objects()
                .map { o -> HabitLogEntity(o.getLong("habitId"), o.getLong("date")) }
                .filter { it.habitId in habitIds }
            val notes = root.optJSONArray("notes").objects().map { o ->
                NoteEntity(
                    id = o.getLong("id"),
                    title = o.optString("title", ""),
                    content = o.optString("content", ""),
                    category = o.optString("category", "Genel"),
                    favorite = o.optBoolean("favorite", false),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                )
            }
            return BackupData(tasks, goals, habits, logs, notes, settings)
        } catch (e: JSONException) {
            throw IllegalArgumentException("Yedek dosyası okunamadı: ${e.message}", e)
        }
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (!has(key) || isNull(key)) null else getLong(key)
}
