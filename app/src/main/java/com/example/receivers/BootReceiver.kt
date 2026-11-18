package com.example.receivers
import com.example.ui.*


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.services.FocusTimerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
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
        }
    }
}
