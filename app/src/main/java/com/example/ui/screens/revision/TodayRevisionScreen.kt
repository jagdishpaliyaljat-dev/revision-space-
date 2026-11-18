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

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import com.example.data.TopicWithSubjectAndChapter
import com.example.viewmodel.RevisionViewModel
import java.text.SimpleDateFormat
import java.util.*

// Design Theme Colors matching Material 3 & Image Reference
val RevBgLight: Color
    @Composable get() = MaterialTheme.colorScheme.background
val RevTextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val RevTextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val RevPrimary = Color(0xFF6C4DFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayRevisionScreen(viewModel: RevisionViewModel, navController: NavController, initialView: String = "dashboard") {
    var currentView by remember { mutableStateOf(initialView) }
    var selectedListFilter by remember { mutableStateOf("Today") }

    BackHandler(enabled = currentView != "dashboard") {
        currentView = "dashboard"
    }

    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(emptyList())
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val isTopicsLoaded by viewModel.isTopicsLoaded.collectAsStateWithLifecycle(false)

    val isInitiallyLoading = !isTopicsLoaded && syncError == null
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkAndTriggerNotifications(context)
    }

    val settings by viewModel.revisionSettings.collectAsStateWithLifecycle()
    var toastShownDayKey by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var showGoalDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showTopicExplosion by remember { mutableStateOf(false) }
    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised!") }
    var previousCompletedCount by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Int?>(null) }

    // One effect handles both the daily reminder and the goal-reached celebration.
    LaunchedEffect(metrics.todayRevisedCount, settings.dailyGoal) {
        val dailyGoal = settings.dailyGoal
        val todayRevisedCount = metrics.todayRevisedCount
        val todayKey = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            .format(java.util.Date())

        // Reminder — once per calendar day (survives midnight while the app is open)
        if (toastShownDayKey != todayKey && dailyGoal > 0 && todayRevisedCount < dailyGoal) {
            Toast.makeText(
                context,
                "Reminder: You have completed $todayRevisedCount/$dailyGoal of your daily revision sessions.",
                Toast.LENGTH_LONG
            ).show()
            toastShownDayKey = todayKey
        }

        // Goal reached — confetti + congrats dialog
        if (dailyGoal > 0) {
            val prev = previousCompletedCount
            if (prev != null && prev < dailyGoal && todayRevisedCount >= dailyGoal) {
                showConfetti = true
                showGoalDialog = true
            }
            previousCompletedCount = todayRevisedCount
        }
    }

    // Navigation and screen routing inside TodayRevisionScreen
    Box(modifier = Modifier.fillMaxSize()) {
        if (currentView == "list") {
            RevisionListScreen(
                viewModel = viewModel,
                navController = navController,
                initialFilter = selectedListFilter,
                onBack = { currentView = "dashboard" }
            )
        } else if (currentView == "add") {
            AddRevisionScreen(
                viewModel = viewModel,
                navController = navController,
                onBack = { currentView = "dashboard" }
            )
        } else {
            when {
        syncError != null -> {
            ErrorStateScreen(
                message = syncError ?: "Unable to sync with Firestore.",
                onRetry = {
                    viewModel.retrySync()
                }
            )
        }
        isInitiallyLoading -> {
            SkeletonLoadingScreen()
        }
        currentView == "overdue" -> {
            OverdueRevisionsView(
                overdueTopics = metrics.pendingRevisions,
                navController = navController,
                onBack = { currentView = "dashboard" }
            )
        }
        currentView == "upcoming" -> {
            UpcomingRevisionsView(
                upcomingTopics = metrics.upcomingRevisions,
                navController = navController,
                onBack = { currentView = "dashboard" }
            )
        }
        currentView == "today_revisions" -> {
            TodayRevisionsView(
                viewModel = viewModel,
                navController = navController,
                onBack = { currentView = "dashboard" },
                onTopicCompleted = { title ->
                    completedTopicTitle = title
                    showTopicExplosion = true
                }
            )
        }
        else -> {
            // Screen 1: Revision Management Dashboard — premium redesign
            val totalTopicsCount = remember(allTopics) { allTopics.size }
            val completedCount = remember(allTopics) { allTopics.count { it.status == "Completed" || it.status == "Complete" } }
            val pendingCount = remember(allTopics) { allTopics.count { it.status != "Completed" && it.status != "Complete" } }
            val streakCount = remember(userProfile) { userProfile?.currentStreak ?: 0 }
            val completionPercentage = remember(totalTopicsCount, completedCount) {
                if (totalTopicsCount > 0) {
                    (completedCount * 100) / totalTopicsCount
                } else {
                    0
                }
            }
            val completionFraction = (completionPercentage / 100f).coerceIn(0f, 1f)

            val todayList = metrics.todayDue
            val overdueList = metrics.pendingRevisions
            val upcomingList = metrics.upcomingRevisions
            val totalTimeMins = remember(todayList) { todayList.sumOf { it.estimatedRevisionTimeMins } }

            Scaffold(
                containerColor = RevBgLight,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "REVISION SPACE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = RevPrimary.copy(alpha = 0.72f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Revision Management",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RevTextDark
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                                    .testTag("drawer_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu Drawer",
                                    tint = RevPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.horizontalGradient(listOf(RevPrimary, Color(0xFF9B8CFF))))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$completionPercentage%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("header_completion_percentage")
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = RevBgLight
                        )
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .testTag("revision_scrollable_layout"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Section kicker — Overview
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(listOf(RevPrimary, Color(0xFF9B8CFF))))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "OVERVIEW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp,
                                    color = RevPrimary
                                )
                                Text(
                                    text = "Today's Progress",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RevTextDark
                                )
                            }
                        }
                    }

                    // Premium gradient progress hero
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF6C4DFF), Color(0xFF9B8CFF))))
                                    .padding(20.dp)
                            ) {
                                // Soft decorative orbs
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .offset(x = 150.dp, y = (-40).dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .offset(x = 230.dp, y = 70.dp)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "KEEP IT UP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(
                                            text = "You're making progress",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$completedCount of $totalTopicsCount topics completed",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        LinearProgressIndicator(
                                            progress = { completionFraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color.White,
                                            trackColor = Color.White.copy(alpha = 0.25f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Box(
                                        modifier = Modifier.size(84.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { completionFraction },
                                            modifier = Modifier.fillMaxSize(),
                                            color = Color.White,
                                            trackColor = Color.White.copy(alpha = 0.22f),
                                            strokeWidth = 7.dp
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$completionPercentage%",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.testTag("overview_completion")
                                            )
                                            Text(
                                                text = "DONE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = Color.White.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Compact stat strip — Total / Pending / Streak
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OverviewStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Total Topics",
                                    value = totalTopicsCount.toString(),
                                    icon = Icons.Default.MenuBook,
                                    accentColor = Color(0xFF6C4DFF),
                                    bgColor = Color(0xFFF1EEFF),
                                    tag = "overview_total_topics"
                                )
                                OverviewStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Pending",
                                    value = pendingCount.toString(),
                                    icon = Icons.Default.HourglassEmpty,
                                    accentColor = Color(0xFFF59E0B),
                                    bgColor = Color(0xFFFFFBEB),
                                    tag = "overview_pending"
                                )
                                OverviewStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Streak",
                                    value = "$streakCount Days",
                                    icon = Icons.Default.Whatshot,
                                    accentColor = Color(0xFF10B981),
                                    bgColor = Color(0xFFECFDF5),
                                    tag = "overview_streak"
                                )
                            }
                        }
                    }

                    // Today's Plan Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("todays_plan_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Brush.horizontalGradient(listOf(RevPrimary, Color(0xFF9B8CFF))))
                                )
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1EEFF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CalendarToday,
                                                contentDescription = null,
                                                tint = RevPrimary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "TODAY'S PLAN",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.2.sp,
                                                color = RevPrimary
                                            )
                                            Text(
                                                text = "Today's Plan",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RevTextDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFF8F9FC))
                                            .padding(vertical = 16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = todayList.size.toString(),
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RevPrimary
                                            )
                                            Text(
                                                text = "Due Today",
                                                fontSize = 12.sp,
                                                color = RevTextGray
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(40.dp)
                                                .background(Color(0xFFE7EAF3))
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = formatTotalMins(totalTimeMins.toLong()),
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RevPrimary
                                            )
                                            Text(
                                                text = "Total Time",
                                                fontSize = 12.sp,
                                                color = RevTextGray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            currentView = "today_revisions"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.horizontalGradient(listOf(RevPrimary, Color(0xFF8B5CF6))))
                                            .testTag("start_todays_revisions_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Start Today's Revisions",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section kicker — Revision Queue
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(listOf(RevPrimary, Color(0xFF9B8CFF))))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "STAY ON TRACK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp,
                                    color = RevPrimary
                                )
                                Text(
                                    text = "Revision Queue",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RevTextDark
                                )
                            }
                        }
                    }

                    // Grouped Queue Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                QueueRowItem(
                                    title = "Overdue",
                                    count = overdueList.size,
                                    icon = Icons.Default.Circle,
                                    iconColor = Color(0xFFEF4444),
                                    borderColor = Color(0xFFEF4444),
                                    bgColor = Color(0xFFFEF2F2),
                                    onClick = { currentView = "overdue" },
                                    tag = "queue_overdue"
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    color = Color(0xFFEFF1F6)
                                )
                                QueueRowItem(
                                    title = "Today",
                                    count = todayList.size,
                                    icon = Icons.Default.Assignment,
                                    iconColor = Color(0xFFF59E0B),
                                    borderColor = Color(0xFFF59E0B),
                                    bgColor = Color(0xFFFFFBEB),
                                    onClick = { currentView = "today_revisions" },
                                    tag = "queue_today"
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    color = Color(0xFFEFF1F6)
                                )
                                QueueRowItem(
                                    title = "Upcoming",
                                    count = upcomingList.size,
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = Color(0xFF6C4DFF),
                                    borderColor = Color(0xFF6C4DFF),
                                    bgColor = Color(0xFFF1EEFF),
                                    onClick = { currentView = "upcoming" },
                                    tag = "queue_upcoming"
                                )
                            }
                        }
                    }
                }
            }
        }
    } // closes when
    } // closes else of if/else if/else
    if (showConfetti) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("confetti_overlay_box")
        ) {
            ConfettiAnimation(onFinished = { showConfetti = false })
        }
    }

    if (showGoalDialog) {
        DailyGoalCongratsDialog(
            goalTarget = settings.dailyGoal,
            unitName = "revision sessions",
            streakDays = userProfile?.currentStreak ?: 1,
            onDismiss = { showGoalDialog = false },
            onReplayAnimation = { showConfetti = true }
        )
    }

    if (showTopicExplosion) {
        CheckmarkExplosionEffect(
            modifier = Modifier.fillMaxSize(),
            title = completedTopicTitle,
            onAnimationEnd = { showTopicExplosion = false }
        )
    }
} // closes Box
}

