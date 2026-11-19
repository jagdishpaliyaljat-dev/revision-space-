package com.example.receivers
import com.example.ui.*


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.services.FocusTimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        try {
            val prefs = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
            val savedDuration = prefs.getLong("total_duration", 0L)
            val savedAbsoluteTarget = prefs.getLong("target_absolute", 0L)
            val savedPaused = prefs.getBoolean("is_paused", false)
            val nowAbsolute = System.currentTimeMillis()

            // Only start the foreground service if there is an active or paused timer session to restore
            if (savedDuration > 0L && (savedAbsoluteTarget > nowAbsolute || savedPaused)) {
                val serviceIntent = Intent(context, FocusTimerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Exact alarms are cleared by the system on reboot. Restore the reminder
        // schedule and every per-topic revision alarm from local storage.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.getSharedPreferences(
                    com.example.notification.NotificationHelper.APP_PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
                val reminderEnabled = prefs.getBoolean("reminder_enabled", false)

                if (notificationsEnabled && reminderEnabled) {
                    com.example.notification.ReminderScheduler.scheduleDailyReminder(
                        context,
                        prefs.getInt("reminder_hour", 20),
                        prefs.getInt("reminder_minute", 0)
                    )
                } else {
                    com.example.notification.ReminderScheduler.cancelReminder(context)
                }

                if (notificationsEnabled) {
                    com.example.services.SrsNotificationManager.scheduleDailyMission(context)
                } else {
                    com.example.services.SrsNotificationManager.cancelDailyMission(context)
                }

                com.example.services.SrsNotificationManager.rescheduleAllRevisionAlarms(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
