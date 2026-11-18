package com.example.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MyApplication
import com.example.data.local.AppNotificationEntity
import java.util.Calendar
import java.util.UUID

class RevisionReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = MyApplication.database
            val now = System.currentTimeMillis()

            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endOfTodayMs = cal.timeInMillis

            val topics = db.topicDao().getTopicsList("offline_user")
            val subjects = db.subjectDao().getSubjectsList("offline_user").associateBy { it.id }

            val dueTopics = topics.filter { topic ->
                topic.nextRevisionDateMs in 1..endOfTodayMs
            }

            if (dueTopics.isNotEmpty()) {
                val firstTopic = dueTopics.first()
                val subjectName = subjects[firstTopic.subjectId]?.name ?: "Subject"
                
                NotificationHelper.showDueRevisionNotification(
                    context = context,
                    dueCount = dueTopics.size,
                    firstTopicName = firstTopic.name,
                    subjectName = subjectName,
                    revisionCount = firstTopic.revisionCount + 1
                )

                val notifTitle = if (dueTopics.size == 1) {
                    "Revision Due: ${firstTopic.name}"
                } else {
                    "${dueTopics.size} Revisions Due Today"
                }

                val notifMessage = "Subject: $subjectName • Next revision scheduled on 1, 3, 7, 14, 30-day pattern."

                db.appNotificationDao().insert(
                    AppNotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "offline_user",
                        topicId = firstTopic.id,
                        subjectId = firstTopic.subjectId,
                        chapterId = firstTopic.chapterId,
                        title = notifTitle,
                        message = notifMessage,
                        timestampMs = now,
                        type = "Revision",
                        notificationType = "scheduled_reminder"
                    )
                )
            } else {
                // Default motivational study reminder if no specific topics are due
                NotificationHelper.showRevisionReminder(context)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationHelper.showRevisionReminder(context)
            Result.success()
        }
    }
}
