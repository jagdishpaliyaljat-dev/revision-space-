import re

with open('app/src/main/java/com/example/services/AnalyticsService.kt', 'r') as f:
    content = f.read()

old_sig = """    fun calculateAnalyticsMetrics(
        allTopics: List<TopicWithSubjectAndChapter>,
        activeId: String,
        logs: List<StudyLog>,
        profile: UserProfile?,
        timeFilter: String,
        revisions: List<Revision>,
        tests: List<com.example.data.SubjectTest> = emptyList(),
        allChapters: List<Chapter> = emptyList(),
        allSubjects: List<Subject> = emptyList()
    ): AnalyticsMetrics {"""

new_sig = """    fun calculateAnalyticsMetrics(
        allTopics: List<TopicWithSubjectAndChapter>,
        activeId: String,
        logs: List<StudyLog>,
        profile: UserProfile?,
        timeFilter: String,
        revisions: List<Revision>,
        tests: List<com.example.data.SubjectTest> = emptyList(),
        allChapters: List<Chapter> = emptyList(),
        allSubjects: List<Subject> = emptyList(),
        recentHistory: List<com.example.data.RevisionHistoryRecord> = emptyList()
    ): AnalyticsMetrics {"""

content = content.replace(old_sig, new_sig)

old_return = """            currentStreak = profile?.currentStreak ?: 0,
            bestStreak = profile?.longestStreak ?: 0,
            targetExamName = profile?.targetExam ?: "N/A",
            daysCompleted = profile?.daysCompleted ?: 0,
            dayStreak = profile?.dayStreak ?: 0
        )
    }"""

new_return = """            currentStreak = profile?.currentStreak ?: 0,
            bestStreak = profile?.longestStreak ?: 0,
            targetExamName = profile?.targetExam ?: "N/A",
            daysCompleted = profile?.daysCompleted ?: 0,
            dayStreak = profile?.dayStreak ?: 0,
            recentSessions = recentHistory
        )
    }"""

content = content.replace(old_return, new_return)

with open('app/src/main/java/com/example/services/AnalyticsService.kt', 'w') as f:
    f.write(content)
