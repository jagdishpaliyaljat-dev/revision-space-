package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.data.local.AppDatabase
import com.example.data.local.toEntity
import com.example.services.RevisionEngine
import com.example.ui.TimerRoute
import com.example.ui.TopicDetailRoute
import com.example.ui.screens.subject.TopicDetailScreen
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], application = MyApplication::class)
class TopicDetailsFunctionalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var inMemoryRoomDb: AppDatabase
    private lateinit var repository: RevisionRepository
    private lateinit var viewModel: RevisionViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        inMemoryRoomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = object : RevisionRepository() {
            override val roomDb: AppDatabase get() = inMemoryRoomDb
            override val userId: String get() = "offline_user"
            override suspend fun generateTestDataIfEmpty() {}
        }

        runBlocking {
            inMemoryRoomDb.subjectDao().insert(
                Subject(
                    id = "sub_1",
                    userId = "offline_user",
                    name = "Physics",
                    colorHex = "#6B4EFF"
                ).toEntity()
            )
            inMemoryRoomDb.chapterDao().insert(
                Chapter(
                    id = "chap_1",
                    userId = "offline_user",
                    subjectId = "sub_1",
                    name = "Mechanics"
                ).toEntity()
            )
            inMemoryRoomDb.userProfileDao().insert(
                UserProfile(
                    id = "offline_user",
                    name = "Student",
                    revisionPattern = listOf(1, 3, 7, 14, 30)
                ).toEntity()
            )
        }

        viewModel = RevisionViewModel(repository)
    }

    @After
    fun tearDown() {
        inMemoryRoomDb.close()
    }

    private suspend fun insertTestTopic(
        id: String = "topic_1",
        name: String = "Kinematics",
        difficulty: String = "Medium",
        pattern: List<Int> = listOf(1, 3, 7, 14, 30),
        revisionCount: Int = 0,
        firstStudyDate: Long? = null,
        firstStudyDuration: Long = 0L,
        lastRevisionDateMs: Long = 0L,
        nextRevisionDateMs: Long = 0L
    ): Topic {
        val topic = Topic(
            id = id,
            userId = "offline_user",
            subjectId = "sub_1",
            chapterId = "chap_1",
            name = name,
            difficulty = difficulty,
            estimatedRevisionTimeMins = 30,
            revisionPattern = pattern,
            customSpacingPattern = pattern,
            revisionCount = revisionCount,
            totalRevisions = revisionCount,
            totalRevisionCount = revisionCount,
            completedRevisions = revisionCount,
            firstStudyDate = firstStudyDate,
            firstStudyTime = if (firstStudyDuration > 0L) formatDurationSpent(firstStudyDuration) else null,
            firstStudyDuration = firstStudyDuration,
            startDateMs = firstStudyDate,
            lastRevisionDateMs = lastRevisionDateMs,
            lastRevisionDate = lastRevisionDateMs.takeIf { it > 0L },
            nextRevisionDateMs = nextRevisionDateMs,
            nextRevisionDate = nextRevisionDateMs.takeIf { it > 0L },
            studyCompleted = firstStudyDate != null || revisionCount > 0
        )
        inMemoryRoomDb.topicDao().insert(topic.toEntity())
        return topic
    }

    // Scenario 1, 2, 3: Select Easy / Medium / Hard difficulty and click Save Revision -> Difficulty & preset intervals saved without incrementing Revision Count
    @Test
    fun testScenarios1_2_3_SaveDifficultyLevels_PersistsWithoutIncrementingRevisionCount() = runBlocking {
        insertTestTopic(id = "topic_diff")

        // Test 1: Easy
        val easyIntervals = listOf(2, 4, 10, 21, 30)
        val easyNext = RevisionEngine.calculateNextRevisionTimestamp(0, easyIntervals, 100000L, 100000L)
        viewModel.updateTopicSpacedRepetitionSuspend("topic_diff", "Easy", easyIntervals, easyNext, 100000L)
        var saved = repository.getTopicById("topic_diff")!!
        assertEquals("Easy", saved.difficulty)
        assertEquals(easyIntervals, saved.revisionPattern)
        assertEquals(0, saved.revisionCount)
        assertEquals(0, saved.totalRevisions)

        // Test 2: Medium
        val mediumIntervals = listOf(1, 3, 7, 14, 30)
        val mediumNext = RevisionEngine.calculateNextRevisionTimestamp(0, mediumIntervals, 100000L, 100000L)
        viewModel.updateTopicSpacedRepetitionSuspend("topic_diff", "Medium", mediumIntervals, mediumNext, 100000L)
        saved = repository.getTopicById("topic_diff")!!
        assertEquals("Medium", saved.difficulty)
        assertEquals(mediumIntervals, saved.revisionPattern)
        assertEquals(0, saved.revisionCount)

        // Test 3: Hard
        val hardIntervals = listOf(1, 2, 5, 10, 21)
        val hardNext = RevisionEngine.calculateNextRevisionTimestamp(0, hardIntervals, 100000L, 100000L)
        viewModel.updateTopicSpacedRepetitionSuspend("topic_diff", "Hard", hardIntervals, hardNext, 100000L)
        saved = repository.getTopicById("topic_diff")!!
        assertEquals("Hard", saved.difficulty)
        assertEquals(hardIntervals, saved.revisionPattern)
        assertEquals(0, saved.revisionCount)
    }

    // Scenario 4 & 5: Modify custom spacing intervals, click Save Revision, and reload topic -> Updated intervals persist
    @Test
    fun testScenarios4_5_CustomIntervalsAndReloadPersistence() = runBlocking {
        val baseTime = System.currentTimeMillis() - 86400000L
        insertTestTopic(id = "topic_custom", firstStudyDate = baseTime, firstStudyDuration = 25 * 60000L)

        val customPattern = listOf(2, 5, 9, 18, 35)
        val expectedNext = RevisionEngine.calculateNextRevisionTimestamp(0, customPattern, baseTime, baseTime)
        viewModel.updateTopicSpacedRepetitionSuspend("topic_custom", "Medium", customPattern, expectedNext, baseTime)

        // Reload via new ViewModel instance to verify persistence
        val reloadedVm = RevisionViewModel(repository)
        val reloadedTopic = repository.getTopicById("topic_custom")!!
        assertEquals(customPattern, reloadedTopic.revisionPattern)
        assertEquals(customPattern, reloadedTopic.customSpacingPattern)
        assertEquals(expectedNext, reloadedTopic.nextRevisionDateMs)
        assertEquals(0, reloadedTopic.revisionCount)
        assertNotNull(reloadedVm)
    }

    // Scenario 6 & 7: Start Timer, Pause, Resume -> Timer runs, elapsed time updates, and starting/pausing/resuming does NOT increment Revision Count
    @Test
    fun testScenarios6_7_StartPauseResumeTimer_DoesNotIncrementRevisionCount() = runBlocking {
        insertTestTopic(id = "topic_timer")

        viewModel.startTimer(
            chapterId = "chap_1",
            subjectId = "sub_1",
            subjectName = "Physics",
            chapterName = "Mechanics",
            topicId = "topic_timer"
        )
        assertTrue(viewModel.isRunning.value)
        assertFalse(viewModel.isPaused.value)
        assertEquals("topic_timer", viewModel.activeTimerTopicId.value)

        // Simulate elapsed time
        viewModel.timerElapsedMs.value = 15000L
        assertEquals(15000L, viewModel.timerElapsedMs.value)

        // Pause
        viewModel.pauseTimer()
        assertTrue(viewModel.isPaused.value)
        assertFalse(viewModel.isTimerRunning.value)

        // Resume
        viewModel.resumeTimer()
        assertFalse(viewModel.isPaused.value)
        assertTrue(viewModel.isTimerRunning.value)

        // Verify Revision Count is still 0 during active/paused/resumed timer
        val topicDuringTimer = repository.getTopicById("topic_timer")!!
        assertEquals(0, topicDuringTimer.revisionCount)
        assertEquals(0, topicDuringTimer.totalRevisions)

        viewModel.resetTimer()
    }

    // Scenario 8, 9, 10, 11, 12: Complete 1st and 2nd revision sessions -> Verify Revision Count (0 -> 1 -> 2), First Study Date/Time, Last Revision Date/Time, Next Revision Date, and Revision History
    @Test
    fun testScenarios8_9_10_11_12_CompleteFirstAndSecondSessions_UpdatesAllStatsAndHistory() = runBlocking {
        insertTestTopic(id = "topic_sessions", pattern = listOf(1, 3, 7, 14, 30))

        // Session 1: Complete 30-minute timer session (0 -> 1)
        viewModel.startTimer(chapterId = "chap_1", subjectId = "sub_1", topicId = "topic_sessions")
        val session1Completed = viewModel.completeTopicTimerSessionSuspend(
            topicId = "topic_sessions",
            durationMs = 30 * 60 * 1000L
        )
        assertTrue(session1Completed)

        val afterSession1 = repository.getTopicById("topic_sessions")!!
        assertEquals(1, afterSession1.revisionCount)
        assertEquals(1, afterSession1.totalRevisions)
        assertEquals(1, afterSession1.completedRevisions)
        assertNotNull(afterSession1.firstStudyDate)
        assertTrue((afterSession1.firstStudyDate ?: 0L) > 0L)
        assertEquals("30 min", afterSession1.firstStudyTime)
        assertEquals(30 * 60 * 1000L, afterSession1.firstStudyDuration)
        assertTrue(afterSession1.lastRevisionDateMs > 0L)
        assertEquals("30 min", afterSession1.lastRevisionTime)

        val firstStudyTimestamp = afterSession1.firstStudyDate!!
        val expectedNext1 = RevisionEngine.calculateNextRevisionTimestamp(
            revisionCount = 1,
            pattern = listOf(1, 3, 7, 14, 30),
            firstStudyMs = firstStudyTimestamp,
            lastRevisionMs = afterSession1.lastRevisionDateMs
        )
        assertEquals(expectedNext1, afterSession1.nextRevisionDateMs)

        // Session 2: Complete 45-minute timer session (1 -> 2)
        viewModel.startTimer(chapterId = "chap_1", subjectId = "sub_1", topicId = "topic_sessions")
        val session2Completed = viewModel.completeTopicTimerSessionSuspend(
            topicId = "topic_sessions",
            durationMs = 45 * 60 * 1000L
        )
        assertTrue(session2Completed)

        val afterSession2 = repository.getTopicById("topic_sessions")!!
        assertEquals(2, afterSession2.revisionCount)
        assertEquals(2, afterSession2.totalRevisions)
        assertEquals(2, afterSession2.completedRevisions)
        // First Study Date and First Study Time must NEVER change after initial recording
        assertEquals(firstStudyTimestamp, afterSession2.firstStudyDate)
        assertEquals("30 min", afterSession2.firstStudyTime)
        // Last Revision Time reflects latest 45 min session
        assertEquals("45 min", afterSession2.lastRevisionTime)
        assertTrue(afterSession2.lastRevisionDateMs >= afterSession1.lastRevisionDateMs)

        val expectedNext2 = RevisionEngine.calculateNextRevisionTimestamp(
            revisionCount = 2,
            pattern = listOf(1, 3, 7, 14, 30),
            firstStudyMs = firstStudyTimestamp,
            lastRevisionMs = afterSession2.lastRevisionDateMs
        )
        assertEquals(expectedNext2, afterSession2.nextRevisionDateMs)

        // Verify Revision History has both sessions
        val history = viewModel.getRevisionHistoryForTopic("topic_sessions").first()
        assertEquals(2, history.size)
        assertTrue(history.any { it.revisionNumber == 1 && it.durationMs == 30 * 60 * 1000L })
        assertTrue(history.any { it.revisionNumber == 2 && it.durationMs == 45 * 60 * 1000L })
    }

    // Scenario 13 & 14: Rapid repeated clicks on Save Revision (5x) and Start Timer / Complete Session (5x)
    @Test
    fun testScenarios13_14_RapidRepeatedClicks_IdempotencyAndNoDuplicates() = runBlocking {
        insertTestTopic(id = "topic_rapid")

        // Scenario 13: Rapidly invoke Save Revision 5 times concurrently
        val pattern = listOf(2, 4, 10, 21, 30)
        val nextMs = System.currentTimeMillis() + 2 * 86400000L
        val jobs = (1..5).map {
            async {
                viewModel.updateTopicSpacedRepetitionSuspend("topic_rapid", "Easy", pattern, nextMs, 0L)
            }
        }
        jobs.awaitAll()

        val afterRapidSave = repository.getTopicById("topic_rapid")!!
        assertEquals(0, afterRapidSave.revisionCount)
        assertEquals("Easy", afterRapidSave.difficulty)
        val activeRevisions = repository.getRevisionsForTopic("topic_rapid").filter { it.status != "Completed" }
        assertEquals(1, activeRevisions.size)

        // Scenario 14: Rapidly click Start Timer 5 times and Complete Session 5 times with same session key
        repeat(5) {
            viewModel.startTimer(chapterId = "chap_1", subjectId = "sub_1", topicId = "topic_rapid")
            viewModel.timerElapsedMs.value = 5000L
        }
        val activeSessionKey = viewModel.activeTimerSessionId.value
        val completionResults = (1..5).map {
            async {
                viewModel.completeTopicTimerSessionSuspend("topic_rapid", 20 * 60 * 1000L, activeSessionKey)
            }
        }.awaitAll()

        // Exactly 1 of the 5 rapid completion calls succeeds; Revision Count increments by 1 (not 5)
        assertEquals(1, completionResults.count { it })
        val afterRapidComplete = repository.getTopicById("topic_rapid")!!
        assertEquals(1, afterRapidComplete.revisionCount)
        assertEquals(1, afterRapidComplete.totalRevisions)
    }

    // Scenario 15: Simulate database write failure -> Error shown, no false UI increment
    @Test
    fun testScenario15_DatabaseWriteFailure_ShowsErrorAndNoFalseIncrement() = runBlocking {
        insertTestTopic(id = "topic_fail")

        val failingRepo = object : RevisionRepository() {
            override val roomDb: AppDatabase get() = inMemoryRoomDb
            override val userId: String get() = "offline_user"
            override suspend fun generateTestDataIfEmpty() {}
            override suspend fun recordStudySessionTransaction(
                topicId: String,
                isRevision: Boolean,
                durationMs: Long,
                dateRaw: String
            ) {
                throw RuntimeException("Simulated disk write failure")
            }
        }
        val failingVm = RevisionViewModel(failingRepo)
        val success = failingVm.completeTopicTimerSessionSuspend("topic_fail", 25 * 60 * 1000L, "fail_key_1")
        assertFalse(success)
        assertNotNull(failingVm.syncError.value)

        val unchangedTopic = repository.getTopicById("topic_fail")!!
        assertEquals(0, unchangedTopic.revisionCount)
        assertEquals(0, unchangedTopic.totalRevisions)
    }

    // Scenario 16, 17, 18: Topic isolation, Existing historical records preservation, and Compose UI verification
    @Test
    fun testScenarios16_17_18_TopicIsolation_LegacyPreservation_AndUIVerification() {
        val now = System.currentTimeMillis()
        runBlocking {
            // Topic A: Existing topic with historical revisions
            insertTestTopic(
                id = "topic_A",
                name = "Thermodynamics",
                difficulty = "Hard",
                pattern = listOf(1, 2, 5, 10, 21),
                revisionCount = 3,
                firstStudyDate = now - 10 * 86400000L,
                firstStudyDuration = 40 * 60000L,
                lastRevisionDateMs = now - 2 * 86400000L,
                nextRevisionDateMs = now + 3 * 86400000L
            )
            // Topic B: Fresh topic
            insertTestTopic(
                id = "topic_B",
                name = "Optics",
                difficulty = "Easy",
                pattern = listOf(2, 4, 10, 21, 30),
                revisionCount = 0
            )

            val topicA = repository.getTopicById("topic_A")!!
            val topicB = repository.getTopicById("topic_B")!!
            assertEquals(3, topicA.revisionCount)
            assertEquals(0, topicB.revisionCount)
            assertEquals("Hard", topicA.difficulty)
            assertEquals("Easy", topicB.difficulty)
        }

        // Render TopicDetailScreen for Topic A and verify live data & buttons
        val aiVm = AiViewModel()
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = TopicDetailRoute(topicId = "topic_A", subjectId = "sub_1", chapterId = "chap_1")
            ) {
                composable<TopicDetailRoute> {
                    TopicDetailScreen(
                        viewModel = viewModel,
                        aiViewModel = aiVm,
                        navController = navController,
                        topicId = "topic_A",
                        subjectId = "sub_1",
                        chapterId = "chap_1"
                    )
                }
                composable<TimerRoute> { }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Thermodynamics").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_revision_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("start_timer_button").assertIsDisplayed().assertIsEnabled()

        // Click Save Revision button in the UI -> opens Manual Study/Revision Time dialog
        composeTestRule.onNodeWithTag("save_revision_button").performClick()
        composeTestRule.waitForIdle()

        // Verify dialog is displayed, select 25 min preset (or enter manual time), and click Confirm Save Revision
        composeTestRule.onNodeWithTag("manual_study_time_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("preset_time_25").performClick()
        composeTestRule.onNodeWithTag("confirm_save_revision_button").performClick()
        composeTestRule.waitForIdle()

        runBlocking {
            val updatedTopicA = repository.getTopicById("topic_A")!!
            assertEquals(4, updatedTopicA.revisionCount)
            assertEquals(4, updatedTopicA.totalRevisions)
            assertEquals("25 min", updatedTopicA.lastRevisionTime)
            assertEquals(25 * 60 * 1000L, updatedTopicA.lastRevisionDuration)
            val topicBUnchanged = repository.getTopicById("topic_B")!!
            assertEquals(0, topicBUnchanged.revisionCount)
        }
    }

    // Mass-Level Stress Test: Demo Data Purge + 50 Topics + Multi-Stage SRS + Manual Time + Exams/Tasks/Notifications
    @Test
    fun testMassLevelOperations_50Topics_SRSLifecycle_And_DemoDataPurge() = runBlocking {
        // 1. Verify Demo Data Purge removes all seeded/mock IDs while keeping real user data intact
        inMemoryRoomDb.subjectDao().insert(Subject(id = "demo_sub_1", userId = "offline_user", name = "Reasoning", totalTopicsExpected = 40).toEntity())
        inMemoryRoomDb.chapterDao().insert(Chapter(id = "demo_chap_1", userId = "offline_user", subjectId = "demo_sub_1", name = "Reasoning - Chapter 1").toEntity())
        inMemoryRoomDb.topicDao().insert(Topic(id = "demo_topic_1", userId = "offline_user", subjectId = "demo_sub_1", chapterId = "demo_chap_1", name = "Reasoning - Ch 1 - Concept 1").toEntity())
        inMemoryRoomDb.examDao().insert(ExamCountdown(id = "demo_exam_1", userId = "offline_user", name = "Demo Exam").toEntity())

        DatabaseSeeder.purgeDemoData(context = context, database = inMemoryRoomDb)

        assertNull(inMemoryRoomDb.subjectDao().getSubjects("offline_user").first().find { it.id == "demo_sub_1" })
        assertNull(inMemoryRoomDb.chapterDao().getChapterById("demo_chap_1"))
        assertNull(inMemoryRoomDb.topicDao().getTopicById("demo_topic_1"))
        // Real setup subject & chapter ("sub_1", "chap_1") must remain untouched
        assertNotNull(inMemoryRoomDb.chapterDao().getChapterById("chap_1"))

        // 2. Mass creation: 5 Subjects, 10 Chapters, 50 Topics
        for (s in 1..5) {
            inMemoryRoomDb.subjectDao().insert(Subject(id = "mass_sub_$s", userId = "offline_user", name = "Subject $s").toEntity())
        }
        for (c in 1..10) {
            val subId = "mass_sub_${((c - 1) % 5) + 1}"
            inMemoryRoomDb.chapterDao().insert(Chapter(id = "mass_chap_$c", userId = "offline_user", subjectId = subId, name = "Chapter $c").toEntity())
        }
        for (t in 1..50) {
            val chapIdx = ((t - 1) % 10) + 1
            val subIdx = ((chapIdx - 1) % 5) + 1
            insertTestTopic(
                id = "mass_topic_$t",
                name = "Mass Topic $t",
                difficulty = when (t % 3) {
                    0 -> "Hard"
                    1 -> "Easy"
                    else -> "Medium"
                },
                pattern = listOf(1, 3, 7, 14, 30)
            )
            // Link to mass subject & chapter
            val topic = repository.getTopicById("mass_topic_$t")!!
            repository.updateTopic(topic.copy(subjectId = "mass_sub_$subIdx", chapterId = "mass_chap_$chapIdx"))
        }

        val allTopics = inMemoryRoomDb.topicDao().getTopics("offline_user").first()
        assertTrue("Expected at least 50 topics in DB", allTopics.size >= 50)

        // 3. Full 6-Stage Spaced Repetition Lifecycle on mass_topic_1 with manual durations (20m, 25m, 30m, 15m, 45m, 60m)
        val manualMinutesList = listOf(20, 25, 30, 15, 45, 60)
        var firstRecordedDate: Long? = null
        for ((index, mins) in manualMinutesList.withIndex()) {
            val durationMs = mins * 60 * 1000L
            val sessionKey = "manual_mass_session_${index + 1}"
            val ok = viewModel.completeTopicTimerSessionSuspend("mass_topic_1", durationMs, sessionKey)
            assertTrue("Stage ${index + 1} revision should succeed", ok)

            val updated = repository.getTopicById("mass_topic_1")!!
            assertEquals(index + 1, updated.revisionCount)
            assertEquals(index + 1, updated.totalRevisions)
            assertEquals(formatDurationSpent(durationMs), updated.lastRevisionTime)
            assertEquals(durationMs, updated.lastRevisionDuration)

            if (index == 0) {
                firstRecordedDate = updated.firstStudyDate
                assertNotNull(firstRecordedDate)
                assertEquals("20 min", updated.firstStudyTime)
                assertEquals(20 * 60 * 1000L, updated.firstStudyDuration)
            } else {
                // First Study Date and First Study Time must NEVER be overwritten on subsequent revisions
                assertEquals(firstRecordedDate, updated.firstStudyDate)
                assertEquals("20 min", updated.firstStudyTime)
                assertEquals(20 * 60 * 1000L, updated.firstStudyDuration)
            }

            val expectedNext = RevisionEngine.calculateNextRevisionTimestamp(
                revisionCount = index + 1,
                pattern = listOf(1, 3, 7, 14, 30),
                firstStudyMs = firstRecordedDate!!,
                lastRevisionMs = updated.lastRevisionDateMs
            )
            assertEquals(expectedNext, updated.nextRevisionDateMs)
        }

        // 4. Concurrent Mass Revision Sessions across 25 distinct topics simultaneously
        val batchResults = (2..26).map { tIdx ->
            async {
                viewModel.completeTopicTimerSessionSuspend(
                    topicId = "mass_topic_$tIdx",
                    durationMs = 25 * 60 * 1000L,
                    sessionKey = "batch_key_$tIdx"
                )
            }
        }.awaitAll()
        assertEquals(25, batchResults.count { it })

        for (tIdx in 2..26) {
            val t = repository.getTopicById("mass_topic_$tIdx")!!
            assertEquals(1, t.revisionCount)
            assertEquals("25 min", t.lastRevisionTime)
        }

        // 5. Verify Quick Tasks, Exams, and Notifications persistence in Room DB
        for (q in 1..15) {
            repository.addQuickTask(QuickTask(id = "task_$q", userId = "offline_user", title = "Task $q", dateCreatedStr = "2026-11-06"))
        }
        val savedTasks = repository.allQuickTasks.first()
        assertEquals(15, savedTasks.size)

        repository.markAllNotificationsAsRead()
        assertEquals(0, repository.unreadNotificationsCount.first())
    }
}

