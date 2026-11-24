package com.example.data

import com.google.firebase.firestore.PropertyName
import com.example.ui.*



data class UserProfile(
    var id: String = "",
    var name: String = "",
    var bio: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var createdAtMs: Long = 0L,
    var lastActiveTimeMs: Long = 0L,
    @get:PropertyName("isPremium") @set:PropertyName("isPremium") var isPremium: Boolean = false,
    var role: String = "User", // "Admin", "User"
    @get:PropertyName("isBanned") @set:PropertyName("isBanned") var isBanned: Boolean = false,
    var banReason: String = "",
    var studyTimeMinutes: Int = 0,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var totalCoins: Int = 0,
    var totalRevisions: Int = 0,
    var focusSettings: FocusSettings = FocusSettings(),
    var referralCode: String = "",
    var referredBy: String = "",
    var referralCount: Int = 0,
    var referralCoinsEarned: Int = 0,
    @get:PropertyName("isEmailVerified") @set:PropertyName("isEmailVerified") var isEmailVerified: Boolean = false,
    @get:PropertyName("isMobileVerified") @set:PropertyName("isMobileVerified") var isMobileVerified: Boolean = false,
    var totalCoinsWithdrawn: Int = 0,
    var profilePictureUrl: String = "",
    var profileImageUrl: String = "",
    var revisionPattern: List<Int> = listOf(1, 3, 7, 14, 30),
    var totalTestsAttempted: Int = 0,
    var averageAccuracy: Float = 0f,
    var fcmToken: String = "",
    var airRank: Int = 0,
    var topicsCompleted: Int = 0,
    var badge: String = "Unranked",
    var league: String = "None",
    var rankPosition: Int? = null,
    @get:PropertyName("isRanked") @set:PropertyName("isRanked") var isRanked: Boolean = false
) {
    fun getOverallPerformanceScore(): Long {
        return (totalTestsAttempted * 10L) + 
               (averageAccuracy * 100).toLong() + 
               (studyTimeMinutes / 60 * 5L) + 
               (totalRevisions * 10L) + 
               (currentStreak * 50L) + 
               totalCoins
    }
}

data class FocusSettings(
    var focusDuration: Int = 25,
    var shortBreakDuration: Int = 5,
    var longBreakDuration: Int = 15,
    var autoStartFocus: Boolean = true,
    var autoStartBreak: Boolean = true,
    var vibrationEnabled: Boolean = true,
    var soundEnabled: Boolean = true,
    /** Alarm sound played when a focus/break session ends. */
    var completionSoundEnabled: Boolean = true,
    /** Spoken announcement when a focus/break session ends. */
    var voiceAnnouncementEnabled: Boolean = true,
    var ambientSound: String = "None",
    var dailyFocusGoalMins: Int = 120,
    var keepScreenAwake: Boolean = true,
    var selectedStudyMode: String = "Study Mode", // "Study Mode", "Revision Mode", "Practice Mode"
    var lastUsedMode: String = "Study Mode",
    var studyModeDurationMins: Int = 45,
    var revisionModeDurationMins: Int = 25,
    var practiceModeDurationMins: Int = 60,
    var mockTestModeDurationMins: Int = 180,
    var writingPracticeModeDurationMins: Int = 45
)

data class Subject(
    var id: String = "",
    var userId: String = "",
    var examId: String = "",
    var name: String = "",
    var colorHex: String = "#0061A4",
    var icon: String = "book",
    var totalTopicsExpected: Int = 0,
    @get:PropertyName("isArchived") @set:PropertyName("isArchived") var isArchived: Boolean = false 
)

data class Chapter(
    var id: String = "",
    var userId: String = "",
    var subjectId: String = "",
    var name: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    var revisionCount: Int = 0,
    var progress: Float = 0f,
    var lastRevisionDateMs: Long = 0L,
    var importance: String = "Medium", // "Low", "Medium", "High", "Very High"
    var notes: String = "",
    var scoreObtained: Float? = null,
    var scoreTotal: Float? = null,
    var colorHex: String = "",
    var icon: String = "",
    @get:PropertyName("isArchived") @set:PropertyName("isArchived") var isArchived: Boolean = false,
    var firstStudyDateMs: Long? = null,
    var lastStudyDateMs: Long? = null,
    var totalStudyTimeMs: Long = 0L,
    var totalRevisionTimeMs: Long = 0L,
    var nextRevisionDateMs: Long = 0L,
    var status: String = "Upcoming", // "Today", "Upcoming", "Overdue", "Completed"
)

