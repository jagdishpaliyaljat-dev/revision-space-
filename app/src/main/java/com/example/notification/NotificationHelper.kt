package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "revision_reminder_channel"
    const val NOTIFICATION_ID = 1001

    /** Channel used by SRS topic alarms and the daily mission worker. */
    const val SRS_CHANNEL_ID = "srs_channel"
    const val SRS_CHANNEL_NAME = "SRS Study Reminders"
    const val SRS_CHANNEL_DESCRIPTION = "Reminders for topic & subject revisions"

    /** Shared-preferences file that holds every user setting. */
    const val APP_PREFS_NAME = "revision_app_prefs"

    /**
     * Single source of truth for "may the app post notifications right now?".
     *
     * Both the in-app master switch and the Android permission/channel state are
     * honoured. Before this helper every producer ignored the master switch, so
     * turning notifications off only hid them from Settings.
     */
    fun notificationsAllowed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Study Revision Reminder"
            val descriptionText = "Daily reminders for your planned study revisions based on 1, 3, 7, 14, 30 days pattern"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates the SRS channel with one canonical name/description.
     *
     * The alarm receiver used to create the same channel id as "SRS Reminders"
     * while the worker used "SRS Study Reminders". Android keeps the first name,
     * so the settings screen could show a different label than the code intended.
     */
    fun createSrsChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SRS_CHANNEL_ID,
                SRS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = SRS_CHANNEL_DESCRIPTION
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRevisionReminder(context: Context) {
        if (!notificationsAllowed(context)) return
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today_revision")
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_revision)
            .setContentTitle("Time for Today's Revision")
            .setContentText("Complete your planned 1, 3, 7, 14, 30-day revision session to keep your streak alive!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun showDueRevisionNotification(
        context: Context,
        dueCount: Int,
        firstTopicName: String,
        subjectName: String,
        revisionCount: Int
    ) {
        if (!notificationsAllowed(context)) return
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today_revision")
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (dueCount == 1) {
            "Revision Due: $firstTopicName"
        } else {
            "$dueCount Topics Due for Revision Today!"
        }

        val text = if (dueCount == 1) {
            "Subject: $subjectName • Revision #$revisionCount (Pattern: 1, 3, 7, 14, 30 days)"
        } else {
            "Subject: $subjectName & more • Tap to review your study schedule."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_revision)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
