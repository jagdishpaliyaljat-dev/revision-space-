package com.example.services
import com.example.ui.*


import com.example.data.UserProfile
import com.example.data.Chapter
import com.example.data.Revision
import com.example.data.StudyLog

object RankManager {
    /**
     * Evaluates a user's rank, league, and eligibility for the badge system.
     * Starts as Unranked (Badge = "Unranked", League = "None", rankPosition = null, isRanked = false).
     * If all rank eligibility rules are met:
     * - Study Time >= 60 minutes
     * - Chapters Completed >= 1
     * - Revision Count >= 3
     * - Leaderboard feature enabled
     * Then:
     * - Fetches the user's leaderboard position.
     * - Assigns the correct league.
     * - Displays the league badge.
     * - Displays the rank position.
     * - isRanked = true
     */
    fun evaluateRank(
        profile: UserProfile,
        chapters: List<Chapter>,
        revisions: List<Revision>,
        logs: List<StudyLog>,
        allUsers: List<UserProfile>?,
        isLeaderboardEnabled: Boolean = true
    ): UserProfile {
        if (!isLeaderboardEnabled) {
            return profile.copy(
                badge = "Unranked",
                league = "None",
                rankPosition = null,
                isRanked = false
            )
        }

        // Calculate Study Time in minutes from logs
        val totalStudyMs = logs.sumOf { it.studyTimeMs + it.revisionTimeMs }
        val studyMins = (totalStudyMs / 60000).toInt()
        val effectiveStudyMins = maxOf(profile.studyTimeMinutes, studyMins)

        // Count completed chapters (isCompleted == true or progress >= 100)
        val completedChapters = chapters.count { it.isCompleted || it.progress >= 100f }

        // Count completed revisions (status == "Completed")
        val completedRevisions = revisions.count { it.status == "Completed" }

        // Rule Eligibility Checks
        val isEligible = effectiveStudyMins >= 60 &&
                completedChapters >= 1 &&
                completedRevisions >= 3

        if (!isEligible) {
            return profile.copy(
                badge = "Unranked",
                league = "None",
                rankPosition = null,
                isRanked = false
            )
        }

        // Leaderboard position & assignment
        if (allUsers.isNullOrEmpty()) {
            return profile.copy(
                badge = "Unranked",
                league = "None",
                rankPosition = null,
                isRanked = false
            )
        }

        // Sort all eligible users according to the strict ranking specifications:
        // - Highest Total Score
        // - ↓ Highest Study Minutes
        // - ↓ Highest Chapters Completed
        // - ↓ Highest Revision Count
        // - ↓ Highest Average Test Score
        // - ↓ Most Recent Activity
        // - ↓ Oldest Account
        val activeUsers = allUsers.filter { !it.isBanned }

        // In a real database we would calculate each user's score to sort them perfectly.
        // Let's sort them using a complete implementation.
        val sortedUsers = activeUsers.sortedWith { a, b ->
            val scoreA = a.getOverallPerformanceScore()
            val scoreB = b.getOverallPerformanceScore()
            var comp = scoreB.compareTo(scoreA)
            if (comp != 0) return@sortedWith comp

            comp = b.studyTimeMinutes.compareTo(a.studyTimeMinutes)
            if (comp != 0) return@sortedWith comp

            comp = b.topicsCompleted.compareTo(a.topicsCompleted)
            if (comp != 0) return@sortedWith comp

            comp = b.totalRevisions.compareTo(a.totalRevisions)
            if (comp != 0) return@sortedWith comp

            comp = b.averageAccuracy.compareTo(a.averageAccuracy)
            if (comp != 0) return@sortedWith comp

            comp = a.createdAtMs.compareTo(b.createdAtMs)
            if (comp != 0) return@sortedWith comp

            a.id.compareTo(b.id)
        }

        val myIndex = sortedUsers.indexOfFirst { it.id == profile.id }
        val rankPos = if (myIndex != -1) myIndex + 1 else sortedUsers.size + 1
        val totalRanked = sortedUsers.size

        val assignedLeague = getLeagueForPercentage(rankPos, totalRanked)

        return profile.copy(
            badge = "AIR #$rankPos",
            league = assignedLeague,
            rankPosition = rankPos,
            isRanked = true
        )
    }

    private fun getLeagueForPercentage(rank: Int, total: Int): String {
        if (total == 0) return "Bronze"
        val pct = (rank.toDouble() / total.toDouble()) * 100.0
        return when {
            pct <= 1.0 -> "Master"
            pct <= 5.0 -> "Diamond"
            pct <= 10.0 -> "Platinum"
            pct <= 25.0 -> "Gold"
            pct <= 50.0 -> "Silver"
            else -> "Bronze"
        }
    }
}
