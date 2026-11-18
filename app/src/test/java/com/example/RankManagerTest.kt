package com.example

import com.example.data.UserProfile
import com.example.data.Chapter
import com.example.data.Revision
import com.example.data.StudyLog
import com.example.services.RankManager
import org.junit.Assert.*
import org.junit.Test

class RankManagerTest {

    @Test
    fun testNewUserDefaultState() {
        val newUser = UserProfile(
            id = "user_new",
            name = "Newbie",
            email = "newbie@example.com"
        )

        // Verify newly initialized user starts with:
        // - Badge = Unranked
        // - League = None
        // - Rank Position = Null
        // - isRanked = false
        assertEquals("Unranked", newUser.badge)
        assertEquals("None", newUser.league)
        assertNull(newUser.rankPosition)
        assertFalse(newUser.isRanked)
    }

    @Test
    fun testEligibleUserRanking() {
        val user = UserProfile(
            id = "user_eligible",
            name = "Elite Learner",
            studyTimeMinutes = 65,
            totalCoins = 1000
        )

        val chapters = listOf(
            Chapter(id = "ch1", isCompleted = true, progress = 100f)
        )

        val revisions = listOf(
            Revision(id = "rev1", status = "Completed"),
            Revision(id = "rev2", status = "Completed"),
            Revision(id = "rev3", status = "Completed")
        )

        val logs = listOf(
            StudyLog(id = "log1", studyTimeMs = 40 * 60000L),
            StudyLog(id = "log2", studyTimeMs = 25 * 60000L) // total 65 mins
        )

        // Other users on the leaderboard to compare with
        val leaderboardUsers = listOf(
            UserProfile(id = "user1", name = "Rank 1 User", totalCoins = 5000), // higher score
            user.copy() // our user
        )

        val evaluated = RankManager.evaluateRank(
            profile = user,
            chapters = chapters,
            revisions = revisions,
            logs = logs,
            allUsers = leaderboardUsers,
            isLeaderboardEnabled = true
        )

        // Since there is 1 user with a higher score, our user is ranked #2 out of 2 (100% of the active pool).
        // 100% rank falls into the "Bronze" league under the percentage system.
        assertTrue(evaluated.isRanked)
        assertEquals("Bronze", evaluated.league)
        assertEquals("AIR #2", evaluated.badge)
        assertEquals(2, evaluated.rankPosition)
    }

    @Test
    fun testIneligibleUserStudyTime() {
        val user = UserProfile(
            id = "user_short_time",
            studyTimeMinutes = 50
        )

        val chapters = listOf(
            Chapter(id = "ch1", isCompleted = true, progress = 100f)
        )

        val revisions = listOf(
            Revision(id = "rev1", status = "Completed"),
            Revision(id = "rev2", status = "Completed"),
            Revision(id = "rev3", status = "Completed")
        )

        val logs = listOf(
            StudyLog(id = "log1", studyTimeMs = 50 * 60000L) // 50 mins (less than 60)
        )

        val evaluated = RankManager.evaluateRank(
            profile = user,
            chapters = chapters,
            revisions = revisions,
            logs = logs,
            allUsers = emptyList(),
            isLeaderboardEnabled = true
        )

        // Should return Unranked because study time < 60 minutes
        assertFalse(evaluated.isRanked)
        assertEquals("Unranked", evaluated.badge)
        assertEquals("None", evaluated.league)
        assertNull(evaluated.rankPosition)
    }

    @Test
    fun testIneligibleUserChapters() {
        val user = UserProfile(
            id = "user_no_chapters",
            studyTimeMinutes = 90
        )

        // No completed chapters
        val chapters = listOf(
            Chapter(id = "ch1", isCompleted = false, progress = 50f)
        )

        val revisions = listOf(
            Revision(id = "rev1", status = "Completed"),
            Revision(id = "rev2", status = "Completed"),
            Revision(id = "rev3", status = "Completed")
        )

        val logs = listOf(
            StudyLog(id = "log1", studyTimeMs = 90 * 60000L)
        )

        val evaluated = RankManager.evaluateRank(
            profile = user,
            chapters = chapters,
            revisions = revisions,
            logs = logs,
            allUsers = emptyList(),
            isLeaderboardEnabled = true
        )

        assertFalse(evaluated.isRanked)
        assertEquals("Unranked", evaluated.badge)
    }

    @Test
    fun testLeaderboardUnavailable() {
        val user = UserProfile(
            id = "user_eligible",
            studyTimeMinutes = 120
        )

        val chapters = listOf(
            Chapter(id = "ch1", isCompleted = true, progress = 100f)
        )

        val revisions = listOf(
            Revision(id = "rev1", status = "Completed"),
            Revision(id = "rev2", status = "Completed"),
            Revision(id = "rev3", status = "Completed")
        )

        val logs = listOf(
            StudyLog(id = "log1", studyTimeMs = 120 * 60000L)
        )

        // Leaderboard data is unavailable (null)
        val evaluated = RankManager.evaluateRank(
            profile = user,
            chapters = chapters,
            revisions = revisions,
            logs = logs,
            allUsers = null,
            isLeaderboardEnabled = true
        )

        // Must safely handle null leaderboard, and never crash or show null fields as "null" string
        assertFalse(evaluated.isRanked)
        assertEquals("Unranked", evaluated.badge)
        assertEquals("None", evaluated.league)
        assertNull(evaluated.rankPosition)
    }

