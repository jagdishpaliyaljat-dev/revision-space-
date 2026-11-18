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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Revision
import com.example.data.QuickTask
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.alpha

val TodoPurple = Color(0xFF6C4DFF)
val TodoBgLight = Color(0xFFF6F7FB)
val TodoTextDark = Color(0xFF0F172A)
val TodoTextGray = Color(0xFF5B6478)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: RevisionViewModel, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val completingTaskIds = remember { mutableStateListOf<String>() }
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val allRevisions = metrics.allRevisions
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    
    val quickTasks by viewModel.allQuickTasks.collectAsStateWithLifecycle(emptyList())
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(Date()) }

    val pendingQuickTasks = quickTasks.filter { 
        !it.isCompleted && (it.dateCreatedStr == todayStr || it.isCarryForward) 
    }
    val completedQuickTasksToday = quickTasks.filter { 
        it.isCompleted && it.completedDateStr == todayStr 
    }

    var showAddQuickTaskDialog by remember { mutableStateOf(false) }
    var editingQuickTask by remember { mutableStateOf<QuickTask?>(null) }

    var taskTitle by remember { mutableStateOf("") }
    var taskSubject by remember { mutableStateOf("") }
    var isOneTime by remember { mutableStateOf(true) }
    var isCarryForward by remember { mutableStateOf(false) }

    LaunchedEffect(editingQuickTask) {
        if (editingQuickTask != null) {
            taskTitle = editingQuickTask!!.title
            taskSubject = editingQuickTask!!.subject
            isOneTime = editingQuickTask!!.isOneTime
            isCarryForward = editingQuickTask!!.isCarryForward
        } else {
            taskTitle = ""
            taskSubject = ""
            isOneTime = true
            isCarryForward = false
        }
    }

    val isRevisionsLoading by viewModel.isRevisionsLoading.collectAsStateWithLifecycle(true)
    val isLoading = isRevisionsLoading

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val endOfToday = cal.timeInMillis

    val completedTodayRevisions = allRevisions.filter {
        it.lastRevisionDateMs in startOfToday..endOfToday || it.status == "Completed"
    }.distinctBy { it.topicId.ifEmpty { it.id } }
    val pendingOrOverdueTodayRevisions = allRevisions.filter {
        it.status == "Today" || it.status == "Overdue"
    }.distinctBy { it.topicId.ifEmpty { it.id } }

    val completedTodayCount = completedTodayRevisions.size + completedQuickTasksToday.size
    val pendingTodayCount = pendingOrOverdueTodayRevisions.size + pendingQuickTasks.size
    val totalTodayCount = completedTodayCount + pendingTodayCount
    val progress = if (totalTodayCount > 0) completedTodayCount.toFloat() / totalTodayCount else 0f

    var selectedRevisionForReschedule by remember { mutableStateOf<Revision?>(null) }
    var showSuccessEffect by remember { mutableStateOf(false) }
    var overdueRevisionToComplete by remember { mutableStateOf<Revision?>(null) }
    var showImpactAnalysisDialog by remember { mutableStateOf<Revision?>(null) }

    val isGoalReached = (totalTodayCount > 0 || completedTodayCount > 0) && pendingTodayCount == 0
    var showGoalReachedDialog by remember { mutableStateOf(false) }
    var previousGoalReached by remember { mutableStateOf(isGoalReached) }

    LaunchedEffect(isGoalReached) {
        if (isGoalReached && !previousGoalReached) {
            showGoalReachedDialog = true
        }
        previousGoalReached = isGoalReached
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = TodoBgLight,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Today's Tasks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.4).sp,
                                color = TodoTextDark
                            )
                            Text(
                                "One small win at a time",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TodoTextGray
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        Surface(
                            onClick = { showAddQuickTaskDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = TodoPurple,
                            contentColor = Color.White,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Quick Task", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Quick Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Header Progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE7EAF3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Daily Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TodoTextDark)
                                Text("$completedTodayCount of $totalTodayCount completed", fontSize = 13.sp, color = TodoTextGray)
                            }
                            Box(contentAlignment = Alignment.Center) {
                                AnimatedCircularProgress(
                                    progress = progress,
                                    modifier = Modifier.size(48.dp),
                                    color = TodoPurple,
                                    trackColor = TodoBgLight,
                                    strokeWidth = 6.dp
                                )
                                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TodoPurple)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shimmerBackground()
                            )
                        }
                    }
                } else {
                    if (pendingOrOverdueTodayRevisions.isNotEmpty() || completedTodayRevisions.isNotEmpty() || pendingQuickTasks.isNotEmpty() || completedQuickTasksToday.isNotEmpty()) {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val sortedPending = pendingOrOverdueTodayRevisions.sortedBy { it.nextRevisionDateMs }
                            val sortedCompleted = completedTodayRevisions.sortedByDescending { it.lastRevisionDateMs }



                            if (pendingQuickTasks.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Quick Tasks (${pendingQuickTasks.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TodoTextGray,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }
                                items(pendingQuickTasks, key = { "quick_${it.id}" }) { task ->
                                    val isCompleting = completingTaskIds.contains(task.id)
                                    AnimatedVisibility(
                                        visible = !isCompleting,
                                        exit = fadeOut(animationSpec = tween(350)) + shrinkVertically(animationSpec = tween(350)),
                                        enter = fadeIn(animationSpec = tween(350)) + expandVertically(animationSpec = tween(350))
                                    ) {
                                        QuickTaskCard(
                                            task = task,
                                            onComplete = {
                                                if (!completingTaskIds.contains(task.id)) {
                                                    completingTaskIds.add(task.id)
                                                    scope.launch {
                                                        AudioFeedback.playCompletionSound(context)
                                                        delay(350)
                                                        val updated = task.copy(
                                                            isCompleted = true,
                                                            completedDateStr = todayStr
                                                        )
                                                        val success = viewModel.updateQuickTaskSuspend(updated)
                                                        completingTaskIds.remove(task.id)
                                                        if (success) {
                                                            showSuccessEffect = true
                                                            Toast.makeText(context, "Task completed! 🎉", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            onEdit = {
                                                editingQuickTask = task
                                                showAddQuickTaskDialog = true
                                            },
                                            onDelete = {
                                                completingTaskIds.add(task.id)
                                                scope.launch {
                                                    delay(350)
                                                    viewModel.deleteQuickTask(task.id)
                                                    completingTaskIds.remove(task.id)
                                                    Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            if (sortedPending.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Revision Sessions Due (${sortedPending.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TodoTextGray,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }
                                items(sortedPending, key = { "rev_${it.id}" }) { rev ->
                                    val isOverdue = rev.nextRevisionDateMs < startOfToday
                                    val isCompleting = completingTaskIds.contains(rev.id)
                                    AnimatedVisibility(
                                        visible = !isCompleting,
                                        exit = fadeOut(animationSpec = tween(350)) + shrinkVertically(animationSpec = tween(350)),
                                        enter = fadeIn(animationSpec = tween(350)) + expandVertically(animationSpec = tween(350))
                                    ) {
                                        TodayTaskCard(
                                            revision = rev,
                                            isOverdue = isOverdue,
                                            isCompleting = isCompleting,
                                            onComplete = {
                                                if (isOverdue) {
                                                    overdueRevisionToComplete = rev
                                                } else {
                                                    completingTaskIds.add(rev.id)
                                                    scope.launch {
                                                        AudioFeedback.playCompletionSound(context)
                                                        delay(350)
                                                        showSuccessEffect = true
                                                        viewModel.markRevisionComplete(rev.id)
                                                        completingTaskIds.remove(rev.id)
                                                        Toast.makeText(context, "Revision saved! 🎉", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            onDelete = {
                                                completingTaskIds.add(rev.id)
                                                scope.launch {
                                                    delay(350)
                                                    viewModel.deleteRevision(context, rev.id)
                                                    completingTaskIds.remove(rev.id)
                                                    Toast.makeText(context, "Revision deleted", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onReschedule = {
                                                selectedRevisionForReschedule = rev
                                            }
                                        )
                                    }
                                }
                            }

                            if (sortedCompleted.isNotEmpty() || completedQuickTasksToday.isNotEmpty()) {
                                item {
                                    val totalCompletedCount = sortedCompleted.size + completedQuickTasksToday.size
                                    Text(
                                        text = "Completed Today ($totalCompletedCount)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TodoTextGray,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                    )
                                }
                                if (sortedPending.isEmpty() && pendingQuickTasks.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(
                                                        "All tasks completed! 🎉",
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1B5E20),
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        "Excellent job! You are fully caught up for today.",
                                                        color = Color(0xFF2E7D32),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                items(sortedCompleted, key = { "rev_${it.id}" }) { rev ->
                                    val isCompleting = completingTaskIds.contains(rev.id)
                                    AnimatedVisibility(
                                        visible = !isCompleting,
                                        exit = fadeOut(animationSpec = tween(350)) + shrinkVertically(animationSpec = tween(350)),
                                        enter = fadeIn(animationSpec = tween(350)) + expandVertically(animationSpec = tween(350))
                                    ) {
                                        TodayTaskCard(
                                            revision = rev,
                                            isOverdue = false,
                                            onComplete = {},
                                            onDelete = {
                                                completingTaskIds.add(rev.id)
                                                scope.launch {
                                                    delay(350)
                                                    viewModel.deleteRevision(context, rev.id)
                                                    completingTaskIds.remove(rev.id)
                                                    Toast.makeText(context, "Revision deleted", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onReschedule = {}
                                        )
                                    }
                                }
                                items(completedQuickTasksToday, key = { "quick_comp_${it.id}" }) { task ->
                                    val isCompleting = completingTaskIds.contains(task.id)
                                    AnimatedVisibility(
                                        visible = !isCompleting,
                                        exit = fadeOut(animationSpec = tween(350)) + shrinkVertically(animationSpec = tween(350)),
                                        enter = fadeIn(animationSpec = tween(350)) + expandVertically(animationSpec = tween(350))
                                    ) {
                                        QuickTaskCard(
                                            task = task,
                                            onComplete = {
                                                if (!completingTaskIds.contains(task.id)) {
                                                    completingTaskIds.add(task.id)
                                                    scope.launch {
                                                        AudioFeedback.playCompletionSound(context)
                                                        delay(350)
                                                        val updated = task.copy(
                                                            isCompleted = false,
                                                            completedDateStr = ""
                                                        )
                                                        val success = viewModel.updateQuickTaskSuspend(updated)
                                                        completingTaskIds.remove(task.id)
                                                        if (success) {
                                                            Toast.makeText(context, "Task uncompleted", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            onEdit = {
                                                editingQuickTask = task
                                                showAddQuickTaskDialog = true
                                            },
                                            onDelete = {
                                                completingTaskIds.add(task.id)
                                                scope.launch {
                                                    delay(350)
                                                    viewModel.deleteQuickTask(task.id)
                                                    completingTaskIds.remove(task.id)
                                                    Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = TodoTextGray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(84.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No tasks for today",
                                    fontWeight = FontWeight.Bold,
                                    color = TodoTextDark,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Enjoy your day!",
                                    color = TodoTextGray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        selectedRevisionForReschedule?.let { rev ->
            AlertDialog(
                onDismissRequest = { selectedRevisionForReschedule = null },
                title = { Text("Reschedule Task", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Do you want to reschedule '${rev.notes.ifBlank { rev.topicName }}' to tomorrow?",
                            color = TodoTextDark
                        )
                        Text(
                            "This will adjust its next due date and maintain your spaced repetition flow.",
                            fontSize = 12.sp,
                            color = TodoTextGray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newTime = startOfToday + 86400000L + 36000000L
                            viewModel.updateRevision(context, rev.copy(nextRevisionDateMs = newTime))
                            selectedRevisionForReschedule = null
                            Toast.makeText(context, "Rescheduled to tomorrow", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TodoPurple)
                    ) {
                        Text("Reschedule")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRevisionForReschedule = null }) {
                        Text("Cancel", color = TodoTextGray)
                    }
                }
            )
        }

        overdueRevisionToComplete?.let { rev ->
            AlertDialog(
                onDismissRequest = { overdueRevisionToComplete = null },
                title = { Text("Complete Overdue Session?", fontWeight = FontWeight.Bold) },
                text = { Text("This session is overdue. Would you like to mark it complete or run impact analysis?") },
                confirmButton = {
                    Button(
                        onClick = {
                            overdueRevisionToComplete = null
                            showImpactAnalysisDialog = rev
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                    ) {
                        Text("Analyze Impact")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val targetId = rev.id
                            overdueRevisionToComplete = null
                            completingTaskIds.add(targetId)
                            scope.launch {
                                AudioFeedback.playCompletionSound(context)
                                delay(350)
                                showSuccessEffect = true
                                viewModel.markRevisionComplete(targetId)
                                completingTaskIds.remove(targetId)
                                Toast.makeText(context, "Revision saved! 🎉", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Save Revision")
                    }
                }
            )
        }

        showImpactAnalysisDialog?.let { rev ->
            AlertDialog(
                onDismissRequest = { showImpactAnalysisDialog = null },
                title = { Text("Cumulative Revision Impact", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = { Text("Because this session is completed late, your learning system will dynamically calibrate to maintain high retention.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetId = rev.id
                            showImpactAnalysisDialog = null
                            completingTaskIds.add(targetId)
                            scope.launch {
                                AudioFeedback.playCompletionSound(context)
                                delay(350)
                                showSuccessEffect = true
                                viewModel.markRevisionComplete(targetId)
                                completingTaskIds.remove(targetId)
                                Toast.makeText(context, "Revision completed with adjusted impact! 🎉", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Confirm & Complete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImpactAnalysisDialog = null }) {
                        Text("Cancel", color = TodoTextGray)
                    }
                }
            )
        }

        if (showGoalReachedDialog) {
            DailyGoalCongratsDialog(
                goalTarget = totalTodayCount,
                unitName = "tasks",
                streakDays = userProfile?.currentStreak ?: 1,
                onDismiss = { showGoalReachedDialog = false }
            )
        }

        if (showAddQuickTaskDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddQuickTaskDialog = false
                    editingQuickTask = null
                },
                title = { 
                    Text(
                        text = if (editingQuickTask == null) "Add Quick Task" else "Edit Quick Task",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TodoTextDark
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task Title (Mandatory)") },
                            placeholder = { Text("Enter task description...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = taskSubject,
                            onValueChange = { taskSubject = it },
                            label = { Text("Subject (Optional)") },
                            placeholder = { Text("e.g. Physics, Chemistry") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Task Type", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TodoTextDark)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isOneTime = true
                                    isCarryForward = false
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isOneTime,
                                onClick = {
                                    isOneTime = true
                                    isCarryForward = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TodoPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("केवल आज के लिए (One-Time Task)", fontSize = 14.sp, color = TodoTextDark)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isOneTime = false
                                    isCarryForward = true
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isCarryForward,
                                onClick = {
                                    isOneTime = false
                                    isCarryForward = true
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TodoPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("यदि आज पूरा न हो तो कल भी दिखाएँ (Carry Forward)", fontSize = 14.sp, color = TodoTextDark)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (taskTitle.isBlank()) {
                                Toast.makeText(context, "Task Title is mandatory", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val currentTask = editingQuickTask
                            if (currentTask != null) {
                                val updated = currentTask.copy(
                                    title = taskTitle.trim(),
                                    subject = taskSubject.trim(),
                                    isOneTime = isOneTime,
                                    isCarryForward = isCarryForward
                                )
                                viewModel.updateQuickTask(updated)
                                Toast.makeText(context, "Task updated! 🎉", Toast.LENGTH_SHORT).show()
                            } else {
                                val newTask = QuickTask(
                                    title = taskTitle.trim(),
                                    subject = taskSubject.trim(),
                                    isOneTime = isOneTime,
                                    isCarryForward = isCarryForward,
                                    dateCreatedStr = todayStr
                                )
                                viewModel.addQuickTask(newTask)
                                Toast.makeText(context, "Task created! 🎉", Toast.LENGTH_SHORT).show()
                            }
                            showAddQuickTaskDialog = false
                            editingQuickTask = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TodoPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddQuickTaskDialog = false
                            editingQuickTask = null
                        }
                    ) {
                        Text("Cancel", color = TodoTextGray)
                    }
                }
            )
        }

        if (showSuccessEffect) {
            CheckmarkExplosionEffect(
                modifier = Modifier.fillMaxSize(),
                title = "Topic Marked as Revised! 🎉",
                onAnimationEnd = { showSuccessEffect = false }
            )
        }
    }
}

@Composable
fun QuickTaskCard(
    task: QuickTask,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.isCompleted
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val indicatorColor = if (isCompleted) Color(0xFF10B981) else TodoPurple
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                AnimatedStrikethroughText(
                    text = task.title,
                    isCompleted = isCompleted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TodoTextDark,
                    completedColor = TodoTextGray
                )
                val subtitle = StringBuilder()
                if (task.subject.isNotBlank()) {
                    subtitle.append(task.subject)
                }
                if (task.isCarryForward) {
                    if (subtitle.isNotEmpty()) subtitle.append(" • ")
                    subtitle.append("Carry Forward")
                } else {
                    if (subtitle.isNotEmpty()) subtitle.append(" • ")
                    subtitle.append("One-Time")
                }
                Text(subtitle.toString(), fontSize = 12.sp, color = TodoTextGray)
            }
            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline, 
                    contentDescription = if (isCompleted) "Undo" else "Complete", 
                    tint = indicatorColor
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TodoTextGray)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color.White
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TodoPurple) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFEF4444)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                    )
                }
            }
        }
    }
}

@Composable
fun TodayTaskCard(
    revision: Revision,
    isOverdue: Boolean,
    isCompleting: Boolean = false,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onReschedule: () -> Unit
) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    val endOfToday = startOfToday + 86400000L - 1
    val isCompleted = revision.status == "Completed" || revision.status == "Complete" || revision.lastRevisionDateMs in startOfToday..endOfToday
    val alpha by animateFloatAsState(targetValue = if (isCompleting) 0f else 1f, label = "alpha")
    val scale by animateFloatAsState(targetValue = if (isCompleting) 0.85f else 1f, label = "scale")
    val iconScale by animateFloatAsState(
        targetValue = if (isCompleting) 1.4f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isCompleting) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "iconRotation"
    )
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val indicatorColor = if (isCompleted) Color(0xFF10B981) else if (isOverdue) Color(0xFFEF4444) else TodoPurple
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                AnimatedStrikethroughText(
                    text = revision.topicName.ifBlank { revision.notes },
                    isCompleted = isCompleted || isCompleting,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TodoTextDark,
                    completedColor = TodoTextGray
                )
                if (revision.subjectName.isNotBlank()) {
                    Text("${revision.subjectName} • ${revision.chapterName}", fontSize = 12.sp, color = TodoTextGray)
                }
            }
            if (!isCompleted) {
                IconButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .scale(iconScale)
                        .rotate(iconRotation)
                ) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = "Complete", tint = indicatorColor)
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TodoTextGray)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = Color.White
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reschedule") },
                            onClick = {
                                menuExpanded = false
                                onReschedule()
                            },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TodoPurple) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF4444)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                        )
                    }
                }
            }
        }
    }
}

