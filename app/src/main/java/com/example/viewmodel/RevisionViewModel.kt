package com.example.viewmodel
import com.example.ui.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi

import com.example.services.AnalyticsService

import com.example.data.Subject
import com.example.data.Topic
import com.example.data.Chapter
import com.example.data.TopicWithSubjectAndChapter
import com.example.data.ExamCountdown
import com.example.data.RevisionRepository
import com.example.data.GlobalAppConfig
import com.example.data.StudyLog
import com.example.data.UserProfile
import com.example.data.Revision
import com.example.data.QuickTask
import com.example.data.formatDurationSpent
import com.example.data.calculateProgressScore
import com.example.data.local.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun ViewModel.launchSafe(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) = viewModelScope.launch { 
    try { 
        block() 
    } catch(e: Exception) { 
        e.printStackTrace() 
    } 
}


// Custom intervals for "Smart Revision Schedule"
// Default intervals
val defaultRevisionIntervals = listOf(1, 3, 5, 7, 14, 21, 28, 45, 60)






data class ProfileStats(
    val totalStudyMs: Long = 0L,
    val displayStudyHours: Long = 0L,
    val displayStudyMinutesOnly: Long = 0L,
    val displayStudyTimeStr: String = "0h 0m",
    val displayRevisions: Int = 0,
    val completedChaptersCount: Int = 0,
    val displayMastery: Int = 0
)

class RevisionViewModel(private val repository: RevisionRepository) : ViewModel() {

