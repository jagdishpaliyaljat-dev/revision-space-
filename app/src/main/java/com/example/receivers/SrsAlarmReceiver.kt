package com.example.receivers
import com.example.ui.*


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MyApplication
import com.example.R
import com.example.data.local.TopicEntity
import com.example.notification.NotificationHelper

class SrsAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_REVISION_DUE = "com.example.action.REVISION_DUE"
        const val ACTION_SNOOZE = "com.example.action.SNOOZE_REVISION"
        const val ACTION_SKIP = "com.example.action.SKIP_REVISION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleReceive(context, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val topicId = intent.getStringExtra("topicId") ?: return
        val subjectId = intent.getStringExtra("subjectId") ?: ""
        val chapterId = intent.getStringExtra("chapterId") ?: ""
        val topicName = intent.getStringExtra("topicName") ?: "Topic"
        val notificationType = intent.getStringExtra("notificationType") ?: "revision"
        val nextRevisionDate = intent.getLongExtra("nextRevisionDate", 0L)
        val sessionType = intent.getStringExtra("sessionType") ?: "Revision Mode"
        val estimatedMins = intent.getIntExtra("estimatedMins", 30)

        when (intent.action) {
            ACTION_SNOOZE -> {
                manager.cancel(topicId.hashCode())
                // Reschedule for 30 mins later
                com.example.services.SrsNotificationManager.scheduleChapterRevision(
                    context, topicId, subjectId, chapterId, topicName,
                    System.currentTimeMillis() + 30 * 60 * 1000L, estimatedMins
                )
            }
            ACTION_SKIP -> {
                manager.cancel(topicId.hashCode())
                // For simplicity we just cancel it for today. The daily check will
                // pick the topic up as overdue tomorrow.
            }
            ACTION_REVISION_DUE -> {
                // Honour the in-app master switch: no notification, and clean up the
                // (already delivered) one-shot alarm.
                if (!NotificationHelper.notificationsAllowed(context)) {
                    manager.cancel(topicId.hashCode())
                    return
                }

                // Read names from the local database instead of Firestore. The old
                // network lookup blocked the receiver for ~10s offline (longer than
                // goAsync tolerates) and then showed raw ids like "sub_123".
                val db = runCatching { MyApplication.database }.getOrNull()
                val topic: TopicEntity? = db?.let {
                    runCatching { it.topicDao().getTopicById(topicId) }.getOrNull()
                }

                // The topic may have been deleted while this alarm was pending.
                if (db != null && topic == null) {
                    manager.cancel(topicId.hashCode())
                    return
                }

                // The alarm may be stale: completing a revision moves the date
                // forward, and alarms are not always cancelled in time.
                if (topic != null &&
                    com.example.services.RevisionAlarmPolicy.isStale(
                        topic.nextRevisionDateMs,
                        nextRevisionDate
                    )
                ) {
                    manager.cancel(topicId.hashCode())
                    return
                }

                val subjectName = db
                    ?.let { runCatching { it.subjectDao().getSubjectById(subjectId) }.getOrNull() }
                    ?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: subjectId.ifBlank { "Subject" }

                val chapterName = db
                    ?.let { runCatching { it.chapterDao().getChapterById(chapterId) }.getOrNull() }
                    ?.name
                    ?.takeIf { it.isNotBlank() }
                    ?: topic?.name?.takeIf { it.isNotBlank() }
                    ?: topicName

                val displayTopicName = topic?.name?.takeIf { it.isNotBlank() } ?: topicName

                NotificationHelper.createSrsChannel(context)

                val title = "Revision Due"
                val body = "Subject: $subjectName\nChapter: $chapterName\nTopic: $displayTopicName\nEstimated Time: $estimatedMins Minutes"

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
                    context, topicId.hashCode(), mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    context, topicId.hashCode() + 1, snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Skip Intent
                val skipIntent = Intent(context, SrsAlarmReceiver::class.java).apply {
                    action = ACTION_SKIP
                    putExtra("topicId", topicId)
                }
                val skipPending = PendingIntent.getBroadcast(
                    context, topicId.hashCode() + 2, skipIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, NotificationHelper.SRS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_revision)
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
        }
    }
}