    @Test
    fun testLeaderboardDisabled() {
        val user = UserProfile(
            id = "user_eligible",
            studyTimeMinutes = 120
        )

        val chapters = listOf(
            Chapter(id = "ch1", isCompleted = true, progress = 100f)
        )

        val revisions = listOf(
            Revision(id = "rev1", status = "Completed"),
            Revision(id = "rev2", status = "Completed"),
            Revision(id = "rev3", status = "Completed")
        )

        val logs = listOf(
            StudyLog(id = "log1", studyTimeMs = 120 * 60000L)
        )

        val leaderboardUsers = listOf(user)

        // Leaderboard feature disabled
        val evaluated = RankManager.evaluateRank(
            profile = user,
            chapters = chapters,
            revisions = revisions,
            logs = logs,
            allUsers = leaderboardUsers,
            isLeaderboardEnabled = false
        )

        assertFalse(evaluated.isRanked)
        assertEquals("Unranked", evaluated.badge)
    }

    @Test
    fun testDuplicateScoresSortingHierarchy() {
        // Tie-breaker order:
        // 1. Score (Tie)
        // 2. Study minutes (Tie-breaker 1)
        // 3. Topics Completed (Tie-breaker 2)
        // 4. Revision Count (Tie-breaker 3)
        // 5. Average accuracy (Tie-breaker 4)
        // 6. CreatedAt (Tie-breaker 5)
        
        val user1 = UserProfile(id = "user1", studyTimeMinutes = 100, topicsCompleted = 10, totalRevisions = 5, averageAccuracy = 90f, createdAtMs = 1000L)
        val user2 = UserProfile(id = "user2", studyTimeMinutes = 120, topicsCompleted = 10, totalRevisions = 5, averageAccuracy = 90f, createdAtMs = 1000L) // winner due to higher study mins
        val user3 = UserProfile(id = "user3", studyTimeMinutes = 120, topicsCompleted = 15, totalRevisions = 5, averageAccuracy = 90f, createdAtMs = 1000L) // winner over 2 due to topics completed
        val user4 = UserProfile(id = "user4", studyTimeMinutes = 120, topicsCompleted = 15, totalRevisions = 8, averageAccuracy = 90f, createdAtMs = 1000L) // winner over 3 due to revisions count
        val user5 = UserProfile(id = "user5", studyTimeMinutes = 120, topicsCompleted = 15, totalRevisions = 8, averageAccuracy = 95f, createdAtMs = 1000L) // winner over 4 due to average accuracy
        val user6 = UserProfile(id = "user6", studyTimeMinutes = 120, topicsCompleted = 15, totalRevisions = 8, averageAccuracy = 95f, createdAtMs = 500L)  // winner over 5 due to older account (createdAtMs)

        val leaderboard = listOf(user1, user2, user3, user4, user5, user6)
        
        val chapters = listOf(Chapter(id = "ch1", isCompleted = true, progress = 100f))
        val revisions = listOf(Revision(id = "rev1", status = "Completed"), Revision(id = "rev2", status = "Completed"), Revision(id = "rev3", status = "Completed"))
        val logs = listOf(StudyLog(id = "l1", studyTimeMs = 60 * 60000L))

        // user6 must be ranked #1
        val evaluatedUser6 = RankManager.evaluateRank(user6, chapters, revisions, logs, leaderboard, true)
        assertEquals(1, evaluatedUser6.rankPosition)
        assertEquals("AIR #1", evaluatedUser6.badge)

        // user5 must be ranked #2
        val evaluatedUser5 = RankManager.evaluateRank(user5, chapters, revisions, logs, leaderboard, true)
        assertEquals(2, evaluatedUser5.rankPosition)

        // user1 must be ranked #6
        val evaluatedUser1 = RankManager.evaluateRank(user1, chapters, revisions, logs, leaderboard, true)
        assertEquals(6, evaluatedUser1.rankPosition)
    }

    @Test
    fun testScaleTenThousandUsers() {
        val totalUsers = 10000
        val targetUser = UserProfile(id = "my_user", studyTimeMinutes = 200)
        
        val fakeUsers = List(totalUsers - 1) { i ->
            UserProfile(id = "user_$i", studyTimeMinutes = i) // score is derived from studyTimeMinutes etc.
        }
        val fullLeaderboard = fakeUsers + targetUser

        val chapters = listOf(Chapter(id = "ch1", isCompleted = true, progress = 100f))
        val revisions = listOf(Revision(id = "rev1", status = "Completed"), Revision(id = "rev2", status = "Completed"), Revision(id = "rev3", status = "Completed"))
        val logs = listOf(StudyLog(id = "l1", studyTimeMs = 60 * 60000L))

        val tStart = System.currentTimeMillis()
        val evaluated = RankManager.evaluateRank(targetUser, chapters, revisions, logs, fullLeaderboard, true)
        val tEnd = System.currentTimeMillis()

        assertNotNull(evaluated.rankPosition)
        assertTrue(evaluated.isRanked)
        // Scaling performance must be fast
        assertTrue("Sorting 10k users should complete quickly", (tEnd - tStart) < 1000)
    }
}
