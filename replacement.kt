@Composable
fun SubjectDetailScreen(
    subjectId: String,
    viewModel: RevisionViewModel,
    onBack: () -> Unit
) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    val allSubjectTests by viewModel.allSubjectTests.collectAsStateWithLifecycle(emptyList())
    val subject = allSubjects.find { it.id == subjectId } ?: return
    val chapters = allChapters.filter { it.subjectId == subjectId && !it.isArchived }
    val tests = allSubjectTests.filter { it.subjectId == subjectId }

    var selectedTab by remember { mutableStateOf(0) } // 0: Chapters, 1: Tests, 2: Performance
    BackHandler { onBack() }
    val themeColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorHex)) }.getOrDefault(MaterialTheme.colorScheme.primary)
    val cTotal = chapters.size
    val cCompleted = chapters.count { it.isCompleted }
    val completionPercent = if (cTotal > 0) (cCompleted * 100) / cTotal else 0
    val totalRevisions = chapters.sumOf { it.revisionCount }
    val avgScore = if (tests.isNotEmpty()) {
        tests.map { if (it.totalMarks > 0f) (it.marksObtained / it.totalMarks) * 100f else 0f }.average().toInt()
    } else 0

    var showSubjectMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showIconColorDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(subject.name) }
    var colorInput by remember { mutableStateOf(subject.colorHex) }
    var iconInput by remember { mutableStateOf(subject.icon) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject.name.uppercase(), fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSubjectMenu = true }) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showSubjectMenu, onDismissRequest = { showSubjectMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit Subject") }, onClick = { showSubjectMenu = false; showRenameDialog = true })
                            DropdownMenuItem(text = { Text("Rename Subject") }, onClick = { showSubjectMenu = false; showRenameDialog = true })
                            DropdownMenuItem(text = { Text("Change Subject Icon & Color") }, onClick = { showSubjectMenu = false; showIconColorDialog = true })
                            DropdownMenuItem(text = { Text("Sort Chapters") }, onClick = { showSubjectMenu = false; Toast.makeText(context, "Sort Chapters", Toast.LENGTH_SHORT).show() })
                            DropdownMenuItem(text = { Text("Search Chapters") }, onClick = { showSubjectMenu = false; Toast.makeText(context, "Search Chapters", Toast.LENGTH_SHORT).show() })
                            DropdownMenuItem(text = { Text("Archive Subject") }, onClick = { 
                                showSubjectMenu = false
                                viewModel.updateSubject(subject.id, subject.name, subject.colorHex, subject.icon, subject.totalTopicsExpected)
                                Toast.makeText(context, "Subject Archived", Toast.LENGTH_SHORT).show()
                                onBack()
                            })
                            DropdownMenuItem(text = { Text("Export Subject") }, onClick = { showSubjectMenu = false; Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show() })
                            DropdownMenuItem(text = { Text("Delete Subject", color = Color.Red) }, onClick = { showSubjectMenu = false; showDeleteConfirmDialog = true })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            // Intro Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).background(themeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(subject.name.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Overall Progress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { completionPercent / 100f },
                                    color = themeColor,
                                    trackColor = themeColor.copy(alpha = 0.2f),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("$completionPercent%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItemSmall(value = "$cCompleted/$cTotal", label = "Chapters")
                        MetricItemSmall(value = "$totalRevisions", label = "Revisions")
                        MetricItemSmall(value = "$avgScore%", label = "Avg Score")
                        MetricItemSmall(value = "${tests.size}", label = "Tests")
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = themeColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = themeColor,
                        height = 3.dp
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Chapters", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 0) themeColor else MaterialTheme.colorScheme.onSurfaceVariant) }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Tests", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 1) themeColor else MaterialTheme.colorScheme.onSurfaceVariant) }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Performance", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 2) themeColor else MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            when (selectedTab) {
                0 -> ChaptersList(subject.id, chapters, viewModel)
                1 -> TestsList(subject.id, tests, viewModel)
                2 -> SubjectAnalytics(chapters, tests)
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Subject") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Subject Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameInput.isNotBlank()) {
                        viewModel.updateSubject(subject.id, renameInput.trim(), subject.colorHex, subject.icon, subject.totalTopicsExpected)
                        Toast.makeText(context, "Subject renamed successfully", Toast.LENGTH_SHORT).show()
                        showRenameDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } }
        )
    }

    if (showIconColorDialog) {
        AlertDialog(
            onDismissRequest = { showIconColorDialog = false },
            title = { Text("Change Icon & Color") },
            text = {
                Column {
                    OutlinedTextField(
                        value = colorInput,
                        onValueChange = { colorInput = it },
                        label = { Text("Color Hex (e.g. #FF0000)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = iconInput,
                        onValueChange = { iconInput = it },
                        label = { Text("Icon Name") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateSubject(subject.id, subject.name, colorInput.trim(), iconInput.trim(), subject.totalTopicsExpected)
                    Toast.makeText(context, "Subject styling updated", Toast.LENGTH_SHORT).show()
                    showIconColorDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showIconColorDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Subject", color = Color.Red) },
            text = { Text("Are you sure you want to delete this subject? All chapters, topics, notes, and scores will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(subject.id)
                        Toast.makeText(context, "Subject deleted", Toast.LENGTH_SHORT).show()
                        showDeleteConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ChaptersList(subjectId: String, chapters: List<Chapter>, viewModel: RevisionViewModel) {
    var showAddChapter by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("All Chapters (${chapters.size})", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                }
            }
            items(items = chapters, key = { it.id }) { chap ->
                var showEditDialog by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current

                ChapterItem(
                    chapter = chap, 
                    viewModel = viewModel,
                    onEdit = { showEditDialog = true },
                    onDelete = { showDeleteDialog = true }
                )
                
                if (showEditDialog) {
                    var editName by remember { mutableStateOf(chap.name) }
                    var editImportance by remember { mutableStateOf(chap.importance) }
                    var editNotes by remember { mutableStateOf(chap.notes) }
                    var editColor by remember { mutableStateOf(chap.colorHex) }
                    var expanded by remember { mutableStateOf(false) }
                    
                    AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        title = { Text("Edit Chapter") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    label = { Text("Chapter Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box {
                                    OutlinedTextField(
                                        value = editImportance,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Difficulty/Importance") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { expanded = true }) }
                                    )
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        listOf("Low", "Medium", "High", "Very High").forEach { level ->
                                            DropdownMenuItem(
                                                text = { Text(level) },
                                                onClick = { 
                                                    editImportance = level
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editColor,
                                    onValueChange = { editColor = it },
                                    label = { Text("Chapter Color (Hex)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editNotes,
                                    onValueChange = { editNotes = it },
                                    label = { Text("Notes") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (editName.isNotBlank()) {
                                    viewModel.updateChapter(chap.copy(name = editName.trim(), importance = editImportance, notes = editNotes.trim(), colorHex = editColor.trim()))
                                    Toast.makeText(context, "Chapter updated", Toast.LENGTH_SHORT).show()
                                    showEditDialog = false
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Chapter?", color = Color.Red) },
                        text = { 
                            Text("Warning: Deleting a Chapter will also delete:\n• All Topics\n• Revision History\n• Revision Schedule\n• Tests\n• Progress\n\nThis action cannot be undone.") 
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteChapter(chap.id)
                                    Toast.makeText(context, "Chapter deleted", Toast.LENGTH_SHORT).show()
                                    showDeleteDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
        
        Button(
            onClick = { showAddChapter = true },
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Chapter")
        }
    }

    if (showAddChapter) {
        var chapterName by remember { mutableStateOf("") }
        var importance by remember { mutableStateOf("Medium") }
        var expanded by remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        AlertDialog(
            onDismissRequest = { showAddChapter = false },
            title = { Text("Add Chapter") },
            text = {
                Column {
                    OutlinedTextField(value = chapterName, onValueChange = { chapterName = it }, label = { Text("Chapter Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Importance", fontSize = 12.sp)
                    Box {
                        OutlinedTextField(
                            value = importance,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { expanded = true }) }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Low", "Medium", "High", "Very High").forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level) },
                                    onClick = { 
                                        importance = level
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (chapterName.isNotBlank()) {
                        viewModel.addChapter(subjectId, chapterName.trim(), importance)
                        Toast.makeText(context, "Chapter added", Toast.LENGTH_SHORT).show()
                        showAddChapter = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddChapter = false }) { Text("Cancel") } }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
fun ChapterItem(chapter: Chapter, viewModel: RevisionViewModel, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    val context = LocalContext.current
    var showNotesDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val view = androidx.compose.ui.platform.LocalView.current
    var isChecking by remember { mutableStateOf(false) }
    
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isChecking) 1.4f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "CheckScale"
    )

    androidx.compose.runtime.LaunchedEffect(isChecking) {
        if (isChecking) {
            kotlinx.coroutines.delay(200)
            isChecking = false
        }
    }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                    onEdit()
                    return@rememberSwipeToDismissBoxState false // Do not dismiss visually
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                    onDelete()
                    return@rememberSwipeToDismissBoxState false // Do not dismiss visually
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.3f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF3B82F6) // Blue
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444) // Red
                else -> Color.Transparent
            }
            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }

            if (color != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp) // Match roughly the card height
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .padding(horizontal = 24.dp),
                    contentAlignment = alignment
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { Toast.makeText(context, "Opening Chapter Details...", Toast.LENGTH_SHORT).show() },
                        onLongClick = { showBottomSheet = true }
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val iconColor = if (chapter.isCompleted) Color(0xFF00E676) else if (chapter.revisionCount > 0) Color(0xFFFFB300) else Color(0xFFE0E0E0)
                        Icon(
                            imageVector = if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isChecking = true
                                    if (!chapter.isCompleted) {
                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                                        android.widget.Toast.makeText(context, "🎉 Chapter Fully Revised!", android.widget.Toast.LENGTH_SHORT).show()
                                        val newRevCount = chapter.revisionCount + 1
                                        viewModel.updateChapter(chapter.copy(
                                            isCompleted = true,
                                            revisionCount = newRevCount,
                                            lastRevisionDateMs = System.currentTimeMillis()
                                        ))
                                    } else {
                                        viewModel.updateChapter(chapter.copy(isCompleted = false))
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chapter.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val importanceColor = when (chapter.importance) {
                                    "High", "Very High" -> Color(0xFFE53935)
                                    "Medium" -> Color(0xFFF57C00)
                                    else -> Color(0xFF43A047)
                                }
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(importanceColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(chapter.importance, fontSize = 10.sp, color = importanceColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Revisions: ${chapter.revisionCount}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { showNotesDialog = true }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(30.dp)) {
                            Icon(if (chapter.notes.isNotEmpty()) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (chapter.notes.isNotEmpty()) "View Notes" else "Add Notes", fontSize = 12.sp)
                        }
                        
                        TextButton(onClick = { showScoreDialog = true }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(30.dp)) {
                            val scoreText = if (chapter.scoreTotal != null && chapter.scoreTotal > 0f) {
                                "${chapter.scoreObtained?.toInt() ?: 0}/${chapter.scoreTotal.toInt()}"
                            } else {
                                "Add Score"
                            }
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(scoreText, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Edit Chapter") },
                    leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { showBottomSheet = false; onEdit() }
                )
                ListItem(
                    headlineContent = { Text("Duplicate Chapter") },
                    leadingContent = { Icon(Icons.Default.FileCopy, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showBottomSheet = false
                        viewModel.addChapter(chapter.subjectId, "${chapter.name} (Copy)", chapter.importance)
                        Toast.makeText(context, "Chapter duplicated", Toast.LENGTH_SHORT).show()
                    }
                )
                ListItem(
                    headlineContent = { Text("Archive Chapter") },
                    leadingContent = { Icon(Icons.Default.Archive, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showBottomSheet = false
                        viewModel.updateChapter(chapter.copy(isArchived = true))
                        Toast.makeText(context, "Chapter archived", Toast.LENGTH_SHORT).show()
                    }
                )
                ListItem(
                    headlineContent = { Text("Delete Chapter") },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable { showBottomSheet = false; onDelete() }
                )
            }
        }
    }

    if (showNotesDialog) {
        var editedNotes by remember { mutableStateOf(chapter.notes) }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Chapter Notes") },
            text = {
                OutlinedTextField(
                    value = editedNotes,
                    onValueChange = { editedNotes = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    label = { Text("Write your notes here...") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateChapter(chapter.copy(notes = editedNotes))
                    showNotesDialog = false
                    Toast.makeText(context, "Notes updated", Toast.LENGTH_SHORT).show()
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) { Text("Close") }
            }
        )
    }

    if (showScoreDialog) {
        var obtained by remember { mutableStateOf(chapter.scoreObtained?.toInt()?.toString() ?: "") }
        var total by remember { mutableStateOf(chapter.scoreTotal?.toInt()?.toString() ?: "100") }
        AlertDialog(
            onDismissRequest = { showScoreDialog = false },
            title = { Text("Add Mock Score") },
            text = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = obtained,
                        onValueChange = { obtained = it },
                        label = { Text("Obtained") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = total,
                        onValueChange = { total = it },
                        label = { Text("Total") },
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val obs = obtained.toFloatOrNull()
                    val tot = total.toFloatOrNull()
                    if (obs != null && tot != null && tot > 0f) {
                        viewModel.updateChapter(chapter.copy(scoreObtained = obs, scoreTotal = tot))
                        showScoreDialog = false
                        Toast.makeText(context, "Score updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showScoreDialog = false }) { Text("Cancel") }
            }
        )
    }
}
