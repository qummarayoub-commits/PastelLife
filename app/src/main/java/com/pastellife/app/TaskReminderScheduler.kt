package com.pastellife.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pastellife.app.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {
    private fun workName(taskId: Long) = "pastellife-task-due-$taskId"

    fun schedule(context: Context, taskId: Long, title: String, dueDate: String) {
        cancel(context, taskId)
        if (dueDate.isBlank()) return
        val parsed = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(dueDate)
        }.getOrNull() ?: return
        val dueAt = Calendar.getInstance().apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val now = System.currentTimeMillis()
        val runAt = if (dueAt > now) dueAt else now + TimeUnit.MINUTES.toMillis(1)
        val request = OneTimeWorkRequestBuilder<TaskDueReminderWorker>()
            .setInputData(workDataOf("taskId" to taskId, "title" to title))
            .setInitialDelay((runAt - now).coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(taskId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }
}

class TaskDueReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong("taskId", -1L)
            val task = AppDatabase.get(applicationContext).appDao().observeTasks().first().firstOrNull { it.id == taskId }
            if (task == null || task.isDone) return Result.success()
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Task due reminders", NotificationManager.IMPORTANCE_DEFAULT))
            }
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Task due: " + task.title)
                .setContentText(if (task.dueDate.isBlank()) "Open PastelLife to review your task." else "Due date: " + task.dueDate)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(applicationContext).notify((taskId % Int.MAX_VALUE).toInt().coerceAtLeast(1), notification)
            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val CHANNEL_ID = "pastellife_task_due"
    }
}
