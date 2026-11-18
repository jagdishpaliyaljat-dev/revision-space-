package com.example.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val REMINDER_WORK_NAME = "daily_revision_reminder_work"

    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the target time is in the past for today, schedule it for tomorrow
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<RevisionReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun scheduleImmediateCheck(context: Context) {
        val oneTimeWork = OneTimeWorkRequestBuilder<RevisionReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(oneTimeWork)
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun rescheduleReminder(context: Context, hour: Int, minute: Int) {
        scheduleDailyReminder(context, hour, minute)
    }
}
