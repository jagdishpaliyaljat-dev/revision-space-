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

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Chapter
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.launch
import com.example.ui.TopicListRoute
import com.example.ui.TopicDetailRoute
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring

// Shared Colors (Ideally from a central theme, defined here for self-containment matching the image)
val SubPurplePrimary = Color(0xFF613FE6)
val SubPurpleLight = Color(0xFFF1EEFF)
val SubBackgroundWhite = Color(0xFFF6F7FB) // Very light off-white background
val SubCardWhite = Color(0xFFFFFFFF)
val SubSurfaceGray = Color(0xFFE7EAF3)
val SubTextPrimary = Color(0xFF0F172A)
val SubTextSecondary = Color(0xFF5B6478)

@Composable
fun SubjectDetailsScreenWrapper(
    viewModel: RevisionViewModel,
    navController: NavController,
    subjectId: String
) {

    SubjectDetailsScreen(
        subjectId = subjectId,
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
        onDeleteInitiated = { navController.popBackStack() },
        onChapterClick = { chapterId, chapterName ->
            navController.navigate(TopicListRoute(chapterId = chapterId, chapterName = chapterName))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailsScreen(
    subjectId: String,
    viewModel: RevisionViewModel,
    onBack: () -> Unit,
    onDeleteInitiated: () -> Unit,
    onChapterClick: (String, String) -> Unit
) {
    val allSubjectsState by viewModel.allSubjects.collectAsStateWithLifecycle(null)
    val allChaptersState by viewModel.allChapters.collectAsStateWithLifecycle(null)
    val allTopicsState by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle(null)
    
    val allSubjects = allSubjectsState ?: emptyList()
    val allChapters = allChaptersState ?: emptyList()
    val allTopics = allTopicsState ?: emptyList()
    
    val subject = allSubjects.find { it.id == subjectId }
    
    LaunchedEffect(allSubjectsState, subject) {
        if (allSubjectsState != null && subject == null) {
            onDeleteInitiated()
        }
    }
    
    if (allSubjectsState == null || subject == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    
    val chapters = allChapters.filter { it.subjectId == subjectId }
    val topics = allTopics.filter { it.subjectId == subjectId }
    
    val completedTopics = topics.count { it.status != "Pending" }
    val totalTopics = topics.size
    val progressPercent = if (totalTopics > 0) (completedTopics * 100) / totalTopics else 0
    val progress = progressPercent / 100f
    
    var showSubjectMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var selectedChapterForReminder by remember { mutableStateOf<Chapter?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    
    var chapterForAction by remember { mutableStateOf<Chapter?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showEditChapterDialog by remember { mutableStateOf(false) }
    var showDeleteChapterDialog by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current

    Scaffold(
        containerColor = SubBackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            subject.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            letterSpacing = (-0.4).sp,
                            color = SubTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            "Chapters and topics",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE7EAF3), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SubTextPrimary, modifier = Modifier.size(19.dp))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSubjectMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = SubTextPrimary)
                        }
                        DropdownMenu(
                            expanded = showSubjectMenu,
                            onDismissRequest = { showSubjectMenu = false },
                            modifier = Modifier.background(SubCardWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = SubTextPrimary) },
                                onClick = { showSubjectMenu = false; showEditDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = { showSubjectMenu = false; showDeleteConfirmDialog = true }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SubBackgroundWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChapterDialog = true },
                containerColor = SubPurplePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Chapter")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chapters.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No chapters yet. Tap + to add one.", color = SubTextSecondary)
                    }
                }
            } else {
                itemsIndexed(chapters, key = { _, it -> it.id }) { index, chapter ->
                    ChapterItemCard(
                        chapter = chapter,
                        index = index + 1,
                        onClick = { onChapterClick(chapter.id, chapter.name) },
                        onRenameClick = {
                            chapterForAction = chapter
                            showEditChapterDialog = true
                        },
                        onDeleteClick = {
                            chapterForAction = chapter
                            showDeleteChapterDialog = true
                        }
                    )
                }
            }
        }
    }

    selectedChapterForReminder?.let { chapter ->
        // Reminder removed
    }
    
    if (showAddChapterDialog) {
        var newChapterName by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isSaving) showAddChapterDialog = false },
            title = { Text("Add Chapter", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newChapterName,
                        onValueChange = { newChapterName = it; errorMessage = null },
                        label = { Text("Chapter Name") },
                        singleLine = true,
                        isError = errorMessage != null,
                        enabled = !isSaving
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        val trimmed = newChapterName.trim()
                        val isDuplicate = chapters.any { it.name.equals(trimmed, ignoreCase = true) }
                        if (trimmed.isEmpty()) {
                            errorMessage = "Name cannot be empty"
                        } else if (isDuplicate) {
                            errorMessage = "Chapter already exists"
                        } else {
                            isSaving = true
                            viewModel.addChapter(subjectId, trimmed)
                            showAddChapterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubPurplePrimary),
                    enabled = !isSaving
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }, enabled = !isSaving) { Text("Cancel", color = SubTextSecondary) }
            }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(subject.name) }
        var expected by remember { mutableStateOf(subject.totalTopicsExpected.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SubCardWhite,
            title = { Text("Edit Subject", fontWeight = FontWeight.Bold, color = SubTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; errorMessage = null },
                        label = { Text("Subject Name") },
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                    OutlinedTextField(
                        value = expected,
                        onValueChange = { expected = it; errorMessage = null },
                        label = { Text("Expected Total Topics") },
                        isError = errorMessage != null,
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
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubPurplePrimary),
                    enabled = !isSavingSubject
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = SubTextSecondary) } }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteSubjectDialog(
            isDeleting = isDeleting,
            onDismissRequest = { showDeleteConfirmDialog = false },
            onConfirm = {
                isDeleting = true
                coroutineScope.launch {
                    try {
                        viewModel.deleteSubjectSuspend(subject.id)
                        showDeleteConfirmDialog = false
                        onDeleteInitiated()
                    } finally {
                        isDeleting = false
                    }
                }
            }
        )
    }

    if (showOptionsDialog && chapterForAction != null) {
        val chapter = chapterForAction!!
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text(chapter.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showOptionsDialog = false
                            showEditChapterDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = SubPurplePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Rename Chapter", color = SubTextPrimary, fontSize = 16.sp)
                        }
                    }
                    TextButton(
                        onClick = {
                            showOptionsDialog = false
                            showDeleteChapterDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Chapter", color = Color.Red, fontSize = 16.sp)
                        }
                    }
                    TextButton(
                        onClick = {
                            showOptionsDialog = false
                            selectedChapterForReminder = chapter
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = SubPurplePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Daily Reminder Settings", color = SubTextPrimary, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("Cancel", color = SubTextSecondary)
                }
            }
        )
    }

    if (showEditChapterDialog && chapterForAction != null) {
        val chapter = chapterForAction!!
        var editChapterName by remember(chapter.id) { mutableStateOf(chapter.name) }
        var editErrorMessage by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showEditChapterDialog = false },
            title = { Text("Rename Chapter", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editChapterName,
                        onValueChange = { editChapterName = it; editErrorMessage = null },
                        label = { Text("Chapter Name") },
                        singleLine = true,
                        isError = editErrorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (editErrorMessage != null) {
                        Text(editErrorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                var isSaving by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        val trimmed = editChapterName.trim()
                        val isDuplicate = chapters.any { it.name.equals(trimmed, ignoreCase = true) && it.id != chapter.id }
                        if (trimmed.isEmpty()) {
                            editErrorMessage = "Name cannot be empty"
                        } else if (isDuplicate) {
                            editErrorMessage = "Chapter already exists"
                        } else {
                            isSaving = true
                            viewModel.updateChapter(chapter.copy(name = trimmed))
                            showEditChapterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubPurplePrimary),
                    enabled = !isSaving
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditChapterDialog = false }) { Text("Cancel", color = SubTextSecondary) }
            }
        )
    }

    if (showDeleteChapterDialog && chapterForAction != null) {
        val chapter = chapterForAction!!
        var isDeletingChapter by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isDeletingChapter) showDeleteChapterDialog = false },
            title = { Text("Delete Chapter?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete this chapter? This action cannot be undone.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isDeletingChapter) return@Button
                        isDeletingChapter = true
                        coroutineScope.launch {
                            try {
                                viewModel.deleteChapterSuspend(chapter.id)
                                showDeleteChapterDialog = false
                                snackbarHostState.showSnackbar("Chapter deleted successfully.")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                val errorMsg = e.localizedMessage ?: "Unknown error"
                                snackbarHostState.showSnackbar("Error: $errorMsg")
                            } finally {
                                isDeletingChapter = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !isDeletingChapter
                ) {
                    if (isDeletingChapter) {
                        Box(modifier = Modifier.size(24.dp)) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChapterDialog = false }, enabled = !isDeletingChapter) { Text("Cancel", color = SubTextSecondary) }
            }
        )
    }
}

@Composable
fun ChapterItemCard(
    chapter: Chapter,
    index: Int,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F3F9)),
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

            // Chapter Name Only
            Text(
                text = chapter.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Chapter Options",
                        tint = Color(0xFF8A94A6)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename Chapter") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4F46E5)) },
                        onClick = {
                            showMenu = false
                            onRenameClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Chapter", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF8A94A6),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
