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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

val FocusBgLight = Color(0xFFF6F7FB)
val FocusPurple = Color(0xFF6C4DFF)
val FocusPurpleLight = Color(0xFFE0D8FE)
val FocusTextDark = Color(0xFF0F172A)
val FocusTextGray = Color(0xFF5B6478)

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

    LaunchedEffect(isRunning, settings.ambientSound) {
        AmbientSoundPlayer.setSound(settings.ambientSound)
        if (isRunning) {
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


    LaunchedEffect(isRunning, settings.ambientSound) {
        AmbientSoundPlayer.setSound(settings.ambientSound)
        if (isRunning) {
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
                        border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
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

    Scaffold(
        containerColor = FocusBgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Focus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.3).sp,
                            color = FocusTextDark
                        )
                        Text(
                            "Stay in the zone",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = FocusTextGray
                        )
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
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                                .clickable { showAmbientSoundSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = FocusTextDark, modifier = Modifier.size(19.dp))
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = FocusBgLight)
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
            // Top Row: Choose Topic Button
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = FocusPurple)
                        .clip(RoundedCornerShape(24.dp))
                        .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(FocusPurple, Color(0xFF8B5CF6))))
                        .clickable(enabled = !isRunning) { 
                            showTopicSelectionSheet = true 
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val buttonText = if (currentChapterName.isNotEmpty()) {
                            "📚 ${currentChapterName.uppercase()}"
                        } else {
                            "Choose Chapter"
                        }
                        
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 240.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Study Session Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Study Session",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FocusTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentSession = sessionCount.coerceIn(1, 4)
                    for (i in 1..4) {
                        val isActive = i <= currentSession
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isActive) FocusPurple else Color(0xFFCBD5E1))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Timer Circle Room
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Pulse Animation
                if (isRunning) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(FocusPurple.copy(alpha = pulseAlpha))
                    )
                }
                
                // The circular progress
                Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF1F3F9),
                        strokeWidth = 10.dp,
                    )
                    AnimatedCircularProgress(
                        progress = progress,
                        modifier = Modifier.fillMaxSize(),
                        color = FocusPurple,
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1EEFF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TurnedInNot, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val statusLabel = when (activeMode) {
                            "Study Mode" -> "STUDYING"
                            "Revision Mode" -> "REVISING"
                            "Practice Mode" -> "PRACTICING"
                            "Mock Test Mode" -> "TESTING"
                            "Writing Practice Mode" -> "WRITING"
                            else -> "PRACTICING"
                        }
                        
                        // Subject & Chapter Display
                        if (currentSessionTypeState == "Break") {
                            Text("Relaxation\nShort Break", color = FocusPurple, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        } else if (currentSubjectName.isNotEmpty() && currentChapterName.isNotEmpty()) {
                            Text("$currentSubjectName\n$currentChapterName", color = FocusPurple, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        } else {
                            Text("Please select a chapter to start studying.", color = FocusPurple, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        
                        val remainingMs = (focusMs - elapsedMs).coerceAtLeast(0L)
                        val m = (remainingMs / 1000) / 60
                        val s = (remainingMs / 1000) % 60
                        Text(String.format("%02d:%02d", m, s), fontSize = 72.sp, fontWeight = FontWeight.Bold, color = FocusTextDark, letterSpacing = (-2).sp)
                        
                        val elapsedM = (elapsedMs / 1000) / 60
                        val elapsedS = (elapsedMs / 1000) % 60
                        Text(
                            text = String.format("Elapsed: %02d:%02d", elapsedM, elapsedS),
                            fontSize = 13.sp,
                            color = FocusTextGray,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("timer_elapsed_text")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF1EEFF)) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Timer, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(String.format("%02d:00", focusDurationMins), fontSize = 14.sp, color = FocusPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }    
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Controls
            var showStopDialog by remember { mutableStateOf(false) }

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

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val controlsEnabled = (currentChapterId.isNotEmpty() || effectiveTopicId.isNotEmpty()) && !isSavingCompletion
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val stopClickable = controlsEnabled
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (stopClickable) Color(0xFFF1EEFF) else Color(0xFFE7EAF3))
                            .clickable(enabled = stopClickable) { 
                                if (elapsedMs > 0 || isRunning || isPaused) showStopDialog = true else navController.popBackStack()
                            }
                            .testTag("timer_stop_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Stop, 
                            contentDescription = "Stop", 
                            tint = if (stopClickable) FocusPurple else Color(0xFF8A94A6), 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stop", fontSize = 14.sp, color = if (stopClickable) FocusTextDark else Color.Gray, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val startEnabled = controlsEnabled
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (startEnabled) Color(0xFF6B4EFF) else Color(0xFF8A94A6))
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
                            .then(if (startEnabled) Modifier.shadow(4.dp, CircleShape, spotColor = Color(0xFF6B4EFF)) else Modifier)
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
                        fontSize = 14.sp, 
                        color = if (startEnabled) FocusTextDark else Color.Gray, 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val skipClickable = controlsEnabled
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (skipClickable) Color(0xFFF1EEFF) else Color(0xFFE7EAF3))
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
                            tint = if (skipClickable) FocusPurple else Color(0xFF8A94A6), 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Skip", fontSize = 14.sp, color = if (skipClickable) FocusTextDark else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

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
                        .padding(horizontal = 32.dp)
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
            
            Spacer(modifier = Modifier.height(32.dp))

            // Today's Focus Summary
            val logs by viewModel.allStudyLogs.collectAsStateWithLifecycle()
            val chaptersList by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
            val testsList by viewModel.allSubjectTests.collectAsStateWithLifecycle(emptyList())
            
            // basic calculation for today
            val todayMs = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startOfToday = calendar.timeInMillis
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = dateFormat.format(java.util.Date(todayMs))
            val todayLog = logs.find { it.dateString == todayStr }
            val todayStudyTimeMs = (todayLog?.studyTimeMs ?: 0L) + (todayLog?.revisionTimeMs ?: 0L)
            
            val totalTodayMins = kotlin.math.ceil(todayStudyTimeMs / 60000.0).toInt()
            val h = totalTodayMins / 60
            val m = totalTodayMins % 60
            val todayStudyTimeStr = if (h > 0) "${h}h ${m}m" else "${m}m"
            
            val focusSessionsToday = todayLog?.focusSessions ?: 0
            val chaptersCompletedToday = chaptersList.count { 
                val fDate = it.firstStudyDateMs ?: 0L
                fDate >= startOfToday
            }
            
            val totalObtained = testsList.sumOf { it.marksObtained.toDouble() }
            val totalMax = testsList.sumOf { it.totalMarks.toDouble() }
            val accuracyValue = if (totalMax > 0.0) {
                "${(totalObtained / totalMax * 100).toInt()}%"
            } else {
                "--"
            }
            
            val goalMins = settings.dailyFocusGoalMins.coerceAtLeast(1)
            val progressPercent = ((totalTodayMins.toFloat() / goalMins.toFloat()) * 100).toInt().coerceIn(0, 100)
            
            if (progressPercent >= 100) {
                CongratsBadgeCard(
                    goalTarget = goalMins,
                    unitName = "minutes of focus",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Today's Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FocusTextDark)
                    Row(modifier = Modifier.clickable { navController.navigate(AnalyticsRoute) { popUpTo(DashboardRoute) } }, verticalAlignment = Alignment.CenterVertically) {
                        Text("View All", color = FocusPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = FocusPurple, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F3F9))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        SummaryColumnItem(Icons.Default.Timer, Color(0xFF8B5CF6), Color(0xFFF1EEFF), "Study Time", todayStudyTimeStr)
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF1F3F9)))
                        SummaryColumnItem(Icons.Default.MenuBook, Color(0xFF3B82F6), Color(0xFFEFF6FF), "Sessions", "$focusSessionsToday")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF1F3F9)))
                        SummaryColumnItem(Icons.Default.FactCheck, Color(0xFF22C55E), Color(0xFFECFDF5), "Chapters Done", "$chaptersCompletedToday")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF1F3F9)))
                        SummaryColumnItem(Icons.Default.Adjust, Color(0xFFF43F5E), Color(0xFFFFF1F2), "Accuracy", accuracyValue)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Customize Focus", fontWeight = FontWeight.Black, fontSize = 24.sp, color = FocusTextDark)
            Text("Adjust settings for $activeMode", fontSize = 14.sp, color = FocusTextGray)
            Spacer(modifier = Modifier.height(24.dp))

            // Pomodoro Selection
            SettingSectionTitle("🍅 Focus Duration")
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

            // Short Break
            SettingSectionTitle("☕ Short Break Duration")
            DurationSelector(
                options = listOf(1, 2, 3, 5, 10, 15, 20, 30),
                selected = draftSettings.shortBreakDuration,
                onSelect = { draftSettings = draftSettings.copy(shortBreakDuration = it) }
            )

            // Long Break
            SettingSectionTitle("🌿 Long Break Duration")
            DurationSelector(
                options = listOf(5, 10, 15, 20, 30, 45, 60),
                selected = draftSettings.longBreakDuration,
                onSelect = { draftSettings = draftSettings.copy(longBreakDuration = it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F3F9))

            // Toggles
            SettingSectionTitle("🔄 Automation")
            SwitchSettingItem("Auto Start Focus Session", draftSettings.autoStartFocus) {
                draftSettings = draftSettings.copy(autoStartFocus = it)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F3F9))

            SettingSectionTitle("📳 Feedback")
            SwitchSettingItem("Enable Vibration", draftSettings.vibrationEnabled) {
                draftSettings = draftSettings.copy(vibrationEnabled = it)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onSave(draftSettings) },
                colors = ButtonDefaults.buttonColors(containerColor = FocusPurple),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingSectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FocusTextDark, modifier = Modifier.padding(bottom = 12.dp, top = 8.dp))
}

