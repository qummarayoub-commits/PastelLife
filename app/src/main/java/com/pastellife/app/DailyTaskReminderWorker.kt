package com.pastellife.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pastellife.app.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyTaskReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val tasks = AppDatabase.get(applicationContext).appDao().observeTasks().first()
            val pending = tasks.count { !it.isDone }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val dueOrOverdue = tasks.count { !it.isDone && it.dueDate.isNotBlank() && it.dueDate <= today }
            if (pending > 0) {
                val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Daily task reminders", NotificationManager.IMPORTANCE_DEFAULT))
                }
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Your PastelLife daily check-in")
                    .setContentText("You have " + pending + " unfinished task(s); " + dueOrOverdue + " due or overdue.")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("You have " + pending + " unfinished task(s), including " + dueOrOverdue + " due or overdue. Open PastelLife and choose what to tackle next."))
                    .setAutoCancel(true)
                    .build()
                NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
            }
            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val CHANNEL_ID = "pastellife_daily_tasks"
        const val NOTIFICATION_ID = 4107
    }
}
