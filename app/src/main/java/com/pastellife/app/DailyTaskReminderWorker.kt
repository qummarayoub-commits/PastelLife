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

class DailyTaskReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val pending = AppDatabase.get(applicationContext).appDao().observeTasks().first().count { !it.isDone }
            if (pending > 0) {
                val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Daily task reminders", NotificationManager.IMPORTANCE_DEFAULT))
                }
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Your PastelLife daily check-in")
                    .setContentText("You have " + pending + " task(s) still to do. Take a quick look at your list.")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("You have " + pending + " unfinished task(s). Open PastelLife and choose what to tackle next."))
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