data class SubjectTest(
    var id: String = "",
    var userId: String = "",
    var subjectId: String = "",
    var testName: String = "",
    var dateMs: Long = 0L,
    var marksObtained: Float = 0f,
    var totalMarks: Float = 0f,
    var remarks: String = ""
)

data class Topic(
    var id: String = "",
    var userId: String = "",
    var chapterId: String = "",
    var subjectId: String = "",
    var name: String = "",
    var notes: String = "",
    var difficulty: String = "Medium",
    var priority: String = "Medium",
    var estimatedRevisionTimeMins: Int = 30,
    var status: String = "Pending", // Pending, Complete
    var studyTimeMs: Long = 0L,
    var revisionTimeMs: Long = 0L,
    var revisionCount: Int = 0,
    var lastRevisionDateMs: Long = 0L,
    var nextRevisionDateMs: Long = 0L,
    var masteryScore: Int = 0,
    var examIds: List<String> = emptyList(),
    var revisionPattern: List<Int>? = null,
    var startDateMs: Long? = null,
    var tags: List<String> = emptyList(),
    
    // Explicit fields requested in prompt
    var firstStudyDate: Long? = null,
    var firstStudyTime: String? = null,
    var firstStudyDuration: Long = 0L,
    var totalRevisions: Int = 0,
    var totalRevisionCount: Int = 0,
    var lastRevisionDate: Long? = null,
    var lastRevisionTime: String? = null,
    var lastRevisionDuration: Long = 0L,
    var nextRevisionDate: Long? = null,
    var revisionStatus: String? = null,
    var progressPercentage: Int = 0,
    var expectedRevisionTime: Int = 30,
    var currentRevisionStage: Int = 0,
    var customSpacingPattern: List<Int>? = null,
    var expectedStudyTime: Long = 0L,
    var customSRS: List<Int>? = null,
    var createdAt: Long? = null,
    var updatedAt: Long? = null,
    var studyCompleted: Boolean = false,
    var completedRevisions: Int = 0,
    var totalRequiredRevisions: Int = 4,
    var progressScore: Int = 0
)

fun calculateProgressScore(studyCompleted: Boolean, completedRevisions: Int, totalRequiredRevisions: Int): Int {
    if (!studyCompleted) return 0
    if (totalRequiredRevisions <= 0 || completedRevisions >= totalRequiredRevisions) return 100
    var progress = 20 + ((completedRevisions.toDouble() / totalRequiredRevisions) * 80).toInt()
    return progress.coerceIn(20, 100)
}

fun formatDurationSpent(ms: Long): String {
    if (ms <= 0) return "0 min"
    var totalMins = kotlin.math.ceil(ms / 60000.0).toLong().coerceAtLeast(1L)
    var hrs = totalMins / 60
    var mins = totalMins % 60
    return if (hrs > 0) {
        if (mins > 0) "${hrs} hr ${mins} min" else "${hrs} hr"
    } else {
        "${mins} min"
    }
}

data class ExamCountdown(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var examDateMs: Long? = null,
    var targetStudyHours: Int? = null,
    var createdAtMs: Long = 0L,
    var progress: Float = 0f,
    var category: String = "",
    var targetRank: String = "",
    var totalSubjects: Int = 0
)

// Daily study log for analytics
data class StudyLog(
    var id: String = "",
    var userId: String = "",
    var dateString: String = "", // e.g. 2026-06-09
    var studyTimeMs: Long = 0L,
    var revisionTimeMs: Long = 0L,
    var focusSessions: Int = 0,
    var deepFocusTimeMs: Long = 0L,
    var longestSessionMs: Long = 0L,
    var totalDistractions: Int = 0,
    var sessionCount: Int = 0 
)

data class DayActivity(var day: String, var hours: Float, var sessions: Int = 0)
data class SubjectBreakdown(var name: String, var percentage: Int, var colorCode: String, var timeStr: String)
data class SubjectPerformance(var name: String, var percentage: Int, var colorCode: String, var icon: String)
data class SubjectProgress(
    var id: String = "",
    var name: String, 
    var progress: Float, 
    var percentage: String, 
    var icon: String, 
    var colorCode: String,
    var totalTopics: Int = 0,
    var completedTopics: Int = 0,
    var pendingTopics: Int = 0,
    var studyTimeMins: Int = 0,
    var revisionDueTopics: Int = 0,
    var completionPercentage: Int = 0,
    var examReadinessMeter: Int = 0
)

