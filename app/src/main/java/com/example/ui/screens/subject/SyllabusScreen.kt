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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.example.data.Subject
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

val BluePrimary = Color(0xFF3B82F6)
val SyllabusPurplePrimary = Color(0xFF6C4DFF)
val SyllabusPurpleLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
val BackgroundWhite: Color
    @Composable get() = MaterialTheme.colorScheme.background // Image shows a very light off-white background
val CardWhite: Color
    @Composable get() = MaterialTheme.colorScheme.surface
val SurfaceGray: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant
val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

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
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(emptyList())
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }
    var isDeletingSubject by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val totalSubjects = allSubjects.size
    val totalTopics = allTopics.count { it.subjectId.isNotEmpty() } + allTopics.count { it.subjectId.isEmpty() }
    val completedTopicsCount = allTopics.count { it.status != "Pending" }
    val remainingTopics = maxOf(0, totalTopics - completedTopicsCount)
    val totalRevisions = allTopics.sumOf { it.revisionCount }

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Syllabus", fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.4).sp, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Track", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(4.dp).background(SyllabusPurplePrimary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Plan", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(4.dp).background(SyllabusPurplePrimary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Achieve", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { /* Drawer menu */ },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(CardWhite, CircleShape)
                            .border(1.dp, SurfaceGray, CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Search */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(CardWhite, CircleShape)
                            .border(1.dp, SurfaceGray, CircleShape)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(
                            onClick = { /* Notifications */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(CardWhite, CircleShape)
                                .border(1.dp, SurfaceGray, CircleShape)
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 12.dp)
                                .size(10.dp)
                                .background(SyllabusPurplePrimary, CircleShape)
                                .border(1.dp, CardWhite, CircleShape)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        floatingActionButton = {
            if (allSubjects.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddSubjectDialog = true },
                    containerColor = SyllabusPurplePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subject")
                }
            }
        }
    ) { padding ->
        if (allSubjects.isEmpty()) {
            Box(modifier = Modifier.padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(80.dp), tint = SurfaceGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No subjects found", 
                        color = TextSecondary, 
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showAddSubjectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Subject")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Overall Progress", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Row(
                            modifier = Modifier
                                .background(SyllabusPurpleLight, RoundedCornerShape(12.dp))
                                .border(1.dp, SyllabusPurplePrimary.copy(alpha=0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = SyllabusPurplePrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("On Track", color = SyllabusPurplePrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                    
                    OverallProgressCard(
                        totalSubjects = totalSubjects,
                        totalTopics = totalTopics,
                        completedTopics = completedTopicsCount,
                        remainingTopics = remainingTopics,
                        totalRevisions = totalRevisions
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subject Progress", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text("View All", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SyllabusPurplePrimary)
                    }
                }
                
                itemsIndexed(allSubjects, key = { _, it -> it.id }) { index, subject ->
                    val subjectTopics = allTopics.filter { it.subjectId == subject.id }
                    val completed = subjectTopics.count { it.status != "Pending" }
                    val revisions = subjectTopics.sumOf { it.revisionCount }
                    val lastRevisionMs = subjectTopics.maxOfOrNull { it.lastRevisionDateMs } ?: 0L
                    
                    val subjectProgress = if (subjectTopics.isNotEmpty()) completed.toFloat() / subjectTopics.size else 0f
                    
                    SubjectItemCard(
                        subject = subject,
                        topicsCount = subjectTopics.size,
                        completedTopics = completed,
                        totalRevisions = revisions,
                        lastRevisionDateMs = lastRevisionMs,
                        index = index,
                        progress = subjectProgress,
                        onClick = { onSubjectSelected(subject.id) },
                        onEditClick = { subjectToEdit = subject },
                        onDeleteClick = { subjectToDelete = subject }
                    )
                }
            }
        }
    }
    
    if (showAddSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var expectedTopics by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            containerColor = CardWhite,
            title = { Text("Create Subject", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expectedTopics,
                        onValueChange = { expectedTopics = it },
                        label = { Text("Expected Total Topics (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty()) {
                            viewModel.addSubject(name = trimmedName, colorHex = "#613FE6", icon = "📚", totalTopicsExpected = expectedTopics.toIntOrNull() ?: 0)
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurplePrimary)
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel", color = TextSecondary) } }
        )
    }

    if (subjectToEdit != null) {
        val subject = subjectToEdit!!
        var editName by remember(subject.id) { mutableStateOf(subject.name) }
        var expected by remember(subject.id) { mutableStateOf(subject.totalTopicsExpected.toString()) }
        var errorMessage by remember(subject.id) { mutableStateOf<String?>(null) }
        val context = androidx.compose.ui.platform.LocalContext.current
        
        AlertDialog(
            onDismissRequest = { subjectToEdit = null },
            containerColor = CardWhite,
            title = { Text("Update Subject", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; errorMessage = null },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                    OutlinedTextField(
                        value = expected,
                        onValueChange = { expected = it },
                        label = { Text("Expected Total Topics") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                var isSavingSubject by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (isSavingSubject) return@Button
                        val trimmedName = editName.trim()
                        val expectedInt = expected.trim().toIntOrNull()
                        if (trimmedName.isEmpty()) {
                            errorMessage = "Subject name cannot be empty"
                        } else if (allSubjects.any { it.id != subject.id && it.name.equals(trimmedName, ignoreCase = true) }) {
                            errorMessage = "Subject already exists"
                        } else if (expected.trim().isEmpty()) {
                            errorMessage = "Expected topics cannot be empty"
                        } else if (expectedInt == null) {
                            errorMessage = "Expected topics must be a valid number"
                        } else if (expectedInt < 0) {
                            errorMessage = "Expected topics cannot be negative"
                        } else if (expected.trim().length > 5) {
                            errorMessage = "Expected topics value is too large"
                        } else {
                            isSavingSubject = true
                            viewModel.updateSubject(subject.id, trimmedName, subject.colorHex, subject.icon, expectedInt)
                            android.widget.Toast.makeText(context, "Subject updated", android.widget.Toast.LENGTH_SHORT).show()
                            subjectToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SyllabusPurplePrimary),
                    enabled = !isSavingSubject
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { subjectToEdit = null }) { Text("Cancel", color = TextSecondary) } }
        )
    }

    if (subjectToDelete != null) {
        val subject = subjectToDelete!!
        val context = androidx.compose.ui.platform.LocalContext.current
        DeleteSubjectDialog(
            isDeleting = isDeletingSubject,
            onDismissRequest = { subjectToDelete = null },
            onConfirm = {
                isDeletingSubject = true
                coroutineScope.launch {
                    try {
                        viewModel.deleteSubjectSuspend(subject.id)
                        android.widget.Toast.makeText(context, "Subject deleted", android.widget.Toast.LENGTH_SHORT).show()
                        subjectToDelete = null
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Failed to delete: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    } finally {
                        isDeletingSubject = false
                    }
                }
            }
        )
    }
}

@Composable
fun OverallProgressCard(
    totalSubjects: Int,
    totalTopics: Int,
    completedTopics: Int,
    remainingTopics: Int,
    totalRevisions: Int
) {
    val progress = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f
    val progressPercent = (progress * 100).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceGray,
                    strokeWidth = 6.dp
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = SyllabusPurplePrimary,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$progressPercent%", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = SyllabusPurplePrimary)
                    Text("Completed", fontSize = 10.sp, color = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Stats Row with vertical dividers
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(
                    value = {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$completedTopics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text(" / $totalTopics", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                        }
                    },
                    label = "Topics"
                )
                
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(SurfaceGray))
                
                StatColumn(
                    value = { Text("$remainingTopics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary) },
                    label = "Remaining"
                )
                
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(SurfaceGray))
                
                StatColumn(
                    value = { Text("$totalSubjects", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary) },
                    label = "Subjects"
                )
                
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(SurfaceGray))
                
                StatColumn(
                    value = { Text("%,d".format(totalRevisions), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary) },
                    label = "Revisions"
                )
            }
        }
    }
}

@Composable
fun StatColumn(value: @Composable () -> Unit, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        value()
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun SubjectItemCard(
    subject: Subject,
    topicsCount: Int,
    completedTopics: Int,
    totalRevisions: Int,
    lastRevisionDateMs: Long,
    index: Int,
    progress: Float,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val denominator = topicsCount
    val progressPercent = (progress * 100).toInt()
    
    // Exact colors from the image
    val colors = listOf(
        Color(0xFF6C4DFF), // Purple for Reasoning
        Color(0xFF10B981), // Green for Maths
        Color(0xFFF59E0B), // Orange for General Awareness
        Color(0xFFEF4444), // Red for English
        Color(0xFF3B82F6)  // Blue for Computer
    )
    val customColor = colors[index % colors.size]
    
    val lastRev = formatLastRevision(lastRevisionDateMs)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colored initial badge keeps the subject colour visible
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(customColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            subject.name.firstOrNull()?.uppercase() ?: "S",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = customColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(subject.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$completedTopics/$denominator", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("$progressPercent%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = customColor)
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        var showCardMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { showCardMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(
                                expanded = showCardMenu,
                                onDismissRequest = { showCardMenu = false },
                                modifier = Modifier.background(CardWhite)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showCardMenu = false
                                        onEditClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color.Red) },
                                    onClick = {
                                        showCardMenu = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = customColor,
                    trackColor = SurfaceGray
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bottom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Revisions: $totalRevisions", fontSize = 12.sp, color = TextSecondary)
                    
                    Row {
                        Text("Last Revision: ", fontSize = 12.sp, color = TextSecondary)
                        Text(lastRev.first, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = lastRev.second)
                    }
                }
            }
        }
    }

@Composable
fun formatLastRevision(lastRevisionDateMs: Long): Pair<String, Color> {
    if (lastRevisionDateMs == 0L) return Pair("Not started", TextSecondary)
    
    val diff = System.currentTimeMillis() - lastRevisionDateMs
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    
    return when {
        days == 0L -> Pair("Today", Color(0xFF10B981))
        days == 1L -> Pair("Yesterday", Color(0xFF10B981))
        days == 2L -> Pair("2 Days Ago", Color(0xFFF59E0B))
        else -> Pair("$days Days Ago", Color(0xFFEF4444))
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
    }
}
