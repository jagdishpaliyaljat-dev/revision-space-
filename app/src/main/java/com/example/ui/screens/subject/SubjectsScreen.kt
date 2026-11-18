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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search subjects...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        )
                    } else {
                        Text("Subjects", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded 
                        if (!isSearchExpanded) searchQuery = ""
                    }) {
                        Icon(if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSubjectDialog = true },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (filteredSubjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No subjects found" else "No subjects yet", 
                            color = Color(0xFF475569), 
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Add your first subject to get started", 
                            color = Color(0xFF94A3B8), 
                            fontSize = 14.sp
                        )
                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { showAddSubjectDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Subject")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add First Subject")
                            }
                        }
                    }
                }
            } else {
                var deletedSubjectIds by remember { mutableStateOf(setOf<String>()) }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    enabled = !isSaving
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val customThemeColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorHex)) }.getOrDefault(Color(0xFF6366F1))

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
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color(0xFF3B82F6)).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEF4444)).padding(horizontal = 24.dp),
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
                    .bounceClick { onTopicClick(subject.id) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Serial Number Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF4F46E5)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Subject Name Only
                    Text(
                        text = subject.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    var showCardMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showCardMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF94A3B8))
                        }
                        DropdownMenu(
                            expanded = showCardMenu,
                            onDismissRequest = { showCardMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                onClick = {
                                    showCardMenu = false
                                    showRenameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showCardMenu = false
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = {
                                    showCardMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }

                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    enabled = !isSaving
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } }
        )
    }
}
