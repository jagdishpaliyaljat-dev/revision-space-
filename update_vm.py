import re

with open("app/src/main/java/com/example/viewmodel/RevisionViewModel.kt", "r") as f:
    content = f.read()

profile_stats_code = """
data class ProfileStats(
    val totalStudyMs: Long = 0L,
    val displayStudyHours: Long = 0L,
    val displayStudyMinutesOnly: Long = 0L,
    val displayStudyTimeStr: String = "0h 0m",
    val displayRevisions: Int = 0,
    val completedChaptersCount: Int = 0,
    val displayMastery: Int = 0
)

"""
if "data class ProfileStats" not in content:
    content = content.replace("class RevisionViewModel(", profile_stats_code + "class RevisionViewModel(")

stats_flow = """
    val profileStats: StateFlow<ProfileStats> = combine(
        allStudyLogs, allRevisions, allChapters, allSubjects
    ) { logs, revisions, chapters, subjects ->
        val totalStudyMs = logs.sumOf { it.studyTimeMs + it.revisionTimeMs }
        val displayStudyMins = totalStudyMs / 60000
        val displayStudyHours = displayStudyMins / 60
        val displayStudyMinutesOnly = displayStudyMins % 60
        val displayStudyTimeStr = "${displayStudyHours}h ${displayStudyMinutesOnly}m"
        
        val displayRevisions = revisions.count { it.status == "Completed" }
        val completedChaptersCount = chapters.count { it.isCompleted || it.progress >= 100f }
        
        val subjectProgressesList = subjects.map { subject ->
            val subjectChapters = chapters.filter { it.subjectId == subject.id && !it.isArchived }
            val chapterProgresses = subjectChapters.map { chapter ->
                val completedRevisions = revisions.count { it.chapterId == chapter.id && it.status == "Completed" }
                (completedRevisions.toFloat() / 10f * 100f).coerceIn(0f, 100f)
            }
            if (chapterProgresses.isNotEmpty()) chapterProgresses.average().toFloat() else 0f
        }
        val overallSyllabusCovered = if (subjectProgressesList.isNotEmpty()) subjectProgressesList.average().toFloat() else 0f
        
        val allActiveChaptersList = chapters.filter { !it.isArchived }
        val allChaptersReachedTen = allActiveChaptersList.isNotEmpty() && allActiveChaptersList.all { chapter ->
            val completedRevisions = revisions.count { it.chapterId == chapter.id && it.status == "Completed" }
            completedRevisions >= 10
        }
        val displayMastery = if (allChaptersReachedTen) {
            overallSyllabusCovered.toInt().coerceIn(0, 100)
        } else {
            minOf(overallSyllabusCovered.toInt(), 99).coerceIn(0, 100)
        }
        
        ProfileStats(
            totalStudyMs = totalStudyMs,
            displayStudyHours = displayStudyHours,
            displayStudyMinutesOnly = displayStudyMinutesOnly,
            displayStudyTimeStr = displayStudyTimeStr,
            displayRevisions = displayRevisions,
            completedChaptersCount = completedChaptersCount,
            displayMastery = displayMastery
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ProfileStats())
"""

if "val profileStats: StateFlow<ProfileStats>" not in content:
    content = content.replace("    val adminAllUsers: Flow<List<UserProfile>> =", stats_flow + "\n    val adminAllUsers: Flow<List<UserProfile>> =")

with open("app/src/main/java/com/example/viewmodel/RevisionViewModel.kt", "w") as f:
    f.write(content)

