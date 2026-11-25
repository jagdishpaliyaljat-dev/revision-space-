package com.example.ui.screens.subject

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.ExamCountdown
import com.example.data.Subject
import com.example.data.TopicWithSubjectAndChapter
import com.example.ui.components.LocalAppDrawer
import com.example.ui.utils.AudioFeedback
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val SyllabusPurple = Color(0xFF6C4DFF)
val SyllabusPurpleLight = Color(0xFFEDE9FE)
val SyllabusGreen = Color(0xFF10B981)
val SyllabusAmber = Color(0xFFF59E0B)
val SyllabusRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    viewModel: RevisionViewModel,
    navController: NavController,
    onSubjectClick: (String) -> Unit
) {
    SyllabusMainContent(
        viewModel = viewModel,
        navController = navController,
        onSubjectSelected = onSubjectClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusMainContent(
    viewModel: RevisionViewModel,
    navController: NavController,
    onSubjectSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appDrawer = LocalAppDrawer.current

    val allExams by viewModel.allExams.collectAsStateWithLifecycle(emptyList())
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(emptyList())

    var selectedExamTabId by remember { mutableStateOf("all") }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var examToEdit by remember { mutableStateOf<ExamCountdown?>(null) }
    var examToDelete by remember { mutableStateOf<ExamCountdown?>(null) }
    var examToLinkSubjects by remember { mutableStateOf<ExamCountdown?>(null) }

    // Map subjects by ID for easy lookup
    val subjectMap = remember(allSubjects) { allSubjects.associateBy { it.id } }

    // Calculate per-exam syllabus statistics
    val examStatsList = remember(allExams, allSubjects, allTopics) {
        allExams.map { exam ->
            val linkedSubjects = allSubjects.filter { it.examId == exam.id }
            val linkedSubjectIds = linkedSubjects.map { it.id }.toSet()
            
            // Topics belong to this exam either directly or via subject
            val examTopics = allTopics.filter { topic ->
                topic.examIds.contains(exam.id) || linkedSubjectIds.contains(topic.subjectId)
            }
            val completedTopics = examTopics.count { it.status == "Completed" || it.status == "Complete" }
            val totalTopics = examTopics.size
            val remainingTopics = maxOf(0, totalTopics - completedTopics)
            val progressRatio = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f
            val progressPercent = (progressRatio * 100).toInt()

            ExamSyllabusStat(
                exam = exam,
                linkedSubjects = linkedSubjects,
                examTopics = examTopics,
                totalTopics = totalTopics,
                completedTopics = completedTopics,
                remainingTopics = remainingTopics,
                progressPercent = progressPercent,
                progressRatio = progressRatio
            )
        }
    }

    // Overall summary across all exams
    val totalAllTopics = remember(examStatsList) { examStatsList.sumOf { it.totalTopics } }
    val totalAllCompleted = remember(examStatsList) { examStatsList.sumOf { it.completedTopics } }
    val overallProgressPercent = remember(totalAllTopics, totalAllCompleted) {
        if (totalAllTopics > 0) (totalAllCompleted * 100) / totalAllTopics else 0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Syllabus Tracker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.4).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Multi-Exam Syllabus Completion",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { appDrawer.open() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Drawer",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { showAddExamDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SyllabusPurple,
                        contentColor = Color.White,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Exam", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (allExams.isEmpty()) {
            // Empty State: Prompt to add exams
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SyllabusPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = SyllabusPurple,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Target Exams Added",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Track your complete syllabus across multiple exams (e.g. UPSC, JEE, NEET, SSC). See exactly how much syllabus is completed for each exam.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddExamDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurple)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Target Exam", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Overall Multi-Exam Summary Banner
                item {
                    OverallMultiExamBanner(
                        totalExams = allExams.size,
                        totalTopics = totalAllTopics,
                        completedTopics = totalAllCompleted,
                        overallProgressPercent = overallProgressPercent
                    )
                }

                // 2. Filter Pills: All Exams + Specific Exam Tabs
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedExamTabId == "all",
                            onClick = { selectedExamTabId = "all" },
                            label = { Text("All Exams (${allExams.size})") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = SyllabusPurple,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedExamTabId == "all") SyllabusPurple else MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        examStatsList.forEach { stat ->
                            val isSelected = selectedExamTabId == stat.exam.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedExamTabId = stat.exam.id },
                                label = { Text("${stat.exam.name} (${stat.progressPercent}%)") },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = SyllabusPurple,
                                    selectedLabelColor = Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SyllabusPurple else MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                }

                // 3. Exam Syllabus Cards List
                val displayedStats = if (selectedExamTabId == "all") {
                    examStatsList
                } else {
                    examStatsList.filter { it.exam.id == selectedExamTabId }
                }

                items(displayedStats, key = { it.exam.id }) { stat ->
                    ExamSyllabusCard(
                        stat = stat,
                        allChapters = allChapters,
                        onEditExam = { examToEdit = stat.exam },
                        onDeleteExam = { examToDelete = stat.exam },
                        onLinkSubjects = { examToLinkSubjects = stat.exam },
                        onSubjectClick = onSubjectSelected,
                        onToggleTopicCompletion = { topicId ->
                            AudioFeedback.playCompletionSound(context)
                            viewModel.updateTopicCompletion(context, topicId)
                        }
                    )
                }
            }
        }
    }

    // Add Exam Dialog
    if (showAddExamDialog) {
        ExamDialog(
            title = "Add Target Exam",
            initialName = "",
            initialDateMs = null,
            initialHours = "",
            onDismiss = { showAddExamDialog = false },
            onSave = { name, dateMs, hours ->
                viewModel.insertExam(name, dateMs, hours)
                showAddExamDialog = false
                Toast.makeText(context, "Exam '$name' added!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Exam Dialog
    if (examToEdit != null) {
        val ex = examToEdit!!
        ExamDialog(
            title = "Edit Exam",
            initialName = ex.name,
            initialDateMs = ex.examDateMs,
            initialHours = ex.targetStudyHours?.toString() ?: "",
            onDismiss = { examToEdit = null },
            onSave = { name, dateMs, hours ->
                viewModel.updateExam(ex.copy(name = name, examDateMs = dateMs, targetStudyHours = hours))
                examToEdit = null
                Toast.makeText(context, "Exam updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Exam Confirmation
    if (examToDelete != null) {
        val ex = examToDelete!!
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("Delete Exam", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${ex.name}' from your syllabus tracking? Your subjects and topics will not be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExam(ex.id)
                        examToDelete = null
                        Toast.makeText(context, "Exam deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SyllabusRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Link Subjects Dialog
    if (examToLinkSubjects != null) {
        val ex = examToLinkSubjects!!
        LinkSubjectsToExamDialog(
            exam = ex,
            allSubjects = allSubjects,
            onDismiss = { examToLinkSubjects = null },
            onSaveLinks = { selectedSubjectIds ->
                // Update each subject
                allSubjects.forEach { sub ->
                    val shouldBeLinked = selectedSubjectIds.contains(sub.id)
                    if (shouldBeLinked && sub.examId != ex.id) {
                        viewModel.updateSubjectExam(sub.id, ex.id)
                    } else if (!shouldBeLinked && sub.examId == ex.id) {
                        viewModel.updateSubjectExam(sub.id, "")
                    }
                }
                examToLinkSubjects = null
                Toast.makeText(context, "Subjects linked to '${ex.name}'!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Top executive banner showing overall syllabus progress across all tracked exams.
 */
@Composable
private fun OverallMultiExamBanner(
    totalExams: Int,
    totalTopics: Int,
    completedTopics: Int,
    overallProgressPercent: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    strokeWidth = 6.dp
                )
                CircularProgressIndicator(
                    progress = { (overallProgressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = SyllabusPurple,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$overallProgressPercent%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SyllabusPurple
                    )
                    Text("Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overall Syllabus Coverage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedTopics of $totalTopics topics completed across $totalExams target ${if (totalExams == 1) "exam" else "exams"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (overallProgressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SyllabusPurple,
                    trackColor = SyllabusPurpleLight
                )
            }
        }
    }
}

/**
 * Dedicated Card for Each Exam showing:
 * - Exam countdown badge
 * - "Kiska kitna hua hai complete" progress bar and percentage
 * - Topics breakdown tiles
 * - Expandable list of subjects, chapters, and topics under this exam
 */
@Composable
private fun ExamSyllabusCard(
    stat: ExamSyllabusStat,
    allChapters: List<com.example.data.Chapter>,
    onEditExam: () -> Unit,
    onDeleteExam: () -> Unit,
    onLinkSubjects: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onToggleTopicCompletion: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val daysLeft = remember(stat.exam.examDateMs) {
        val target = stat.exam.examDateMs ?: return@remember null
        val diff = target - System.currentTimeMillis()
        if (diff < 0) 0 else TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Exam Title + Days Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stat.exam.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (daysLeft != null) {
                        Text(
                            text = "$daysLeft days left",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (daysLeft <= 30) SyllabusRed else SyllabusPurple
                        )
                    } else {
                        Text(
                            text = "No exam date set",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Options Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Link / Assign Subjects") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = SyllabusPurple) },
                            onClick = {
                                showMenu = false
                                onLinkSubjects()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Exam Details") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEditExam()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Exam", color = SyllabusRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SyllabusRed) },
                            onClick = {
                                showMenu = false
                                onDeleteExam()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Banner: "Kiska kitna hua hai complete"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Syllabus Completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stat.progressPercent}%",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            stat.progressPercent >= 80 -> SyllabusGreen
                            stat.progressPercent >= 40 -> SyllabusPurple
                            else -> SyllabusAmber
                        }
                    )
                }

                Surface(
                    color = when {
                        stat.progressPercent >= 80 -> SyllabusGreen.copy(alpha = 0.12f)
                        stat.progressPercent >= 40 -> SyllabusPurple.copy(alpha = 0.12f)
                        stat.progressPercent > 0 -> SyllabusAmber.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            stat.progressPercent >= 100 -> "100% Complete"
                            stat.progressPercent >= 80 -> "Almost Done"
                            stat.progressPercent >= 40 -> "On Track"
                            stat.progressPercent > 0 -> "In Progress"
                            else -> "Not Started"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            stat.progressPercent >= 80 -> SyllabusGreen
                            stat.progressPercent >= 40 -> SyllabusPurple
                            stat.progressPercent > 0 -> SyllabusAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { stat.progressRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    stat.progressPercent >= 80 -> SyllabusGreen
                    stat.progressPercent >= 40 -> SyllabusPurple
                    else -> SyllabusAmber
                },
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Stats Tiles: Total, Completed, Remaining, Subjects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(
                    label = "Total Topics",
                    value = "${stat.totalTopics}",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Completed",
                    value = "${stat.completedTopics}",
                    valueColor = SyllabusGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Remaining",
                    value = "${stat.remainingTopics}",
                    valueColor = if (stat.remainingTopics > 0) SyllabusAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Subjects",
                    value = "${stat.linkedSubjects.size}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expand / Collapse Subject Breakdown Button
            Surface(
                onClick = { isExpanded = !isExpanded },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isExpanded) "Hide Subject Breakdown" else "View Subject Breakdown (${stat.linkedSubjects.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SyllabusPurple
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SyllabusPurple
                    )
                }
            }

            // Expandable Content: Subjects & Topics under this Exam
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (stat.linkedSubjects.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No subjects linked to '${stat.exam.name}'",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onLinkSubjects,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurple),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Link Subjects Now", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        stat.linkedSubjects.forEach { subject ->
                            ExamSubjectProgressCard(
                                subject = subject,
                                allExamTopics = stat.examTopics,
                                allChapters = allChapters,
                                onSubjectClick = onSubjectClick,
                                onToggleTopicCompletion = onToggleTopicCompletion
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Subject card inside an exam showing subject completion and expandable chapters/topics.
 */
@Composable
private fun ExamSubjectProgressCard(
    subject: Subject,
    allExamTopics: List<TopicWithSubjectAndChapter>,
    allChapters: List<com.example.data.Chapter>,
    onSubjectClick: (String) -> Unit,
    onToggleTopicCompletion: (String) -> Unit
) {
    var isSubjectExpanded by remember { mutableStateOf(false) }

    val subjectTopics = remember(subject.id, allExamTopics) {
        allExamTopics.filter { it.subjectId == subject.id }
    }
    val completedCount = subjectTopics.count { it.status == "Completed" || it.status == "Complete" }
    val totalCount = subjectTopics.size
    val subProgressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val subProgressPercent = (subProgressRatio * 100).toInt()

    val subjectChapters = remember(subject.id, allChapters) {
        allChapters.filter { it.subjectId == subject.id && !it.isArchived }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Subject header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSubjectExpanded = !isSubjectExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color bar
                Box(
                    modifier = Modifier
                        .size(10.dp, 32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(subject.colorHex))
                            } catch (e: Exception) {
                                SyllabusPurple
                            }
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$completedCount / $totalCount topics ($subProgressPercent%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isSubjectExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini progress bar
            LinearProgressIndicator(
                progress = { subProgressRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (subProgressPercent >= 100) SyllabusGreen else SyllabusPurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Expanded chapters & topics list
            AnimatedVisibility(visible = isSubjectExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (subjectTopics.isEmpty()) {
                        Text(
                            text = "No topics added under this subject yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        // Group topics by chapter
                        val topicsByChapter = remember(subjectTopics) {
                            subjectTopics.groupBy { it.chapterName.ifEmpty { "General Topics" } }
                        }

                        topicsByChapter.forEach { (chapterName, topics) ->
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = chapterName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                topics.forEach { topic ->
                                    val isCompleted = topic.status == "Completed" || topic.status == "Complete"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onToggleTopicCompletion(topic.topicId) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                                contentDescription = "Toggle completion",
                                                tint = if (isCompleted) SyllabusGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = topic.topicName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                                            color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Status Chip
                                        Surface(
                                            color = when (topic.status) {
                                                "Completed", "Complete" -> SyllabusGreen.copy(alpha = 0.12f)
                                                "Today" -> SyllabusAmber.copy(alpha = 0.12f)
                                                "Overdue" -> SyllabusRed.copy(alpha = 0.12f)
                                                "Upcoming" -> SyllabusPurple.copy(alpha = 0.12f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = topic.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (topic.status) {
                                                    "Completed", "Complete" -> SyllabusGreen
                                                    "Today" -> SyllabusAmber
                                                    "Overdue" -> SyllabusRed
                                                    "Upcoming" -> SyllabusPurple
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Button to open full subject details
                    TextButton(
                        onClick = { onSubjectClick(subject.id) },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Manage in Subject View →", fontSize = 11.sp, color = SyllabusPurple)
                    }
                }
            }
        }
    }
}

/**
 * Reusable Metric Tile for stats.
 */
@Composable
private fun MetricTile(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dialog to Add or Edit Target Exam with name, target date, and target hours.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamDialog(
    title: String,
    initialName: String,
    initialDateMs: Long?,
    initialHours: String,
    onDismiss: () -> Unit,
    onSave: (String, Long?, Int?) -> Unit
) {
    var examName by remember { mutableStateOf(initialName) }
    var targetHours by remember { mutableStateOf(initialHours) }
    var examDateMs by remember { mutableStateOf(initialDateMs) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = examDateMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    examDateMs = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK", color = SyllabusPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name (e.g. UPSC, JEE, NEET, SSC)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Date Picker row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = SyllabusPurple)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = examDateMs?.let {
                            SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Select Exam Date",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (examDateMs != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    label = { Text("Target Study Hours (optional)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = examName.trim()
                    if (trimmed.isNotEmpty()) {
                        onSave(trimmed, examDateMs, targetHours.toIntOrNull())
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurple)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to Link / Assign Subjects to a specific Exam.
 */
@Composable
private fun LinkSubjectsToExamDialog(
    exam: ExamCountdown,
    allSubjects: List<Subject>,
    onDismiss: () -> Unit,
    onSaveLinks: (Set<String>) -> Unit
) {
    val selectedSubjectIds = remember {
        mutableStateListOf<String>().apply {
            addAll(allSubjects.filter { it.examId == exam.id }.map { it.id })
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Link Subjects", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Select subjects for ${exam.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            if (allSubjects.isEmpty()) {
                Text("No subjects created yet. Add subjects in the Subjects tab first.")
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allSubjects.forEach { subject ->
                        val isChecked = selectedSubjectIds.contains(subject.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isChecked) selectedSubjectIds.remove(subject.id)
                                    else selectedSubjectIds.add(subject.id)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedSubjectIds.add(subject.id)
                                    else selectedSubjectIds.remove(subject.id)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subject.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveLinks(selectedSubjectIds.toSet()) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurple)
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Data holder for computed statistics of an exam's syllabus.
 */
private data class ExamSyllabusStat(
    val exam: ExamCountdown,
    val linkedSubjects: List<Subject>,
    val examTopics: List<TopicWithSubjectAndChapter>,
    val totalTopics: Int,
    val completedTopics: Int,
    val remainingTopics: Int,
    val progressPercent: Int,
    val progressRatio: Float
)
