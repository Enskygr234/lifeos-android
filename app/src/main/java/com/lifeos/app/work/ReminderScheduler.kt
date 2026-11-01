package com.lifeos.app.work

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lifeos.app.MainActivity
import com.lifeos.app.R
import com.lifeos.app.data.TaskEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val CHANNEL_REMINDERS = "lifeos_reminders"
    const val CHANNEL_SUMMARY = "lifeos_summary"
    private const val SUMMARY_WORK = "lifeos_daily_summary"
    const val DEFAULT_REMINDER_MINUTES = 9 * 60

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.channel_reminders),
                NotificationManager.IMPORTANCE_HIGH,
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SUMMARY,
                context.getString(R.string.channel_summary),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }

    private fun taskWorkName(id: Long) = "task_reminder_$id"

    fun reminderTimeMillis(task: TaskEntity, zone: ZoneId = ZoneId.systemDefault()): Long? {
        val date = task.dueDate ?: return null
        val minutes = task.dueTime ?: DEFAULT_REMINDER_MINUTES
        return LocalDate.ofEpochDay(date)
            .atTime(minutes / 60, minutes % 60)
            .atZone(zone).toInstant().toEpochMilli()
    }

    fun scheduleTask(context: Context, task: TaskEntity) {
        val wm = WorkManager.getInstance(context)
        val name = taskWorkName(task.id)
        val trigger = reminderTimeMillis(task)
        if (!task.reminder || task.completed || trigger == null) {
            wm.cancelUniqueWork(name)
            return
        }
        val delay = trigger - System.currentTimeMillis()
        if (delay <= 0) {
            wm.cancelUniqueWork(name)
            return
        }
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_TASK_ID to task.id))
            .addTag("task_reminder")
            .build()
        wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelTask(context: Context, id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(taskWorkName(id))
    }

    fun scheduleDailySummary(context: Context, enabled: Boolean, hour: Int) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(SUMMARY_WORK)
            return
        }
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour.coerceIn(0, 23), 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next).toMillis()
        val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(SUMMARY_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun canNotify(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission") // canNotify() ile izin kontrol ediliyor
    fun notify(context: Context, channel: String, id: Int, title: String, text: String) {
        if (!canNotify(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setColor(0xFF8B5CF6.toInt())
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // İzin çalışma anında geri alındıysa sessizce geç.
        }
    }
}