data class AnalyticsMetrics(
    var totalStudyTimeStr: String = "0h 0m",
    var productivityScore: Int = 0,
    var focusSessions: Int = 0,
    var coinsEarned: Int = 0,

    var dailyHours: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    var maxDailyHours: Float = 8f,

    var subjectProgress: List<SubjectProgress> = emptyList(),

    var totalRevisions: Int = 0,
    var completedTopics: Int = 0,
    var pendingTopics: Int = 0,
    var retentionRate: Int = 0,

    var deepFocusTimeStr: String = "0h 0m",
    var averageSessionStr: String = "0 min",
    var longestSessionStr: String = "0h 0m",
    var distractionCount: Int = 0,

    var currentStreak: Int = 0,
    var bestStreak: Int = 0,

    var targetExamName: String = "N/A",
    var readinessPercentage: Int = 0,
    var expectedScoreRange: String = "0 - 0%",

    var daysCompleted: Int = 0,
    var dayStreak: Int = 0,
    var averageScore: Int = 0,
    var overallProgressPercentage: Int = 0,
    var overallCompletionPercentage: Int = 0,
    var examReadiness: Int = 0,
    var studyTimeBreakdown: List<SubjectBreakdown> = emptyList(),
    var subjectPerformance: List<SubjectPerformance> = emptyList(),
    var weeklyActivity: List<DayActivity> = emptyList(),
    var totalTimeIncreasePercentage: Int = 0,
    var daysCompletedIncreasePercentage: Int = 0,
    var scoreIncreasePercentage: Int = 0,
    var recentSessions: List<RevisionHistoryRecord> = emptyList() 
)

data class CoinTransaction(
    var id: String = "",
    var userId: String = "",
    var amount: Int = 0,
    var description: String = "",
    var timestampMs: Long = 0L,
    var type: String = "EARNED" // "EARNED", "SPENT"
)

data class TopicWithSubjectAndChapter(
    var topicId: String = "",
    var topicName: String = "",
    var notes: String = "",
    var difficulty: String = "Medium",
    var estimatedRevisionTimeMins: Int = 30,
    var status: String = "",
    var studyTimeMs: Long = 0L,
    var revisionTimeMs: Long = 0L,
    var revisionCount: Int = 0,
    var lastRevisionDateMs: Long = 0L,
    var nextRevisionDateMs: Long = 0L,
    var masteryScore: Int = 0,
    var chapterId: String = "",
    var chapterName: String = "",
    var subjectId: String = "",
    var subjectName: String = "",
    var subjectColorHex: String = "",
    var examIds: List<String> = emptyList(),
    var revisionPattern: List<Int>? = null,
    var startDateMs: Long? = null,
    var tags: List<String> = emptyList(),
    var totalTopicsExpected: Int = 0,
    
    // Explicit fields requested in prompt
    var firstStudyDate: Long? = null,
    var firstStudyTime: String? = null,
    var firstStudyDuration: Long = 0L,
    var totalRevisions: Int = 0,
    var totalRevisionCount: Int = 0,
    var lastRevisionDate: Long? = null,
    var lastRevisionTime: String? = null,
    var lastRevisionDuration: Long = 0L,
    var nextRevisionDate: Long? = null,
    var revisionStatus: String? = null,
    var progressPercentage: Int = 0,
    var expectedRevisionTime: Int = 30,
    var currentRevisionStage: Int = 0,
    var customSpacingPattern: List<Int>? = null,
    var expectedStudyTime: Long = 0L,
    var customSRS: List<Int>? = null,
    var createdAt: Long? = null,
    var updatedAt: Long? = null,
    var studyCompleted: Boolean = false,
    var completedRevisions: Int = 0,
    var totalRequiredRevisions: Int = 4,
    var progressScore: Int = 0
)

data class ReferralRecord(
    var id: String = "",
    var referrerId: String = "",
    var referrerName: String = "",
    var referredId: String = "",
    var referredEmail: String = "",
    var referredName: String = "",
    var status: String = "PENDING", // PENDING, VERIFIED, COMPLETED
    var createdAtMs: Long = 0L,
    var studyTimeWhenRewardedMins: Int = 0,
    var rewardCredited: Boolean = false
)

