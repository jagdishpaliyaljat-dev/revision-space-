package com.example.services
import com.example.ui.*


import com.example.data.AnalyticsMetrics
import com.example.data.DashboardMetrics
import com.example.data.Revision
import com.example.data.Chapter
import com.example.data.Subject
import com.example.data.StudyLog
import com.example.data.TopicWithSubjectAndChapter
import com.example.data.UserProfile
import java.util.Calendar

class AnalyticsService {

    fun calculateDashboardMetrics(
        allTopics: List<TopicWithSubjectAndChapter>,
        activeId: String,
        logs: List<StudyLog>,
        revisions: List<Revision>
    ): DashboardMetrics {
        val topics = if (activeId.isEmpty() || activeId == "all") {
            allTopics
        } else {
            val matching = allTopics.filter { it.examIds.contains(activeId) }
            if (matching.isNotEmpty()) matching else allTopics
        }
        val todayStr = currentDayString()
        val todayLog = logs.find { it.dateString == todayStr }
        val todayStudyTimeMs = (todayLog?.studyTimeMs ?: 0L) + (todayLog?.revisionTimeMs ?: 0L)
        
        val startOfTodayMs = getStartOfDayMs()
        val endOfTodayMs = startOfTodayMs + 86400000L - 1
        
        val activeTopicIds = topics.map { it.topicId }.toSet()
        val filteredRevisions = revisions.distinctBy { it.topicId.ifEmpty { it.id } }.map { rev ->
            if (rev.status == "Completed" || rev.status == "Complete") {
                rev
            } else {
                val nextRevStart = getStartOfDayMs(rev.nextRevisionDateMs)
                val calculatedStatus = when {
                    rev.nextRevisionDateMs <= 0L -> "Pending"
                    nextRevStart < startOfTodayMs -> "Overdue"
                    nextRevStart == startOfTodayMs -> "Today"
                    else -> "Upcoming"
                }
                rev.copy(status = calculatedStatus)
            }
        }
        
        val revMap = filteredRevisions.associateBy { it.topicId }
        val enrichedTopics = topics.map { topic ->
            val rev = revMap[topic.topicId]
            if (rev != null) {
                val effectiveNextDate = if (rev.nextRevisionDateMs > 0L) {
                    rev.nextRevisionDateMs
                } else if ((topic.nextRevisionDate ?: 0L) > 0L) {
                    topic.nextRevisionDate ?: 0L
                } else {
                    topic.nextRevisionDateMs
                }
                val isCompleted = (rev.status == "Completed" || rev.status == "Complete" || topic.status == "Completed" || topic.status == "Complete") && effectiveNextDate <= 0L
                val calculatedStatus = if (isCompleted) {
                    "Completed"
                } else if (effectiveNextDate <= 0L) {
                    "Pending"
                } else {
                    val nextRevStart = getStartOfDayMs(effectiveNextDate)
                    when {
                        nextRevStart < startOfTodayMs -> "Overdue"
                        nextRevStart == startOfTodayMs -> "Today"
                        else -> "Upcoming"
                    }
                }
                topic.copy(
                    revisionCount = if (rev.revisionCount > 0) rev.revisionCount else topic.revisionCount,
                    lastRevisionDateMs = if (rev.lastRevisionDateMs > 0L) rev.lastRevisionDateMs else topic.lastRevisionDateMs,
                    nextRevisionDateMs = effectiveNextDate,
                    status = calculatedStatus
                )
            } else {
                val effectiveNextDate = if ((topic.nextRevisionDate ?: 0L) > 0L) {
                    topic.nextRevisionDate ?: 0L
                } else {
                    topic.nextRevisionDateMs
                }
                val isCompleted = (topic.status == "Completed" || topic.status == "Complete") && effectiveNextDate <= 0L
                val calculatedStatus = if (isCompleted) {
                    "Completed"
                } else if (effectiveNextDate <= 0L) {
                    "Pending"
                } else {
                    val nextRevStart = getStartOfDayMs(effectiveNextDate)
                    when {
                        nextRevStart < startOfTodayMs -> "Overdue"
                        nextRevStart == startOfTodayMs -> "Today"
                        else -> "Upcoming"
                    }
                }
                topic.copy(
                    nextRevisionDateMs = effectiveNextDate,
                    status = calculatedStatus
                )
            }
        }

        val coveredTopicIds = enrichedTopics.map { it.topicId }.toSet()
        val orphanTopics = filteredRevisions.filter {
            it.topicId.isNotEmpty() && it.topicId !in coveredTopicIds && it.status != "Completed" && it.status != "Complete"
        }.map { rev ->
            TopicWithSubjectAndChapter(
                topicId = rev.topicId,
                topicName = rev.topicName.ifEmpty { "Revision Item" },
                chapterId = rev.chapterId,
                chapterName = rev.chapterName.ifEmpty { "General" },
                subjectId = rev.subjectId,
                subjectName = rev.subjectName.ifEmpty { "General Subject" },
                nextRevisionDateMs = rev.nextRevisionDateMs,
                status = rev.status,
                revisionStatus = rev.status,
                revisionCount = rev.revisionCount,
                lastRevisionDateMs = rev.lastRevisionDateMs,
                estimatedRevisionTimeMins = rev.duration.coerceAtLeast(15)
            )
        }
        val allEnrichedTopics = enrichedTopics + orphanTopics

        val todayDueTopics = allEnrichedTopics.filter { it.status == "Today" }
            .sortedWith(compareBy<TopicWithSubjectAndChapter> { it.nextRevisionDateMs }.thenBy { it.topicName })
        val overdueTopics = allEnrichedTopics.filter { it.status == "Overdue" }
            .sortedWith(compareBy<TopicWithSubjectAndChapter> { it.nextRevisionDateMs }.thenBy { it.topicName })
        val upcomingTopics = allEnrichedTopics.filter { it.status == "Upcoming" }
            .sortedWith(compareBy<TopicWithSubjectAndChapter> { it.nextRevisionDateMs }.thenBy { it.topicName })
        val pendingOnlyTopics = allEnrichedTopics.filter { it.status == "Pending" }
            .sortedWith(compareBy<TopicWithSubjectAndChapter> { it.topicName })
        val completedTopics = allEnrichedTopics.filter { it.status == "Completed" || it.status == "Complete" }
        val weakTopics = allEnrichedTopics.filter { it.masteryScore in 0..40 }
        val todayRevisedCount = allEnrichedTopics.count { it.lastRevisionDateMs in startOfTodayMs..endOfTodayMs }

        val existingTopicIdsInRevisions = filteredRevisions.map { it.topicId }.toSet()
        val synthesizedRevisions = allEnrichedTopics.filter { 
            it.topicId !in existingTopicIdsInRevisions && 
            (it.status == "Today" || it.status == "Overdue" || it.status == "Upcoming") 
        }.map { topic ->
            Revision(
                id = "topic_rev_${topic.topicId}",
                userId = "offline_user",
                subjectId = topic.subjectId,
                chapterId = topic.chapterId,
                topicId = topic.topicId,
                subjectName = topic.subjectName,
                chapterName = topic.chapterName,
                topicName = topic.topicName,
                revisionCount = topic.revisionCount,
                lastRevisionDateMs = topic.lastRevisionDateMs,
                nextRevisionDateMs = topic.nextRevisionDateMs,
                status = topic.status,
                duration = topic.estimatedRevisionTimeMins.coerceAtLeast(15)
            )
        }
        val combinedRevisions = filteredRevisions.filter { it.status != "Pending" } + synthesizedRevisions

        return DashboardMetrics(
            todayDue = todayDueTopics,
            pendingRevisions = overdueTopics,
            upcomingRevisions = upcomingTopics,
            completedTotal = completedTopics,
            studyTimeTodayStr = formatTime(todayStudyTimeMs),
            weakTopics = weakTopics,
            todayRevisedCount = todayRevisedCount,
            allRevisions = combinedRevisions,
            pendingTopics = pendingOnlyTopics
        )
    }

