package com.example.ui.screens.subject
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute
import com.example.viewmodel.RevisionViewModel
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.AiUiState
import com.example.data.formatDurationSpent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import kotlinx.coroutines.launch

// Colors
val TdPurplePrimary = Color(0xFF6C4DFF)
val TdPurpleLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
val TdBackgroundWhite: Color
    @Composable get() = MaterialTheme.colorScheme.background
val TdCardWhite: Color
    @Composable get() = MaterialTheme.colorScheme.surface
val TdSurfaceGray: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant
val TdTextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val TdTextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TdGreen = Color(0xFF10B981)
val TdGreenLight = Color(0xFFECFDF5)

// Density-aware custom dashed border modifier
fun Modifier.dashedBorder(
    strokeWidth: androidx.compose.ui.unit.Dp = 1.5.dp,
    color: Color = Color.LightGray,
    cornerRadius: androidx.compose.ui.unit.Dp = 10.dp,
    dashLength: androidx.compose.ui.unit.Dp = 4.dp,
    gapLength: androidx.compose.ui.unit.Dp = 3.dp
) = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f),
        cap = StrokeCap.Round
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

@Composable
fun TopicDetailScreenWrapper(viewModel: RevisionViewModel, navController: NavController, backStackEntry: NavBackStackEntry) {
    val aiViewModel: AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val route = backStackEntry.toRoute<TopicDetailRoute>()
    TopicDetailScreen(
        viewModel = viewModel,
        aiViewModel = aiViewModel,
        navController = navController,
        topicId = route.topicId,
        subjectId = route.subjectId,
        chapterId = route.chapterId,
        highlightStartRevision = route.highlightStartRevision,
        showHistory = route.showHistory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    viewModel: RevisionViewModel,
    aiViewModel: AiViewModel,
    navController: NavController,
    topicId: String,
    subjectId: String = "",
    chapterId: String = "",
    highlightStartRevision: Boolean = false,
    showHistory: Boolean = false
) {
    val allTopicsState by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(null)
    val allSubjectsState by viewModel.allSubjects.collectAsStateWithLifecycle(null)
    val allChaptersState by viewModel.allChapters.collectAsStateWithLifecycle(null)
    
    val allTopics = allTopicsState ?: emptyList()
    val allSubjects = allSubjectsState ?: emptyList()
    val allChapters = allChaptersState ?: emptyList()
    
    // Resolve the topic using topicId or automatically
    var resolvedTopic = allTopics.find { it.topicId == topicId }
    if (resolvedTopic == null && chapterId.isNotEmpty()) {
        val chapterTopics = allTopics.filter { it.chapterId == chapterId }
        resolvedTopic = chapterTopics.find { it.status == "Pending" } ?: chapterTopics.firstOrNull()
    }
    
    val topic = resolvedTopic
    
    LaunchedEffect(topic, chapterId, subjectId, allChaptersState, allTopicsState) {
        if (allTopicsState != null && allChaptersState != null) {
            if (topic == null && chapterId.isNotEmpty() && subjectId.isNotEmpty()) {
                val chapterTopics = allTopicsState!!.filter { it.chapterId == chapterId }
                if (chapterTopics.isEmpty()) {
                    val newTopicId = java.util.UUID.randomUUID().toString()
                    val chapterName = allChaptersState!!.find { it.id == chapterId }?.name ?: "Topic"
                    viewModel.createEmptyTopicSuspend(newTopicId, subjectId, chapterId, chapterName)
                }
            }
        }
    }
    val subject = allSubjects.find { it.id == (topic?.subjectId ?: subjectId) }?.name
        ?: topic?.subjectName?.ifBlank { "Subject" }
        ?: "Subject"
    val chapter = allChapters.find { it.id == (topic?.chapterId ?: chapterId) }?.name
        ?: topic?.chapterName?.ifBlank { "Chapter" }
        ?: "Chapter"
    
    val context = navController.context
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val revisionHistoryList by remember(topic?.topicId) {
        viewModel.getRevisionHistoryForTopic(topic?.topicId ?: "")
    }.collectAsStateWithLifecycle(emptyList())
    
    var showHistoryDialogState by remember { mutableStateOf(showHistory) }
    
    LaunchedEffect(scrollState, highlightStartRevision, showHistory) {
        if (highlightStartRevision || showHistory) {
            kotlinx.coroutines.delay(300)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    
    var showExplosion by remember { mutableStateOf(false) }
    var showTagEditDialog by remember { mutableStateOf(false) }
    var showEditTopicDialog by remember { mutableStateOf(false) }
    var showDeleteTopicConfirmDialog by remember { mutableStateOf(false) }
    var showStudyTimeDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var manualStudyTimeMins by remember(topic?.topicId) {
        mutableStateOf((topic?.estimatedRevisionTimeMins?.takeIf { it > 0 } ?: 25).toString())
    }
    var isPerformingAction by remember { mutableStateOf(false) }
    
    var selectedDifficulty by remember(topic?.topicId, topic?.difficulty) {
        mutableStateOf(topic?.difficulty ?: "Medium")
    }
    
    fun getSpacedRepetitionIntervals(diff: String): List<Int> {
        return com.example.services.RevisionEngine.getPatternForDifficulty(diff)
    }
    
    var currentIntervals by remember(topic?.topicId, topic?.revisionPattern) {
        mutableStateOf(topic?.revisionPattern?.takeIf { it.isNotEmpty() } ?: getSpacedRepetitionIntervals(topic?.difficulty ?: "Medium"))
    }
    
    // Formatting dates
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val shortDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Dynamic Calculations
    val revisionCount = maxOf(
        topic?.revisionCount ?: 0,
        topic?.completedRevisions ?: 0,
        topic?.totalRevisionCount ?: 0,
        topic?.totalRevisions ?: 0
    )
    val completedRevisionCount = revisionCount

    val realFirstStudyDateMs = topic?.firstStudyDate ?: topic?.startDateMs ?: 0L
    val hasStudied = realFirstStudyDateMs > 0L || topic?.studyCompleted == true || revisionCount > 0
    
    val firstStudyDateStr = if (realFirstStudyDateMs > 0L) dateFormat.format(Date(realFirstStudyDateMs)) else "-"
    val firstStudyDayStr = if (realFirstStudyDateMs > 0L) dayFormat.format(Date(realFirstStudyDateMs)) else "Pending"
    
    val realLastRevisionDateMs = topic?.lastRevisionDate ?: topic?.lastRevisionDateMs ?: 0L
    val hasRevised = realLastRevisionDateMs > 0L && revisionCount > 0
    
    val lastRevisionDateStr = if (hasRevised) dateFormat.format(Date(realLastRevisionDateMs)) else "-"
    val lastRevisionDayStr = if (hasRevised) dayFormat.format(Date(realLastRevisionDateMs)) else "Pending"
    
    val realNextRevMs = topic?.nextRevisionDate ?: topic?.nextRevisionDateMs ?: 0L
    val hasNextRevision = realNextRevMs > 0L
    
    val nextRevisionDateStr = if (hasNextRevision) dateFormat.format(Date(realNextRevMs)) else "-"
    val nextRevisionDayStr = if (hasNextRevision) dayFormat.format(Date(realNextRevMs)) else "Not scheduled"
    
    // Scheduled reminder system states for topic spaced-repetition
    val sharedPrefs = navController.context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE)
    
    var isBookmarked by remember(topic?.topicId) {
        mutableStateOf(sharedPrefs.getBoolean("topic_bookmark_${topic?.topicId ?: ""}", false))
    }
    
    // Check due status using start-of-day comparison
    val nextRevStartOfDay = if (hasNextRevision) {
        Calendar.getInstance().apply {
            timeInMillis = realNextRevMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } else 0L
    val diffDays = if (hasNextRevision) ((nextRevStartOfDay - todayStart) / 86400000L).toInt() else 999
    val badgeText = when {
        topic == null -> "No Topics"
        !hasNextRevision -> "Pending"
        diffDays < 0 -> "Overdue"
        diffDays == 0 -> "Due Today"
        diffDays == 1 -> "Due Tomorrow"
        else -> "Upcoming"
    }
    val statusColor = when (badgeText) {
        "Overdue" -> Color(0xFFEF4444)
        "Due Today" -> Color(0xFFF59E0B)
        "Due Tomorrow" -> Color(0xFF3B82F6)
        "Upcoming" -> TdPurplePrimary
        "Pending" -> Color(0xFF64748B)
        else -> TdGreen
    }
    
    // Setup study time format
    fun formatDuration(ms: Long, defaultStr: String): String {
        if (ms <= 0) return defaultStr
        val totalMins = ms / 60000
        val hrs = totalMins / 60
        val mins = totalMins % 60
        return if (hrs > 0) "${hrs}h ${mins} min" else "${mins} min"
    }

    val firstStudyTimeStr = if (hasStudied) {
        val rawTime = topic?.firstStudyTime
        if (rawTime != null && (rawTime.contains("min") || rawTime.contains("hr"))) {
            rawTime
        } else {
            val duration = topic?.firstStudyDuration ?: 0L
            val actualMs = if (duration > 0) duration else (topic?.studyTimeMs ?: 0L)
            if (actualMs > 0) formatDurationSpent(actualMs) else "${topic?.estimatedRevisionTimeMins ?: 30} min"
        }
    } else "-"

    val lastRevisionTimeStr = if (hasRevised) {
        val rawTime = topic?.lastRevisionTime
        if (rawTime != null && (rawTime.contains("min") || rawTime.contains("hr"))) {
            rawTime
        } else {
            val duration = topic?.lastRevisionDuration ?: 0L
            val actualMs = if (duration > 0) duration else (topic?.revisionTimeMs ?: 0L)
            if (actualMs > 0) formatDurationSpent(actualMs) else "${topic?.estimatedRevisionTimeMins ?: 30} min"
        }
    } else "-"
    
    val totalStudyTimeAll = formatDuration(topic?.studyTimeMs ?: 0L, "0 min")
    val totalRevisionTimeAll = formatDuration(topic?.revisionTimeMs ?: 0L, "0 min")
    
    val totalScheduled = currentIntervals.size.coerceAtLeast(1)
    val progressPercent = com.example.data.calculateProgressScore(
        studyCompleted = hasStudied,
        completedRevisions = completedRevisionCount,
        totalRequiredRevisions = totalScheduled
    )
    val progressFraction = (progressPercent / 100f).coerceIn(0f, 1f)

    Scaffold(
        containerColor = TdBackgroundWhite,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.3).sp,
                            color = TdTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Topic details and revisions",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TdTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(44.dp)
                            .background(TdCardWhite, RoundedCornerShape(14.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TdTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (topic != null) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(44.dp)
                                .background(TdCardWhite, RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { menuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TdTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(TdCardWhite)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Topic") },
                                    onClick = {
                                        menuExpanded = false
                                        showEditTopicDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Topic", color = Color.Red) },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteTopicConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TdBackgroundWhite)
            )
        },
        bottomBar = {
            if (topic != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TdCardWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    // Save Revision (opens dialog to enter manual study/revision time in minutes and saves revision)
                    OutlinedButton(
                        onClick = {
                            if (isPerformingAction) return@OutlinedButton
                            if (topic.topicId.isBlank() || currentIntervals.isEmpty() || currentIntervals.any { it <= 0 }) {
                                android.widget.Toast.makeText(
                                    navController.context,
                                    "Invalid topic or spacing intervals",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@OutlinedButton
                            }
                            if (manualStudyTimeMins.isBlank() || (manualStudyTimeMins.toIntOrNull() ?: 0) <= 0) {
                                manualStudyTimeMins = (topic.estimatedRevisionTimeMins.takeIf { it > 0 } ?: 25).toString()
                            }
                            showStudyTimeDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("save_revision_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TdPurplePrimary),
                        border = BorderStroke(1.5.dp, TdPurplePrimary),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isPerformingAction
                    ) {
                        Text(
                            text = if (isPerformingAction) "Saving..." else "Save Revision",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Start Timer (Filled)
                    val isHighlighted = highlightStartRevision
                    Button(
                        onClick = {
                            if (isPerformingAction) return@Button
                            viewModel.startTimer(
                                chapterId = topic.chapterId,
                                subjectId = topic.subjectId,
                                subjectName = subject,
                                chapterName = chapter,
                                topicId = topic.topicId
                            )
                            navController.navigate(
                                com.example.ui.TimerRoute(
                                    topicId = topic.topicId,
                                    isRevision = true,
                                    subjectId = topic.subjectId,
                                    subjectName = subject,
                                    chapterId = topic.chapterId,
                                    chapterName = chapter
                                )
                            )
                        },
                        enabled = !isPerformingAction,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp)
                            .then(if (isHighlighted) Modifier.border(3.dp, Color(0xFFFFB000), RoundedCornerShape(16.dp)) else Modifier)
                            .testTag("start_timer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighlighted) Color(0xFFE0B0FF) else TdPurplePrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = if (isHighlighted) TdPurplePrimary else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHighlighted) "Start Revision" else "Start Timer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isHighlighted) TdPurplePrimary else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (topic == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TdPurplePrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                ) {
                    // Breadcrumb (premium pill)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 10.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = TdPurplePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subject,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = TdTextSecondary,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TdTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = chapter,
                                fontWeight = FontWeight.SemiBold,
                                color = TdPurplePrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    // Hero card — topic identity, progress, status
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = TdCardWhite),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = topic.topicName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp,
                                    lineHeight = 27.sp,
                                    letterSpacing = (-0.3).sp,
                                    color = TdTextPrimary,
                                    modifier = Modifier.weight(1f).padding(end = 10.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isBookmarked) TdPurpleLight
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .clickable {
                                            isBookmarked = !isBookmarked
                                            sharedPrefs.edit().putBoolean("topic_bookmark_${topic.topicId}", isBookmarked).apply()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = TdPurplePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completion Progress",
                                    fontSize = 12.sp,
                                    color = TdTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.2.sp
                                )
                                Text(
                                    text = "$progressPercent%",
                                    fontSize = 16.sp,
                                    color = TdPurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("topic_progress_percentage")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Gradient progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progressFraction)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(TdPurplePrimary, Color(0xFFA78BFA))
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            val statusLabel = if (badgeText.isNotEmpty() && badgeText != "No Topics") badgeText else topic.status

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Status chip — colour follows due state
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(statusColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = statusLabel,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.testTag("topic_status_badge")
                                        )
                                    }
                                }

                                // Tags / Add tags
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { showTagEditDialog = true }
                                ) {
                                    if (topic.tags.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(50))
                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Add tags",
                                                    color = TdTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Tags",
                                                    tint = TdPurplePrimary,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            topic.tags.take(3).forEach { tag ->
                                                TopicTagChip(tag = tag)
                                            }
                                            if (topic.tags.size > 3) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "+${topic.tags.size - 3}",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Tags",
                                                tint = TdPurplePrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick stats — everything visible, no horizontal scrolling
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = TdCardWhite),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            TdStatTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Event,
                                tint = TdPurplePrimary,
                                label = "FIRST STUDY",
                                value = firstStudyDateStr,
                                sub = if (hasStudied) listOf(firstStudyDayStr, firstStudyTimeStr)
                                    .filter { it.isNotBlank() && it != "-" }
                                    .joinToString(" • ") else "Not started"
                            )
                            TdStatDivider()
                            TdStatTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Autorenew,
                                tint = Color(0xFF3B82F6),
                                label = "LAST REVISION",
                                value = lastRevisionDateStr,
                                sub = if (hasRevised) listOf(lastRevisionDayStr, lastRevisionTimeStr)
                                    .filter { it.isNotBlank() && it != "-" }
                                    .joinToString(" • ") else "Not revised yet"
                            )
                            TdStatDivider()
                            TdStatTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.CheckCircle,
                                tint = TdGreen,
                                label = "REVISIONS",
                                value = "$revisionCount",
                                sub = "of $totalScheduled scheduled"
                            )
                        }
                    }

                // Next revision + expected time (two balanced tiles)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TdCardWhite),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(TdPurplePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Event, contentDescription = null, tint = TdPurplePrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(9.dp))
                                Text("Next Revision", color = TdTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(nextRevisionDateStr, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TdTextPrimary, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(nextRevisionDayStr, color = TdTextSecondary, fontSize = 11.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(badgeText, color = statusColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(modifier = Modifier.width(1.dp).height(92.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(0.9f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.HourglassEmpty, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(9.dp))
                                Text("Expected Time", color = TdTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                formatEstimatedTime(topic?.estimatedRevisionTimeMins ?: 40),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TdTextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("per revision", color = TdTextSecondary, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                // Spaced Repetition Schedule
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TdCardWhite),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TdPurplePrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Autorenew, contentDescription = null, tint = TdPurplePrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Spaced Repetition", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TdTextPrimary)
                                Text("Smart intervals for long-term memory", fontSize = 11.sp, color = TdTextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TdPurplePrimary.copy(alpha = 0.10f))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text("SRS", color = TdPurplePrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showEditTopicDialog = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Schedule",
                                    tint = TdPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Optimize recall by scheduling intervals based on difficulty level. Shorter intervals reinforce fragile memory, while longer intervals save time.",
                            fontSize = 12.sp,
                            color = TdTextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text("Difficulty", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TdTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val difficulties = listOf("Easy", "Medium", "Hard")
                            difficulties.forEach { diff ->
                                val isSelected = selectedDifficulty.lowercase(Locale.getDefault()) == diff.lowercase(Locale.getDefault())
                                val selectedBg = when (diff.lowercase(Locale.getDefault())) {
                                    "easy" -> Color(0xFFDCFCE7)
                                    "hard" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                }
                                val selectedText = when (diff.lowercase(Locale.getDefault())) {
                                    "easy" -> Color(0xFF15803D)
                                    "hard" -> Color(0xFFB91C1C)
                                    else -> Color(0xFFB45309)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) selectedBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                        .border(
                                            1.dp,
                                            if (isSelected) selectedText.copy(alpha = 0.35f) else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedDifficulty = diff
                                            currentIntervals = getSpacedRepetitionIntervals(diff)
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("difficulty_${diff.lowercase(Locale.ROOT)}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) selectedText else TdTextSecondary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = diff,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) selectedText else TdTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Spacing gaps", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TdTextPrimary)
                            Text(
                                text = "Tap  -  /  +  to fine-tune",
                                fontSize = 10.5.sp,
                                color = TdTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentIntervals.forEachIndexed { index, interval ->
                                Column(
                                    modifier = Modifier
                                        .width(62.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${interval}d",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TdTextPrimary,
                                        modifier = Modifier.testTag("interval_value_$index")
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                                .clickable {
                                                    val newList = currentIntervals.toMutableList()
                                                    val newVal = (interval - 1).coerceAtLeast(1)
                                                    newList[index] = newVal
                                                    currentIntervals = newList
                                                }
                                                .testTag("interval_minus_$index"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TdPurplePrimary)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                                .clickable {
                                                    val newList = currentIntervals.toMutableList()
                                                    val newVal = interval + 1
                                                    newList[index] = newVal
                                                    currentIntervals = newList
                                                }
                                                .testTag("interval_plus_$index"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TdPurplePrimary)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Timeline Preview header
                        Text("Preview", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TdTextPrimary)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Horizontally scrollable timeline preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasStudied) {
                                // Start Day 0
                                ScheduleItem(
                                    day = "Day 0",
                                    date = shortDateFormat.format(Date(realFirstStudyDateMs)),
                                    status = "done"
                                )
                                
                                currentIntervals.forEachIndexed { idx, intervalDays ->
                                    val dateMs = realFirstStudyDateMs + (intervalDays * 86400000L)
                                    val revNum = idx + 1
                                    val status = when {
                                        revNum <= completedRevisionCount -> "done"
                                        revNum == completedRevisionCount + 1 -> "next"
                                        else -> "future"
                                    }
                                    
                                    TimelineLine(isDashed = status != "done", modifier = Modifier.width(36.dp))
                                    ScheduleItem(
                                        day = "Day $intervalDays",
                                        date = shortDateFormat.format(Date(dateMs)),
                                        status = status
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Preview will be available after first study",
                                        fontSize = 12.sp,
                                        color = TdTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // Save Button (Only show if diff detected)
                        val isModified = selectedDifficulty != topic.difficulty || currentIntervals != (topic.revisionPattern ?: getSpacedRepetitionIntervals(topic.difficulty))
                        
                        if (isModified) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (isPerformingAction) return@Button
                                    scope.launch {
                                        isPerformingAction = true
                                        try {
                                            val baseFirstStudyMs = if (realFirstStudyDateMs > 0L) realFirstStudyDateMs else System.currentTimeMillis()
                                            val baseLastRevMs = if (realLastRevisionDateMs > 0L) realLastRevisionDateMs else baseFirstStudyMs
                                            // Calculate new next revision date based on current revision count and pattern (including recurring 30 days)
                                            val nextDateMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                                                revisionCount = completedRevisionCount,
                                                pattern = currentIntervals,
                                                firstStudyMs = baseFirstStudyMs,
                                                lastRevisionMs = baseLastRevMs
                                            )
                                            
                                            // Save to database
                                            viewModel.updateTopicSpacedRepetitionSuspend(
                                                topicId = topic.topicId,
                                                difficulty = selectedDifficulty,
                                                revisionPattern = currentIntervals,
                                                nextRevisionDateMs = nextDateMs,
                                                startDateMs = realFirstStudyDateMs
                                            )
                                            
                                            // Show toast confirmation
                                            android.widget.Toast.makeText(
                                                navController.context,
                                                "Spaced repetition schedule successfully applied for $selectedDifficulty level!",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(
                                                navController.context,
                                                "Failed to save schedule: ${e.message}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            isPerformingAction = false
                                        }
                                    }
                                },
                                enabled = !isPerformingAction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TdPurplePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SAVE & APPLY SPACED SCHEDULE",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                    // AI Revision Assistant section
                    AiRevisionAssistant(
                        topicName = topic.topicName,
                        notes = topic.notes,
                        viewModel = aiViewModel
                    )

                    // Topic Details Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TdCardWhite),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TdPurplePrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Assignment, contentDescription = null, tint = TdPurplePrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Topic Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TdTextPrimary)
                                Text("Everything about this topic", fontSize = 11.sp, color = TdTextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        TopicDetailRow(Icons.Outlined.Assignment, "Subject", subject, isPurple = true)
                        TopicDetailDivider()
                        TopicDetailRow(Icons.Outlined.Assignment, "Chapter", chapter)
                        TopicDetailDivider()
                        
                        // Status row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TdPurplePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Status", color = TdTextSecondary, fontSize = 14.sp)
                            }
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(topic.status, color = TdPurplePrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        TopicDetailDivider()
                        TopicDetailRow(Icons.Outlined.Schedule, "Total Study Time (All)", totalStudyTimeAll, isPurple = true)
                        TopicDetailDivider()
                        TopicDetailRow(Icons.Outlined.Autorenew, "Total Revision Time (All)", totalRevisionTimeAll, isPurple = true)
                        TopicDetailDivider()
                        TopicDetailRow(Icons.Outlined.Assignment, "Notes", if (topic.notes.isNotBlank()) topic.notes else "No notes added")
                    }
                }
                
                Spacer(modifier = Modifier.height(96.dp))
            }
            
            if (showHistoryDialogState) {
                AlertDialog(
                    onDismissRequest = { showHistoryDialogState = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Autorenew,
                                contentDescription = null,
                                tint = TdPurplePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Revision History", fontWeight = FontWeight.Bold, color = TdTextPrimary)
                        }
                    },
                    text = {
                        if (revisionHistoryList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No revision history available yet.", color = TdTextSecondary, fontSize = 14.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                revisionHistoryList.forEachIndexed { index, record ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Revision #${revisionHistoryList.size - index}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = TdTextPrimary
                                                )
                                                Text(
                                                    text = "Completed",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF16A34A)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Date: " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(record.completedDateMs)),
                                                fontSize = 12.sp,
                                                color = TdTextSecondary
                                            )
                                            val durationMins = record.durationMs / (60 * 1000L)
                                            if (durationMins > 0) {
                                                Text(
                                                    text = "Duration: ${durationMins} mins",
                                                    fontSize = 12.sp,
                                                    color = TdTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showHistoryDialogState = false }) {
                            Text("Close", color = TdPurplePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
            
            // Celebration explosion animation overlay
            AnimatedVisibility(
                visible = showExplosion,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CheckmarkExplosionEffect(
                    modifier = Modifier.fillMaxSize(),
                    onAnimationEnd = { showExplosion = false }
                )
            }

            // Tag Editor Dialog Overlay
            if (showTagEditDialog) {
                TagEditDialog(
                    currentTags = topic.tags,
                    onDismiss = { showTagEditDialog = false },
                    onTagsUpdated = { updatedTags ->
                        viewModel.updateTopicDetails(
                            topicId = topic.topicId,
                            name = topic.topicName,
                            difficulty = topic.difficulty,
                            estimatedRevisionTimeMins = topic.estimatedRevisionTimeMins,
                            notes = topic.notes,
                            tags = updatedTags
                        )
                        showTagEditDialog = false
                    }
                )
            }

            // Edit Topic Dialog Overlay
            if (showEditTopicDialog) {
                var editTopicName by remember { mutableStateOf(topic.topicName) }
                var editTopicNotes by remember { mutableStateOf(topic.notes) }
                var editTopicTime by remember { mutableStateOf(topic.estimatedRevisionTimeMins.toString()) }
                var editTopicDifficulty by remember { mutableStateOf(topic.difficulty) }
                var editTopicTags by remember { mutableStateOf(topic.tags.joinToString(", ")) }

                AlertDialog(
                    onDismissRequest = { showEditTopicDialog = false },
                    containerColor = TdCardWhite,
                    title = { Text("Edit Topic Details", fontWeight = FontWeight.Bold, color = TdTextPrimary) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = editTopicName,
                                onValueChange = { editTopicName = it },
                                label = { Text("Topic Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )
                            
                            OutlinedTextField(
                                value = editTopicNotes,
                                onValueChange = { editTopicNotes = it },
                                label = { Text("Notes (Optional)") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )

                            OutlinedTextField(
                                value = editTopicTime,
                                onValueChange = { editTopicTime = it },
                                label = { Text("Estimated Revision Time (mins)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )

                            Column {
                                Text("Difficulty Level", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TdTextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                                        val isSelected = editTopicDifficulty.lowercase(Locale.getDefault()) == diff.lowercase(Locale.getDefault())
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { editTopicDifficulty = diff },
                                            label = { Text(diff) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TdPurpleLight,
                                                selectedLabelColor = TdPurplePrimary
                                            )
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = editTopicTags,
                                onValueChange = { editTopicTags = it },
                                label = { Text("Tags (comma separated)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmedName = editTopicName.trim()
                                if (trimmedName.isNotEmpty()) {
                                    val finalTime = editTopicTime.toIntOrNull() ?: topic.estimatedRevisionTimeMins
                                    val finalTags = editTopicTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.updateTopicDetails(
                                        topicId = topic.topicId,
                                        name = trimmedName,
                                        difficulty = editTopicDifficulty,
                                        estimatedRevisionTimeMins = finalTime,
                                        notes = editTopicNotes.trim(),
                                        tags = finalTags
                                    )
                                    showEditTopicDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TdPurplePrimary)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditTopicDialog = false }) {
                            Text("Cancel", color = TdTextSecondary)
                        }
                    }
                )
            }

            // Delete Topic Confirmation Dialog
            if (showDeleteTopicConfirmDialog && topic != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteTopicConfirmDialog = false },
                    containerColor = TdCardWhite,
                    title = { Text("Delete Topic", fontWeight = FontWeight.Bold, color = Color.Red) },
                    text = {
                        Text(
                            "Are you sure you want to delete \"${topic.topicName}\"? This topic and all its revision history will be permanently deleted.",
                            color = TdTextPrimary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteTopic(context, topic.topicId)
                                showDeleteTopicConfirmDialog = false
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteTopicConfirmDialog = false }) {
                            Text("Cancel", color = TdTextSecondary)
                        }
                    }
                )
            }
            
            if (showStudyTimeDialog && topic != null) {
                AlertDialog(
                    onDismissRequest = { if (!isPerformingAction) showStudyTimeDialog = false },
                    containerColor = TdCardWhite,
                    title = {
                        Text(
                            "Enter Study / Revision Time",
                            fontWeight = FontWeight.Bold,
                            color = TdTextPrimary
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "How many minutes did it take to study or revise \"${topic.topicName}\"? (e.g., 20 or 25 mins)",
                                fontSize = 14.sp,
                                color = TdTextSecondary
                            )
                            // Quick time preset chips (15, 20, 25, 30, 45, 60 mins)
                            val presets = listOf(15, 20, 25, 30, 45, 60)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                presets.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { presetMins ->
                                            val isSelectedPreset = manualStudyTimeMins.trim() == presetMins.toString()
                                            Surface(
                                                onClick = { manualStudyTimeMins = presetMins.toString() },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .testTag("preset_time_${presetMins}"),
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelectedPreset) TdPurplePrimary else TdPurpleLight,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelectedPreset) TdPurplePrimary else TdPurplePrimary.copy(alpha = 0.25f)
                                                )
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${presetMins} min",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelectedPreset) Color.White else TdPurplePrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = manualStudyTimeMins,
                                onValueChange = { input ->
                                    manualStudyTimeMins = input.filter { it.isDigit() }
                                },
                                label = { Text("Time (Minutes)") },
                                placeholder = { Text("e.g. 20 or 25") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("manual_study_time_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isPerformingAction) return@Button
                                val mins = manualStudyTimeMins.trim().toIntOrNull() ?: 0
                                if (mins <= 0) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        try {
                                            android.widget.Toast.makeText(
                                                navController.context,
                                                "Please enter valid minutes (e.g., 20 or 25)",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (_: Throwable) {}
                                    }
                                    return@Button
                                }
                                scope.launch {
                                    isPerformingAction = true
                                    try {
                                        val nowMs = System.currentTimeMillis()
                                        viewModel.updateTopicSpacedRepetitionSuspend(
                                            topicId = topic.topicId,
                                            difficulty = selectedDifficulty,
                                            revisionPattern = currentIntervals,
                                            nextRevisionDateMs = if (realFirstStudyDateMs > 0L) com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                                                revisionCount = completedRevisionCount,
                                                pattern = currentIntervals,
                                                firstStudyMs = realFirstStudyDateMs,
                                                lastRevisionMs = if (realLastRevisionDateMs > 0L) realLastRevisionDateMs else realFirstStudyDateMs
                                            ) else 0L,
                                            startDateMs = if (realFirstStudyDateMs > 0L) realFirstStudyDateMs else 0L
                                        )
                                        val sessionDurationMs = mins * 60 * 1000L
                                        val saved = viewModel.completeTopicTimerSessionSuspend(
                                            topicId = topic.topicId,
                                            durationMs = sessionDurationMs,
                                            sessionKey = "save_rev_${topic.topicId}_$nowMs"
                                        )
                                        showStudyTimeDialog = false
                                        if (saved) {
                                            showExplosion = true
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                try {
                                                    android.widget.Toast.makeText(
                                                        navController.context,
                                                        "Revision saved (${mins} min)!",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } catch (_: Throwable) {}
                                            }
                                        } else {
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                try {
                                                    android.widget.Toast.makeText(
                                                        navController.context,
                                                        viewModel.syncError.value ?: "Failed to save revision",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                } catch (_: Throwable) {}
                                            }
                                        }
                                    } catch (ce: kotlinx.coroutines.CancellationException) {
                                        throw ce
                                    } catch (e: Exception) {
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            try {
                                                android.widget.Toast.makeText(
                                                    navController.context,
                                                    "Failed to save revision: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            } catch (_: Throwable) {}
                                        }
                                    } finally {
                                        isPerformingAction = false
                                        showStudyTimeDialog = false
                                    }
                                }
                            },
                            modifier = Modifier.testTag("confirm_save_revision_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TdPurplePrimary),
                            enabled = !isPerformingAction
                        ) {
                            Text("Save Revision")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showStudyTimeDialog = false },
                            enabled = !isPerformingAction
                        ) {
                            Text("Cancel", color = TdTextSecondary)
                        }
                    }
                )
            }

            // Add Topic Dialog Overlay
            if (showAddTopicDialog) {
                var name by remember { mutableStateOf("") }
                var notes by remember { mutableStateOf("") }
                var tagsInput by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAddTopicDialog = false },
                    containerColor = TdCardWhite,
                    title = { Text("Add Topic", fontWeight = FontWeight.Bold, color = TdTextPrimary) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Topic Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notes (Optional)") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )
                            OutlinedTextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it },
                                label = { Text("Tags (comma separated)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TdPurplePrimary,
                                    focusedLabelColor = TdPurplePrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isNotBlank() && chapterId.isNotEmpty()) {
                                    val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.addTopic(chapterId, name, notes, tagsList)
                                    showAddTopicDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TdPurplePrimary)
                        ) { Text("Add") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddTopicDialog = false }) {
                            Text("Cancel", color = TdTextSecondary)
                        }
                    }
                )
            }
        }
    }
}
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null, tint = TdPurplePrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TdTextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            minLines = 2,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TdTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subValue.ifEmpty { " " },
            color = if (subValue.isEmpty()) Color.Transparent else TdTextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun StatVerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(TdSurfaceGray))
}

@Composable
fun ScheduleItem(day: String, date: String, status: String) {
    val bgColor = when (status) {
        "done" -> TdGreen.copy(alpha = 0.12f)
        "next" -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when (status) {
        "done" -> Color.Transparent
        "next" -> TdPurplePrimary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(38.dp)) {
        if (status == "done") {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TdGreen, modifier = Modifier.size(18.dp))
            }
        } else if (status == "next") {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bgColor, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Next", fontSize = 8.sp, color = TdPurplePrimary, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .dashedBorder(
                        strokeWidth = 1.5.dp,
                        color = borderColor,
                        cornerRadius = 10.dp,
                        dashLength = 4.dp,
                        gapLength = 3.dp
                    )
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {}
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(day, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = TdTextPrimary, textAlign = TextAlign.Center)
        Text(date, fontSize = 8.sp, color = TdTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun TimelineLine(isDashed: Boolean, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            val pathEffect = if (isDashed) {
                PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            } else null

            // Draw connector line
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2f,
                pathEffect = pathEffect
            )

            // Central dot
            drawCircle(
                color = lineColor,
                radius = 3f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}

@Composable
fun TopicDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, isPurple: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(TdPurplePrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TdPurplePrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                label,
                color = TdTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            value,
            color = if (isPurple) TdPurplePrimary else TdTextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isPurple) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.25f)
        )
    }
}

@Composable
fun TopicDetailDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TdSurfaceGray))
}

@Composable
fun AiRevisionAssistant(
    topicName: String,
    notes: String,
    viewModel: AiViewModel
) {
    val uiState by viewModel.summaryState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "AI Revision Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TdTextPrimary
                        )
                        Text(
                            "Smart summary from your notes",
                            fontSize = 11.sp,
                            color = TdTextSecondary
                        )
                    }
                }
                
                if (uiState is AiUiState.Success) {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text("Clear", color = Color(0xFF007BFF), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = uiState) {
                is AiUiState.Idle -> {
                    Text(
                        "Get a smart AI-powered summary of your notes to speed up your revision.",
                        fontSize = 13.sp,
                        color = TdTextSecondary,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.generateSummary(topicName, notes) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Summary", fontWeight = FontWeight.Bold)
                    }
                }
                is AiUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF007BFF))
                    }
                }
                is AiUiState.Success -> {
                    Text(
                        state.text,
                        fontSize = 14.sp,
                        color = TdTextPrimary,
                        lineHeight = 21.sp
                    )
                }
                is AiUiState.Error -> {
                    Text(
                        state.message,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.generateSummary(topicName, notes) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun TdStatTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    value: String,
    sub: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TdTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TdTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sub.ifBlank { " " },
            color = TdTextSecondary,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TdStatDivider() {
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .width(1.dp)
            .height(62.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