@Composable
fun DurationSelector(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        items(options) { opt ->
            val isSelected = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) FocusPurple else Color.White)
                    .border(1.dp, if (isSelected) FocusPurple else Color(0xFFE7EAF3), RoundedCornerShape(12.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("$opt m", color = if (isSelected) Color.White else FocusTextDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SwitchSettingItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = FocusTextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FocusPurple)
        )
    }
}

@Composable
fun DurationSelectorStr(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        items(options) { opt ->
            val isSelected = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) FocusPurple else Color.White)
                    .border(1.dp, if (isSelected) FocusPurple else Color(0xFFE7EAF3), RoundedCornerShape(12.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(opt, color = if (isSelected) Color.White else FocusTextDark, fontWeight = FontWeight.Bold)
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text("Ambient Focus Sounds", fontWeight = FontWeight.Black, fontSize = 24.sp, color = FocusTextDark)
            Text("Enhance your focus with background noise", fontSize = 14.sp, color = FocusTextGray)
            Spacer(modifier = Modifier.height(24.dp))
            
            sounds.forEach { sound ->
                val isSelected = currentSound == sound
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FocusPurpleLight else Color.Transparent)
                        .clickable { onSelect(sound) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (sound == "None") Icons.Outlined.MusicNote else Icons.Outlined.MusicNote
                    Icon(
                        icon,
                        contentDescription = sound,
                        tint = if (isSelected) FocusPurple else FocusTextGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = sound,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) FocusPurple else FocusTextDark,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = FocusPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