    fun calculateAnalyticsMetrics(
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
    ): AnalyticsMetrics {
        val topics = if (activeId.isEmpty() || activeId == "all") {
            allTopics
        } else {
            val matching = allTopics.filter { it.examIds.contains(activeId) }
            if (matching.isNotEmpty()) matching else allTopics
        }
        
        val activeTopicIds = topics.map { it.topicId }.toSet()
        val startOfTodayMs = getStartOfDayMs()
        val endOfTodayMs = startOfTodayMs + 86400000L - 1
        val filteredRevisions = revisions.filter { it.topicId in activeTopicIds }.distinctBy { it.topicId.ifEmpty { it.id } }.map { rev ->
            if (rev.status == "Pending" || rev.status == "Today" || rev.status == "Overdue" || rev.status == "Upcoming") {
                val calculatedStatus = when {
                    rev.nextRevisionDateMs <= 0L -> "Pending"
                    rev.nextRevisionDateMs < startOfTodayMs -> "Overdue"
                    rev.nextRevisionDateMs in startOfTodayMs..endOfTodayMs -> "Today"
                    else -> "Upcoming"
                }
                rev.copy(status = calculatedStatus)
            } else {
                rev
            }
        }
        
        val filteredLogs = when {
            timeFilter == "Today" -> {
                val todayStr = currentDayString()
                logs.filter { it.dateString == todayStr }
            }
            timeFilter == "Week" || timeFilter == "This Week" -> {
                val startOfWeek = getStartOfWeekMs()
                logs.filter { parseDateStringToMs(it.dateString) >= startOfWeek }
            }
            timeFilter == "Month" || timeFilter == "This Month" -> {
                val startOfMonth = getStartOfMonthMs()
                logs.filter { parseDateStringToMs(it.dateString) >= startOfMonth }
            }
            timeFilter == "Yearly" || timeFilter == "This Year" -> {
                val startOfYear = getStartOfYearMs()
                logs.filter { parseDateStringToMs(it.dateString) >= startOfYear }
            }
            timeFilter.startsWith("Custom:") -> {
                try {
                    val dates = timeFilter.removePrefix("Custom:").split("|")
                    if (dates.size == 2) {
                        val startMs = parseDateStringToMs(dates[0])
                        val endMs = parseDateStringToMs(dates[1]) + 86400000L - 1
                        logs.filter {
                            val logMs = parseDateStringToMs(it.dateString)
                            logMs in startMs..endMs
                        }
                    } else logs
                } catch (e: Exception) { logs }
            }
            else -> logs
        }
        
        val totalStudyTimeMs = filteredLogs.sumOf { it.studyTimeMs + it.revisionTimeMs }
        val focusSessions = filteredLogs.sumOf { it.focusSessions }
        val coinsEarned = profile?.totalCoins ?: 0
        
        // Revision Strength = Completed Revisions / Scheduled Revisions * 100
        val completedRevisionsCount = filteredRevisions.count { it.status == "Completed" }
        val scheduledRevisionsCount = filteredRevisions.size
        val revisionStrength = if (scheduledRevisionsCount > 0) {
            (completedRevisionsCount * 100) / scheduledRevisionsCount
        } else {
            0
        }
        
        // Revision Strength mapped to productivityScore
        val productivityScore = revisionStrength
        
        val revisionDueTopics = filteredRevisions.count { it.status == "Overdue" || it.status == "Today" }
        
        val totalTopicsCount = topics.size
        val completedTopicsCount = topics.count { it.status == "Completed" || it.status == "Complete" }
        
        val baseSubjects = if (allSubjects.isNotEmpty()) {
            allSubjects
        } else {
            topics.map {
                com.example.data.Subject(
                    id = it.subjectId,
                    name = it.subjectName,
                    examId = it.examIds.firstOrNull() ?: ""
                )
            }.distinctBy { it.id }
        }

        val filteredSubjects = if (activeId.isEmpty() || activeId == "all") {
            baseSubjects.filter { !it.isArchived }
        } else {
            baseSubjects.filter { !it.isArchived && (it.examId == activeId || it.examId.isEmpty()) }
        }
        
        val subjectProgressList = filteredSubjects.map { subject ->
            val subjectChapters = allChapters.filter { it.subjectId == subject.id && !it.isArchived }
            val subjectTopics = topics.filter { it.subjectId == subject.id }
            val completedTopics = subjectTopics.count { it.status == "Completed" || it.status == "Complete" }
            val totalTopics = if (subject.totalTopicsExpected > 0) {
                subject.totalTopicsExpected
            } else {
                maxOf(subjectTopics.size, subjectChapters.size)
            }
            val progressPercent = if (subjectChapters.isNotEmpty() && subjectTopics.isEmpty()) {
                val chapProgressList = subjectChapters.map { chapter ->
                    val chapRevs = revisions.count { it.chapterId == chapter.id && it.status == "Completed" }
                    minOf(chapRevs * 10, 100)
                }
                chapProgressList.average().toInt().coerceIn(0, 100)
            } else if (totalTopics > 0) {
                ((completedTopics.toFloat() / totalTopics) * 100f).toInt().coerceIn(0, 100)
            } else {
                0
            }
            val rawSubTimeMs = subjectTopics.sumOf { it.studyTimeMs + it.revisionTimeMs }
            val subTimeMs = if (rawSubTimeMs > 0) {
                rawSubTimeMs
            } else if (completedTopics > 0) {
                completedTopics * 30 * 60000L
            } else if (subjectChapters.isNotEmpty()) {
                subjectChapters.count { it.isCompleted || it.progress >= 100f } * 45 * 60000L
            } else {
                0L
            }
            val subReadiness = if (subjectTopics.isNotEmpty()) subjectTopics.sumOf { it.masteryScore } / subjectTopics.size else 0

            com.example.data.SubjectProgress(
                id = subject.id,
                name = subject.name,
                progress = progressPercent / 100f,
                percentage = "$progressPercent%",
                icon = subject.icon.ifEmpty { "book" },
                colorCode = subject.colorHex.ifEmpty { "#0061A4" },
                totalTopics = totalTopics,
                completedTopics = completedTopics,
                pendingTopics = maxOf(0, totalTopics - completedTopics),
                studyTimeMins = (subTimeMs / 60000).toInt(),
                revisionDueTopics = filteredRevisions.count { it.subjectId == subject.id && (it.status == "Overdue" || it.status == "Today") },
                completionPercentage = progressPercent,
                examReadinessMeter = subReadiness
            )
        }
        
        val overallCompletionPercentage = if (allChapters.isNotEmpty() && allSubjects.isNotEmpty()) {
            if (subjectProgressList.isNotEmpty()) {
                val rawAvg = subjectProgressList.map { it.completionPercentage }.average().toInt()
                val allActiveChapters = allChapters.filter { !it.isArchived }
                val allChaptersReachedTen = allActiveChapters.isNotEmpty() && allActiveChapters.all { chapter ->
                    val completedRevisions = revisions.count { it.chapterId == chapter.id && it.status == "Completed" }
                    completedRevisions >= 10
                }
                if (allChaptersReachedTen) {
                    rawAvg.coerceIn(0, 100)
                } else {
                    minOf(rawAvg, 99).coerceIn(0, 100)
                }
            } else {
                0
            }
        } else {
            val totalTopicsCount = topics.size
            val completedTopicsCount = topics.count { it.status == "Completed" || it.status == "Complete" }
            if (totalTopicsCount > 0) (completedTopicsCount * 100) / totalTopicsCount else 0
        }
        
        val averageScore = if (tests.isNotEmpty()) {
            val validTests = tests.filter { it.totalMarks > 0f }
            if (validTests.isNotEmpty()) {
                val totalScore = validTests.sumOf { (it.marksObtained / it.totalMarks * 100).toDouble() }
                (totalScore / validTests.size).toInt()
            } else {
                0
            }
        } else {
            0
        }
        
        // Calculate Study Consistency
        val activeDays = filteredLogs.count { (it.studyTimeMs + it.revisionTimeMs) > 0 }
        val totalDaysPeriod = when {
            timeFilter == "Today" -> 1
            timeFilter == "Week" || timeFilter == "This Week" -> 7
            timeFilter == "Month" || timeFilter == "This Month" -> 30
            timeFilter == "Yearly" || timeFilter == "This Year" -> 365
            timeFilter.startsWith("Custom:") -> {
                try {
                    val dates = timeFilter.removePrefix("Custom:").split("|")
                    if (dates.size == 2) {
                        val startMs = parseDateStringToMs(dates[0])
                        val endMs = parseDateStringToMs(dates[1])
                        val diffMs = endMs - startMs
                        val days = (diffMs / 86400000L).toInt() + 1
                        maxOf(1, days)
                    } else 7
                } catch (e: Exception) { 7 }
            }
            else -> maxOf(7, filteredLogs.map { it.dateString }.distinct().size)
        }
        val studyConsistency = if (totalDaysPeriod > 0) (activeDays * 100) / totalDaysPeriod else 0
        
        // Calculate Exam Readiness using weighted values:
        // Syllabus Completion = 40%, Revision Strength = 30%, Average Test Score = 20%, Study Consistency = 10%
        val examReadiness = if (totalTopicsCount > 0) {
            val weighted = (overallCompletionPercentage * 0.40) + (revisionStrength * 0.30) + (averageScore * 0.20) + (studyConsistency * 0.10)
            weighted.toInt().coerceIn(0, 100)
        } else {
            0
        }
        
        val readinessPercentage = examReadiness
        
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val weeklyActivity = if (timeFilter.startsWith("Custom:")) {
            try {
                val dates = timeFilter.removePrefix("Custom:").split("|")
                if (dates.size == 2) {
                    val startMs = parseDateStringToMs(dates[0])
                    val endMs = parseDateStringToMs(dates[1])
                    val daysCount = ((endMs - startMs) / 86400000L).toInt() + 1
                    
                    if (daysCount in 1..14) {
                        (0 until daysCount).map { idx ->
                            val dayMs = startMs + (idx * 86400000L)
                            val cal = Calendar.getInstance().apply { timeInMillis = dayMs }
                            val dayName = if (daysCount <= 7) {
                                when (cal.get(Calendar.DAY_OF_WEEK)) {
                                    Calendar.MONDAY -> "Mon"
                                    Calendar.TUESDAY -> "Tue"
                                    Calendar.WEDNESDAY -> "Wed"
                                    Calendar.THURSDAY -> "Thu"
                                    Calendar.FRIDAY -> "Fri"
                                    Calendar.SATURDAY -> "Sat"
                                    Calendar.SUNDAY -> "Sun"
                                    else -> ""
                                }
                            } else {
                                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
                            }
                            
                            val dayLogs = logs.filter {
                                val logMs = parseDateStringToMs(it.dateString)
                                logMs in dayMs..(dayMs + 86400000L - 1)
                            }
                            val hours = dayLogs.sumOf { it.studyTimeMs + it.revisionTimeMs } / 3600000f
                        val sessions = dayLogs.sumOf { it.focusSessions }
                        com.example.data.DayActivity(dayName, hours, sessions)
                        }
                    } else {
                        val step = maxOf(1, daysCount / 7)
                        (0 until 7).map { idx ->
                            val chunkStartMs = startMs + (idx * step * 86400000L)
                            val chunkEndMs = if (idx == 6) endMs else (chunkStartMs + step * 86400000L - 1)
                            val calStart = Calendar.getInstance().apply { timeInMillis = chunkStartMs }
                            val label = "${calStart.get(Calendar.DAY_OF_MONTH)}/${calStart.get(Calendar.MONTH) + 1}"
                            
                            val dayLogs = logs.filter {
                                val logMs = parseDateStringToMs(it.dateString)
                                logMs in chunkStartMs..chunkEndMs
                            }
                            val hours = dayLogs.sumOf { it.studyTimeMs + it.revisionTimeMs } / 3600000f
                        val sessions = dayLogs.sumOf { it.focusSessions }
                        com.example.data.DayActivity(label, hours, sessions)
                        }
                    }
                } else {
                    daysOfWeek.mapIndexed { idx, day ->
                        val weekStartMs = getStartOfWeekMs()
                        val dayStartMs = weekStartMs + (idx * 86400000L)
                        val dayEndMs = dayStartMs + 86400000L - 1
                        val dayLogs = logs.filter { 
                            val logMs = parseDateStringToMs(it.dateString)
                            logMs in dayStartMs..dayEndMs
                        }
                        val hours = dayLogs.sumOf { it.studyTimeMs + it.revisionTimeMs } / 3600000f
                        val sessions = dayLogs.sumOf { it.focusSessions }
                        com.example.data.DayActivity(day, hours, sessions)
                    }
                }
            } catch (e: Exception) {
                daysOfWeek.mapIndexed { idx, day ->
                    val weekStartMs = getStartOfWeekMs()
                    val dayStartMs = weekStartMs + (idx * 86400000L)
                    val dayEndMs = dayStartMs + 86400000L - 1
                    val dayLogs = logs.filter { 
                        val logMs = parseDateStringToMs(it.dateString)
                        logMs in dayStartMs..dayEndMs
                    }
                    val hours = dayLogs.sumOf { it.studyTimeMs + it.revisionTimeMs } / 3600000f
                        val sessions = dayLogs.sumOf { it.focusSessions }
                        com.example.data.DayActivity(day, hours, sessions)
                }
            }
        } else {
            daysOfWeek.mapIndexed { idx, day ->
                val weekStartMs = getStartOfWeekMs()
                val dayStartMs = weekStartMs + (idx * 86400000L)
                val dayEndMs = dayStartMs + 86400000L - 1
                val dayLogs = logs.filter { 
                    val logMs = parseDateStringToMs(it.dateString)
                    logMs in dayStartMs..dayEndMs
                }
                val hours = dayLogs.sumOf { it.studyTimeMs + it.revisionTimeMs } / 3600000f
                        val sessions = dayLogs.sumOf { it.focusSessions }
                        com.example.data.DayActivity(day, hours, sessions)
            }
        }
        
        val dailyHours = weeklyActivity.map { it.hours }
        val maxDailyHours = maxOf(8f, dailyHours.maxOrNull() ?: 8f)
        
        val subjectPerformance = subjectProgressList.map {
            com.example.data.SubjectPerformance(
                name = it.name,
                percentage = it.examReadinessMeter,
                colorCode = it.colorCode,
                icon = "school"
            )
        }
        
        val totalTimeMins = subjectProgressList.sumOf { it.studyTimeMins }
        val studyTimeBreakdown = subjectProgressList.map {
            val share = if (totalTimeMins > 0) (it.studyTimeMins * 100) / totalTimeMins else 0
            val hrs = it.studyTimeMins / 60
            val mins = it.studyTimeMins % 60
            val timeStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
            com.example.data.SubjectBreakdown(
                name = it.name,
                percentage = share,
                colorCode = it.colorCode,
                timeStr = timeStr
            )
        }
        
        val expectedScoreRange = if (averageScore > 0) {
            "${maxOf(0, averageScore - 5)} - ${minOf(100, averageScore + 5)}%"
        } else {
            "0 - 0%"
        }
        
        // Get actual revision count from revisions list for the selected time filter
        val revisionsCompletedCount = filteredRevisions.count { rev -> 
            if (rev.status == "Completed") {
                val revMs = rev.lastRevisionDateMs
                when {
                    timeFilter == "Today" -> revMs >= getStartOfDayMs()
                    timeFilter == "Week" || timeFilter == "This Week" -> revMs >= getStartOfWeekMs()
                    timeFilter == "Month" || timeFilter == "This Month" -> revMs >= getStartOfMonthMs()
                    timeFilter == "Yearly" || timeFilter == "This Year" -> revMs >= getStartOfYearMs()
                    timeFilter.startsWith("Custom:") -> {
                        try {
                            val dates = timeFilter.removePrefix("Custom:").split("|")
                            if (dates.size == 2) {
                                val startMs = parseDateStringToMs(dates[0])
                                val endMs = parseDateStringToMs(dates[1]) + 86400000L - 1
                                revMs in startMs..endMs
                            } else true
                        } catch (e: Exception) { true }
                    }
                    else -> true
                }
            } else false
        }
        
        return AnalyticsMetrics(
            totalStudyTimeStr = formatTime(totalStudyTimeMs),
            productivityScore = productivityScore,
            focusSessions = focusSessions,
            coinsEarned = coinsEarned,
            totalRevisions = revisionsCompletedCount,
            completedTopics = completedTopicsCount,
            pendingTopics = totalTopicsCount - completedTopicsCount,
            overallCompletionPercentage = overallCompletionPercentage,
            overallProgressPercentage = overallCompletionPercentage,
            averageScore = averageScore,
            readinessPercentage = readinessPercentage,
            examReadiness = readinessPercentage,
            subjectProgress = subjectProgressList,
            weeklyActivity = weeklyActivity,
            dailyHours = dailyHours,
            maxDailyHours = maxDailyHours,
            subjectPerformance = subjectPerformance,
            studyTimeBreakdown = studyTimeBreakdown,
            currentStreak = profile?.currentStreak ?: 0,
            bestStreak = profile?.longestStreak ?: 0,
            targetExamName = "SSC", // This could also be fetched from profile/activeExam
            expectedScoreRange = expectedScoreRange,
            retentionRate = run {
                val pastRevisions = filteredRevisions.count {
                    it.status == "Completed" || it.status == "Complete"
                }
                if (pastRevisions > 0) {
                    val completed = filteredRevisions.count { it.status == "Completed" }
                    (completed * 100) / pastRevisions
                } else {
                    0
                }
            }
        )
    }
    
    private fun getStartOfDayMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getStartOfDayMs(timeMs: Long): Long {
        if (timeMs <= 0L) return 0L
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMs
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getStartOfWeekMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val now = System.currentTimeMillis()
        if (calendar.timeInMillis > now) {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
        }
        return calendar.timeInMillis
    }
    
    private fun getStartOfMonthMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfYearMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun currentDayString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun formatTime(ms: Long): String {
        val totalMinutes = ms / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }

    private fun parseDateStringToMs(dateStr: String): Long {
        try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val calendar = Calendar.getInstance()
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return calendar.timeInMillis
            }
        } catch (e: Exception) {}
        return 0L
    }
}

