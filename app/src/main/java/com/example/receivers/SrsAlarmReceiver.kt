package com.example.receivers
import com.example.ui.*


import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class SrsAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_REVISION_DUE = "com.example.action.REVISION_DUE"
        const val ACTION_SNOOZE = "com.example.action.SNOOZE_REVISION"
        const val ACTION_SKIP = "com.example.action.SKIP_REVISION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                handleReceive(context, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "srs_channel",
                "SRS Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setBypassDnd(true)
            manager.createNotificationChannel(channel)
        }

                val topicId = intent.getStringExtra("topicId") ?: return
        val subjectId = intent.getStringExtra("subjectId") ?: ""
        val chapterId = intent.getStringExtra("chapterId") ?: ""
        val topicName = intent.getStringExtra("topicName") ?: "Topic"
        val notificationType = intent.getStringExtra("notificationType") ?: "revision"
        val nextRevisionDate = intent.getLongExtra("nextRevisionDate", 0L)
        val sessionType = intent.getStringExtra("sessionType") ?: "Revision Mode"
        val estimatedMins = intent.getIntExtra("estimatedMins", 30)

        var subjectName = subjectId
        var chapterName = topicName

        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            if (subjectId.isNotEmpty()) {
                val subjectSnap = db.collection("subjects").document(subjectId).get().await()
                if (subjectSnap.exists()) {
                    val sName = subjectSnap.getString("name")
                    if (!sName.isNullOrEmpty()) {
                        subjectName = sName
                    }
                }
            }
            if (chapterId.isNotEmpty()) {
                val chapterSnap = db.collection("chapters").document(chapterId).get().await()
                if (chapterSnap.exists()) {
                    val cName = chapterSnap.getString("name")
                    if (!cName.isNullOrEmpty()) {
                        chapterName = cName
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        when (intent.action) {
            ACTION_REVISION_DUE -> {
                val title = "📚 Revision Due"
                val body = "Subject: $subjectName\nChapter: $chapterName\nTopic: $topicName\nEstimated Time: $estimatedMins Minutes"

                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("open_topic_id", topicId)
                    putExtra("subjectId", subjectId)
                    putExtra("chapterId", chapterId)
                    putExtra("topicId", topicId)
                    putExtra("notificationType", notificationType)
                    putExtra("nextRevisionDate", nextRevisionDate)
                    putExtra("sessionType", sessionType)
                }
                val mainPending = PendingIntent.getActivity(
                    context, topicId.hashCode(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Snooze Intent
                val snoozeIntent = Intent(context, SrsAlarmReceiver::class.java).apply {
                    action = ACTION_SNOOZE
                    putExtra("topicId", topicId)
                    putExtra("subjectId", subjectId)
                    putExtra("chapterId", chapterId)
                    putExtra("topicName", topicName)
                    putExtra("notificationType", notificationType)
                    putExtra("nextRevisionDate", nextRevisionDate)
                    putExtra("sessionType", sessionType)
                    putExtra("estimatedMins", estimatedMins)
                }
                val snoozePending = PendingIntent.getBroadcast(
                    context, topicId.hashCode() + 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Skip Intent
                val skipIntent = Intent(context, SrsAlarmReceiver::class.java).apply {
                    action = ACTION_SKIP
                    putExtra("topicId", topicId)
                }
                val skipPending = PendingIntent.getBroadcast(
                    context, topicId.hashCode() + 2, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, "srs_channel")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText("Subject: $subjectName - Chapter: $chapterName")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(mainPending)
                    .addAction(android.R.drawable.ic_media_play, "Start Revision", mainPending)
                    .addAction(android.R.drawable.ic_popup_sync, "Snooze 30 Min", snoozePending)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip Today", skipPending)

                manager.notify(topicId.hashCode(), builder.build())
            }
                        ACTION_SNOOZE -> {
                manager.cancel(topicId.hashCode())
                // Reschedule for 30 mins later
                com.example.services.SrsNotificationManager.scheduleChapterRevision(
                    context, topicId, subjectId, chapterId, topicName, System.currentTimeMillis() + 30 * 60 * 1000L, estimatedMins
                )
            }
            ACTION_SKIP -> {
                manager.cancel(topicId.hashCode())
                // Maybe update database to move nextRevisionDateMs to tomorrow? 
                // For simplicity, we just cancel it for today. The daily check will pick it up as overdue tomorrow.
            }
        }
    }
}