@Composable
fun OverviewStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    tag: String
) {
    Column(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag(tag),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = value,
            color = accentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = title,
            color = RevTextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QueueRowItem(
    title: String,
    count: Int,
    icon: ImageVector,
    iconColor: Color,
    borderColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    val subtitle = when (title) {
        "Overdue" -> "Needs your attention"
        "Today" -> "Scheduled for today"
        else -> "Coming up next"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(if (icon == Icons.Default.Circle) 12.dp else 20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = RevTextDark
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = RevTextGray
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(borderColor.copy(alpha = 0.10f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F3F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = RevTextGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Screen 2 – Overdue Revisions Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueRevisionsView(
    overdueTopics: List<TopicWithSubjectAndChapter>,
    navController: NavController,
    onBack: () -> Unit
) {
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var showFilterMenu by remember { mutableStateOf(false) }

    val availableSubjects = remember(overdueTopics) {
        listOf("All") + overdueTopics.map { it.subjectName }.distinct().sorted()
    }

    val filteredOverdueTopics = remember(overdueTopics, selectedSubjectFilter) {
        if (selectedSubjectFilter == "All") {
            overdueTopics
        } else {
            overdueTopics.filter { it.subjectName.equals(selectedSubjectFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = RevBgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Overdue Revisions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RevTextDark
                        )
                        Text(
                            text = "(${filteredOverdueTopics.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = RevTextDark
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.testTag("overdue_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = RevTextDark
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            availableSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject, fontWeight = if (selectedSubjectFilter == subject) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        selectedSubjectFilter = subject
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(1.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (overdueTopics.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF6C4DFF)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No overdue revisions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RevTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You're completely up to date.",
                            fontSize = 14.sp,
                            color = RevTextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("overdue_scrollable_layout"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Warning Banner
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("warning_banner"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "These topics are overdue.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Revise them to stay on track.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }

                    // List of Overdue Topic Cards
                    items(filteredOverdueTopics, key = { it.topicId }) { topic ->
                        OverdueTopicCard(
                            topic = topic,
                            onStartRevision = {
                                navController.navigate(
                                    com.example.ui.TopicDetailRoute(
                                        topicId = topic.topicId,
                                        subjectId = topic.subjectId,
                                        chapterId = topic.chapterId
                                    )
                                )
                            }
                        )
                    }

                    // Keep Going Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("keep_going_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CrisisAlert,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Keep Going!",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Clearing overdue topics today will help you stay ahead.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
@Composable
fun OverdueTopicCard(
    topic: TopicWithSubjectAndChapter,
    onStartRevision: () -> Unit
) {
    val dueText = remember(topic.nextRevisionDateMs) { getOverdueBadgeText(topic.nextRevisionDateMs) }
    val formattedLastDate = remember(topic.lastRevisionDateMs) {
        if (topic.lastRevisionDateMs > 0L) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(topic.lastRevisionDateMs))
        } else {
            "Never studied"
        }
    }

    val lastRevMins = remember(topic.lastRevisionDuration, topic.lastRevisionTime, topic.estimatedRevisionTimeMins) {
        if (topic.lastRevisionDuration > 0) {
            topic.lastRevisionDuration / 60000
        } else {
            val rawTime = topic.lastRevisionTime
            if (rawTime != null && rawTime.contains("min")) {
                val digits = rawTime.filter { it.isDigit() }.toLongOrNull()
                if (digits != null) digits else topic.estimatedRevisionTimeMins.toLong()
            } else {
                topic.estimatedRevisionTimeMins.toLong()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overdue_topic_card_${topic.topicId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = topic.topicName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RevTextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Column {
                        Text(
                            text = "Chapter",
                            fontSize = 11.sp,
                            color = RevTextGray
                        )
                        Text(
                            text = topic.chapterName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dueText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Info rows with thin horizontal dividers
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OverdueInfoRow(
                    icon = Icons.Outlined.CalendarToday,
                    label = "Last Revision Date",
                    value = formattedLastDate
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                OverdueInfoRow(
                    icon = Icons.Outlined.Timer,
                    label = "Last Revision Time",
                    value = "$lastRevMins min"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                OverdueInfoRow(
                    icon = Icons.Outlined.History,
                    label = "Total Revisions",
                    value = topic.revisionCount.toString()
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                OverdueInfoRow(
                    icon = Icons.Outlined.HourglassEmpty,
                    label = "Expected Time",
                    value = formatEstimatedTime(topic.estimatedRevisionTimeMins)
                )
            }

            // Start Revision Button
            Button(
                onClick = onStartRevision,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_revision_button_${topic.topicId}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = "Start Revision",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }


}
@Composable
fun OverdueInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RevTextGray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = RevTextGray
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = RevTextDark
        )
    }
}

private fun getOverdueBadgeText(nextRevisionMs: Long): String {
    if (nextRevisionMs <= 0L) return "Overdue"
    val now = System.currentTimeMillis()
    val diffMs = now - nextRevisionMs
    if (diffMs <= 0L) return "Overdue"
    val daysAgo = (diffMs / 86400000L).toInt() + 1
    return when {
        daysAgo <= 1 -> "Due Yesterday"
        else -> "Due $daysAgo Days Ago"
    }
}

@Composable
fun ErrorStateScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RevBgLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Firestore Sync Issue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RevTextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = RevTextGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C4DFF)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Retry Sync",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }


}
@Composable
fun SkeletonLoadingScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        userScrollEnabled = false
    ) {
        // Skeleton Stats Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShimmerSkeletonItem(modifier = Modifier.weight(1f).height(100.dp))
                    ShimmerSkeletonItem(modifier = Modifier.weight(1f).height(100.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShimmerSkeletonItem(modifier = Modifier.weight(1f).height(100.dp))
                    ShimmerSkeletonItem(modifier = Modifier.weight(1f).height(100.dp))
                }
            }
        }

        // Overdue Skeleton Row
        item {
            ShimmerSkeletonItem(modifier = Modifier.fillMaxWidth().height(24.dp))
        }
        items(2) {
            ShimmerSkeletonItem(modifier = Modifier.fillMaxWidth().height(80.dp))
        }

        // Today's Skeleton Row
        item {
            ShimmerSkeletonItem(modifier = Modifier.fillMaxWidth().height(24.dp))
        }
        item {
            ShimmerSkeletonItem(modifier = Modifier.fillMaxWidth().height(80.dp))
        }
    }
}

@Composable
fun ShimmerSkeletonItem(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color(0xFFE5E7EB),
        Color(0xFFF3F4F6),
        Color(0xFFE5E7EB)
    )

    val brush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingRevisionsView(
    upcomingTopics: List<TopicWithSubjectAndChapter>,
    navController: NavController,
    onBack: () -> Unit
) {
    var selectedSortOption by remember { mutableStateOf("Next Revision Date") }
    var selectedSortOrder by remember { mutableStateOf("Ascending") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    // Derive available subjects for filtering
    val availableSubjects = remember(upcomingTopics) {
        listOf("All") + upcomingTopics.map { it.subjectName }.distinct().sorted()
    }

    // Filter topics
    val filteredTopics = remember(upcomingTopics, selectedSubjectFilter) {
        if (selectedSubjectFilter == "All") {
            upcomingTopics
        } else {
            upcomingTopics.filter { it.subjectName.equals(selectedSubjectFilter, ignoreCase = true) }
        }
    }

    // Sort topics
    val sortedTopics = remember(filteredTopics, selectedSortOption, selectedSortOrder) {
        val comparator = when (selectedSortOption) {
            "Expected Time" -> compareBy<TopicWithSubjectAndChapter> { it.estimatedRevisionTimeMins }
            "Topic Name" -> compareBy { it.topicName.lowercase() }
            "Subject" -> compareBy { it.subjectName.lowercase() }
            "Chapter" -> compareBy { it.chapterName.lowercase() }
            else -> compareBy<TopicWithSubjectAndChapter> { it.nextRevisionDateMs }
        }
        val orderedComparator = if (selectedSortOrder == "Descending") {
            comparator.reversed()
        } else {
            if (selectedSortOption == "Next Revision Date") {
                compareBy<TopicWithSubjectAndChapter> { it.nextRevisionDateMs }.thenBy { it.topicName.lowercase() }
            } else {
                comparator
            }
        }
        filteredTopics.sortedWith(orderedComparator)
    }

    // Header values
    val totalTimeMins = remember(sortedTopics) {
        sortedTopics.sumOf { it.estimatedRevisionTimeMins.toLong() }
    }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Upcoming Revisions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "(${sortedTopics.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C4DFF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFilterBottomSheet = true },
                        modifier = Modifier.testTag("upcoming_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(2.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (sortedTopics.isEmpty()) {
                // Empty state view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF6C4DFF),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Upcoming Revisions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're fully scheduled for now.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    // 1. Blue Info Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE4DBFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE4DBFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF6C4DFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "These topics are scheduled for the coming days.",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = "Plan ahead and stay consistent!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E40AF).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Section: Upcoming Topics
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFF6C4DFF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Upcoming Topics",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${sortedTopics.size}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Vertical Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                // Right Section: Total Time
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF6C4DFF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Total Time",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatTotalMins(totalTimeMins),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 3. Active sorting label
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sorted by $selectedSortOption ${if (selectedSortOrder == "Ascending") "↑" else "↓"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 4. Topic Cards List
                    items(sortedTopics, key = { it.topicId }) { topic ->
                        val subjectColor = remember(topic.subjectColorHex) { parseHexColor(topic.subjectColorHex) }
                        val daysRemaining = remember(topic.nextRevisionDateMs) { getDaysRemaining(topic.nextRevisionDateMs) }
                        val badgeText = remember(daysRemaining, topic.nextRevisionDateMs) {
                            if (daysRemaining == 1) "Tomorrow" else SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(topic.nextRevisionDateMs))
                        }
                        val formattedNextDate = remember(topic.nextRevisionDateMs) { formatRevisionDate(topic.nextRevisionDateMs) }
                        val estimatedTimeStr = remember(topic.estimatedRevisionTimeMins) { formatEstimatedTime(topic.estimatedRevisionTimeMins) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        com.example.ui.TopicDetailRoute(
                                            topicId = topic.topicId,
                                            subjectId = topic.subjectId,
                                            chapterId = topic.chapterId
                                        )
                                    )
                                }
                                .testTag("upcoming_topic_card_${topic.topicId}"),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // First Row: Subject Icon + Topic Name & Chapter Name + Right Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SubjectIconCircle(
                                        subjectName = topic.subjectName,
                                        color = subjectColor
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = topic.topicName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Chapter: ",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = topic.chapterName,
                                                fontSize = 13.sp,
                                                color = subjectColor,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Right Badge: Tomorrow or date string
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (daysRemaining == 1) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                1.dp,
                                                if (daysRemaining == 1) Color(0xFFE4DBFF)
                                                else MaterialTheme.colorScheme.outlineVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontSize = 11.sp,
                                            color = if (daysRemaining == 1) Color(0xFF6C4DFF) else Color(0xFF475569),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Next Revision Date Info Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF8A94A6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Next Revision Date",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = formattedNextDate,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Expected Time Info Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFF8A94A6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Expected Time",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = estimatedTimeStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Countdown element at bottom right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val countText = if (daysRemaining == 1) "1 Day Left" else "$daysRemaining Days Left"
                                    Text(
                                        text = countText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6C4DFF)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF6C4DFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Filters & Sorting
    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
            ) {
                Text(
                    text = "Filter & Sort Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. Sort Options
                Text(
                    text = "Sort By",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val sortOptions = listOf("Next Revision Date", "Expected Time", "Topic Name", "Subject", "Chapter")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.take(3).forEach { option ->
                        Box(modifier = Modifier.weight(1f)) {
                            CustomFilterChip(
                                selected = selectedSortOption == option,
                                onClick = { selectedSortOption = option },
                                label = option
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.drop(3).forEach { option ->
                        Box(modifier = Modifier.weight(1f)) {
                            CustomFilterChip(
                                selected = selectedSortOption == option,
                                onClick = { selectedSortOption = option },
                                label = option
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 2. Sort Order
                Text(
                    text = "Sort Order",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Ascending", "Descending").forEach { order ->
                        Box(modifier = Modifier.weight(1f)) {
                            CustomFilterChip(
                                selected = selectedSortOrder == order,
                                onClick = { selectedSortOrder = order },
                                label = if (order == "Ascending") "Ascending (↑)" else "Descending (↓)"
                            )
                        }
                    }
                }

                // 3. Filter by Subject
                Text(
                    text = "Filter by Subject",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Horizontal scrolling container of chips
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableSubjects) { subject ->
                        CustomFilterChip(
                            selected = selectedSubjectFilter == subject,
                            onClick = { selectedSubjectFilter = subject },
                            label = subject
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Apply button
                Button(
                    onClick = { showFilterBottomSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C4DFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Apply Filters",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectIconCircle(subjectName: String, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        val initials = when {
            subjectName.lowercase().contains("geography") -> "Ge"
            subjectName.lowercase().contains("science") -> "Sc"
            subjectName.lowercase().contains("economy") -> "₹"
            subjectName.lowercase().contains("polity") -> "Po"
            subjectName.lowercase().contains("english") -> "Aa"
            else -> {
                if (subjectName.length >= 2) {
                    subjectName.take(2).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                } else {
                    subjectName.take(1).uppercase(Locale.getDefault())
                }
            }
        }
        Text(
            text = initials,
            color = color,
            fontSize = if (initials == "Aa") 15.sp else 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CustomFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200)
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF6C4DFF) else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 200)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF6C4DFF) else Color(0xFF475569),
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .bounceClick { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

fun getDaysRemaining(nextRevisionMs: Long): Int {
    if (nextRevisionMs <= 0L) return 0
    val now = System.currentTimeMillis()
    val diffMs = nextRevisionMs - now
    if (diffMs <= 0L) return 0
    val days = (diffMs / 86400000L).toInt()
    return if (days < 0) 0 else days
}

fun formatRevisionDate(ms: Long): String {
    if (ms <= 0L) return "Not scheduled"
    val sdf = SimpleDateFormat("dd MMM yyyy (EEE)", Locale.getDefault())
    return sdf.format(Date(ms))
}

fun formatTotalMins(totalMins: Long): String {
    if (totalMins == 0L) return "0 min"
    if (totalMins < 60) return "$totalMins min"
    val h = totalMins / 60
    val m = totalMins % 60
    return if (m == 0L) "${h}h" else "${h}h ${m}m"
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFF6C4DFF)): Color {
    return try {
        if (hex.isNotEmpty()) {
            Color(android.graphics.Color.parseColor(hex))
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayRevisionsView(
    viewModel: RevisionViewModel,
    navController: NavController,
    onBack: () -> Unit,
    onTopicCompleted: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showTopicExplosion by remember { mutableStateOf(false) }
    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised!") }
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val todayList = metrics.todayDue

    Scaffold(
        containerColor = RevBgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Today's Revisions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RevTextDark
                        )
                        if (todayList.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer, // Blue-50
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "(${todayList.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C4DFF), // Blue-700
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("today_revisions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = RevTextDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Filtering Today's Revisions", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("today_revisions_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = RevTextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (todayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(RevBgLight),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)), // light emerald/green
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF10B981) // Emerald-500
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No revisions due today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = RevTextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        fontSize = 14.sp,
                        color = RevTextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(RevBgLight),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 3. Information Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light warning orange
                        border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("info_banner_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "These topics are due today. Complete them to keep your streak going!",
                                fontSize = 14.sp,
                                color = Color(0xFF9A3412),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 4. Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("today_summary_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Topics Due Today",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RevTextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${todayList.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RevTextDark
                                )
                            }
                            
                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Color(0xFFE5E7EB))
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Total Estimated Time",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RevTextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val totalMins = todayList.sumOf { it.estimatedRevisionTimeMins }
                                Text(
                                    text = formatTotalMins(totalMins.toLong()),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981) // Green-500
                                )
                            }
                        }
                    }
                }

                // 5. Topic Cards
                items(todayList, key = { it.topicId }) { topic ->
                    val subjectColor = remember(topic.subjectColorHex) { parseHexColor(topic.subjectColorHex) }
                    val lastDateStr = remember(topic.lastRevisionDate, topic.lastRevisionDateMs) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        if ((topic.lastRevisionDate ?: 0L) > 0L) {
                            dateFormat.format(Date(topic.lastRevisionDate ?: 0L))
                        } else if (topic.lastRevisionDateMs > 0) {
                            dateFormat.format(Date(topic.lastRevisionDateMs))
                        } else {
                            "Never"
                        }
                    }
                    val lastTimeStr = remember(topic.lastRevisionTime) {
                        if (!topic.lastRevisionTime.isNullOrBlank()) {
                            val timeVal = topic.lastRevisionTime ?: ""
                            if (timeVal.contains("min") || timeVal.contains("hr")) {
                                " ($timeVal)"
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }
                    }
                    val totalRevsCount = remember(topic.totalRevisions, topic.revisionCount) {
                        if (topic.totalRevisions > 0) {
                            topic.totalRevisions
                        } else if (topic.revisionCount > 0) {
                            topic.revisionCount
                        } else {
                            0
                        }
                    }
                    val estimatedTimeStr = remember(topic.estimatedRevisionTimeMins) { formatEstimatedTime(topic.estimatedRevisionTimeMins) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_card_${topic.topicId}")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Header: Subject, Chapter, and Due Today badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(subjectColor)
                                        )
                                        Text(
                                            text = topic.subjectName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = subjectColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = topic.chapterName,
                                        fontSize = 12.sp,
                                        color = RevTextGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Due Today badge
                                Surface(
                                    color = Color(0xFFFEF2F2), // Red-50
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)) // Red-300
                                ) {
                                    Text(
                                        text = "Due Today",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626), // Red-600
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Topic Name
                            Text(
                                text = topic.topicName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RevTextDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            HorizontalDivider(color = Color(0xFFF3F4F6))

                            Spacer(modifier = Modifier.height(12.dp))

                            // Revision Metadata (Last Revision Date, Last Revision Time, Total Revisions, Expected Time)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Last Revision Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = RevTextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Last Revision: $lastDateStr$lastTimeStr",
                                        fontSize = 12.sp,
                                        color = RevTextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Total Revisions Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = RevTextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Total Revisions: $totalRevsCount",
                                        fontSize = 12.sp,
                                        color = RevTextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Expected Time Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = RevTextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Expected Time: $estimatedTimeStr",
                                        fontSize = 12.sp,
                                        color = RevTextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Start Revision button & Quick complete button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        navController.navigate(com.example.ui.TimerRoute(topic.topicId, true))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("start_revision_button_${topic.topicId}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RevPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start Revision",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                var isCompleting by remember { mutableStateOf(false) }
                                FilledIconButton(
                                    onClick = {
                                        isCompleting = true
                                        onTopicCompleted("${topic.topicName.take(24)} Marked as Revised!")
                                        viewModel.recordTimerSession(
                                            topic.topicId,
                                            isRevision = true,
                                            durationMs = topic.estimatedRevisionTimeMins * 60 * 1000L
                                        )
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("quick_complete_button_${topic.topicId}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFFD1FAE5), // Light green-100
                                        contentColor = Color(0xFF065F46) // Emerald-800
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Quick Complete",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTopicExplosion) {
        CheckmarkExplosionEffect(
            modifier = Modifier.fillMaxSize(),
            title = completedTopicTitle,
            onAnimationEnd = { showTopicExplosion = false }
        )
    }
}


