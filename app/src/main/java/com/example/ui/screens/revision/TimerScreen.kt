package com.example.ui.screens.revision
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute
import com.example.data.FocusSettings
import com.example.data.Chapter
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val FocusBgLight: Color
    @Composable get() = MaterialTheme.colorScheme.background
val FocusPurple = Color(0xFF6C4DFF)
val FocusPurpleLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
val FocusTextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val FocusTextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun TimerScreenWrapper(viewModel: RevisionViewModel, navController: NavController, backStackEntry: NavBackStackEntry) {
    val route = backStackEntry.toRoute<TimerRoute>()
    TimerScreen(
        viewModel = viewModel,
        navController = navController,
        topicId = route.topicId,
        isRevision = route.isRevision,
        subjectId = route.subjectId,
        subjectName = route.subjectName,
        chapterId = route.chapterId,
        chapterName = route.chapterName,
        onSessionComplete = { elapsedMins, topicsRev, coins ->
            navController.navigate(SessionCompleteRoute(elapsedMins, topicsRev, coins)) {
                popUpTo(TimerRoute(route.topicId, route.isRevision, route.subjectId, route.subjectName, route.chapterId, route.chapterName)) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: RevisionViewModel, 
    navController: NavController, 
    topicId: String, 
    isRevision: Boolean,
    subjectId: String = "",
    subjectName: String = "",
    chapterId: String = "",
    chapterName: String = "",
    onSessionComplete: (Int, Int, Int) -> Unit
) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    var showTopicSelectionSheet by remember { mutableStateOf(false) }
    
    val activeSavedTopicId by viewModel.activeTimerTopicId.collectAsStateWithLifecycle()
    val activeSavedChapterId by viewModel.activeTimerChapterId.collectAsStateWithLifecycle()
    val activeSavedChapterName by viewModel.activeTimerChapterName.collectAsStateWithLifecycle()
    val activeSavedSubjectId by viewModel.activeTimerSubjectId.collectAsStateWithLifecycle()
    val activeSavedSubjectName by viewModel.activeTimerSubjectName.collectAsStateWithLifecycle()
    val effectiveTopicId = if (topicId.isNotEmpty()) topicId else activeSavedTopicId
    
    var currentSubjectId by remember(subjectId) { mutableStateOf(if (viewModel.isTimerRunning.value || viewModel.timerElapsedMs.value > 0) activeSavedSubjectId.ifEmpty { subjectId } else subjectId) }
    var currentSubjectName by remember(subjectName) { mutableStateOf(if (viewModel.isTimerRunning.value || viewModel.timerElapsedMs.value > 0) activeSavedSubjectName.ifEmpty { subjectName } else subjectName) }
    var currentChapterId by remember(chapterId) { mutableStateOf(if (viewModel.isTimerRunning.value || viewModel.timerElapsedMs.value > 0) activeSavedChapterId.ifEmpty { chapterId } else chapterId) }
    var currentChapterName by remember(chapterName) { mutableStateOf(if (viewModel.isTimerRunning.value || viewModel.timerElapsedMs.value > 0) activeSavedChapterName.ifEmpty { chapterName } else chapterName) }

    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle()
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(effectiveTopicId, allTopics) {
        if (effectiveTopicId.isNotEmpty()) {
            val topicItem = allTopics.find { it.topicId == effectiveTopicId }
            if (topicItem != null) {
                if (currentSubjectId.isEmpty()) currentSubjectId = topicItem.subjectId
                if (currentSubjectName.isEmpty()) currentSubjectName = topicItem.subjectName
                if (currentChapterId.isEmpty()) currentChapterId = topicItem.chapterId
                if (currentChapterName.isEmpty()) currentChapterName = topicItem.chapterName
            }
        }
    }

    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val settings = userProfile?.focusSettings ?: FocusSettings()

    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()
    var isSavingCompletion by remember { mutableStateOf(false) }

    // Real-time fallback ticker if ForegroundService does not tick (e.g. background restriction or test environment)
    LaunchedEffect(isRunning, isPaused) {
        while (isRunning && !isPaused) {
            val before = viewModel.timerElapsedMs.value
            kotlinx.coroutines.delay(1000L)
            if (viewModel.isTimerRunning.value && !viewModel.isPaused.value && viewModel.timerElapsedMs.value == before) {
                viewModel.timerElapsedMs.value = before + 1000L
            }
        }
    }

    LaunchedEffect(isRunning, settings.ambientSound, settings.soundEnabled) {
        AmbientSoundPlayer.setSound(settings.ambientSound)
        if (isRunning && settings.soundEnabled && settings.ambientSound != "None") {
            AmbientSoundPlayer.play()
        } else {
            AmbientSoundPlayer.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AmbientSoundPlayer.stop()
        }
    }

    val elapsedMs by viewModel.timerElapsedMs.collectAsStateWithLifecycle()
    val sessionCount by viewModel.activeTimerSessionCount.collectAsStateWithLifecycle()
    
    // Save indicator state variables
    var isSavingMode by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val currentSessionTypeState by viewModel.currentSessionType.collectAsStateWithLifecycle()
    val isCompletedState by viewModel.isCompleted.collectAsStateWithLifecycle()
    val hasShownCompletionDialogState by viewModel.hasShownCompletionDialog.collectAsStateWithLifecycle()

    // Dynamically retrieve stored study mode settings
    val activeMode = settings.selectedStudyMode
    val isRevisionActive = activeMode == "Revision Mode"

    // Configuration for focus session based on active mode or break mode
    val focusDurationMins = if (currentSessionTypeState == "Break") {
        settings.shortBreakDuration
    } else {
        when (activeMode) {
            "Study Mode" -> settings.studyModeDurationMins
            "Revision Mode" -> settings.revisionModeDurationMins
            "Practice Mode" -> settings.practiceModeDurationMins
            "Mock Test Mode" -> settings.mockTestModeDurationMins
            "Writing Practice Mode" -> settings.writingPracticeModeDurationMins
            else -> 45
        }
    }
    
    val focusMs = focusDurationMins * 60 * 1000L
    val progress = if (focusMs > 0) (elapsedMs.toFloat() / focusMs).coerceIn(0f, 1f) else 0f

    // ----- Today's focus stats (shared by goal card + summary card) -----
    val logs by viewModel.allStudyLogs.collectAsStateWithLifecycle()
    val chaptersList by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    val testsList by viewModel.allSubjectTests.collectAsStateWithLifecycle(emptyList())

    val startOfToday = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val todayLog = logs.find { it.dateString == todayStr }
    val todayStudyTimeMs = (todayLog?.studyTimeMs ?: 0L) + (todayLog?.revisionTimeMs ?: 0L)

    val totalTodayMins = kotlin.math.ceil(todayStudyTimeMs / 60000.0).toInt()
    val hToday = totalTodayMins / 60
    val mToday = totalTodayMins % 60
    val todayStudyTimeStr = if (hToday > 0) "${hToday}h ${mToday}m" else "${mToday}m"

    val focusSessionsToday = todayLog?.focusSessions ?: 0
    val chaptersCompletedToday = chaptersList.count { (it.firstStudyDateMs ?: 0L) >= startOfToday }

    val totalObtained = testsList.sumOf { it.marksObtained.toDouble() }
    val totalMax = testsList.sumOf { it.totalMarks.toDouble() }
    val accuracyValue = if (totalMax > 0.0) "${(totalObtained / totalMax * 100).toInt()}%" else "--"

    val goalMins = settings.dailyFocusGoalMins.coerceAtLeast(1)
    val progressPercent = ((totalTodayMins.toFloat() / goalMins.toFloat()) * 100).toInt().coerceIn(0, 100)

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAmbientSoundSheet by remember { mutableStateOf(false) }

    // Save selected mode both locally and to Firebase Firestore
    val changeMode: (String) -> Unit = { targetMode ->
        if (!isSavingMode) {
            coroutineScope.launch {
                try {
                    isSavingMode = true
                    errorMessage = ""
                    userProfile?.let { profile ->
                        val updatedSettings = settings.copy(
                            selectedStudyMode = targetMode,
                            lastUsedMode = targetMode,
                            focusDuration = when (targetMode) {
                                "Study Mode" -> settings.studyModeDurationMins
                                "Revision Mode" -> settings.revisionModeDurationMins
                                "Practice Mode" -> settings.practiceModeDurationMins
                                "Mock Test Mode" -> settings.mockTestModeDurationMins
                                "Writing Practice Mode" -> settings.writingPracticeModeDurationMins
                                else -> 45
                            }
                        )
                        viewModel.updateUserProfile(profile.copy(focusSettings = updatedSettings))
                    }
                    delay(500) // Premium feel validation delay
                    isSavingMode = false
                    saveSuccess = true
                    delay(1200)
                    saveSuccess = false
                } catch (e: Exception) {
                    isSavingMode = false
                    errorMessage = "Firestore Sync Error"
                    delay(1500)
                    errorMessage = ""
                }
            }
        }
    }

    // NEW: quick duration presets use the exact same profile-update path as settings.
    val updateActiveDuration: (Int) -> Unit = { mins ->
        userProfile?.let { profile ->
            val updated = settings.copy(
                focusDuration = mins,
                studyModeDurationMins = if (activeMode == "Study Mode") mins else settings.studyModeDurationMins,
                revisionModeDurationMins = if (activeMode == "Revision Mode") mins else settings.revisionModeDurationMins,
                practiceModeDurationMins = if (activeMode == "Practice Mode") mins else settings.practiceModeDurationMins,
                mockTestModeDurationMins = if (activeMode == "Mock Test Mode") mins else settings.mockTestModeDurationMins,
                writingPracticeModeDurationMins = if (activeMode == "Writing Practice Mode") mins else settings.writingPracticeModeDurationMins
            )
            viewModel.updateUserProfile(profile.copy(focusSettings = updated))
        }
    }

    val showDialog = isCompletedState && !hasShownCompletionDialogState && 
        ((currentSessionTypeState == "Focus" && !settings.autoStartBreak) || 
         (currentSessionTypeState == "Break" && !settings.autoStartFocus))

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.hasShownCompletionDialog.value = true
                viewModel.isCompleted.value = false
                viewModel.cancelCompletionNotification()
            },
            title = {
                Text(
                    text = if (currentSessionTypeState == "Break") "Break Completed 🎉" else "Session Completed 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = FocusTextDark
                )
            },
            text = {
                Text(
                    text = if (currentSessionTypeState == "Break") {
                        "Your break has ended. Ready to start your next focus session?"
                    } else {
                        "Your focus session has ended. What would you like to do next?"
                    },
                    fontSize = 15.sp,
                    color = FocusTextGray
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentSessionTypeState == "Break") {
                        Button(
                            onClick = {
                                viewModel.hasShownCompletionDialog.value = true
                                viewModel.isCompleted.value = false
                                viewModel.cancelCompletionNotification()
                                viewModel.startTimer(
                                    chapterId = currentChapterId,
                                    subjectId = currentSubjectId,
                                    subjectName = currentSubjectName,
                                    chapterName = currentChapterName
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusPurple),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Focus Session", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.hasShownCompletionDialog.value = true
                                viewModel.isCompleted.value = false
                                viewModel.cancelCompletionNotification()
                                val breakDuration = settings.shortBreakDuration
                                viewModel.startBreakTimer(breakDuration)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusPurple),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Break", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.hasShownCompletionDialog.value = true
                                viewModel.isCompleted.value = false
                                viewModel.cancelCompletionNotification()
                                viewModel.incrementSessionCount()
                                viewModel.startTimer(
                                    chapterId = currentChapterId,
                                    subjectId = currentSubjectId,
                                    subjectName = currentSubjectName,
                                    chapterName = currentChapterName
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusPurple),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next Session", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.hasShownCompletionDialog.value = true
                            viewModel.isCompleted.value = false
                            viewModel.cancelCompletionNotification()
                            viewModel.resetTimer()
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusTextDark)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (showTopicSelectionSheet) {
        ChapterSelectionBottomSheet(
            allSubjects = allSubjects,
            allChapters = allChapters,
            onChapterSelected = { subject, chapter ->
                currentSubjectId = subject.id
                currentSubjectName = subject.name
                currentChapterId = chapter.id
                currentChapterName = chapter.name
                showTopicSelectionSheet = false
            },
            onDismiss = { showTopicSelectionSheet = false }
        )
    }

    if (showSettingsSheet) {
        FocusSettingsBottomSheet(
            currentSettings = settings,
            activeMode = activeMode,
            onDismiss = { showSettingsSheet = false },
            onSave = { updatedSettings ->
                userProfile?.let {
                    viewModel.updateUserProfile(it.copy(focusSettings = updatedSettings))
                }
                showSettingsSheet = false
            }
        )
    }

    if (showAmbientSoundSheet) {
        AmbientSoundBottomSheet(
            currentSound = settings.ambientSound,
            onDismiss = { showAmbientSoundSheet = false },
            onSelect = { newSound ->
                userProfile?.let {
                    val newSettings = settings.copy(ambientSound = newSound, soundEnabled = true)
                    viewModel.updateUserProfile(it.copy(focusSettings = newSettings))
                }
                showAmbientSoundSheet = false
            }
        )
    }

    var showStopDialog by remember { mutableStateOf(false) }

    val statusLabel = when (activeMode) {
        "Study Mode" -> "STUDYING"
        "Revision Mode" -> "REVISING"
        "Practice Mode" -> "PRACTICING"
        "Mock Test Mode" -> "TESTING"
        "Writing Practice Mode" -> "WRITING"
        else -> "FOCUSING"
    }
    val isBreakSession = currentSessionTypeState == "Break"

    Scaffold(
        containerColor = FocusBgLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Brush.linearGradient(listOf(FocusPurple, Color(0xFFA78BFA)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Focus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp,
                                color = FocusTextDark
                            )
                            Text(
                                "Stay in the zone",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = FocusTextGray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, "Menu", tint = FocusTextDark, modifier = Modifier.size(19.dp))
                    }
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (settings.soundEnabled && settings.ambientSound != "None") Color(0xFFF1EEFF) else Color.White)
                                .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                                .clickable { showAmbientSoundSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = if (settings.soundEnabled && settings.ambientSound != "None") FocusPurple else FocusTextDark,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                                .clickable { showSettingsSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = FocusTextDark, modifier = Modifier.size(19.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FocusBgLight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- 1. QUICK MODE SELECTOR (new premium feature) ----------
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionKicker("CHOOSE YOUR MODE")
                    if (isSavingMode) {
                        Text("Saving...", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = FocusTextGray)
                    } else if (saveSuccess) {
                        Text("✓ Synced", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modeOptions = listOf(
                        "Study Mode" to Icons.Default.School,
                        "Revision Mode" to Icons.Default.History,
                        "Practice Mode" to Icons.Default.FitnessCenter,
                        "Mock Test Mode" to Icons.Default.Assignment,
                        "Writing Practice Mode" to Icons.Default.Edit
                    )
                    modeOptions.forEach { (mode, icon) ->
                        ModePill(
                            label = mode.replace(" Mode", ""),
                            icon = icon,
                            selected = mode == activeMode,
                            enabled = !isSavingMode && !isRunning && !isPaused,
                            onClick = { changeMode(mode) }
                        )
                    }
                }
            }

            // ---------- 3. CHAPTER SELECTOR ----------
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(FocusPurple, Color(0xFF8B5CF6))))
                        .clickable(enabled = !isRunning) { showTopicSelectionSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        val buttonText = if (currentChapterName.isNotEmpty()) {
                            "📚 ${currentChapterName.uppercase()}"
                        } else {
                            "Choose Chapter"
                        }
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- 4. SESSION CYCLE TRACKER ----------
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val currentSession = sessionCount.coerceIn(1, 4)
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i <= currentSession) FocusPurple else Color(0xFFE1DDF0))
                    )
                    if (i < 4) Spacer(modifier = Modifier.width(6.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "SESSION $currentSession / 4",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusTextGray
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- 5. GRADIENT TIMER RING ----------
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRunning) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.07f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.16f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(260.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background((if (isBreakSession) Color(0xFF0EA5A4) else FocusPurple).copy(alpha = pulseAlpha))
                    )
                }

                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PremiumTimerRing(
                        progress = progress,
                        isBreak = isBreakSession,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = RoundedCornerShape(50), color = if (isBreakSession) Color(0xFFECFDF5) else Color(0xFFF1EEFF)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isBreakSession) Color(0xFF10B981) else FocusPurple)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBreakSession) "BREAK TIME" else statusLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (isBreakSession) Color(0xFF0EA5A4) else FocusPurple
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val remainingMs = (focusMs - elapsedMs).coerceAtLeast(0L)
                        val minsLeft = (remainingMs / 1000) / 60
                        val secsLeft = (remainingMs / 1000) % 60
                        Text(
                            String.format("%02d:%02d", minsLeft, secsLeft),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusTextDark,
                            letterSpacing = (-2).sp
                        )

                        val elapsedM = (elapsedMs / 1000) / 60
                        val elapsedS = (elapsedMs / 1000) % 60
                        Text(
                            text = String.format("Elapsed: %02d:%02d", elapsedM, elapsedS),
                            fontSize = 12.5.sp,
                            color = FocusTextGray,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("timer_elapsed_text")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        val focusContext = when {
                            isBreakSession -> "Relax • Recharge • Return"
                            currentSubjectName.isNotEmpty() && currentChapterName.isNotEmpty() -> "$currentSubjectName • $currentChapterName"
                            else -> "Select a chapter to begin"
                        }
                        Text(
                            focusContext,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = FocusTextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "of $focusDurationMins min session",
                            fontSize = 11.sp,
                            color = Color(0xFF8A94A6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val controlsEnabled = (currentChapterId.isNotEmpty() || effectiveTopicId.isNotEmpty()) && !isSavingCompletion

                // Stop
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val stopClickable = controlsEnabled
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                            .clickable(enabled = stopClickable) {
                                if (elapsedMs > 0 || isRunning || isPaused) showStopDialog = true else navController.popBackStack()
                            }
                            .testTag("timer_stop_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = if (stopClickable) FocusTextDark else Color(0xFF8A94A6),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stop", fontSize = 13.sp, color = if (stopClickable) FocusTextDark else Color.Gray, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Play / Pause — gradient hero button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val startEnabled = controlsEnabled
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(
                                if (startEnabled) Brush.linearGradient(listOf(FocusPurple, Color(0xFF8B5CF6)))
                                else Brush.linearGradient(listOf(Color(0xFFB8BECB), Color(0xFFB8BECB)))
                            )
                            .clickable(enabled = startEnabled) {
                                if (isRunning && !isPaused) {
                                    viewModel.pauseTimer()
                                } else if (isPaused) {
                                    viewModel.resumeTimer()
                                } else {
                                    viewModel.startTimer(
                                        chapterId = currentChapterId,
                                        subjectId = currentSubjectId,
                                        subjectName = currentSubjectName,
                                        chapterName = currentChapterName,
                                        topicId = effectiveTopicId
                                    )
                                }
                            }
                            .testTag("timer_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning && !isPaused) "Pause" else "Start or Resume",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isRunning && !isPaused) "Pause" else if (isPaused || elapsedMs > 0) "Resume" else "Start",
                        fontSize = 13.sp,
                        color = if (startEnabled) FocusTextDark else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Skip
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val skipClickable = controlsEnabled
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                            .clickable(enabled = skipClickable) {
                                viewModel.skipSession(
                                    chapterId = currentChapterId,
                                    elapsedMs = elapsedMs,
                                    focusMs = focusMs,
                                    isRevisionActive = isRevisionActive,
                                    subjectId = currentSubjectId,
                                    subjectName = currentSubjectName,
                                    chapterName = currentChapterName
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = if (skipClickable) FocusTextDark else Color(0xFF8A94A6),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Skip", fontSize = 13.sp, color = if (skipClickable) FocusTextDark else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            // ---------- 6. QUICK DURATION PRESETS (new premium feature) ----------
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                SectionKicker("QUICK SESSION LENGTH")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 25, 45, 60, 90).forEach { mins ->
                        val isSelected = mins == focusDurationMins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFF1EEFF) else Color.White)
                                .border(1.dp, if (isSelected) FocusPurple else Color(0xFFE7EAF3), RoundedCornerShape(12.dp))
                                .clickable(enabled = !isRunning && !isPaused && !isBreakSession) { updateActiveDuration(mins) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${mins}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FocusPurple else FocusTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- 7. PREMIUM CONTROLS (logic & testTags unchanged) ----------
            if (showStopDialog) {
                AlertDialog(
                    onDismissRequest = { showStopDialog = false },
                    title = { Text("End Study Session?") },
                    text = { Text("Would you like to save and complete this revision session, or discard it without changing your revision count?") },
                    confirmButton = {
                        TextButton(
                            enabled = !isSavingCompletion,
                            onClick = {
                                if (isSavingCompletion) return@TextButton
                                isSavingCompletion = true
                                showStopDialog = false
                                viewModel.pauseTimer()
                                val actualElapsedMs = if (elapsedMs > focusMs && focusMs > 0) focusMs else elapsedMs.coerceAtLeast(60000L)
                                val mins = kotlin.math.ceil(actualElapsedMs / 60000.0).toInt().coerceAtLeast(1)
                                coroutineScope.launch {
                                    try {
                                        if (effectiveTopicId.isNotEmpty()) {
                                            viewModel.completeTopicTimerSessionSuspend(effectiveTopicId, actualElapsedMs)
                                        } else {
                                            viewModel.savePomodoroSession("Completed", actualElapsedMs, true, "Focus")
                                            viewModel.resetTimer()
                                        }
                                        val earnedBaseCoins = (mins * globalConfig.coinsPerBaselineTime) / maxOf(1, globalConfig.activeTimeBaselineMinutes)
                                        if (earnedBaseCoins > 0) {
                                            val sessionId = java.util.UUID.randomUUID().toString()
                                            viewModel.awardCoins(earnedBaseCoins, "Study Session (${mins} mins)", sessionId)
                                        }
                                        onSessionComplete(mins, 1, earnedBaseCoins)
                                    } finally {
                                        isSavingCompletion = false
                                    }
                                }
                            },
                            modifier = Modifier.testTag("confirm_complete_session_button")
                        ) {
                            Text("Complete & Save", color = FocusPurple, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Row {
                            TextButton(
                                onClick = {
                                    showStopDialog = false
                                    viewModel.resetTimer()
                                    navController.popBackStack()
                                },
                                modifier = Modifier.testTag("discard_session_button")
                            ) {
                                Text("Discard", color = Color.Red)
                            }
                            TextButton(onClick = { showStopDialog = false }) {
                                Text("Cancel", color = FocusTextGray)
                            }
                        }
                    }
                )
            }



            Spacer(modifier = Modifier.height(20.dp))

            // ---------- 8. AMBIENT SOUND QUICK PILL (new premium feature) ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE7EAF3), RoundedCornerShape(16.dp))
                    .clickable { showAmbientSoundSheet = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1EEFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(17.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ambient Sound", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FocusTextDark)
                    Text(
                        if (settings.ambientSound == "None" || !settings.soundEnabled) "Off — tap to choose a sound" else settings.ambientSound,
                        fontSize = 11.sp,
                        color = FocusTextGray
                    )
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8A94A6), modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- 9. ROTATING FOCUS TIP (new premium feature) ----------
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                FocusTipCard()
            }

            // ---------- 10. COMPLETE & SAVE (unchanged logic) ----------
            if (effectiveTopicId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (isSavingCompletion) return@Button
                        isSavingCompletion = true
                        viewModel.pauseTimer()
                        val actualElapsedMs = if (elapsedMs > 0L) elapsedMs else (focusDurationMins * 60 * 1000L)
                        val mins = kotlin.math.ceil(actualElapsedMs / 60000.0).toInt().coerceAtLeast(1)
                        coroutineScope.launch {
                            try {
                                val saved = viewModel.completeTopicTimerSessionSuspend(effectiveTopicId, actualElapsedMs)
                                if (saved) {
                                    android.widget.Toast.makeText(
                                        navController.context,
                                        "Revision completed and saved!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                            } finally {
                                isSavingCompletion = false
                            }
                        }
                    },
                    enabled = !isSavingCompletion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp)
                        .testTag("complete_session_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPurple)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSavingCompletion) "Saving Session..." else "Complete & Save Revision",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // ---------- 11. GOAL-REACHED BADGE ----------
            if (progressPercent >= 100) {
                Spacer(modifier = Modifier.height(16.dp))
                CongratsBadgeCard(
                    goalTarget = goalMins,
                    unitName = "minutes of focus",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
            }

            // ---------- 12. TODAY'S SUMMARY ----------
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Today's Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FocusTextDark)
                        Text("Your progress at a glance", fontSize = 11.sp, color = FocusTextGray)
                    }
                    Row(
                        modifier = Modifier.clickable { navController.navigate(AnalyticsRoute) { popUpTo(DashboardRoute) } },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("View All", color = FocusPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryColumnItem(Icons.Default.Timer, Color(0xFF6C4DFF), Color(0xFFF1EEFF), "Study Time", todayStudyTimeStr)
                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(Color(0xFFE7EAF3)))
                        SummaryColumnItem(Icons.Default.MenuBook, Color(0xFF3B82F6), Color(0xFFEFF6FF), "Sessions", "$focusSessionsToday")
                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(Color(0xFFE7EAF3)))
                        SummaryColumnItem(Icons.Default.FactCheck, Color(0xFF22C55E), Color(0xFFECFDF5), "Chapters Done", "$chaptersCompletedToday")
                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(Color(0xFFE7EAF3)))
                        SummaryColumnItem(Icons.Default.Adjust, Color(0xFFF43F5E), Color(0xFFFFF1F2), "Accuracy", accuracyValue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SummaryColumnItem(icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, iconBg: Color, title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontSize = 10.sp, color = FocusTextGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = 14.sp, color = FocusTextDark, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun FocusSettingItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 10.sp, color = FocusTextDark, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 10.sp, color = FocusTextGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSettingsBottomSheet(
    currentSettings: FocusSettings,
    activeMode: String,
    onDismiss: () -> Unit,
    onSave: (FocusSettings) -> Unit
) {
    var draftSettings by remember { mutableStateOf(currentSettings.copy()) }

    val activeDuration = when (activeMode) {
        "Study Mode" -> draftSettings.studyModeDurationMins
        "Revision Mode" -> draftSettings.revisionModeDurationMins
        "Practice Mode" -> draftSettings.practiceModeDurationMins
        "Mock Test Mode" -> draftSettings.mockTestModeDurationMins
        "Writing Practice Mode" -> draftSettings.writingPracticeModeDurationMins
        else -> draftSettings.focusDuration
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FocusBgLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFD9D2EE)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Premium sheet header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(FocusPurple, Color(0xFFA78BFA)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Customize Focus", fontWeight = FontWeight.Bold, fontSize = 21.sp, color = FocusTextDark)
                    Text("Settings for $activeMode", fontSize = 12.sp, color = FocusTextGray)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Focus duration
            SettingsSectionCard(title = "FOCUS DURATION", icon = Icons.Default.Timer) {
                DurationSelector(
                    options = listOf(5, 10, 15, 20, 25, 30, 45, 60, 90, 120, 180),
                    selected = activeDuration,
                    onSelect = { newDuration ->
                        draftSettings = when (activeMode) {
                            "Study Mode" -> draftSettings.copy(studyModeDurationMins = newDuration, focusDuration = newDuration)
                            "Revision Mode" -> draftSettings.copy(revisionModeDurationMins = newDuration, focusDuration = newDuration)
                            "Practice Mode" -> draftSettings.copy(practiceModeDurationMins = newDuration, focusDuration = newDuration)
                            "Mock Test Mode" -> draftSettings.copy(mockTestModeDurationMins = newDuration, focusDuration = newDuration)
                            "Writing Practice Mode" -> draftSettings.copy(writingPracticeModeDurationMins = newDuration, focusDuration = newDuration)
                            else -> draftSettings.copy(focusDuration = newDuration)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Breaks
            SettingsSectionCard(title = "SHORT BREAK", icon = Icons.Outlined.Coffee) {
                DurationSelector(
                    options = listOf(1, 2, 3, 5, 10, 15, 20, 30),
                    selected = draftSettings.shortBreakDuration,
                    onSelect = { draftSettings = draftSettings.copy(shortBreakDuration = it) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSectionCard(title = "LONG BREAK", icon = Icons.Outlined.Eco) {
                DurationSelector(
                    options = listOf(5, 10, 15, 20, 30, 45, 60),
                    selected = draftSettings.longBreakDuration,
                    onSelect = { draftSettings = draftSettings.copy(longBreakDuration = it) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Automation + feedback
            SettingsSectionCard(title = "AUTOMATION & FEEDBACK", icon = Icons.Default.Refresh) {
                SwitchSettingItem(
                    title = "Auto Start Focus",
                    subtitle = "Begin the next session automatically",
                    checked = draftSettings.autoStartFocus,
                    onCheckedChange = { draftSettings = draftSettings.copy(autoStartFocus = it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SwitchSettingItem(
                    title = "Enable Vibration",
                    subtitle = "Haptic cue when a session ends",
                    checked = draftSettings.vibrationEnabled,
                    onCheckedChange = { draftSettings = draftSettings.copy(vibrationEnabled = it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(draftSettings) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(FocusPurple, Color(0xFF8B5CF6)))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/** White card wrapper used by every section in the focus settings sheet. */
@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFFF1EEFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF8A94A6))
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun DurationSelector(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        items(options) { opt ->
            val isSelected = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) FocusPurple else Color(0xFFF8F9FC))
                    .border(1.dp, if (isSelected) FocusPurple else Color(0xFFE7EAF3), RoundedCornerShape(12.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    "$opt m",
                    color = if (isSelected) Color.White else FocusTextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8F9FC))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = FocusTextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = FocusTextGray, fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FocusPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD9D2EE)
            )
        )
    }
}

@Composable
fun DurationSelectorStr(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        items(options) { opt ->
            val isSelected = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) FocusPurple else Color(0xFFF8F9FC))
                    .border(1.dp, if (isSelected) FocusPurple else Color(0xFFE7EAF3), RoundedCornerShape(12.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(opt, color = if (isSelected) Color.White else FocusTextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbientSoundBottomSheet(
    currentSound: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sounds = listOf("None", "White Noise", "Pink Noise", "Brown Noise")
    val soundDescriptions = listOf("Silent studio", "Steady, even hum", "Softer, warmer tone", "Deep, calming rumble")
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FocusBgLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFD9D2EE)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(FocusPurple, Color(0xFFA78BFA)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Ambient Sounds", fontWeight = FontWeight.Bold, fontSize = 21.sp, color = FocusTextDark)
                    Text("Set the mood for deep work", fontSize = 12.sp, color = FocusTextGray)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    sounds.forEachIndexed { index, sound ->
                        val isSelected = currentSound == sound
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFF1EEFF) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) FocusPurple.copy(alpha = 0.35f) else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelect(sound) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) FocusPurple else Color(0xFFF1F3F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (sound == "None") Icons.Default.VolumeOff else Icons.Default.MusicNote,
                                    contentDescription = sound,
                                    tint = if (isSelected) Color.White else Color(0xFF8A94A6),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sound,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FocusPurple else FocusTextDark
                                )
                                Text(soundDescriptions[index], fontSize = 11.sp, color = FocusTextGray)
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(FocusPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        if (index < sounds.lastIndex) Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/* ============================================================================
   PREMIUM TIMER UI COMPONENTS
   Pure UI building blocks — no new data sources, no changes to timer logic.
   ============================================================================ */

/** Gradient arc timer ring with subtle tick marks around it. */
@Composable
private fun PremiumTimerRing(
    progress: Float,
    isBreak: Boolean,
    modifier: Modifier = Modifier
) {
    val accentA = if (isBreak) Color(0xFF0EA5A4) else Color(0xFF6C4DFF)
    val accentB = if (isBreak) Color(0xFF34D399) else Color(0xFFA78BFA)
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val stroke = 13.dp.toPx()
        val radius = (size.minDimension - stroke - 12.dp.toPx()) / 2f
        val arcTopLeft = Offset(cx - radius, cy - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        // Decorative minute ticks
        for (i in 0 until 48) {
            val angle = (i * 7.5f - 90f) * (kotlin.math.PI.toFloat() / 180f)
            val cos = kotlin.math.cos(angle)
            val sin = kotlin.math.sin(angle)
            val inner = radius + stroke / 2f + 4.dp.toPx()
            val outer = inner + if (i % 4 == 0) 7.dp.toPx() else 3.dp.toPx()
            drawLine(
                color = Color(0xFFD9D2EE).copy(alpha = if (i % 4 == 0) 0.9f else 0.45f),
                start = Offset(cx + inner * cos, cy + inner * sin),
                end = Offset(cx + outer * cos, cy + outer * sin),
                strokeWidth = if (i % 4 == 0) 1.8.dp.toPx() else 1.dp.toPx()
            )
        }

        // Track
        drawArc(
            color = Color(0xFFEBE7F6),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )

        // Animated progress arc
        val sweep = 360f * progress.coerceIn(0f, 1f)
        if (sweep > 0.5f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(accentA, accentB, accentA),
                    center = Offset(cx, cy)
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/** Small circular ring used by the daily goal card. */
@Composable
private fun MiniGoalRing(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val stroke = 7.dp.toPx()
        val radius = (size.minDimension - stroke) / 2f
        val topLeft = Offset(cx - radius, cy - radius)
        val arcSize = Size(radius * 2f, radius * 2f)
        drawArc(Color(0xFFEBE7F6), 0f, 360f, false, topLeft, arcSize, style = Stroke(width = stroke))
        val sweep = 360f * progress.coerceIn(0f, 1f)
        if (sweep > 0.5f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFF6C4DFF), Color(0xFFA78BFA), Color(0xFF6C4DFF)),
                    center = Offset(cx, cy)
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/** Premium pill used in the quick study-mode selector. */
@Composable
private fun ModePill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) FocusPurple else Color.White)
            .border(1.dp, if (selected) FocusPurple else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) Color.White else FocusPurple,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else FocusTextDark
            )
        }
    }
}

/** Small uppercase section label used across the new timer screen. */
@Composable
private fun SectionKicker(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = Color(0xFF8A94A6)
    )
}

/** Rotating motivation card — purely visual, auto-advances every 7 seconds. */
@Composable
private fun FocusTipCard() {
    val tips = listOf(
        "Small steps every day add up to big results.",
        "You don't have to see the whole staircase — just take the first step.",
        "Deep work beats long hours. Stay in the zone.",
        "Consistency is the secret weapon of top performers.",
        "Every focused minute is an investment in your future self."
    )
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(7000L)
            index = (index + 1) % tips.size
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, Color(0xFFE4DBFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "focus_tip"
            ) { idx ->
                Text(
                    text = tips[idx],
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = FocusTextDark,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
