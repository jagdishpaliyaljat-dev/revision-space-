    }.catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val analyticsMetrics = combine(
        allTopicsWithDetails, 
        activeExamId, 
        allStudyLogs, 
        currentUserProfile, 
        analyticsTimeFilter
    ) { allTopics, activeId, logs, profile, timeFilter ->
        val topics = if (activeId.isEmpty() || activeId == "all") {
            allTopics
        } else {
            allTopics.filter { it.examIds.contains(activeId) }
        }
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val todayMs = cal.timeInMillis
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekMs = cal.timeInMillis
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val monthMs = cal.timeInMillis
        
        val filteredLogs = when (timeFilter) {
            "Today" -> logs.filter { it.timestampMs >= todayMs }
            "Week" -> logs.filter { it.timestampMs >= weekMs }
            "Month" -> logs.filter { it.timestampMs >= monthMs }
            else -> logs
        }
        
        val totalTimeMs = filteredLogs.sumOf { it.studyTimeMs + it.revisionTimeMs }
        val hours = totalTimeMs / (1000 * 60 * 60)
        val mins = (totalTimeMs / (1000 * 60)) % 60
        val totalStudyTimeStr = "${hours}h ${mins}m"
        
        val daysCompleted = filteredLogs.count { it.studyTimeMs > 0 || it.revisionTimeMs > 0 }
        val dayStreak = profile?.currentStreak ?: 0
        
        val completedTopics = topics.filter { it.status == "Complete" }
        val averageScore = if (completedTopics.isNotEmpty()) completedTopics.map { it.masteryScore }.average().toInt() else 0
        
        val overallProgressPercentage = if (topics.isNotEmpty()) (completedTopics.size * 100) / topics.size else 0