data class WithdrawalRequest(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var userEmail: String = "",
    var amountInInr: Int = 0,
    var coinsDeducted: Int = 0,
    var method: String = "UPI", // UPI, BANK
    var upiId: String = "",
    var bankAccountName: String = "",
    var bankAccountNumber: String = "",
    var bankIfscCode: String = "",
    var status: String = "PENDING", // PENDING, APPROVED, REJECTED
    var createdAtMs: Long = 0L,
    var updatedAtMs: Long = 0L,
    var rejectReason: String = "",
    @get:PropertyName("isFraudulent") @set:PropertyName("isFraudulent") var isFraudulent: Boolean = false
)

data class LeaderboardEntry(
    var userId: String = "",
    var userName: String = "",
    var studyTimeMinutes: Int = 0,
    var rank: Int = 0,
    var rewardCoins: Int = 0,
    var monthString: String = "" // "YYYY-MM"
)

data class AdminLog(
    var id: String = "",
    var adminId: String = "",
    var adminEmail: String = "",
    var action: String = "",
    var details: String = "",
    var timestampMs: Long = 0L
)

data class WalletInfo(
    var userId: String = "",
    var totalCoinsEarned: Int = 0,
    var totalCoinsWithdrawn: Int = 0,
    var totalEarnedInr: Int = 0,
    var totalWithdrawnInr: Int = 0
)

data class RevisionPlan(
    var topicId: String = "",
    var intervals: List<Int> = emptyList(),
    var mode: String = "SMART" // "SMART" or "CUSTOM"
)

data class RevisionSettings(
    var firstRevisionDays: Int = 1,
    var secondRevisionDays: Int = 3,
    var thirdRevisionDays: Int = 7,
    var fourthRevisionDays: Int = 14,
    var fifthRevisionDays: Int = 30,
    var dailyGoal: Int = 20,
    var smartRevisionEnabled: Boolean = true,
    var showCompletedTopics: Boolean = true,
    var showStreak: Boolean = true,
    var showProgress: Boolean = true,
    var viewMode: String = "detailed"
)

data class TopicRevision(
    var id: String = "",
    var topicId: String = "",
    var topicName: String = "",
    var revisionCount: Int = 0,
    var lastRevisionDateMs: Long = 0L,
    var nextRevisionDateMs: Long = 0L
)

data class RevisionSchedule(
    var id: String = "",
    var topicId: String = "",
    var topicName: String = "",
    var scheduledDateMs: Long = 0L,
    var status: String = "PENDING" // "PENDING", "COMPLETED", "MISSED"
)

data class RevisionHistoryRecord(
    var id: String = "",
    var topicId: String = "",
    var topicName: String = "",
    var completedDateMs: Long = 0L,
    var revisionNumber: Int = 0,
    var durationMs: Long = 0L,
    var notes: String = ""
)

data class RevisionNotification(
    var id: String = "",
    var topicId: String = "",
    var title: String = "",
    var message: String = "",
    var timestampMs: Long = 0L,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    var type: String = "DUE" // "DUE", "MISSED", "BROADCAST"
)

data class NotificationBroadcast(
    var id: String = "",
    var title: String = "",
    var message: String = "",
    var audience: String = "",
    var timestampMs: Long = 0L,
    var targetCount: Int = 0,
    var deliveredCount: Int = 0,
    var openedCount: Int = 0,
    var failedCount: Int = 0
)

