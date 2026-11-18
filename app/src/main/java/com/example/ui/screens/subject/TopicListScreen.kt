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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.toRoute
import com.example.viewmodel.RevisionViewModel

@Composable
fun TopicListScreenWrapper(viewModel: RevisionViewModel, navController: NavController, backStackEntry: NavBackStackEntry) {
    val route = backStackEntry.toRoute<TopicListRoute>()
    TopicListScreen(viewModel, navController, route.chapterId, route.chapterName)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicListScreen(viewModel: RevisionViewModel, navController: NavController, chapterId: String, chapterName: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle()
    val chapterTopics = allTopics.filter { it.chapterId == chapterId }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialogFromList by remember { mutableStateOf(false) }
    var showDeleteDialogFromList by remember { mutableStateOf(false) }
    var editingTopic by remember { mutableStateOf<com.example.data.TopicWithSubjectAndChapter?>(null) }
    var deletingTopic by remember { mutableStateOf<com.example.data.TopicWithSubjectAndChapter?>(null) }

    Scaffold(
        containerColor = Color(0xFFF6F7FB),
        topBar = {
            TopAppBar(
                title = { Text(chapterName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Topic")
            }
        }
    ) { padding ->
        if (chapterTopics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No topics yet. Tap + to add one.", color = Color(0xFF5B6478))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(chapterTopics, key = { _, it -> it.topicId }) { index, topic ->
                    var showTopicMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(TopicDetailRoute(topic.topicId)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F3F9)),
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
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF4F46E5)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Topic Name Only
                            Text(
                                text = topic.topicName,
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
                                    onClick = { showTopicMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Topic Options",
                                        tint = Color(0xFF8A94A6)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showTopicMenu,
                                    onDismissRequest = { showTopicMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Topic") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4F46E5)) },
                                        onClick = {
                                            showTopicMenu = false
                                            editingTopic = topic
                                            showEditDialogFromList = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Topic", color = Color.Red) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                        onClick = {
                                            showTopicMenu = false
                                            deletingTopic = topic
                                            showDeleteDialogFromList = true
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
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var tagsInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Topic") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Topic Name") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.addTopic(chapterId, name, notes, tagsList)
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialogFromList) {
        editingTopic?.let { topic ->
            var name by remember { mutableStateOf(topic.topicName) }
            var notes by remember { mutableStateOf(topic.notes) }
            var tagsInput by remember { mutableStateOf(topic.tags.joinToString(", ")) }
            var difficulty by remember { mutableStateOf(topic.difficulty) }
            var timeInput by remember { mutableStateOf(topic.estimatedRevisionTimeMins.toString()) }

            AlertDialog(
                onDismissRequest = { showEditDialogFromList = false },
                title = { Text("Edit Topic") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Topic Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = timeInput,
                            onValueChange = { timeInput = it },
                            label = { Text("Estimated Revision Time (mins)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Column {
                            Text("Difficulty Level", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Easy", "Medium", "Hard").forEach { diff ->
                                    val isSelected = difficulty.lowercase(java.util.Locale.getDefault()) == diff.lowercase(java.util.Locale.getDefault())
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { difficulty = diff },
                                        label = { Text(diff) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = tagsInput,
                            onValueChange = { tagsInput = it },
                            label = { Text("Tags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty()) {
                            val finalTime = timeInput.toIntOrNull() ?: topic.estimatedRevisionTimeMins
                            val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            viewModel.updateTopicDetails(
                                topicId = topic.topicId,
                                name = trimmedName,
                                difficulty = difficulty,
                                estimatedRevisionTimeMins = finalTime,
                                notes = notes.trim(),
                                tags = tagsList
                            )
                            showEditDialogFromList = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialogFromList = false }) { Text("Cancel") }
                }
            )
        }
    }

    if (showDeleteDialogFromList) {
        deletingTopic?.let { topic ->
            AlertDialog(
                onDismissRequest = { showDeleteDialogFromList = false },
                title = { Text("Delete Topic", color = Color.Red) },
                text = { Text("Are you sure you want to delete \"${topic.topicName}\"? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTopic(context, topic.topicId)
                            showDeleteDialogFromList = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialogFromList = false }) { Text("Cancel") }
                }
            )
        }
    }
}