    fun handleDeepLinkNavigation(
        topicId: String,
        subjectIdFallback: String,
        chapterIdFallback: String,
        navController: androidx.navigation.NavController,
        context: android.content.Context
    ) = launchSafe {
        var subjectId = subjectIdFallback
        var chapterId = chapterIdFallback
        var chapterName = "Chapter"
        
        // 1. Validate Topic
                val topicDetails = allTopicsWithDetails.value.find { it.topicId == topicId }
        val topicPlain = if (topicDetails == null) allTopics.value.find { it.id == topicId } else null
        
        if (topicDetails == null && topicPlain == null) {
            // Topic is deleted or invalid
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "This revision topic is no longer available.", android.widget.Toast.LENGTH_LONG).show()
                navController.navigate(com.example.ui.TodayRevisionRoute()) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                if (subjectId.isNotEmpty()) {
                    navController.navigate(com.example.ui.SubjectDetailsRoute(subjectId))
                }
            }
            return@launchSafe
        }
        
        // Update IDs from the actual topic in DB to be safe
        subjectId = topicDetails?.subjectId ?: topicPlain?.subjectId ?: subjectId
        chapterId = topicDetails?.chapterId ?: topicPlain?.chapterId ?: chapterId
        
        // 2. Validate Subject
        val subject = repository.getSubjectById(subjectId)
        if (subject == null) {
            // Subject deleted
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                navController.navigate(com.example.ui.TodayRevisionRoute()) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
            return@launchSafe
        }
        
        // 3. Validate Chapter
        val chapter = repository.getChapterById(chapterId)
        if (chapter == null) {
            // Chapter deleted
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                navController.navigate(com.example.ui.TodayRevisionRoute()) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                navController.navigate(com.example.ui.SubjectDetailsRoute(subjectId))
            }
            return@launchSafe
        }
        chapterName = chapter.name
        
        // 4. Navigate to deep stack safely
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            navController.navigate(com.example.ui.TodayRevisionRoute()) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
            navController.navigate(com.example.ui.SubjectDetailsRoute(subjectId))
            navController.navigate(com.example.ui.TopicListRoute(chapterId, chapterName))
            navController.navigate(com.example.ui.TopicDetailRoute(topicId, subjectId))
            navController.navigate(com.example.ui.TimerRoute(topicId, true))
        }
    }

    private val analyticsService = AnalyticsService()
    private val revisionSyncCoordinator = com.example.services.RevisionSyncCoordinator(repository, viewModelScope)
    fun getRevisionPattern(): List<Int>  {
        val pattern = currentUserProfile.value?.revisionPattern ?: emptyList()
        return if (pattern.isNotEmpty()) pattern else defaultRevisionIntervals
    }

    fun updateRevisionPattern(pattern: List<Int>) = launchSafe {
        val profile = currentUserProfile.value ?: return@launchSafe
        repository.updateUserProfile(profile.copy(revisionPattern = pattern))
        repository.recalculateFutureRevisions(pattern)
    }


    private val _isDarkMode = MutableStateFlow(
        com.example.MyApplication.instance.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("dark_mode_enabled", false)
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: kotlinx.coroutines.flow.StateFlow<String?> = _syncError.asStateFlow()

    fun clearSyncError() { _syncError.value = null }

    /** Retry the lightweight Firestore sync before dismissing a sync error. */
    fun retrySync() = launchSafe {
        try {
            repository.checkAndUpdateOverdueRevisionsStatus()
            _syncError.value = null
        } catch (e: Exception) {
            _syncError.value = e.message ?: "Unable to sync with Firestore."
        }
    }

    private val _goalReachedEvent = MutableSharedFlow<Unit>()
    val goalReachedEvent = _goalReachedEvent.asSharedFlow()

    fun setDarkMode(enabled: Boolean ) {
        _isDarkMode.value = enabled
    }

    fun clearLocalData() = launchSafe {
        repository.clearAllData()
    }

    init {
        launchSafe {
            try {
                repository.generateTestDataIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val allSubjects = repository.allSubjects

        .map { list ->
            list.filter { it.id.isNotBlank() }.distinctBy { it.id }
        }
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isRevisionsLoading = MutableStateFlow(true)
    val isRevisionsLoading: StateFlow<Boolean> = _isRevisionsLoading.asStateFlow()

    private val _isTopicsLoaded = MutableStateFlow(false)
    val isTopicsLoaded: StateFlow<Boolean> = _isTopicsLoaded.asStateFlow()

    val currentUserProfile = combine(
        repository.currentUserProfile,
        combine(
            repository.allChapters,
            repository.allRevisions,
            repository.allStudyLogs
        ) { ch, rev, lg -> Triple(ch, rev, lg) },
        combine(
            repository.getAllUsersProfiles(),
            repository.globalConfig
        ) { users, config -> Pair(users, config) }
    ) { rawProfile, triple, pair ->
        if (rawProfile == null) return@combine null
        val (chapters, revisions, logs) = triple
        val (allUsers, config) = pair

        val evaluatedProfile = com.example.services.RankManager.evaluateRank(
            profile = rawProfile,
            chapters = chapters,
            revisions = revisions,
            logs = logs,
            allUsers = allUsers,
            isLeaderboardEnabled = config.isLeaderboardEnabled
        )

        if (evaluatedProfile.badge != rawProfile.badge ||
            evaluatedProfile.league != rawProfile.league ||
            evaluatedProfile.rankPosition != rawProfile.rankPosition ||
            evaluatedProfile.isRanked != rawProfile.isRanked
        ) {
            launchSafe {
                repository.updateUserProfileFieldsOnly(
                    userId = rawProfile.id,
                    badge = evaluatedProfile.badge,
                    league = evaluatedProfile.league,
                    rankPosition = evaluatedProfile.rankPosition,
                    isRanked = evaluatedProfile.isRanked
                )
            }
        }

        evaluatedProfile
    }
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val revisionSettings = repository.getRevisionSettings()
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.RevisionSettings())

    fun updateRevisionSettings(settings: com.example.data.RevisionSettings) = launchSafe {
        try {
            repository.updateRevisionSettings(settings)
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            val sharedPrefs = context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val globalConfig = repository.globalConfig
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, GlobalAppConfig())

    fun updateGlobalConfig(updates: Map<String, Any>) = launchSafe {
        repository.updateGlobalConfig(updates)
    }

    fun resetAllUserStreaks(adminEmail: String) = launchSafe {
        try {
            repository.resetAllUserStreaks()
            repository.logSystemAction(repository.userId, adminEmail, "Reset Streaks", "All user streaks reset")
        } catch (e: Exception ) {
            e.printStackTrace()
        }
    }

    fun resetAllUserCoins(adminEmail: String) = launchSafe {
        try {
            repository.resetAllUserCoins()
            repository.logSystemAction(repository.userId, adminEmail, "Reset Coins", "All user coins reset")
        } catch (e: Exception ) {
            e.printStackTrace()
        }
    }

    fun sendEmergencyBroadcast(title: String, message: String, adminEmail: String) = launchSafe {
        try {
            repository.sendEmergencyBroadcast(title, message, adminEmail)
            repository.logSystemAction(repository.userId, adminEmail, "Emergency Broadcast", "Emergency alert sent. Title: $title")
        } catch (e: Exception ) {
            e.printStackTrace()
        }
    }
        
    val allChapters = combine(
        repository.allChapters,
        allSubjects
    ) { chapters, subjects ->
        val subjectIds = subjects.map { it.id }.toSet()
        chapters
            .filter { it.id.isNotBlank() && it.subjectId.isNotBlank() && subjectIds.contains(it.subjectId) }
            .distinctBy { it.id }
    }
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allSubjectTests = repository.allSubjectTests
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateChapter(chapter: Chapter) = launchSafe {
        repository.updateChapter(chapter)
    }

    fun addSubjectTest(subjectId: String, testName: String, dateMs: Long, marksObtained: Float, totalMarks: Float, remarks: String) = launchSafe {
        repository.insertSubjectTest(subjectId, testName, dateMs, marksObtained, totalMarks, remarks)
    }

    fun updateSubjectTest(test: com.example.data.SubjectTest) = launchSafe {
        repository.updateSubjectTest(test)
    }

    fun deleteSubjectTest(testId: String) = launchSafe {
        repository.deleteSubjectTest(testId)
    }
        
    val allTopics = combine(
        repository.allTopics,
        repository.allChapters,
        repository.allSubjects
    ) { topics, chapters, subjects ->
        val subjectIds = subjects.map { it.id }.toSet()
        val chapterIdToSubjectId = chapters
            .filter { it.id.isNotBlank() && it.subjectId.isNotBlank() && subjectIds.contains(it.subjectId) }
            .associate { it.id to it.subjectId }

        topics
            .filter { it.id.isNotBlank() && it.chapterId.isNotBlank() && chapterIdToSubjectId.containsKey(it.chapterId) }
            .distinctBy { it.id }
            .map { topic ->
                val resolvedSubjectId = chapterIdToSubjectId[topic.chapterId] ?: topic.subjectId
                val todayStart = getStartOfDayMs(System.currentTimeMillis())
                val nextRev = topic.nextRevisionDate ?: topic.nextRevisionDateMs
                val hasActivity = topic.totalRevisions > 0 || topic.revisionCount > 0 || topic.studyCompleted || topic.firstStudyDate != null || nextRev > 0L
                val computedStatus = if (!hasActivity || nextRev == 0L) {
                    "Pending"
                } else {
                    val nextRevStart = getStartOfDayMs(nextRev)
                    when {
                        nextRevStart > todayStart -> "Upcoming"
                        nextRevStart == todayStart -> "Today"
                        else -> "Overdue"
                    }
                }
                topic.copy(
                    subjectId = resolvedSubjectId,
                    status = computedStatus,
                    revisionStatus = computedStatus
                )
            }
    }
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val cachedTopicsDetails = java.util.concurrent.ConcurrentHashMap<String, TopicWithSubjectAndChapter>()
    private val cachedTopics = java.util.concurrent.ConcurrentHashMap<String, Topic>()
    private var lastChaptersMap = emptyMap<String, Chapter>()
    private var lastSubjectsMap = emptyMap<String, Subject>()

    val allTopicsWithDetails = combine(
        repository.allTopics,
        repository.allChapters,
        repository.allSubjects
    ) { topics, chapters, subjects ->
        _isTopicsLoaded.value = true
        val chaptersMap = chapters.associateBy { it.id }
        val subjectsMap = subjects.associateBy { it.id }

        val chaptersChanged = lastChaptersMap != chaptersMap
        val subjectsChanged = lastSubjectsMap != subjectsMap
        
        lastChaptersMap = chaptersMap
        lastSubjectsMap = subjectsMap

        val subjectIds = subjects.map { it.id }.toSet()
        val chapterIdToSubjectId = chapters
            .filter { it.id.isNotBlank() && it.subjectId.isNotBlank() && subjectIds.contains(it.subjectId) }
            .associate { it.id to it.subjectId }

        val validTopics = topics.filter { it.id.isNotBlank() && it.chapterId.isNotBlank() && chapterIdToSubjectId.containsKey(it.chapterId) }
        val validTopicIds = validTopics.map { it.id }.toSet()
        
        val toRemove = cachedTopics.keys - validTopicIds
        toRemove.forEach { 
            cachedTopics.remove(it)
            cachedTopicsDetails.remove(it)
        }

        validTopics.forEach { topic ->
                val cached = cachedTopics[topic.id]
                if (cached != topic || chaptersChanged || subjectsChanged) {
                    val chapterId = topic.chapterId
                    val resolvedSubjectId = chapterIdToSubjectId[chapterId] ?: ""
                    val chapter = chaptersMap[chapterId]
                    val subject = subjectsMap[resolvedSubjectId]
                    
                    val todayStart = getStartOfDayMs(System.currentTimeMillis())
                    val nextRev = topic.nextRevisionDate ?: topic.nextRevisionDateMs
                    val hasActivity = topic.totalRevisions > 0 || topic.revisionCount > 0 || topic.studyCompleted || topic.firstStudyDate != null || nextRev > 0L
                    val computedStatus = if (topic.revisionStatus == "Completed" || ((topic.totalRevisions > 0 || topic.revisionCount > 0) && nextRev == 0L)) {
                        "Completed"
                    } else if (!hasActivity || nextRev == 0L) {
                        "Pending"
                    } else {
                        val nextRevStart = getStartOfDayMs(nextRev)
                        when {
                            nextRevStart > todayStart -> "Upcoming"
                            nextRevStart == todayStart -> "Today"
                            else -> "Overdue"
                        }
                    }
                    
                    val detail = TopicWithSubjectAndChapter(
                        topicId = topic.id,
                        topicName = topic.name,
                        notes = topic.notes,
                        difficulty = topic.difficulty,
                        estimatedRevisionTimeMins = topic.estimatedRevisionTimeMins,
                        status = computedStatus,
                        studyTimeMs = topic.studyTimeMs,
                        revisionTimeMs = topic.revisionTimeMs,
                        revisionCount = topic.revisionCount,
                        lastRevisionDateMs = topic.lastRevisionDateMs,
                        nextRevisionDateMs = topic.nextRevisionDateMs,
                        masteryScore = topic.masteryScore,
                        chapterId = chapterId,
                        chapterName = chapter?.name ?: "Unknown Chapter",
                        subjectId = resolvedSubjectId,
                        subjectName = subject?.name ?: "Unknown Subject",
                        subjectColorHex = subject?.colorHex ?: "#0061A4",
                        examIds = if (topic.examIds.isNotEmpty()) topic.examIds else if (subject?.examId?.isNotEmpty() == true) listOf(subject.examId) else emptyList(),
                        tags = topic.tags,
                        totalTopicsExpected = 0,
                        revisionPattern = topic.revisionPattern,
                        startDateMs = topic.startDateMs,
                        firstStudyDate = topic.firstStudyDate,
                        firstStudyTime = topic.firstStudyTime,
                        firstStudyDuration = topic.firstStudyDuration,
                        totalRevisions = topic.totalRevisions,
                        totalRevisionCount = topic.totalRevisionCount,
                        lastRevisionDate = topic.lastRevisionDate,
                        lastRevisionTime = topic.lastRevisionTime,
                        lastRevisionDuration = topic.lastRevisionDuration,
                        nextRevisionDate = topic.nextRevisionDate,
                        revisionStatus = computedStatus,
                        progressPercentage = topic.progressPercentage,
                        expectedRevisionTime = topic.expectedRevisionTime,
                        currentRevisionStage = topic.currentRevisionStage,
                        customSpacingPattern = topic.customSpacingPattern,
                        studyCompleted = topic.studyCompleted,
                        completedRevisions = topic.completedRevisions,
                        totalRequiredRevisions = topic.totalRequiredRevisions,
                        progressScore = topic.progressScore
                    )
                    cachedTopics[topic.id] = topic
                    cachedTopicsDetails[topic.id] = detail
                }
            }

        // Return a stable list
        cachedTopicsDetails.values
            .sortedWith(compareBy<com.example.data.TopicWithSubjectAndChapter> { it.nextRevisionDateMs }
                .thenBy { it.topicName })
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .catch { e: Throwable ->
        e.printStackTrace()
        _isTopicsLoaded.value = true
        _syncError.value = e.message ?: "Unable to load revision topics."
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allRevisions = repository.allRevisions.map { revisions ->
        _isRevisionsLoading.value = true
        val todayStart = getStartOfDayMs(System.currentTimeMillis())
        val list = revisions.distinctBy { it.topicId.ifEmpty { it.id } }.map { r ->
            if (r.status == "Completed") {
                r
            } else {
                val nextRevStart = getStartOfDayMs(r.nextRevisionDateMs)
                val revStatus = when {
                    nextRevStart > todayStart -> "Upcoming"
                    nextRevStart == todayStart -> "Today"
                    else -> "Overdue"
                }
                r.copy(status = revStatus)
            }
        }
        _isRevisionsLoading.value = false
        list
    }
        .catch { e: Throwable -> 
            _isRevisionsLoading.value = false
            e.printStackTrace() 
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Timer State
    // Timer State synced from Foreground Service
    val remainingTimeMs: kotlinx.coroutines.flow.StateFlow<Long> = com.example.services.FocusTimerService.remainingTimeMs
    val timerState: kotlinx.coroutines.flow.StateFlow<com.example.services.FocusTimerService.TimerState> = com.example.services.FocusTimerService.timerState

    val isRunning = com.example.services.FocusTimerService.isRunning
    val isPaused = com.example.services.FocusTimerService.isPaused
    val isCompleted = com.example.services.FocusTimerService.isCompleted
    val hasShownCompletionDialog = com.example.services.FocusTimerService.hasShownCompletionDialog
    val hasSentNotification = com.example.services.FocusTimerService.hasSentNotification
    val currentSessionType = com.example.services.FocusTimerService.currentSessionType
    val completedPomodorosToday = com.example.services.FocusTimerService.completedPomodorosToday

    val allQuickTasks = repository.allQuickTasks
        .catch { e -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addQuickTask(task: QuickTask) = launchSafe {
        repository.addQuickTask(task)
    }

    fun updateQuickTask(task: QuickTask) = launchSafe {
        repository.updateQuickTask(task)
    }
    
    suspend fun updateQuickTaskSuspend(task: QuickTask): Boolean {
        return try {
            repository.updateQuickTask(task)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteQuickTask(taskId: String) = launchSafe {
        repository.deleteQuickTask(taskId)
    }

    val isTimerRunning = MutableStateFlow(false)
    val timerElapsedMs = MutableStateFlow(0L)
    val activeTimerTopicId = MutableStateFlow("")
    val activeTimerSubjectId = MutableStateFlow("")
    val activeTimerSubjectName = MutableStateFlow("")
    val activeTimerChapterId = MutableStateFlow("")
    val activeTimerChapterName = MutableStateFlow("")
    val activeTimerSessionCount = MutableStateFlow(1)
    val activeTimerSessionId = MutableStateFlow("")
    val activeTimerStartTimeMs = MutableStateFlow(0L)
    
    private val topicOperationMutexes = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.sync.Mutex>()
    private val completedSessionKeys = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    private var lastAwardedMins: Int = 0

    init {
        if (repository.roomDb == null) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val uid = repository.userId
                if (uid.isNotEmpty()) {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val todayStr = dateFormat.format(java.util.Date())
                    try {
                        val query = repository.db?.collection("users")
                            ?.document(uid)
                            ?.collection("pomodoro_sessions")
                            ?.whereEqualTo("date", todayStr)
                            ?.whereEqualTo("completionStatus", "Completed")
                            ?.get()
                            ?.await()
                        if (query != null) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                completedPomodorosToday.value = query.size()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        launchSafe {
            isCompleted.collect { completed ->
                if (completed) {
                    val settings = currentUserProfile.value?.focusSettings ?: com.example.data.FocusSettings()
                    val wasFocus = currentSessionType.value == "Focus"
                    
                    if (wasFocus) {
                        val durationMin = when (settings.selectedStudyMode) {
                            "Study Mode" -> settings.studyModeDurationMins
                            "Revision Mode" -> settings.revisionModeDurationMins
                            "Practice Mode" -> settings.practiceModeDurationMins
                            "Mock Test Mode" -> settings.mockTestModeDurationMins
                            "Writing Practice Mode" -> settings.writingPracticeModeDurationMins
                            else -> 45
                        }
                        val durationMs = durationMin * 60 * 1000L
                        savePomodoroSession("Completed", durationMs, true, "Focus")
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {

                            
                            if (settings.autoStartBreak) {
                                kotlinx.coroutines.delay(1000)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    com.example.services.FocusTimerService.isCompleted.value = false
                                    val breakDuration = settings.shortBreakDuration
                                    startBreakTimer(breakDuration)
                                }
                            }
                        }
                    } else {
                        val durationMin = settings.shortBreakDuration
                        val durationMs = durationMin * 60 * 1000L
                        
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {

                            
                            if (settings.autoStartFocus) {
                                kotlinx.coroutines.delay(1000)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    com.example.services.FocusTimerService.isCompleted.value = false
                                    if (activeTimerSessionCount.value < 4) {
                                        activeTimerSessionCount.value++
                                    } else {
                                        activeTimerSessionCount.value = 1
                                    }
                                    startTimer(activeTimerTopicId.value)
                                }
                            }
                        }
                    }
                }
            }
        }
        launchSafe {
            timerState.collect { state ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    isTimerRunning.value = (state == com.example.services.FocusTimerService.TimerState.STUDY || state == com.example.services.FocusTimerService.TimerState.BREAK)
                    if (state == com.example.services.FocusTimerService.TimerState.COMPLETED ) {
                        val durationMin = if (currentSessionType.value == "Break") {
                            currentUserProfile.value?.focusSettings?.shortBreakDuration ?: 5
                        } else {
                            currentUserProfile.value?.focusSettings?.studyModeDurationMins ?: 45
                        }
                        timerElapsedMs.value = durationMin * 60 * 1000L
                    }
                }
            }
        }
        launchSafe {
            remainingTimeMs.collect { remaining ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    val durationMin = if (currentSessionType.value == "Break") {
                        currentUserProfile.value?.focusSettings?.shortBreakDuration?.toLong() ?: 5L
                    } else {
                        currentUserProfile.value?.focusSettings?.studyModeDurationMins?.toLong() ?: 45L
                    }
                    val totalDuration = durationMin * 60 * 1000L
                    if (remaining > 0 ) {
                        timerElapsedMs.value = totalDuration - remaining
                        
                        val currentMins = (timerElapsedMs.value / 60000).toInt()
                        if (currentMins > lastAwardedMins && currentSessionType.value != "Break") {
                            val newCoins = currentMins - lastAwardedMins
                            lastAwardedMins = currentMins
                            awardCoins(
                                amount = newCoins, 
                                description = "Study Timer Reward (minute by minute)",
                                sessionId = "timer_${java.util.UUID.randomUUID()}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun startTimer(
        chapterId: String = "",
        subjectId: String = "",
        subjectName: String = "",
        chapterName: String = "",
        topicId: String = ""
    ) {
        val targetTopicOrChapter = topicId.ifEmpty { activeTimerTopicId.value.ifEmpty { chapterId } }
        // Prevent duplicate timer sessions if already running for the same topic/chapter
        if (isRunning.value && !isPaused.value && targetTopicOrChapter.isNotEmpty() && activeTimerTopicId.value == targetTopicOrChapter && timerElapsedMs.value > 0L) {
            return
        }

        timerElapsedMs.value = 0L
        lastAwardedMins = 0
        if (chapterId.isNotEmpty()) {
            activeTimerChapterId.value = chapterId
        }
        if (topicId.isNotEmpty()) {
            activeTimerTopicId.value = topicId
        } else if (activeTimerTopicId.value.isEmpty() && chapterId.isNotEmpty()) {
            activeTimerTopicId.value = chapterId
        }
        if (subjectId.isNotEmpty()) activeTimerSubjectId.value = subjectId
        if (subjectName.isNotEmpty()) activeTimerSubjectName.value = subjectName
        if (chapterName.isNotEmpty()) activeTimerChapterName.value = chapterName

        activeTimerSessionId.value = java.util.UUID.randomUUID().toString()
        activeTimerStartTimeMs.value = System.currentTimeMillis()
        currentSessionType.value = "Focus"
        isRunning.value = true
        isPaused.value = false
        isCompleted.value = false
        isTimerRunning.value = true

        val durationMin = currentUserProfile.value?.focusSettings?.studyModeDurationMins?.toLong() ?: 45L

        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            val intent = android.content.Intent(context, com.example.services.FocusTimerService::class.java).apply {
                action = com.example.services.FocusTimerService.ACTION_START
                putExtra(com.example.services.FocusTimerService.EXTRA_DURATION, durationMin * 60 * 1000L)
                putExtra(com.example.services.FocusTimerService.EXTRA_TITLE, "Focus Session")
                putExtra(com.example.services.FocusTimerService.EXTRA_SUBJECT, if (subjectName.isNotEmpty()) subjectName else "Exam Prep")
                putExtra(com.example.services.FocusTimerService.EXTRA_TOPIC_ID, activeTimerTopicId.value)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                try {
                    androidx.core.content.ContextCompat.startForegroundService(context, intent)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        } catch (e: Exception) {
            // Context unavailable in unit tests; state flows are already updated above
        }
    }

    fun pauseTimer() {
        if (!isRunning.value) return
        isPaused.value = true
        isTimerRunning.value = false
        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            val intent = android.content.Intent(context, com.example.services.FocusTimerService::class.java).apply {
                action = com.example.services.FocusTimerService.ACTION_PAUSE
            }
            context.startService(intent)
        } catch (e: Exception) {}
    }

    fun resumeTimer() {
        if (!isRunning.value) return
        isPaused.value = false
        isTimerRunning.value = true
        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            val intent = android.content.Intent(context, com.example.services.FocusTimerService::class.java).apply {
                action = com.example.services.FocusTimerService.ACTION_RESUME
            }
            context.startService(intent)
        } catch (e: Exception) {}
    }

    fun resetTimer() {
        activeTimerTopicId.value = ""
        activeTimerSubjectId.value = ""
        activeTimerSubjectName.value = ""
        activeTimerChapterId.value = ""
        activeTimerChapterName.value = ""
        activeTimerSessionId.value = ""
        activeTimerStartTimeMs.value = 0L
        timerElapsedMs.value = 0L
        activeTimerSessionCount.value = 1
        lastAwardedMins = 0
        isRunning.value = false
        isPaused.value = false
        isCompleted.value = false
        isTimerRunning.value = false

        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            val intent = android.content.Intent(context, com.example.services.FocusTimerService::class.java).apply {
                action = com.example.services.FocusTimerService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {}
    }

    suspend fun completeTopicTimerSessionSuspend(
        topicId: String,
        durationMs: Long,
        sessionKey: String = activeTimerSessionId.value.ifEmpty { "session_${topicId}_${activeTimerStartTimeMs.value}" }
    ): Boolean {
        if (topicId.isBlank()) return false
        if (sessionKey.isNotEmpty() && !completedSessionKeys.add(sessionKey)) {
            return false
        }
        return try {
            val effectiveDuration = if (durationMs > 0L) durationMs else timerElapsedMs.value.coerceAtLeast(60000L)
            recordTimerSessionSuspend(topicId = topicId, isRevision = true, durationMs = effectiveDuration)
            resetTimer()
            true
        } catch (e: Exception) {
            if (sessionKey.isNotEmpty()) {
                completedSessionKeys.remove(sessionKey)
            }
            _syncError.value = e.message ?: "Failed to save revision session"
            false
        }
    }

    private var isSkipInProgress = false

    fun skipSession(chapterId: String, elapsedMs: Long, focusMs: Long, isRevisionActive: Boolean, subjectId: String = "", subjectName: String = "", chapterName: String = "") {
        if (isSkipInProgress) return
        isSkipInProgress = true
        
        android.util.Log.d("RevisionViewModel", "Session skipped. Previous type: ${currentSessionType.value}")
        
        val wasFocus = currentSessionType.value == "Focus"
        val actualElapsedMs = if (elapsedMs > focusMs && focusMs > 0) focusMs else elapsedMs
        
        // 1. Move directly to next session immediately
        if (wasFocus) {
            val breakDuration = currentUserProfile.value?.focusSettings?.shortBreakDuration ?: 5
            startBreakTimer(breakDuration)
        } else {
            val sessionCount = activeTimerSessionCount.value
            if (sessionCount < 4) {
                activeTimerSessionCount.value = sessionCount + 1
            } else {
                activeTimerSessionCount.value = 1
            }
            startTimer(chapterId, subjectId, subjectName, chapterName)
        }
        
        // 2. Record session and award coins in background thread
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (actualElapsedMs > 0 && wasFocus) {
                try {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val dateStr = dateFormat.format(java.util.Date())
                    repository.recordChapterStudySessionTransaction(chapterId, isRevisionActive, actualElapsedMs, dateStr)
                    
                    val mins = kotlin.math.ceil(actualElapsedMs / 60000.0).toInt()
                    val config = globalConfig.value
                    if (mins > 0 && config != null) {
                        val earnedBaseCoins = (mins * config.coinsPerBaselineTime) / maxOf(1, config.activeTimeBaselineMinutes)
                        if (earnedBaseCoins > 0) {
                            val sessionId = java.util.UUID.randomUUID().toString()
                            repository.awardCoins(earnedBaseCoins, "Study Session (${mins} mins)", sessionId)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            kotlinx.coroutines.delay(1000)
            isSkipInProgress = false
        }
    }

    val allStudyLogs = repository.allStudyLogs
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val rawExams = repository.allExams
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allExams = combine(
        rawExams,
        allRevisions,
        allSubjects,
        allChapters
    ) { exams, revisions, subjects, chapters ->
        exams.map { exam ->
            val examSubjects = subjects.filter { it.examId == exam.id }
            val subjectProgresses = examSubjects.map { subject ->
                val subjectChapters = chapters.filter { it.subjectId == subject.id && !it.isArchived }
                val chapterProgresses = subjectChapters.map { chapter ->
                    val completedRevisions = revisions.count { it.chapterId == chapter.id && it.status == "Completed" }
                    (completedRevisions.toFloat() / 10f * 100f).coerceAtMost(100f)
                }
                if (chapterProgresses.isNotEmpty()) {
                    chapterProgresses.average().toFloat()
                } else {
                    0f
                }
            }

            val overallProgressRaw = if (subjectProgresses.isNotEmpty()) {
                subjectProgresses.average().toFloat()
            } else {
                0f
            }

            val examSubjectsIds = examSubjects.map { it.id }.toSet()
            val examChapters = chapters.filter { it.subjectId in examSubjectsIds && !it.isArchived }
            val allChaptersReachedTen = examChapters.isNotEmpty() && examChapters.all { chapter ->
                val completedRevisions = revisions.count { it.chapterId == chapter.id && it.status == "Completed" }
                completedRevisions >= 10
            }

            val overallProgress = if (allChaptersReachedTen) {
                overallProgressRaw
            } else {
                minOf(overallProgressRaw, 99f)
            }

            exam.copy(progress = overallProgress.coerceIn(0f, 100f))
        }
    }.catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        launchSafe {
            try {
                allExams.collect { computedExams ->
                    val rawExamsList = rawExams.value
                    for (computedExam in computedExams) {
                        val rawExam = rawExamsList.find { it.id == computedExam.id }
                        if (rawExam != null && kotlin.math.abs(rawExam.progress - computedExam.progress) > 0.01f) {
                            try {
                                repository.updateExam(computedExam)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _selectedExamId = MutableStateFlow<String>("all")
    val selectedExamId = _selectedExamId.asStateFlow()

    val activeExamId = combine(_selectedExamId, allExams) { selectedId, exams ->

        if (exams.isEmpty()) {
            ""
        } else if (selectedId.isEmpty() || selectedId == "all" || exams.none { it.id == selectedId }) {
            exams.first().id
        } else {
            selectedId
        }
    }.catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun selectExam(examId: String) {}
    private val _analyticsTimeFilter = MutableStateFlow("Overall")
    val analyticsTimeFilter = _analyticsTimeFilter.asStateFlow()
    fun setAnalyticsTimeFilter(filter: String ) {
        _analyticsTimeFilter.value = filter
    }

    fun insertExam(name: String, examDateMs: Long?, targetStudyHours: Int?, category: String = "", targetRank: String = "", totalSubjects: Int = 0) {
        launchSafe { repository.insertExam(name, examDateMs, targetStudyHours, category, targetRank, totalSubjects) }
    }

    fun insertExtendedSyllabus(examId: String, subjectName: String, topicName: String, totalChapters: Int, estimatedRevisionTimeMins: Int ) {
        launchSafe { repository.insertExtendedSyllabus(examId, subjectName, topicName, totalChapters, estimatedRevisionTimeMins) }
    }

    fun updateExam(exam: ExamCountdown ) {
        launchSafe { repository.updateExam(exam) }
    }

    fun deleteExam(examId: String ) {
        launchSafe { repository.deleteExam(examId) }
    }

    val coinTransactions = repository.coinTransactions
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allCoinTransactions = repository.getAllCoinTransactions()
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val referrals = repository.referrals
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allReferrals = repository.getAllReferrals()
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val withdrawalRequests = repository.withdrawalRequests
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allWithdrawalRequests = repository.getAllWithdrawalRequests()
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val adminLogs = repository.getAdminLogs()
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val broadcastAnalytics = repository.getBroadcastAnalytics()
        .catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun sendBroadcastNotification(title: String, message: String, audience: String, adminEmail: String, onResult: (Boolean, String) -> Unit) {
        launchSafe {
            try {
                repository.sendBroadcastNotification(title, message, audience, adminEmail)
                onResult(true, "Broadcast sent successfully to $audience")
            } catch (e: Exception ) {
                onResult(false, e.message ?: "Failed to send broadcast")
            }
        }
    }

    fun markBroadcastAsOpened(broadcastId: String ) {
        launchSafe {
            repository.markBroadcastAsOpened(broadcastId)
        }
    }

    fun submitReferralCode(code: String, onResult: (String) -> Unit) {
        launchSafe {
            val res = repository.submitReferralCode(code)
            onResult(res)
            repository.evaluateReferralPayouts()
        }
    }

    fun logSystemAction(action: String, details: String) = launchSafe {
        var uid = "admin"
        var email = "admin@o.com"
        try {
            uid = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
            email = FirebaseAuth.getInstance().currentUser?.email ?: "admin@o.com"
        } catch (e: Exception) { e.printStackTrace() }
        repository.logSystemAction(uid, email, action, details)
    }

    fun submitWithdrawalRequest(req: com.example.data.WithdrawalRequest, onResult: (Boolean) -> Unit) {
        launchSafe {
            val res = repository.submitWithdrawalRequest(req)
            onResult(res)
        }
    }

    fun approveWithdrawal(reqId: String, adminId: String, adminEmail: String, onResult: (Boolean) -> Unit) {
        launchSafe {
            val res = repository.approveWithdrawal(reqId, adminId, adminEmail)
            onResult(res)
        }
    }

    fun rejectWithdrawal(reqId: String, reason: String, adminId: String, adminEmail: String, onResult: (Boolean) -> Unit) {
        launchSafe {
            val res = repository.rejectWithdrawal(reqId, reason, adminId, adminEmail)
            onResult(res)
        }
    }

    fun evaluateReferralPayouts( ) {
        launchSafe {
            repository.evaluateReferralPayouts()
        }
    }

    fun updateVerificationStatus(emailVerified: Boolean, mobileVerified: Boolean ) {
        launchSafe {
            val current = currentUserProfile.value ?: return@launchSafe
            val updated = current.copy(isEmailVerified = emailVerified, isMobileVerified = mobileVerified)
            repository.updateUserProfile(updated)
            repository.evaluateReferralPayouts()
        }
    }

    fun awardCoins(amount: Int, description: String, sessionId: String? = null ) {
        launchSafe {
            repository.awardCoins(amount, description, sessionId)
        }
    }

    fun recordRevisionEarnings(topicId: String, topicName: String, coins: Int ) {
        launchSafe {
            repository.recordRevisionEarnings(topicId, topicName, coins)
        }
    }

    fun recordStreakReward(streakDays: Int, coins: Int ) {
        launchSafe {
            repository.recordStreakReward(streakDays, coins)
        }
    }

    fun recordAchievementBonus(achievementId: String, achievementName: String, coins: Int ) {
        launchSafe {
            repository.recordAchievementBonus(achievementId, achievementName, coins)
        }
    }
        
    // Derived dashboard metrics
    val dashboardMetrics = combine(
        allTopicsWithDetails,
        activeExamId,
        allStudyLogs,
        allRevisions
    ) { allTopics, activeId, logs, revisions ->
        analyticsService.calculateDashboardMetrics(allTopics, activeId, logs, revisions)
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .catch { e: Throwable -> e.printStackTrace() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.DashboardMetrics())


    val analyticsMetrics = combine(
        combine(allTopicsWithDetails, activeExamId, allStudyLogs, allSubjectTests) { topics, activeId, logs, tests ->
            com.example.data.AnalyticsQuadruple(topics, activeId, logs, tests)
        },
        currentUserProfile,
        analyticsTimeFilter,
        combine(allRevisions, allChapters, allSubjects) { revisions, chapters, subjects ->
            Triple(revisions, chapters, subjects)
        },
        repository.recentRevisionHistory
    ) { quadruple, profile, timeFilter, triple, recentHistory ->
        val (revisions, chapters, subjects) = triple
        analyticsService.calculateAnalyticsMetrics(
            quadruple.topics,
            quadruple.activeId,
            quadruple.logs,
            profile,
            timeFilter,
            revisions,
            quadruple.tests,
            chapters,
            subjects,
            recentHistory
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.AnalyticsMetrics())

    val realtimeCompletionPercentage: StateFlow<Int> = analyticsMetrics
        .map { it.overallCompletionPercentage }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)







    val profileStats: kotlinx.coroutines.flow.StateFlow<ProfileStats> = kotlinx.coroutines.flow.combine(
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
            kotlin.math.min(overallSyllabusCovered.toInt(), 99).coerceIn(0, 100)
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
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, ProfileStats())

    private val _adminAllUsers = MutableStateFlow<List<com.example.data.UserProfile>?>(null)
    val adminAllUsers: StateFlow<List<com.example.data.UserProfile>?> = _adminAllUsers

    val adminDashboardMetrics: StateFlow<com.example.data.AdminDashboardMetrics> = _adminAllUsers.map { users ->
        if (users == null) com.example.data.AdminDashboardMetrics()
        else {
            val total = users.size
            val active = users.count { !it.isBanned }
            val online = users.count { (System.currentTimeMillis() - it.lastActiveTimeMs < 60 * 60 * 1000L) }
            val revisions = users.sumOf { it.totalRevisions }
            val study = users.sumOf { it.studyTimeMinutes }
            val coins = users.sumOf { it.totalCoins }
            com.example.data.AdminDashboardMetrics(
                totalUsers = total,
                activeUsers = active,
                onlineUsers = online,
                totalRevisions = revisions,
                totalStudyTimeMinutes = study,
                totalCoinsEarned = coins
            )
        }
    }.catch { e: Throwable -> e.printStackTrace() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.AdminDashboardMetrics())

    init {
        if (repository.roomDb == null) {
            launchSafe {
                try {
                    repository.userIdFlow.collect { uid ->
                        if (uid.isNotEmpty()) {
                            val email = try { FirebaseAuth.getInstance().currentUser?.email ?: "" } catch (e: Exception) { "" }
                            if (email.isNotEmpty()) {
                                repository.saveUserProfile(email)
                            }
                            repository.recalculateAllChaptersProgress()
                            repository.evaluateDailyRewardsAndStreak(uid)
                        }
                    }
                } catch (e: Exception ) {
                    e.printStackTrace()
                }
            }
        }

        launchSafe {
            try {
                repository.getAllUsersProfiles().collect { list ->
                    _adminAllUsers.value = list
                }
            } catch (e: Exception ) {
                e.printStackTrace()
            }
        }
    }

    fun adminAwardCoins(targetUserId: String, amount: Int, description: String ) {
        launchSafe {
            var email = "admin@o.com"
            try {
                email = FirebaseAuth.getInstance().currentUser?.email ?: "admin@o.com"
            } catch (e: Exception) { e.printStackTrace() }
            repository.adminAwardCoins(targetUserId, amount, description, email)
        }
    }

    suspend fun getUserSubjectsCount(targetUserId: String): Int {
        return repository.getUserSubjectsCount(targetUserId)
    }

    suspend fun getUserTopicsCount(targetUserId: String): Int {
        return repository.getUserTopicsCount(targetUserId)
    }

    fun getAdminSubjects(targetUserId: String) = repository.getAdminSubjects(targetUserId)
    fun getAdminTopics(targetUserId: String) = repository.getAdminTopics(targetUserId)

    suspend fun addSubjectSuspend(name: String, colorHex: String = "#0061A4", examId: String = "", icon: String = "book", totalTopicsExpected: Int = 0) {
        repository.insertSubject(name, colorHex, examId, icon, totalTopicsExpected)
    }

    /** Bulk CSV/JSON syllabus import. Existing names are reused, nothing is deleted. */
    suspend fun importSyllabus(
        tree: com.example.data.syllabus.SyllabusTree
    ): com.example.data.syllabus.SyllabusImportOutcome {
        return repository.importSyllabus(tree)
    }

    fun addSubject(name: String, colorHex: String = "#0061A4", examId: String = "", icon: String = "book", totalTopicsExpected: Int = 0) = launchSafe {
        repository.insertSubject(name, colorHex, examId, icon, totalTopicsExpected)
    }
    
    fun updateSubject(subjectId: String, name: String, colorHex: String, icon: String, totalTopicsExpected: Int) = launchSafe {
        repository.updateSubject(subjectId, name, colorHex, icon, totalTopicsExpected)
    }




    fun deleteSubject(id: String) = launchSafe {
        repository.deleteSubject(id)
    }
    
    suspend fun deleteSubjectSuspend(id: String) {
        repository.deleteSubject(id)
    }

    fun addChapter(subjectId: String, name: String, importance: String = "Medium") = launchSafe {
        repository.insertChapter(subjectId, name, importance)
    }

    private val activeChapterDeletions = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun deleteChapter(id: String) = launchSafe {
        deleteChapterSuspend(id)
    }

    suspend fun deleteChapterSuspend(id: String) {
        if (!activeChapterDeletions.add(id)) return
        try {
            repository.deleteChapter(id)
        } finally {
            activeChapterDeletions.remove(id)
        }
    }

        fun addTopicFull(subjectId: String, chapterId: String, name: String, nextRevisionDateMs: Long, estimatedRevisionTimeMins: Int, notes: String) = launchSafe {
        repository.insertTopicFull(subjectId, chapterId, name, nextRevisionDateMs, estimatedRevisionTimeMins, notes)
    }

    fun addSubjectWithId(id: String, name: String, colorHex: String = "#0061A4", examId: String = "", icon: String = "book", totalTopicsExpected: Int = 0) = launchSafe {
        repository.insertSubjectWithId(id, name, colorHex, examId, icon, totalTopicsExpected)
    }

    fun addChapterWithId(id: String, subjectId: String, name: String, importance: String = "Medium") = launchSafe {
        repository.insertChapterWithId(id, subjectId, name, importance)
    }

    fun addTopicWithId(id: String, subjectId: String, chapterId: String, name: String, difficulty: String = "Medium", estimatedRevisionTimeMins: Int = 30, notes: String = "", tags: List<String> = emptyList()) = launchSafe {
        repository.insertTopicWithId(id, subjectId, chapterId, name, difficulty, estimatedRevisionTimeMins, notes, tags)
    }

    suspend fun createEmptyTopicSuspend(id: String, subjectId: String, chapterId: String, name: String): Boolean {
        return try {
            repository.insertTopicWithId(
                id = id,
                subjectId = subjectId,
                chapterId = chapterId,
                name = name,
                difficulty = "Medium",
                estimatedRevisionTimeMins = 30,
                notes = "",
                tags = emptyList()
            )
            true
        } catch (e: java.lang.Exception) {
            false
        }
    }

    fun addRevisionWithId(
        context: android.content.Context,
        id: String,
        subjectId: String,
        chapterId: String,
        topicId: String,
        subjectName: String,
        chapterName: String,
        topicName: String,
        pattern: List<Int>,
        nextRevisionDateMs: Long,
        reminderTimeMins: Int,
        reminderTime: String,
        notes: String,
        studyDate: String,
        studyTime: String,
        duration: Int,
        priority: String,
        createdAt: Long
    ) = launchSafe {
        val rev = repository.insertRevisionWithId(
            id, subjectId, chapterId, topicId, subjectName, chapterName, topicName,
            pattern, nextRevisionDateMs, reminderTimeMins, reminderTime, notes,
            studyDate, studyTime, duration, priority, createdAt
        )
        if (rev != null) {
        }
    }


    fun addRevision(context: android.content.Context, subjectId: String, chapterId: String, topicId: String, subjectName: String, chapterName: String, topicName: String, pattern: List<Int>, nextRevisionDateMs: Long, reminderTimeMins: Int, reminderTime: String, notes: String) = launchSafe {
        val rev = repository.insertRevision(subjectId, chapterId, topicId, subjectName, chapterName, topicName, pattern, nextRevisionDateMs, reminderTimeMins, reminderTime, notes)
        if (rev != null) {
        }
    }

    fun markRevisionComplete(revisionId: String) = launchSafe {
        repository.markRevisionComplete(revisionId)
    }

    fun updateRevision(context: android.content.Context, revision: com.example.data.Revision) = launchSafe {
        repository.updateRevision(revision)
    }

    fun deleteRevision(context: android.content.Context, revisionId: String) = launchSafe {
        repository.deleteRevision(revisionId)
    }

fun addTopic(chapterId: String, name: String, notes: String, tags: List<String> = emptyList()) = launchSafe {
        repository.insertTopic(chapterId, name, notes = notes, tags = tags)
    }
    
    fun addTopicWithSubjectContext(subjectId: String, name: String, difficulty: String, estimatedRevisionTimeMins: Int, notes: String, tags: List<String> = emptyList()) = launchSafe {
        repository.insertTopicWithSubjectId(subjectId, name, difficulty, estimatedRevisionTimeMins, notes, tags)
    }

    fun updateTopicDetails(topicId: String, name: String, difficulty: String, estimatedRevisionTimeMins: Int, notes: String, tags: List<String> = emptyList()) = launchSafe {
        repository.updateTopicDetails(topicId, name, difficulty, estimatedRevisionTimeMins, notes, tags)
    }

    fun updateTopicSpacedRepetition(
        topicId: String,
        difficulty: String,
        revisionPattern: List<Int>,
        nextRevisionDateMs: Long,
        startDateMs: Long
    ) = launchSafe {
        updateTopicSpacedRepetitionSuspend(topicId, difficulty, revisionPattern, nextRevisionDateMs, startDateMs)
    }
    
    suspend fun updateTopicSpacedRepetitionSuspend(
        topicId: String,
        difficulty: String,
        revisionPattern: List<Int>,
        nextRevisionDateMs: Long,
        startDateMs: Long
    ) {
        val mutex = topicOperationMutexes.getOrPut(topicId) { kotlinx.coroutines.sync.Mutex() }
        mutex.lock()
        try {
            repository.updateTopicSpacedRepetition(topicId, difficulty, revisionPattern, nextRevisionDateMs, startDateMs)
        } catch (e: Exception) {
            _syncError.value = e.message ?: "Failed to save revision settings"
            throw e
        } finally {
            mutex.unlock()
        }
    }
    
    fun deleteTopic(context: android.content.Context, id: String) = launchSafe {
        repository.deleteTopic(id)
    }

    fun updateTopicCompletion(context: android.content.Context, topicId: String) = launchSafe {
        val topic = allTopics.value.find { it.id == topicId } ?: return@launchSafe
        recordTimerSessionSuspend(topicId, isRevision = false, durationMs = topic.estimatedRevisionTimeMins * 60 * 1000L)
    }

    fun toggleTopicCompletion(context: android.content.Context, topicId: String) = launchSafe {
        val topic = allTopics.value.find { it.id == topicId } ?: return@launchSafe
        val isCurrentlyComplete = topic.totalRevisions > 0
        val updated = if (isCurrentlyComplete) {
            repository.deleteRevisionsForTopic(topicId)
            topic.copy(
                status = "Pending",
                revisionStatus = "Pending",
                revisionCount = 0,
                totalRevisions = 0,
                firstStudyDate = null,
                firstStudyTime = null,
                firstStudyDuration = 0L,
                lastRevisionDate = null,
                lastRevisionTime = null,
                lastRevisionDuration = 0L,
                nextRevisionDate = null,
                nextRevisionDateMs = 0L,
                lastRevisionDateMs = 0L,
                startDateMs = null,
                masteryScore = 0,
                studyCompleted = false,
                completedRevisions = 0,
                totalRequiredRevisions = 4,
                progressScore = 0
            )
        } else {
            val now = System.currentTimeMillis()
            val intervals = getRevisionPattern()
            val daysMulti = intervals.firstOrNull() ?: 1
            val nextDate = now + (daysMulti * 86400000L)
            val expected = topic.estimatedRevisionTimeMins * 60000L
            topic.copy(
                status = "Complete",
                revisionStatus = "Pending Revision",
                revisionCount = 0,
                startDateMs = topic.startDateMs ?: now,
                lastRevisionDateMs = now,
                nextRevisionDateMs = nextDate,
                masteryScore = 20,
                revisionPattern = intervals,
                firstStudyDate = now,
                firstStudyTime = formatDurationSpent(expected),
                firstStudyDuration = expected,
                totalRevisions = 1,
                totalRevisionCount = 0,
                lastRevisionDate = now,
                lastRevisionTime = formatDurationSpent(expected),
                lastRevisionDuration = expected,
                nextRevisionDate = nextDate,
                expectedRevisionTime = topic.estimatedRevisionTimeMins,
                currentRevisionStage = 0,
                customSpacingPattern = intervals,
                progressPercentage = 10,
                expectedStudyTime = expected,
                updatedAt = now,
                studyCompleted = true,
                completedRevisions = 0,
                totalRequiredRevisions = intervals.size,
                progressScore = calculateProgressScore(true, 0, intervals.size)
            )
        }
        repository.updateTopic(updated)
    }

    fun recordTimerSession(topicId: String, isRevision: Boolean, durationMs: Long) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                recordTimerSessionSuspend(topicId, isRevision, durationMs)
            } catch (_: Exception) {}
        }
    }

    suspend fun recordTimerSessionSuspend(topicId: String, isRevision: Boolean, durationMs: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val mutex = topicOperationMutexes.getOrPut(topicId) { kotlinx.coroutines.sync.Mutex() }
        mutex.lock()
        val todayStr = currentDayString()
        try {
            val todayLog = repository.getTodayStudyLog(todayStr)
            val previousStudyTimeMs = (todayLog?.studyTimeMs ?: 0L) + (todayLog?.revisionTimeMs ?: 0L)
            val previousMins = (previousStudyTimeMs / 60000).toInt()
            
            val isTopic = try {
                repository.getTopicById(topicId) != null
            } catch (e: Exception) {
                false
            }
            
            if (isTopic) {
                repository.recordStudySessionTransaction(topicId, isRevision, durationMs, todayStr)
            } else {
                repository.recordStudyTime(todayStr, isRevision, durationMs)
            }
            
            val newStudyTimeMs = previousStudyTimeMs + durationMs
            val newMins = (newStudyTimeMs / 60000).toInt()
            val goalMins = currentUserProfile.value?.focusSettings?.dailyFocusGoalMins ?: 120
            
            if (previousMins < goalMins && newMins >= goalMins) {
                _goalReachedEvent.emit(Unit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _syncError.value = e.message ?: "Sync Failed"
            throw e
        } finally {
            mutex.unlock()
        }
    }

    fun startBreakTimer(durationMins: Int) {
        timerElapsedMs.value = 0L
        lastAwardedMins = 0
        currentSessionType.value = "Break"
        isRunning.value = true
        isPaused.value = false
        isCompleted.value = false
        hasShownCompletionDialog.value = false
        hasSentNotification.value = false
        
        val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
        val prefs = context.getSharedPreferences("timer_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putLong("break_start_time", System.currentTimeMillis()).apply()

        val intent = android.content.Intent(context, com.example.services.FocusTimerService::class.java).apply {
            action = com.example.services.FocusTimerService.ACTION_START
            putExtra(com.example.services.FocusTimerService.EXTRA_DURATION, durationMins * 60 * 1000L)
            putExtra(com.example.services.FocusTimerService.EXTRA_TITLE, "Short Break")
            putExtra(com.example.services.FocusTimerService.EXTRA_SUBJECT, "Relaxation")
        }
        try {
            context.startService(intent)
        } catch (e: Exception ) {
            try {
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
                isRunning.value = false
                isPaused.value = false
            }
        }
    }

    fun incrementSessionCount() {
        val current = activeTimerSessionCount.value
        if (current < 4) {
            activeTimerSessionCount.value = current + 1
        } else {
            activeTimerSessionCount.value = 1
        }
    }

    fun cancelCompletionNotification() {
        val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
        val manager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.cancel(102)
    }

    fun savePomodoroSession(status: String, durationMs: Long, isCompleted: Boolean, sessionType: String = currentSessionType.value) {
        val uid = repository.userId
        if (uid.isEmpty()) return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val dateStr = dateFormat.format(java.util.Date(now))
            val endTimeStr = timeFormat.format(java.util.Date(now))
            val startTimeStr = timeFormat.format(java.util.Date(now - durationMs))
            
            val todayLog = allStudyLogs.value.find { it.dateString == dateStr }
            val todayStudyTimeMs = (todayLog?.studyTimeMs ?: 0L) + (todayLog?.revisionTimeMs ?: 0L) + durationMs
            
            val activeTopicId = activeTimerTopicId.value
            val activeChapterId = if (activeTimerChapterId.value.isNotEmpty()) activeTimerChapterId.value else activeTopicId
            
            if (sessionType != "Break" && durationMs > 0 && isCompleted && status == "Completed") {
                val isKnownTopic = activeTopicId.isNotEmpty() && (try { repository.getTopicById(activeTopicId) != null } catch (e: Exception) { false })
                if (isKnownTopic) {
                    completeTopicTimerSessionSuspend(activeTopicId, durationMs)
                } else if (activeChapterId.isNotEmpty()) {
                    val isRevision = (currentUserProfile.value?.focusSettings?.selectedStudyMode == "Revision Mode")
                    repository.recordChapterStudySessionTransaction(activeChapterId, isRevision, durationMs, dateStr)
                }
            }

            if (isCompleted && status == "Completed") {
                completedPomodorosToday.value += 1
            }
            
            val mins = kotlin.math.ceil(durationMs / 60000.0).toInt()
            if (mins > 0 && sessionType != "Break") {
                val earnedBaseCoins = (mins * (globalConfig.value?.coinsPerBaselineTime ?: 10)) / maxOf(1, globalConfig.value?.activeTimeBaselineMinutes ?: 45)
                if (earnedBaseCoins > 0) {
                    awardCoins(earnedBaseCoins, "Study Session (${mins} mins)", java.util.UUID.randomUUID().toString())
                }
            }
            
            val activeChapter = allChapters.value.find { it.id == activeChapterId }
            val chapterName = activeChapter?.name ?: activeTimerChapterName.value.ifEmpty { "General" }
            val subjectId = activeChapter?.subjectId ?: activeTimerSubjectId.value
            val subject = allSubjects.value.find { it.id == subjectId }
            val subjectName = subject?.name ?: activeTimerSubjectName.value.ifEmpty { "Exam Prep" }
            
            val sessionId = java.util.UUID.randomUUID().toString()
            val sessionData = hashMapOf(
                "sessionId" to sessionId,
                "userId" to uid,
                "subject" to subjectName,
                "chapter" to chapterName,
                "topic" to chapterName,
                "subjectId" to subjectId,
                "chapterId" to activeChapterId,
                "startTime" to startTimeStr,
                "endTime" to endTimeStr,
                "duration" to durationMs,
                "sessionType" to (if (currentUserProfile.value?.focusSettings?.selectedStudyMode == "Revision Mode") "Revision" else "Study"),
                "completionStatus" to status,
                "date" to dateStr,
                "todayTotalFocusTime" to todayStudyTimeMs,
                "completedPomodoroCount" to completedPomodorosToday.value
            )
            
            if (repository.roomDb == null) {
                try {
                    repository.db?.collection("users")
                        ?.document(uid)
                        ?.collection("pomodoro_sessions")
                        ?.document(sessionId)
                        ?.set(sessionData)
                        ?.await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun currentDayString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    private fun getStartOfDayMs(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfDayMs(timeMs: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timeMs
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatTime(ms: Long): String {
        val secs = ms / 1000
        val h = secs / 3600
        val m = (secs % 3600) / 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    private fun calculateMastery(revisions: Int, consistency: Boolean): Int {
        var score = (revisions * 10).coerceAtMost(70)
        if (consistency) score += 15
        return score.coerceIn(0, 100)
    }

    fun banUser(targetUserId: String, isBanned: Boolean, reason: String) = launchSafe {
        repository.banUser(targetUserId, isBanned, reason)
    }

    fun deleteUser(targetUserId: String) = launchSafe {
        repository.deleteUser(targetUserId)
    }

    fun updateUserProfile(profile: com.example.data.UserProfile) = launchSafe {
        repository.updateUserProfile(profile)
    }

    fun addManualRevision(
        topicId: String,
        dateMs: Long,
        durationMins: Int,
        revisionNumber: Int,
        notes: String,
        status: String
    ) = launchSafe {
        repository.addManualRevision(topicId, dateMs, durationMins, revisionNumber, notes, status)
    }
    fun updateTopic(topic: com.example.data.Topic) = launchSafe {
        repository.updateTopic(topic)
    }

    fun saveCustomRevisionPlan(topicId: String, intervals: List<Int>) = launchSafe {
        repository.saveCustomRevisionPlan(topicId, intervals)
    }

    fun checkAndTriggerNotifications(context: android.content.Context) {
        launchSafe {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val uid = repository.userId
                    if (uid.isEmpty()) return@withContext

                    repository.checkAndUpdateOverdueRevisionsStatus()

                    // The in-app master switch and the Android permission must gate
                    // every notification producer, including this one.
                    if (!com.example.notification.NotificationHelper.notificationsAllowed(context)) {
                        return@withContext
                    }

                    val now = System.currentTimeMillis()
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = now
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    cal.set(java.util.Calendar.MINUTE, 0)
                    cal.set(java.util.Calendar.SECOND, 0)
                    cal.set(java.util.Calendar.MILLISECOND, 0)
                    val startOfTodayMs = cal.timeInMillis
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    cal.set(java.util.Calendar.MINUTE, 59)
                    cal.set(java.util.Calendar.SECOND, 59)
                    val endOfTodayMs = cal.timeInMillis

                    val topicsList = repository.allTopics.first()
                    val completedTopics = topicsList.filter { it.status == "Complete" && it.nextRevisionDateMs > 0 }
                    val existingNotifications = repository.allNotifications.first()

                    for (topic in completedTopics) {
                        val dueDate = topic.nextRevisionDateMs
                        if (dueDate in startOfTodayMs..endOfTodayMs) {
                            val notifiedToday = existingNotifications.any {
                                it.topicId == topic.id && it.type == "DUE" && it.timestampMs in startOfTodayMs..endOfTodayMs
                            }
                            if (!notifiedToday) {
                                showRealLocalNotification(
                                    context,
                                    topic.id,
                                    "Revision Due Today",
                                    "${topic.name} revision is scheduled today. Tap to revise now.",
                                    "DUE"
                                )
                            }
                        } else if (dueDate < startOfTodayMs) {
                            val notifiedMissedToday = existingNotifications.any {
                                it.topicId == topic.id && it.type == "MISSED" && it.timestampMs in startOfTodayMs..endOfTodayMs
                            }
                            if (!notifiedMissedToday) {
                                showRealLocalNotification(
                                    context,
                                    topic.id,
                                    "Revision Missed",
                                    "You missed your ${topic.name} revision yesterday. Revise now.",
                                    "MISSED"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun showRealLocalNotification(context: android.content.Context, topicId: String, title: String, msg: String, type: String) {
        try {
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "revision_space_alerts"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Revision Alerts",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for revision and spacing alerts"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val intent = android.content.Intent(context, Class.forName("com.example.MainActivity")).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                (topicId.hashCode() + type.hashCode()),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
                
            notificationManager.notify((topicId.hashCode() + type.hashCode()), notification)
            
            val rDb = repository.roomDb
            if (rDb != null) {
                val notif = com.example.data.AppNotification(
                    id = java.util.UUID.randomUUID().toString(),
                    topicId = topicId,
                    title = title,
                    message = msg,
                    timestampMs = System.currentTimeMillis(),
                    isRead = false,
                    type = type
                )
                rDb.appNotificationDao().insert(notif.toEntity())
            } else {
                val uid = repository.userId
                if (uid.isNotEmpty()) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val notifRef = db.collection("users").document(uid).collection("notifications").document()
                    notifRef.set(mapOf(
                        "id" to notifRef.id,
                        "topicId" to topicId,
                        "title" to title,
                        "message" to msg,
                        "timestampMs" to System.currentTimeMillis(),
                        "isRead" to false,
                        "type" to type
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uploadProfileImage(uri: android.net.Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) = launchSafe {
        try {
            val url = repository.uploadProfileImage(uri)
            val current = currentUserProfile.value
            if (current != null) {
                updateUserProfile(current.copy(profilePictureUrl = url, profileImageUrl = url))
            }
            onSuccess(url)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun uploadProfileImageBytes(bytes: ByteArray, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) = launchSafe {
        try {
            val url = try {
                repository.uploadProfileImageBytes(bytes)
            } catch (e: Exception) {
                // FALLBACK: If Firebase Storage fails or is unconfigured, use 100% reliable local Base64 encoding.
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64"
            }
            
            val current = currentUserProfile.value
            if (current != null) {
                updateUserProfile(current.copy(profilePictureUrl = url, profileImageUrl = url))
            }
            onSuccess(url)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun removeProfileImage(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = launchSafe {
        try {
            repository.removeProfileImage()
            val current = currentUserProfile.value
            if (current != null) {
                updateUserProfile(current.copy(profilePictureUrl = "", profileImageUrl = ""))
            }
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private val _qaTestStatus = MutableStateFlow<String>("")
    val qaTestStatus: StateFlow<String> = _qaTestStatus

    private val _qaTestProgress = MutableStateFlow<Float>(0f)
    val qaTestProgress: StateFlow<Float> = _qaTestProgress

    private val _qaTestLog = MutableStateFlow<List<String>>(emptyList())
    val qaTestLog: StateFlow<List<String>> = _qaTestLog

    fun injectQADummyData(context: android.content.Context, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = launchSafe {
        _qaTestLog.value = emptyList()
        _qaTestProgress.value = 0f
        com.example.data.DatabaseSeeder.generate(
            viewModel = this@RevisionViewModel,
            context = context,
            onSuccess = onSuccess,
            onFailure = onFailure,
            logProgress = { msg, progress -> 
                _qaTestLog.value = _qaTestLog.value + listOf(msg)
                _qaTestStatus.value = msg
                _qaTestProgress.value = progress
            }
        )
    }
    
    val unreadNotificationsCount = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val allNotifications = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun markNotificationAsRead(notificationId: String) = launchSafe {
        repository.markNotificationAsRead(notificationId)
    }

    fun markAllNotificationsAsRead() = launchSafe {
        repository.markAllNotificationsAsRead()
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String, referralCode: String = "") {
        repository.signUpWithEmail(email, password, name, referralCode)
    }

    suspend fun signInWithEmail(email: String, password: String) {
        repository.signInWithEmail(email, password)
    }

    suspend fun signInWithGoogle(idToken: String) {
        repository.signInWithGoogle(idToken)
    }

    suspend fun signInAnonymously() {
        repository.signInAnonymously()
    }

    suspend fun checkUserExistsAndNotBanned(userId: String): Boolean? {
        return repository.checkUserExistsAndNotBanned(userId)
    }

    fun deleteNotification(notificationId: String) = launchSafe {
        repository.deleteNotification(notificationId)
    }
    
    fun logBroadcastOpen(topicId: String) = launchSafe {
        repository.logBroadcastOpen(topicId)
    }

    fun getRevisionHistoryForTopic(topicId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.RevisionHistoryRecord>> {
        return repository.getRevisionHistoryFlowForTopic(topicId).flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    fun logout(context: android.content.Context, onLogoutComplete: () -> Unit) {
        launchSafe {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                // Firebase sign-out alone is not enough: com.google.android.gms.auth
                // keeps the last Google account cached for this app, so the next
                // "Continue with Google" silently signs the same Gmail ID back in and
                // the user can never pick a different account. Clearing the cached
                // Google session makes Google show its account picker again.
                clearCachedGoogleAccount(context)

                // Also clear the guest bypass flag. It lives in a different prefs file
                // ("revision_space_prefs"); without this, Splash keeps routing straight
                // to the dashboard after logout and the login screen never returns.
                context.getSharedPreferences("revision_space_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                onLogoutComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Signs out of the Google Sign-In client so its cached account is dropped.
     * Bounded and exception-safe: a Play Services problem must never block logout.
     */
    private suspend fun clearCachedGoogleAccount(context: android.content.Context) {
        try {
            val options = com.google.android.gms.auth.api.signin.GoogleSignInOptions
                .Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, options)
            kotlinx.coroutines.withTimeoutOrNull(5_000L) {
                client.signOut().await()
            }
        } catch (e: Exception) {
            // Play Services unavailable / not installed: Google's cached account does
            // not exist either, so there is nothing to clear.
            e.printStackTrace()
        }
    }
}
class RevisionViewModelFactory(private val repository: com.example.data.RevisionRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RevisionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RevisionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}