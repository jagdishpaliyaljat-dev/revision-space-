package com.example.services
import com.example.ui.*


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import com.example.receivers.SrsAlarmReceiver
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SrsNotificationManager {
    
    fun scheduleDailyMission(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
            
        val workRequest = PeriodicWorkRequestBuilder<DailySrsWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelayToMorning(), TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailySrsMission",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    
    private fun calculateInitialDelayToMorning(): Long {
        val now = Calendar.getInstance()
        val morning = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (now.after(morning)) {
            morning.add(Calendar.DAY_OF_MONTH, 1)
        }
        return morning.timeInMillis - now.timeInMillis
    }
    
    fun scheduleChapterRevision(context: Context, topicId: String, subjectId: String, chapterId: String, topicName: String, nextRevisionMs: Long, estimatedMins: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SrsAlarmReceiver::class.java).apply {
            action = SrsAlarmReceiver.ACTION_REVISION_DUE
            putExtra("topicId", topicId)
            putExtra("subjectId", subjectId)
            putExtra("chapterId", chapterId)
            putExtra("topicName", topicName)
            putExtra("notificationType", "revision")
            putExtra("nextRevisionDate", nextRevisionMs)
            putExtra("sessionType", "Revision Mode")
            putExtra("estimatedMins", estimatedMins)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            topicId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Ensure alarm is in the future
        if (nextRevisionMs > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextRevisionMs,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // For Android 14+, exact alarms require permission
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextRevisionMs,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelChapterRevision(context: Context, topicId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SrsAlarmReceiver::class.java).apply {
            action = SrsAlarmReceiver.ACTION_REVISION_DUE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            topicId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
