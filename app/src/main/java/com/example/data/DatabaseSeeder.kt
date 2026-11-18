package com.example.data

import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    private val SEEDED_SUBJECT_NAMES = setOf(
        "Reasoning", "Mathematics", "English", "Hindi", "Computer",
        "General Science", "Current Affairs", "Indian History", "Rajasthan History", "Indian Polity",
        "Rajasthan Polity", "Geography", "Rajasthan Geography", "Economy", "Miscellaneous"
    )

    private val SEEDED_TEST_REMARKS = setOf(
        "Excellent performance, high accuracy in questions.",
        "Good attempt. Work on time management.",
        "Superb command over formulas.",
        "Average score. Focus on weak areas."
    )

    private val SEEDED_NOTIFICATION_MESSAGES = setOf(
        "Your scheduled revision for today is now overdue. Complete it to avoid losing your progress.",
        "Fantastic job! You have hit your daily study goal and earned 20 Coins.",
        "Your 15-day study streak has been safely maintained. Keep up the high focus!",
        "Check out the new Revision Management dashboard update with visual stats.",
        "A new revision has been scheduled based on your active spaced repetition pattern."
    )

    suspend fun purgeDemoData(
        context: android.content.Context? = try { com.example.MyApplication.instance.applicationContext } catch (e: Throwable) { null },
        database: com.example.data.local.AppDatabase? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val rDb = database ?: try { com.example.MyApplication.database } catch (e: Throwable) { null } ?: return@withContext
            val uid = "offline_user"

            val allTopics = rDb.topicDao().getTopicsList(uid)
            val seededTopics = allTopics.filter { t ->
                t.id.startsWith("demo_") ||
                    t.notes.startsWith("Key formulas, critical diagrams, and important conceptual notes for ") ||
                    (t.name.contains(" - Ch ") && t.name.contains(" - Concept ")) ||
                    t.name in setOf("Swar Sandhi", "Vyanjan Sandhi", "Visarga Sandhi")
            }

            if (seededTopics.isNotEmpty()) {
                val sharedPrefs = context?.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE)
                val editor = sharedPrefs?.edit()
                for (t in seededTopics) {
                    rDb.topicDao().delete(t.id)
                    rDb.revisionDao().deleteByTopic(t.id)
                    rDb.revisionHistoryDao().deleteByTopic(t.id)
                    editor?.remove("topic_bookmark_${t.id}")
                }
                editor?.apply()
            }

            val remainingTopics = rDb.topicDao().getTopicsList(uid)
            val remainingChapterIdsWithTopics = remainingTopics.map { it.chapterId }.toSet()

            val allChapters = rDb.chapterDao().getChaptersList(uid)
            for (ch in allChapters) {
                val isSeededChapter = ch.id.startsWith("demo_") || ch.name.contains(" - Chapter ") || ch.name == "Sandhi"
                if (isSeededChapter && ch.id !in remainingChapterIdsWithTopics) {
                    rDb.chapterDao().delete(ch.id)
                }
            }

            val remainingChapters = rDb.chapterDao().getChaptersList(uid)
            val remainingSubjectIds = (remainingChapters.map { it.subjectId } + remainingTopics.map { it.subjectId }).toSet()

            val allSubjects = rDb.subjectDao().getSubjectsList(uid)
            for (sub in allSubjects) {
                val isSeededSubject = sub.id.startsWith("demo_") || (sub.totalTopicsExpected == 40 && sub.name in SEEDED_SUBJECT_NAMES) || sub.name == "Hindi"
                if (isSeededSubject && sub.id !in remainingSubjectIds) {
                    rDb.subjectDao().delete(sub.id)
                }
            }

            // Remove seeded demo tests & notifications via SQL
            val writableDb = rDb.openHelper.writableDatabase
            SEEDED_TEST_REMARKS.forEach { remark ->
                writableDb.execSQL("DELETE FROM subject_tests WHERE remarks = ?", arrayOf<Any>(remark))
            }
            SEEDED_NOTIFICATION_MESSAGES.forEach { msg ->
                writableDb.execSQL("DELETE FROM app_notifications WHERE message = ?", arrayOf<Any>(msg))
            }

            // If seeded profile stats (longestStreak = 42, totalRevisions = 1000, totalCoins = 1500) exist, reset them and clear demo study logs
            val profile = rDb.userProfileDao().getProfileSync(uid)
            if (seededTopics.isNotEmpty() || (profile != null && profile.longestStreak == 42 && profile.totalRevisions >= 1000)) {
                if (remainingTopics.isEmpty()) {
                    rDb.studyLogDao().deleteAllForUserSync(uid)
                    rDb.revisionDao().deleteAllForUserSync(uid)
                    rDb.revisionHistoryDao().deleteAllForUserSync(uid)
                }
                if (profile != null) {
                    rDb.userProfileDao().insert(
                        profile.copy(
                            studyTimeMinutes = 0,
                            currentStreak = 0,
                            longestStreak = 0,
                            totalRevisions = remainingTopics.sumOf { it.revisionCount },
                            totalCoins = 0
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generate(
        viewModel: RevisionViewModel? = null,
        context: android.content.Context? = try { com.example.MyApplication.instance.applicationContext } catch (e: Throwable) { null },
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {},
        logProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        try {
            purgeDemoData(context)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onFailure(e)
            }
        }
    }
}
