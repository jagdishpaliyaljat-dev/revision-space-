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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Subject
import com.example.data.TopicWithSubjectAndChapter
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    viewModel: RevisionViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onTopicClick: (String) -> Unit,
    onStartRevision: (String) -> Unit
) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())

    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(emptyList())
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val filteredSubjects = allSubjects.filter { subject ->
        if (subject.isArchived) return@filter false
        val matchesSubject = subject.name.contains(searchQuery, ignoreCase = true)
        
        val subjectChapters = allChapters.filter { it.subjectId == subject.id && !it.isArchived }
        val matchesChapter = subjectChapters.any { it.name.contains(searchQuery, ignoreCase = true) }
        
        val subjectTopics = allTopics.filter { it.subjectId == subject.id }
        val matchesTopic = subjectTopics.any { it.topicName.contains(searchQuery, ignoreCase = true) }
        
        matchesSubject || matchesChapter || matchesTopic
    }

    val totalSubjects = filteredSubjects.size
    
    val activeSubjects = allSubjects.filter { !it.isArchived }

    val activeSubjectIds = activeSubjects.map { it.id }.toSet()
    val totalTopicsCount = allTopics.count { activeSubjectIds.contains(it.subjectId) }
    val completedTopicsCount = allTopics.count {
        activeSubjectIds.contains(it.subjectId) && it.status == "Complete"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search subjects...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF7C3AED),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    } else {
                        Column {
                            Text(
                                "Subjects",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${activeSubjects.size} subjects \u2022 $totalTopicsCount topics",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8A94A6)
                            )
                        }
                    }
                },
                navigationIcon = {
                    val appDrawer = LocalAppDrawer.current
                    IconButton(
                        onClick = { appDrawer.open() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) searchQuery = ""
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSubjectDialog = true },
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                icon = {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                text = {
                    Text("Add Subject", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (filteredSubjects.isEmpty()) {
                SubjectsEmptyState(
                    isSearching = searchQuery.isNotEmpty(),
                    onAdd = { showAddSubjectDialog = true }
                )
            } else {
                var deletedSubjectIds by remember { mutableStateOf(setOf<String>()) }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp)
                ) {
                    item {
                        SubjectsLibrarySummary(
                            subjects = activeSubjects.size,
                            topics = totalTopicsCount,
                            completed = completedTopicsCount
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "YOUR SUBJECTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF8A94A6)
                            )
                            Text(
                                "${filteredSubjects.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                    itemsIndexed(filteredSubjects, key = { _, it -> it.id }) { index, subject ->
                        if (!deletedSubjectIds.contains(subject.id)) {
                            val subjectTopics = allTopics.filter { it.subjectId == subject.id }
                            ExpandableSubjectCard(
                                index = index + 1,
                                subject = subject,
                                topics = subjectTopics,
                                allSubjects = allSubjects,
                                viewModel = viewModel,
                                onTopicClick = onTopicClick,
                                snackbarHostState = snackbarHostState,
                                coroutineScope = coroutineScope,
                                onDeleteStart = {
                                    deletedSubjectIds = deletedSubjectIds + subject.id
                                },
                                onReviewClick = onStartRevision
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var expectedTopics by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Create Subject", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                    OutlinedTextField(
                        value = expectedTopics,
                        onValueChange = { expectedTopics = it },
                        label = { Text("Expected Topics (Optional)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                var isSaving by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        val trimmedName = name.trim()
                        val expectedInt = expectedTopics.trim().toIntOrNull()
                        if (trimmedName.isEmpty()) {
                            errorMessage = "Subject name cannot be empty"
                        } else if (allSubjects.any { it.name.equals(trimmedName, ignoreCase = true) }) {
                            errorMessage = "Subject already exists"
                        } else if (expectedTopics.trim().isNotEmpty() && (expectedInt == null || expectedInt < 0)) {
                            errorMessage = "Expected topics must be a valid non-negative number"
                        } else if (expectedTopics.trim().length > 5) {
                            errorMessage = "Expected topics value is too large"
                        } else {
                            isSaving = true
                            viewModel.addSubject(name = trimmedName, examId = viewModel.activeExamId.value, totalTopicsExpected = expectedInt ?: 0)
                            Toast.makeText(context, "Subject Created", Toast.LENGTH_SHORT).show()
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    enabled = !isSaving
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}


/** Compact overview of the whole subject library. */
@Composable
private fun SubjectsLibrarySummary(subjects: Int, topics: Int, completed: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryStat(value = subjects.toString(), label = "Subjects", modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            SummaryStat(value = topics.toString(), label = "Topics", modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            SummaryStat(
                value = completed.toString(),
                label = "Completed",
                valueColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF8A94A6))
    }
}

@Composable
private fun SubjectsEmptyState(isSearching: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = Color(0xFF7C3AED)
            )
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = if (isSearching) "No subjects found" else "No subjects yet",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearching) "Try a different search term" else "Add your first subject to start building your revision library",
            color = Color(0xFF8A94A6),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!isSearching) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Subject", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpandableSubjectCard(
    index: Int = 1,
    subject: Subject,
    topics: List<TopicWithSubjectAndChapter>,
    allSubjects: List<Subject>,
    viewModel: RevisionViewModel,
    onTopicClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onDeleteStart: () -> Unit = {},
    onReviewClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val customThemeColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorHex)) }.getOrDefault(Color(0xFF7C3AED))

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                showEditDialog = true
                false
            } else if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirm = true
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            if (direction == SwipeToDismissBoxValue.StartToEnd) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).background(Color(0xFF3B82F6)).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).background(Color(0xFFEF4444)).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onTopicClick(subject.id) }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        clip = false,
                        spotColor = customThemeColor.copy(alpha = 0.12f),
                        ambientColor = customThemeColor.copy(alpha = 0.06f)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colour accent for instant subject recognition
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(customThemeColor)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(customThemeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = index.toString().padStart(2, '0'),
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = customThemeColor
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.combinedClickable(
                                    onClick = { onTopicClick(subject.id) },
                                    onLongClick = { showRenameDialog = true }
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFF8A94A6),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${topics.size} topics",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8A94A6)
                                )
                                val completedCount = topics.count { it.status == "Complete" }
                                if (completedCount > 0) {
                                    Text(" \u2022 ", fontSize = 12.sp, color = Color(0xFF8A94A6))
                                    Text(
                                        text = "$completedCount done",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }

                    // Completion progress for this subject
                    val completedFraction = if (topics.isEmpty()) 0f
                        else topics.count { it.status == "Complete" }.toFloat() / topics.size
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(customThemeColor.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(completedFraction.coerceIn(0f, 1f))
                                .height(4.dp)
                                .background(customThemeColor)
                        )
                    }
                }
            }
        }
    )

    if (showEditDialog) {
        var editName by remember { mutableStateOf(subject.name) }
        var expected by remember { mutableStateOf(subject.totalTopicsExpected.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Subject", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; errorMessage = null },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                    OutlinedTextField(
                        value = expected,
                        onValueChange = { expected = it },
                        label = { Text("Expected Topics") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                var isSaving by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (isSaving) return@Button
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
                            isSaving = true
                            viewModel.updateSubject(subject.id, trimmedName, subject.colorHex, subject.icon, expectedInt)
                            Toast.makeText(context, "Subject updated", Toast.LENGTH_SHORT).show()
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    enabled = !isSaving
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteConfirm) {
        DeleteSubjectDialog(
            isDeleting = isDeleting,
            onDismissRequest = { showDeleteConfirm = false },
            onConfirm = {
                isDeleting = true
                coroutineScope.launch {
                    try {
                        onDeleteStart()
                        kotlinx.coroutines.delay(400) // Wait for slide-out animation to complete
                        viewModel.deleteSubjectSuspend(subject.id)
                        showDeleteConfirm = false
                    } finally {
                        isDeleting = false
                    }
                }
            }
        )
    }

    if (showRenameDialog) {
        var renameName by remember { mutableStateOf(subject.name) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Subject", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameName,
                        onValueChange = { renameName = it; errorMessage = null },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            },
            confirmButton = {
                var isSaving by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        val trimmedName = renameName.trim()
                        if (trimmedName.isEmpty()) {
                            errorMessage = "Subject name cannot be empty"
                        } else if (allSubjects.any { it.id != subject.id && it.name.equals(trimmedName, ignoreCase = true) }) {
                            errorMessage = "Subject already exists"
                        } else {
                            isSaving = true
                            viewModel.updateSubject(subject.id, trimmedName, subject.colorHex, subject.icon, subject.totalTopicsExpected)
                            Toast.makeText(context, "Subject renamed", Toast.LENGTH_SHORT).show()
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    enabled = !isSaving
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } }
        )
    }
}
