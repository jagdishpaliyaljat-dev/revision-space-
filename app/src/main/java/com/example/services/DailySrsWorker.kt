package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.MyApplication
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.data.local.TopicEntity
import java.util.Calendar
import java.util.UUID

class DailySrsWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = MyApplication.database
            val now = System.currentTimeMillis()
            val startOfToday = getStartOfDay(now)
            val endOfToday = startOfToday + 86400000L

            val topics = db.topicDao().getTopicsList("offline_user")
            val subjects = db.subjectDao().getSubjectsList("offline_user").associateBy { it.id }

            val todayTopics = mutableListOf<TopicEntity>()
            val overdueTopics = mutableListOf<TopicEntity>()

            for (topic in topics) {
                if (topic.nextRevisionDateMs != 0L) {
                    if (topic.nextRevisionDateMs in startOfToday until endOfToday) {
                        todayTopics.add(topic)
                    } else if (topic.nextRevisionDateMs < startOfToday) {
                        overdueTopics.add(topic)
                    }
                }
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createChannels(manager)

            // 1. Overdue Notifications
            for (topic in overdueTopics) {
                val daysOverdue = (now - topic.nextRevisionDateMs) / 86400000L
                if (daysOverdue > 0) {
                    val subjectName = subjects[topic.subjectId]?.name ?: "Subject"
                    val title = "Pending Revision: ${topic.name}"
                    val body = "Subject: $subjectName • Overdue by $daysOverdue days (1, 3, 7, 14, 30 days pattern)"
                    sendNotification(manager, topic.id.hashCode(), title, body, topic)

                    db.appNotificationDao().insert(
                        AppNotificationEntity(
                            id = UUID.randomUUID().toString(),
                            userId = "offline_user",
                            topicId = topic.id,
                            subjectId = topic.subjectId,
                            chapterId = topic.chapterId,
                            title = title,
                            message = body,
                            timestampMs = now,
                            type = "Revision",
                            notificationType = "overdue"
                        )
                    )
                }
            }

            // 2. Today's Due Topics
            if (todayTopics.isNotEmpty()) {
                val totalTopics = todayTopics.size
                val estimatedMins = todayTopics.sumOf { it.estimatedRevisionTimeMins }
                val sampleNames = todayTopics.take(2).joinToString(", ") { it.name }

                val title = "Today's Revision Mission ($totalTopics Topics)"
                val body = "Topics: $sampleNames\nEstimated Time: ${estimatedMins} mins (1, 3, 7, 14, 30 days schedule)"

                sendNotification(manager, "today_mission".hashCode(), title, body, todayTopics.firstOrNull())

                db.appNotificationDao().insert(
                    AppNotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "offline_user",
                        title = title,
                        message = body,
                        timestampMs = now,
                        type = "Revision",
                        notificationType = "daily_mission"
                    )
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success()
        }
    }

    private fun getStartOfDay(timeMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun createChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "srs_channel",
                "SRS Study Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders for topic & subject revisions"
            }
            channel.setBypassDnd(true)
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(
        manager: NotificationManager,
        id: Int,
        title: String,
        body: String,
        topic: TopicEntity?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "today_revision")
            if (topic != null) {
                putExtra("open_topic_id", topic.id)
                putExtra("subjectId", topic.subjectId)
                putExtra("chapterId", topic.chapterId)
                putExtra("topicId", topic.id)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "srs_channel")
            .setSmallIcon(R.drawable.ic_stat_revision)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(id, builder.build())
    }
}
