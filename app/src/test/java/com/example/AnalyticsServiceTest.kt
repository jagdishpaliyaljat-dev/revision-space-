package com.example

import com.example.data.Revision
import com.example.data.StudyLog
import com.example.data.TopicWithSubjectAndChapter
import com.example.services.AnalyticsService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AnalyticsServiceTest {

    private val analyticsService = AnalyticsService()

    private fun getStartOfDayMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    @Test
    fun testCalculateDashboardMetricsWithActiveExamFilter() {
        val startOfToday = getStartOfDayMs()
        
        // 1. Create mock topics: Topic 1 belongs to examA, Topic 2 belongs to examB
        val topic1 = TopicWithSubjectAndChapter(
            topicId = "topic_1",
            topicName = "Algebra",
            examIds = listOf("examA"),
            subjectId = "sub_math",
            subjectName = "Mathematics"
        )
        val topic2 = TopicWithSubjectAndChapter(
            topicId = "topic_2",
            topicName = "Grammar",
            examIds = listOf("examB"),
            subjectId = "sub_english",
            subjectName = "English"
        )
        val topics = listOf(topic1, topic2)

        // 2. Create mock revisions for both topics, both scheduled for today
        val rev1 = Revision(
            id = "rev_1",
            topicId = "topic_1",
            nextRevisionDateMs = startOfToday + 1000,
            status = "Pending"
        )
        val rev2 = Revision(
            id = "rev_2",
            topicId = "topic_2",
            nextRevisionDateMs = startOfToday + 1000,
            status = "Pending"
        )
        val revisions = listOf(rev1, rev2)

        // Case A: Filtered by Exam A
        val metricsExamA = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "examA",
            logs = emptyList(),
            revisions = revisions
        )
        
        // Should only see due topic from Exam A
        assertEquals(1, metricsExamA.todayDue.size)
        assertEquals("topic_1", metricsExamA.todayDue[0].topicId)

        // Case B: Filtered by Exam B
        val metricsExamB = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "examB",
            logs = emptyList(),
            revisions = revisions
        )
        
        // Should only see due topic from Exam B
        assertEquals(1, metricsExamB.todayDue.size)
        assertEquals("topic_2", metricsExamB.todayDue[0].topicId)

        // Case C: Unfiltered (activeId = "all")
        val metricsAll = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "all",
            logs = emptyList(),
            revisions = revisions
        )
        
        // Should see both
        assertEquals(2, metricsAll.todayDue.size)
    }

    @Test
    fun testCalculateDashboardMetricsFiltersCompletedStatus() {
        val startOfToday = getStartOfDayMs()
        
        val topic = TopicWithSubjectAndChapter(
            topicId = "topic_1",
            topicName = "Algebra",
            examIds = listOf("examA")
        )
        
        // Revision has status = "Completed" (should be excluded from pending/due even if nextDate is past/today)
        val completedRevision = Revision(
            id = "rev_1",
            topicId = "topic_1",
            nextRevisionDateMs = startOfToday - 50000, // past date
            status = "Completed"
        )
        
        val metrics = analyticsService.calculateDashboardMetrics(
            allTopics = listOf(topic),
            activeId = "all",
            logs = emptyList(),
            revisions = listOf(completedRevision)
        )
        
        assertTrue(metrics.todayDue.isEmpty())
        assertTrue(metrics.pendingRevisions.isEmpty())
    }

    @Test
    fun testCalculateAnalyticsMetricsWithActiveExamFilter() {
        val startOfToday = getStartOfDayMs()

        val topic1 = TopicWithSubjectAndChapter(
            topicId = "topic_1",
            topicName = "Physics L1",
            examIds = listOf("examA"),
            subjectId = "sub_physics",
            subjectName = "Physics",
            masteryScore = 80
        )
        val topic2 = TopicWithSubjectAndChapter(
            topicId = "topic_2",
            topicName = "Chemistry L1",
            examIds = listOf("examB"),
            subjectId = "sub_chem",
            subjectName = "Chemistry",
            masteryScore = 90
        )
        val topics = listOf(topic1, topic2)

        val rev1 = Revision(
            id = "rev_1",
            topicId = "topic_1",
            nextRevisionDateMs = startOfToday - 5000,
            status = "Pending",
            subjectId = "sub_physics"
        )
        val rev2 = Revision(
            id = "rev_2",
            topicId = "topic_2",
            nextRevisionDateMs = startOfToday - 5000,
            status = "Pending",
            subjectId = "sub_chem"
        )
        val revisions = listOf(rev1, rev2)

        // Analyze for Exam A
        val analyticsA = analyticsService.calculateAnalyticsMetrics(
            allTopics = topics,
            activeId = "examA",
            logs = emptyList(),
            profile = null,
            timeFilter = "All Time",
            revisions = revisions
        )

        // Physics has 1 pending revision due. Chemistry's revision should NOT count for Exam A.
        val physicsProgress = analyticsA.subjectProgress.find { it.id == "sub_physics" }
        val chemistryProgress = analyticsA.subjectProgress.find { it.id == "sub_chem" }

        assertTrue(physicsProgress != null)
        assertEquals(1, physicsProgress?.revisionDueTopics)
        assertTrue(chemistryProgress == null) // Chemistry is not in Exam A, so its progress isn't listed under Exam A Progress list!
    }

    @Test
    fun testCalculateDashboardMetricsWithEstimatedRevisionTimes() {
        val startOfToday = getStartOfDayMs()
        
        val topic1 = TopicWithSubjectAndChapter(
            topicId = "topic_1",
            topicName = "Algebra",
            examIds = listOf("examA"),
            estimatedRevisionTimeMins = 45
        )
        val topic2 = TopicWithSubjectAndChapter(
            topicId = "topic_2",
            topicName = "Grammar",
            examIds = listOf("examA"),
            estimatedRevisionTimeMins = 30
        )
        val topics = listOf(topic1, topic2)

        val rev1 = Revision(
            id = "rev_1",
            topicId = "topic_1",
            nextRevisionDateMs = startOfToday + 1000,
            status = "Pending"
        )
        val rev2 = Revision(
            id = "rev_2",
            topicId = "topic_2",
            nextRevisionDateMs = startOfToday + 1000,
            status = "Pending"
        )
        val revisions = listOf(rev1, rev2)

        val metrics = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "all",
            logs = emptyList(),
            revisions = revisions
        )
        
        assertEquals(2, metrics.todayDue.size)
        
        val totalEstimatedMins = metrics.todayDue.sumOf { it.estimatedRevisionTimeMins }
        assertEquals(75, totalEstimatedMins)
        
        val completedRev1 = rev1.copy(
            lastRevisionDateMs = startOfToday + 5000,
            nextRevisionDateMs = startOfToday + 86400000L * 3
        )
        
        val metricsAfterCompletion = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "all",
            logs = emptyList(),
            revisions = listOf(completedRev1, rev2)
        )
        
        assertEquals(1, metricsAfterCompletion.todayDue.size)
        assertEquals("topic_2", metricsAfterCompletion.todayDue[0].topicId)
        
        val totalEstimatedMinsAfter = metricsAfterCompletion.todayDue.sumOf { it.estimatedRevisionTimeMins }
        assertEquals(30, totalEstimatedMinsAfter)
        
        assertEquals(1, metricsAfterCompletion.todayRevisedCount)
    }

    @Test
    fun testUpcomingRevisionFilteringAndSorting() {
        val startOfToday = getStartOfDayMs()
        val endOfToday = startOfToday + 86400000L - 1

        val topicA = TopicWithSubjectAndChapter(topicId = "topic_a", topicName = "A_Algebra")
        val topicB = TopicWithSubjectAndChapter(topicId = "topic_b", topicName = "B_Calculus")
        val topicC = TopicWithSubjectAndChapter(topicId = "topic_c", topicName = "C_Geometry")
        val topicD = TopicWithSubjectAndChapter(topicId = "topic_d", topicName = "D_History")
        val topicE = TopicWithSubjectAndChapter(topicId = "topic_e", topicName = "E_Physics")
        val topics = listOf(topicA, topicB, topicC, topicD, topicE)

        val revOverdue = Revision(
            id = "rev_overdue",
            topicId = "topic_a",
            nextRevisionDateMs = startOfToday - 10000,
            status = "Pending"
        )
        val revToday = Revision(
            id = "rev_today",
            topicId = "topic_b",
            nextRevisionDateMs = startOfToday + 1000,
            status = "Pending"
        )
        val revUpcomingLater = Revision(
            id = "rev_upcoming_later",
            topicId = "topic_c",
            nextRevisionDateMs = endOfToday + 86400000L * 2, // 2 days later
            status = "Pending"
        )
        val revUpcomingEarlierNameZ = Revision(
            id = "rev_upcoming_earlier_z",
            topicId = "topic_d",
            nextRevisionDateMs = endOfToday + 86400000L, // tomorrow
            status = "Pending"
        )
        val revUpcomingEarlierNameA = Revision(
            id = "rev_upcoming_earlier_a",
            topicId = "topic_e",
            nextRevisionDateMs = endOfToday + 86400000L, // tomorrow, same day as earlier_z but alphabetical name E
            status = "Pending"
        )

        val metrics = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "all",
            logs = emptyList(),
            revisions = listOf(revOverdue, revToday, revUpcomingLater, revUpcomingEarlierNameZ, revUpcomingEarlierNameA)
        )

        val upcoming = metrics.upcomingRevisions
        assertEquals(3, upcoming.size)

        // Check correct detection (no overdue, no today, only > endOfToday)
        assertTrue(upcoming.none { it.topicId == "topic_a" }) // Overdue excluded
        assertTrue(upcoming.none { it.topicId == "topic_b" }) // Today excluded

        // Check Sorting:
        // Expected order:
        // Index 0: topic_d (D_History) -> tomorrow, name D
        // Index 1: topic_e (E_Physics) -> tomorrow, name E (alphabetically after D)
        // Index 2: topic_c (C_Geometry) -> 2 days later
        assertEquals("topic_d", upcoming[0].topicId)
        assertEquals("topic_e", upcoming[1].topicId)
        assertEquals("topic_c", upcoming[2].topicId)
    }

    @Test
    fun testOverdueRevisionFilteringAndSorting() {
        val startOfToday = getStartOfDayMs()
        val endOfToday = startOfToday + 86400000L - 1

        val topicA = TopicWithSubjectAndChapter(topicId = "topic_a", topicName = "A_Algebra")
        val topicB = TopicWithSubjectAndChapter(topicId = "topic_b", topicName = "B_Calculus")
        val topicC = TopicWithSubjectAndChapter(topicId = "topic_c", topicName = "C_Geometry")
        val topicD = TopicWithSubjectAndChapter(topicId = "topic_d", topicName = "D_History")
        val topicE = TopicWithSubjectAndChapter(topicId = "topic_e", topicName = "E_Physics")
        val topics = listOf(topicA, topicB, topicC, topicD, topicE)

        val revOverdue1 = Revision(
            id = "rev_overdue_1",
            topicId = "topic_a",
            nextRevisionDateMs = startOfToday - 86400000L * 2, // 2 days overdue
            status = "Pending"
        )
        val revOverdue2Z = Revision(
            id = "rev_overdue_2z",
            topicId = "topic_d",
            nextRevisionDateMs = startOfToday - 86400000L, // 1 day overdue, name D
            status = "Pending"
        )
        val revOverdue2A = Revision(
            id = "rev_overdue_2a",
            topicId = "topic_e",
            nextRevisionDateMs = startOfToday - 86400000L, // 1 day overdue, same date, name E
            status = "Pending"
        )
        val revToday = Revision(
            id = "rev_today",
            topicId = "topic_b",
            nextRevisionDateMs = startOfToday + 1000,
            status = "Pending"
        )
        val revUpcoming = Revision(
            id = "rev_upcoming",
            topicId = "topic_c",
            nextRevisionDateMs = endOfToday + 1000,
            status = "Pending"
        )

        val metrics = analyticsService.calculateDashboardMetrics(
            allTopics = topics,
            activeId = "all",
            logs = emptyList(),
            revisions = listOf(revOverdue1, revOverdue2Z, revOverdue2A, revToday, revUpcoming)
        )

        val overdue = metrics.pendingRevisions
        assertEquals(3, overdue.size)

        // Check correct detection (no today, no upcoming)
        assertTrue(overdue.none { it.topicId == "topic_b" }) // Today excluded
        assertTrue(overdue.none { it.topicId == "topic_c" }) // Upcoming excluded

        // Check Sorting:
        // Expected order:
        // Index 0: topic_a (A_Algebra) -> 2 days overdue (oldest nextRevisionDateMs)
        // Index 1: topic_d (D_History) -> 1 day overdue, name D
        // Index 2: topic_e (E_Physics) -> 1 day overdue, name E
        assertEquals("topic_a", overdue[0].topicId)
        assertEquals("topic_d", overdue[1].topicId)
        assertEquals("topic_e", overdue[2].topicId)
    }

    @Test
    fun testRevisionEngineIntervalCalculations() {
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.JANUARY, 10, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDateMs = calendar.timeInMillis

        // 1, 3, 7 days pattern
        val pattern = listOf(1, 3, 7)
        val timestamps = com.example.services.RevisionEngine.calculateRevisionTimestamps(pattern, startDateMs)

        assertEquals(3, timestamps.size)

        // Day + 1 -> Jan 11
        calendar.timeInMillis = timestamps[0]
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(11, calendar.get(Calendar.DAY_OF_MONTH))

        // Day + 3 -> Jan 13
        calendar.timeInMillis = timestamps[1]
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(13, calendar.get(Calendar.DAY_OF_MONTH))

        // Day + 7 -> Jan 17
        calendar.timeInMillis = timestamps[2]
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(17, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testRevisionEngineLeapYearHandling() {
        val calendar = Calendar.getInstance()
        // 2024 is a Leap Year. Let's start on Feb 28, 2024
        calendar.set(2024, Calendar.FEBRUARY, 28, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDateMs = calendar.timeInMillis

        val pattern = listOf(1, 2)
        val timestamps = com.example.services.RevisionEngine.calculateRevisionTimestamps(pattern, startDateMs)

        // Feb 28 + 1 Day = Feb 29, 2024 (Leap Day)
        calendar.timeInMillis = timestamps[0]
        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH))
        assertEquals(29, calendar.get(Calendar.DAY_OF_MONTH))

        // Feb 28 + 2 Days = March 1, 2024
        calendar.timeInMillis = timestamps[1]
        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testRevisionEngineMonthEndTransition() {
        val calendar = Calendar.getInstance()
        // Start on October 31, 2026
        calendar.set(2026, Calendar.OCTOBER, 31, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDateMs = calendar.timeInMillis

        val pattern = listOf(1)
        val timestamps = com.example.services.RevisionEngine.calculateRevisionTimestamps(pattern, startDateMs)

        // Oct 31 + 1 Day = Nov 1, 2026
        calendar.timeInMillis = timestamps[0]
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testRevisionEngineYearTransition() {
        val calendar = Calendar.getInstance()
        // Start on December 31, 2026
        calendar.set(2026, Calendar.DECEMBER, 31, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDateMs = calendar.timeInMillis

        val pattern = listOf(1)
        val timestamps = com.example.services.RevisionEngine.calculateRevisionTimestamps(pattern, startDateMs)

        // Dec 31 + 1 Day = Jan 1, 2027
        calendar.timeInMillis = timestamps[0]
        assertEquals(2027, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testRevisionPatternValidation() {
        val validate = { input: String -> com.example.services.RevisionEngine.validatePatternInput(input) }

        // 1. Success cases
        assertTrue(validate("1, 3, 7, 15, 30").first)
        assertTrue(validate("1, 2, 4, 8").first)
        assertTrue(validate("2, 5, 10, 20").first)
        assertTrue(validate("1, 3, 5, 7, 14, 21, 28, 45, 60").first)
        assertTrue(validate("1").first) // Single interval

        // 2. Empty pattern
        val emptyResult = validate("")
        assertFalse(emptyResult.first)
        assertEquals("Pattern cannot be empty", emptyResult.second)

        // 3. Duplicate values
        val duplicateResult = validate("1, 3, 3, 7")
        assertFalse(duplicateResult.first)
        assertEquals("Duplicate intervals are not allowed", duplicateResult.second)

        // 4. Negative numbers
        val negativeResult = validate("1, 3, -5, 7")
        assertFalse(negativeResult.first)
        assertEquals("Intervals must be positive integers: -5", negativeResult.second)

        // 5. Zero value
        val zeroResult = validate("0, 2, 4")
        assertFalse(zeroResult.first)
        assertEquals("Intervals must be positive integers: 0", zeroResult.second)

        // 6. Decimal values
        val decimalResult = validate("1, 2.5, 4")
        assertFalse(decimalResult.first)
        assertEquals("Decimal values are not allowed: '2.5'", decimalResult.second)

        // 7. Alphabetic input
        val alphaResult = validate("1, abc, 5")
        assertFalse(alphaResult.first)
        assertEquals("Invalid number format: 'abc'", alphaResult.second)

        // 8. Special characters
        val specialResult = validate("1, 3#, 7")
        assertFalse(specialResult.first)
        assertEquals("Invalid number format: '3#'", specialResult.second)

        // 9. Very large values (e.g. over 10 years / 3650 days or overflow)
        val overflowResult = validate("1, 9999999999999, 7")
        assertFalse(overflowResult.first)
        assertEquals("Invalid number format: '9999999999999'", overflowResult.second)

        val tooLargeResult = validate("1, 5000")
        assertFalse(tooLargeResult.first)
        assertEquals("Interval value too large (max 3650 days): 5000", tooLargeResult.second)

        // 10. Non-increasing/decreasing order
        val decreasingResult = validate("5, 3, 1")
        assertFalse(decreasingResult.first)
        assertEquals("Intervals must be in strictly increasing order (e.g., 1, 3, 7)", decreasingResult.second)
    }
}
