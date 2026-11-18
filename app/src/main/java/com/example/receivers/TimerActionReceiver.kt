package com.example.receivers
import com.example.ui.*


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.services.FocusTimerService

class TimerActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PAUSE = "com.example.receivers.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.receivers.ACTION_RESUME"
        const val ACTION_GIVE_UP = "com.example.receivers.ACTION_GIVE_UP"
        
        const val ACTION_START_BREAK = "com.example.receivers.ACTION_START_BREAK"
        const val ACTION_CONTINUE_STUDY = "com.example.receivers.ACTION_CONTINUE_STUDY"
        const val ACTION_SKIP_BREAK = "com.example.receivers.ACTION_SKIP_BREAK"
        const val ACTION_START_FOCUS = "com.example.receivers.ACTION_START_FOCUS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, FocusTimerService::class.java)
        
        try {
            when (intent.action) {
                ACTION_PAUSE -> {
                    serviceIntent.action = FocusTimerService.ACTION_PAUSE
                    context.startService(serviceIntent)
                }
                ACTION_RESUME -> {
                    serviceIntent.action = FocusTimerService.ACTION_RESUME
                    context.startService(serviceIntent)
                }
                ACTION_GIVE_UP -> {
                    serviceIntent.action = FocusTimerService.ACTION_STOP
                    context.startService(serviceIntent)
                }
                ACTION_START_BREAK -> {
                    serviceIntent.action = FocusTimerService.ACTION_START_BREAK
                    context.startService(serviceIntent)
                }
                ACTION_CONTINUE_STUDY, ACTION_START_FOCUS -> {
                    serviceIntent.action = FocusTimerService.ACTION_CONTINUE_STUDY
                    context.startService(serviceIntent)
                }
                ACTION_SKIP_BREAK -> {
                    serviceIntent.action = FocusTimerService.ACTION_SKIP_BREAK
                    context.startService(serviceIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
