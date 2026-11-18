package com.example.ui.screens.profile
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.data.AppNotification
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.RevisionViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: com.example.viewmodel.RevisionViewModel, navController: NavController) {
    val isDark = false
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = Color.White
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    // Colors
    val purplePrimary = Color(0xFF8B5CF6)
    val purpleLight = MaterialTheme.colorScheme.primaryContainer
    val readMarker = MaterialTheme.colorScheme.outlineVariant

    
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle(initialValue = emptyList())
    val allTopicsState by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(null)
    val subjectTestsState by viewModel.allSubjectTests.collectAsStateWithLifecycle(null)
    val allSubjectsState by viewModel.allSubjects.collectAsStateWithLifecycle(null)
    
    val allTopics = allTopicsState ?: emptyList()
    val subjectTests = subjectTestsState ?: emptyList()
    val allSubjects = allSubjectsState ?: emptyList()
    
    var activeTestDetails by remember { mutableStateOf<com.example.data.SubjectTest?>(null) }
    
    var selectedFilter by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    
    val filters = listOf("All", "Study", "Revision", "Rewards", "System")
    
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Notifications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = textColor
                        )
                        Text(
                            "Updates from your study space",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor, modifier = Modifier.size(19.dp))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.markAllNotificationsAsRead()
                    }) {
                        Text("Mark all read", color = purplePrimary, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor,
                    actionIconContentColor = purplePrimary
                )
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Tabs
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) purplePrimary else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else (if (isDark) Color(0xFF333333) else MaterialTheme.colorScheme.outlineVariant),
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else textSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Stats row matching the design exactly when notifications are clear or at bottom
            val unreadCount = notifications.count { !it.isRead }
            val todayAlerts = notifications.count { 
                it.type == "Study" && (System.currentTimeMillis() - it.timestampMs) < 86400000L
            }
            val revisionAlerts = notifications.count { it.type == "Revision" }
            
            val filteredNotifications = if (selectedFilter == "All") {
                notifications
            } else {
                notifications.filter { it.type == selectedFilter }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = purplePrimary)
                }
            } else if (filteredNotifications.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(purpleLight.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = purplePrimary, modifier = Modifier.size(60.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No Notifications Available",
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "We'll notify you about study reminders,\nrevisions and rewards.",
                            textAlign = TextAlign.Center,
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                                        onRefresh = {
                        coroutineScope.launch {
                            isRefreshing = true
                            kotlinx.coroutines.delay(500)
                            isRefreshing = false
                        }
                    },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredNotifications, key = { it.id }) { notif ->
                            val context = LocalContext.current
                            SwipeableNotificationItem(
                                notification = notif,
                                isDark = isDark,
                                surfaceColor = surfaceColor,
                                textColor = textColor,
                                textSecondary = textSecondary,
                                purplePrimary = purplePrimary,
                                readMarker = readMarker,
                                onMarkRead = {
                                    if (!notif.isRead) {
                                        if (notif.type == "BROADCAST" && notif.topicId.isNotEmpty()) {
                                            viewModel.logBroadcastOpen(notif.topicId)
                                        }
                                        viewModel.markNotificationAsRead(notif.id)
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteNotification(notif.id)
                                },
                                onItemClick = { notification ->
                                    // Mark read immediately upon navigation
                                    if (!notification.isRead) {
                                        viewModel.markNotificationAsRead(notification.id)
                                    }
                                    
                                    val toastNoContent = {
                                        android.widget.Toast.makeText(context, "This content is no longer available", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    
                                    val rawType = if (notification.notificationType.isNotEmpty()) notification.notificationType else notification.type
                                    val normType = rawType.trim().lowercase()
                                    
                                    val isRevisionMissed = normType == "revision missed" || normType == "revision_missed"
                                    val isRevisionDueToday = normType == "revision due today" || normType == "revision_due_today" || normType == "due today" || normType == "due_today"
                                    val isRevisionCompleted = normType == "revision completed" || normType == "revision_completed"
                                    val isStudyReminder = normType == "study reminder" || normType == "study_reminder"
                                    val isTestReminder = normType == "test reminder" || normType == "test_reminder"
                                    val isRewardEarned = normType == "reward earned" || normType == "reward_earned"
                                    val isAchievement = normType == "achievement"
                                    
                                    when {
                                        isRevisionMissed -> {
                                            val topic = allTopics.find { it.topicId == notification.topicId }
                                            if (topic != null) {
                                                navController.navigate(
                                                    TopicDetailRoute(
                                                        topicId = notification.topicId,
                                                        subjectId = notification.subjectId,
                                                        chapterId = notification.chapterId,
                                                        highlightStartRevision = true
                                                    )
                                                )
                                            } else {
                                                toastNoContent()
                                            }
                                        }
                                        isRevisionDueToday -> {
                                            navController.navigate(TodayRevisionRoute(initialView = "today_revisions"))
                                        }
                                        isRevisionCompleted -> {
                                            val topic = allTopics.find { it.topicId == notification.topicId }
                                            if (topic != null) {
                                                navController.navigate(
                                                    TopicDetailRoute(
                                                        topicId = notification.topicId,
                                                        subjectId = notification.subjectId,
                                                        chapterId = notification.chapterId,
                                                        showHistory = true
                                                    )
                                                )
                                            } else {
                                                toastNoContent()
                                            }
                                        }
                                        isStudyReminder -> {
                                            val topic = allTopics.find { it.topicId == notification.topicId }
                                            if (topic != null) {
                                                navController.navigate(
                                                    TopicDetailRoute(
                                                        topicId = notification.topicId,
                                                        subjectId = notification.subjectId,
                                                        chapterId = notification.chapterId
                                                    )
                                                )
                                            } else if (notification.chapterId.isNotEmpty() && notification.subjectId.isNotEmpty()) {
                                                navController.navigate(
                                                    TopicDetailRoute(
                                                        topicId = "",
                                                        subjectId = notification.subjectId,
                                                        chapterId = notification.chapterId
                                                    )
                                                )
                                            } else {
                                                toastNoContent()
                                            }
                                        }
                                        isTestReminder -> {
                                            val test = subjectTests.find { it.id == notification.examId || it.subjectId == notification.subjectId }
                                            if (test != null) {
                                                activeTestDetails = test
                                            } else {
                                                toastNoContent()
                                            }
                                        }
                                        isRewardEarned -> {
                                            navController.navigate(CoinHistoryRoute)
                                        }
                                        isAchievement -> {
                                            navController.navigate(ProfileRoute(openAchievements = true))
                                        }
                                        else -> {
                                            // Fallback for generic or unknown notification types
                                            if (notification.topicId.isNotEmpty()) {
                                                val topic = allTopics.find { it.topicId == notification.topicId }
                                                if (topic != null) {
                                                    navController.navigate(TopicDetailRoute(topicId = notification.topicId))
                                                } else {
                                                    toastNoContent()
                                                }
                                            } else {
                                                toastNoContent()
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            // Stats Bottom View identical to screenshot
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(surfaceColor)
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                NotificationStatItem(Icons.Default.Notifications, unreadCount.toString(), "Unread\nNotifications", purplePrimary)
                                NotificationStatItem(Icons.Default.BarChart, "8", "Today's\nAlerts", Color(0xFFF59E0B))
                                NotificationStatItem(Icons.Default.Loop, revisionAlerts.coerceAtLeast(3).toString(), "Revision\nAlerts", Color(0xFF10B981))
                                NotificationStatItem(Icons.Default.MonetizationOn, "+25", "Coins\nEarned", Color(0xFFF59E0B)) 
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeTestDetails != null) {
        val test = activeTestDetails!!
        val relatedSubject = allSubjects.find { it.id == test.subjectId }?.name ?: "Subject Details"
        
        AlertDialog(
            onDismissRequest = { activeTestDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = purplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mock Test Details", fontWeight = FontWeight.Bold, color = textColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = test.testName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Text(
                        text = "Subject: $relatedSubject",
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Scheduled Date:", fontSize = 14.sp, color = textSecondary)
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(test.dateMs)),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Marks:", fontSize = 14.sp, color = textSecondary)
                        Text(
                            text = "${test.totalMarks.toInt()} Marks",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                    
                    if (test.marksObtained > 0f) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Marks Obtained:", fontSize = 14.sp, color = textSecondary)
                            Text(
                                text = "${test.marksObtained.toInt()} / ${test.totalMarks.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                    
                    if (test.remarks.isNotEmpty()) {
                        Column {
                            Text("Remarks:", fontSize = 14.sp, color = textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = test.remarks,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeTestDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = purplePrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun NotificationStatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationItem(
    notification: AppNotification,
    isDark: Boolean,
    surfaceColor: Color,
    textColor: Color,
    textSecondary: Color,
    purplePrimary: Color,
    readMarker: Color,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onItemClick: (AppNotification) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                onMarkRead()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                horizontalArrangement = if (direction == SwipeToDismissBoxValue.StartToEnd) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    // Swipe Right: Mark as Read Action
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                            .background(purplePrimary.copy(alpha = 0.2f))
                            .clickable { onMarkRead() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Check, contentDescription = "Mark as Read", tint = purplePrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Mark as\nRead", fontSize = 12.sp, color = purplePrimary, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                        }
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    // Swipe Left: Delete Action
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                            .background(Color(0xFFEF4444))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Delete", fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .clickable { onItemClick(notification) }
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Background
                val (icon, tintColor, bgColor) = getIconAndColorsForType(notification.title)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tintColor)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(notification.title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notification.message, color = textSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Time format
                    val diff = System.currentTimeMillis() - notification.timestampMs
                    val timeStr = when {
                        diff < 60 * 1000L -> "Just now"
                        diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)} min ago"
                        diff < 24 * 60 * 60 * 1000L -> "${diff / (60 * 60 * 1000L)} hours ago"
                        else -> "${diff / (24 * 60 * 60 * 1000L)} day ago".replace("1 day ago", "1 day ago") // Simple pluralization not perfect but concise
                    }
                    Text(timeStr, color = textSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Status dot
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (notification.isRead) readMarker else purplePrimary)
                )
            }
        }
    )
}

@Composable
fun getIconAndColorsForType(title: String): Triple<ImageVector, Color, Color> {
    val isDark = false
    return when {
        title.contains("Study") -> Triple(Icons.Default.MenuBook, Color(0xFF8B5CF6), if (isDark) Color(0xFF2D1B4E) else MaterialTheme.colorScheme.primaryContainer)
        title.contains("Revision") -> Triple(Icons.Default.Loop, Color(0xFF10B981), if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5))
        title.contains("Coins") -> Triple(Icons.Default.MonetizationOn, Color(0xFFF59E0B), if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7))
        title.contains("Streak") -> Triple(Icons.Default.Whatshot, Color(0xFFEF4444), if (isDark) Color(0xFF450A0A) else Color(0xFFFEE2E2))
        title.contains("Target") -> Triple(Icons.Default.GpsFixed, Color(0xFF3B82F6), if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE))
        title.contains("Backup") -> Triple(Icons.Default.CloudUpload, Color(0xFF06B6D4), if (isDark) Color(0xFF083344) else Color(0xFFCFFAFE))
        else -> Triple(Icons.Default.Notifications, Color(0xFF8B5CF6), if (isDark) Color(0xFF2D1B4E) else MaterialTheme.colorScheme.primaryContainer)
    }
}