data class GlobalAppConfig(
    @get:PropertyName("isBetaFeatureEnabled") @set:PropertyName("isBetaFeatureEnabled") var isBetaFeatureEnabled: Boolean = false,
    @get:PropertyName("isMaintenanceModeEnabled") @set:PropertyName("isMaintenanceModeEnabled") var isMaintenanceModeEnabled: Boolean = false,
    var minimumAppVersion: Int = 1,
    var serverCacheGeneration: Long = 0L,
    var activeAnnouncementTitle: String = "",
    var activeAnnouncementMessage: String = "",
    var activeAnnouncementTimestampMs: Long = 0L,
    var maintenanceMessage: String = "App is currently under maintenance. Please try again later.",
    @get:PropertyName("isNotificationsEnabled") @set:PropertyName("isNotificationsEnabled") var isNotificationsEnabled: Boolean = true,
    @get:PropertyName("isRevisionRemindersEnabled") @set:PropertyName("isRevisionRemindersEnabled") var isRevisionRemindersEnabled: Boolean = true,
    @get:PropertyName("isAnnouncementsEnabled") @set:PropertyName("isAnnouncementsEnabled") var isAnnouncementsEnabled: Boolean = true,
    @get:PropertyName("isStreakSystemEnabled") @set:PropertyName("isStreakSystemEnabled") var isStreakSystemEnabled: Boolean = true,
    var activeTimeBaselineMinutes: Int = 20,
    var allowNewRegistration: Boolean = true,
    var coinsPerBaselineTime: Int = 1,
    var forceLogoutGeneration: Long = 0L,
    @get:PropertyName("isLeaderboardEnabled") @set:PropertyName("isLeaderboardEnabled") var isLeaderboardEnabled: Boolean = true
)


data class DashboardMetrics(
    var pendingRevisions: List<TopicWithSubjectAndChapter> = emptyList(),
    var todayDue: List<TopicWithSubjectAndChapter> = emptyList(),
    var upcomingRevisions: List<TopicWithSubjectAndChapter> = emptyList(),
    var completedTotal: List<TopicWithSubjectAndChapter> = emptyList(),
    var studyTimeTodayStr: String = "0h 0m",
    var weakTopics: List<TopicWithSubjectAndChapter> = emptyList(),
    var todayRevisedCount: Int = 0,
    var allRevisions: List<Revision> = emptyList(),
    var pendingTopics: List<TopicWithSubjectAndChapter> = emptyList()
)


data class AdminDashboardMetrics(
    var totalUsers: Int = 0,
    var activeUsers: Int = 0,
    var onlineUsers: Int = 0,
    var totalRevisions: Int = 0,
    var totalStudyTimeMinutes: Int = 0,
    var totalCoinsEarned: Int = 0
)

data class Revision(
    var id: String = "",
    var userId: String = "",
    var subjectId: String = "",
    var chapterId: String = "",
    var topicId: String = "",
    var subjectName: String = "",
    var chapterName: String = "",
    var topicName: String = "",
    var revisionCount: Int = 0,
    var pattern: List<Int> = listOf(1, 3, 7, 14, 30),
    var lastRevisionDateMs: Long = 0L,
    var nextRevisionDateMs: Long = 0L,
    var status: String = "Pending",
    var notes: String = "",
    var studyDate: String = "",
    var studyTime: String = "",
    var duration: Int = 0,
    var priority: String = "Medium",
    var createdAt: Long = 0L
)


data class AnalyticsQuadruple(
    var topics: List<TopicWithSubjectAndChapter>,
    var activeId: String,
    var logs: List<StudyLog>,
    var tests: List<SubjectTest>
)

data class AppNotification(
    var id: String = "",
    var topicId: String = "",
    var title: String = "",
    var message: String = "",
    var timestampMs: Long = 0L,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    var type: String = "System",
    var notificationType: String = "",
    var examId: String = "",
    var subjectId: String = "",
    var chapterId: String = "",
    var revisionId: String = ""
)

enum class SrsRating(val value: Int, val label: String) {
    AGAIN(1, "Again"),
    HARD(2, "Hard"),
    GOOD(3, "Good"),
    EASY(4, "Easy")
}

data class Flashcard(
    var id: String = "",
    var userId: String = "",
    var subjectId: String = "",
    var chapterId: String = "",
    var topicId: String = "",
    var front: String = "",
    var back: String = "",
    var hint: String = "",
    var difficulty: String = "Medium",
    var intervalDays: Int = 1,
    var easeFactor: Float = 2.5f,
    var repetitionCount: Int = 0,
    var lapses: Int = 0,
    var nextReviewDateMs: Long = 0L,
    var lastReviewedMs: Long = 0L,
    var createdAtMs: Long = 0L
)

data class FlashcardStats(
    var totalCards: Int = 0,
    var dueToday: Int = 0,
    var masteredCards: Int = 0,
    var learningCards: Int = 0,
    var reviewCards: Int = 0,
    var reviewedTodayCount: Int = 0,
    var retentionRatePercentage: Int = 100
)

