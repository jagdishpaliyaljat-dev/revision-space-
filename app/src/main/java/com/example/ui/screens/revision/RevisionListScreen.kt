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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import com.example.viewmodel.RevisionViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionListScreen(viewModel: RevisionViewModel, navController: NavController, initialFilter: String = "Today", onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    var searchQuery by remember { mutableStateOf("") }
    var showTopicExplosion by remember { mutableStateOf(false) }
    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised!") }
    var selectedFilter by remember { mutableStateOf(initialFilter) } // Today, Upcoming, Overdue, Completed
    
    var selectedSubjectFilter by remember { mutableStateOf("All Subjects") }
    var selectedDifficultyFilter by remember { mutableStateOf("All Levels") }
    
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showDifficultyDropdown by remember { mutableStateOf(false) }
    
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle()
    
    val focusRequester = remember { FocusRequester() }
    
    val subjects = remember(allTopics) { 
        listOf("All Subjects") + allTopics.map { it.subjectName }.distinct().sorted() 
    }
    val difficulties = listOf("All Levels", "Easy", "Medium", "Hard")
        
    var listToShow by remember { mutableStateOf<List<com.example.data.TopicWithSubjectAndChapter>>(emptyList()) }
    
    LaunchedEffect(selectedFilter, searchQuery, metrics, allTopics, selectedSubjectFilter, selectedDifficultyFilter) {
        if (searchQuery.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
        }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val baseList = when (selectedFilter) {
                "Today" -> metrics.todayDue
                "Pending" -> metrics.pendingRevisions
                "Upcoming" -> metrics.upcomingRevisions
                "Overdue" -> metrics.pendingRevisions.filter { it.status == "Overdue" }
                "Completed" -> metrics.completedTotal
                else -> metrics.todayDue
            }
            val trimmedQuery = searchQuery.trim()
            var filtered = if (trimmedQuery.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.topicName.contains(trimmedQuery, ignoreCase = true) ||
                    it.chapterName.contains(trimmedQuery, ignoreCase = true) ||
                    it.subjectName.contains(trimmedQuery, ignoreCase = true)
                }
            }
            
            if (selectedSubjectFilter != "All Subjects") {
                filtered = filtered.filter { it.subjectName == selectedSubjectFilter }
            }
            if (selectedDifficultyFilter != "All Levels") {
                filtered = filtered.filter { it.difficulty.equals(selectedDifficultyFilter, ignoreCase = true) }
            }
            listToShow = filtered
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Revisions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            "Track every scheduled recall",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(19.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(RevBgLight)) {
            // Search & Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search revisions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            focusRequester.requestFocus()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Pending", "Upcoming", "Overdue", "Completed").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = RevPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedFilter == filter) RevPrimary else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
            
            // Secondary filters
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subject Filter
                Box {
                    FilterChip(
                        selected = selectedSubjectFilter != "All Subjects",
                        onClick = { showSubjectDropdown = true },
                        label = { Text(selectedSubjectFilter) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDBEAFE),
                            selectedLabelColor = RevPrimary
                        )
                    )
                    DropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    selectedSubjectFilter = subject
                                    showSubjectDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Difficulty Filter
                Box {
                    FilterChip(
                        selected = selectedDifficultyFilter != "All Levels",
                        onClick = { showDifficultyDropdown = true },
                        label = { Text(selectedDifficultyFilter) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFCE7F3),
                            selectedLabelColor = Color(0xFFDB2777)
                        )
                    )
                    DropdownMenu(
                        expanded = showDifficultyDropdown,
                        onDismissRequest = { showDifficultyDropdown = false }
                    ) {
                        difficulties.forEach { diff ->
                            DropdownMenuItem(
                                text = { Text(diff) },
                                onClick = {
                                    selectedDifficultyFilter = diff
                                    showDifficultyDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            if (listToShow.isEmpty()) {
                SearchEmptyState(onClearSearch = {
                    searchQuery = ""
                    focusRequester.requestFocus()
                })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(listToShow, key = { _, it -> it.topicId }) { index, topic ->
                        var isInitiallyVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(topic.topicId) {
                            kotlinx.coroutines.delay(index * 40L)
                            isInitiallyVisible = true
                        }
                        AnimatedVisibility(
                            visible = isInitiallyVisible,
                            enter = fadeIn(animationSpec = tween(durationMillis = 350)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
                                    )
                        ) {
                            RevisionItemCard(
                                topic = topic,
                                onClick = {
                                    navController.navigate(TopicDetailRoute(topic.topicId))
                                },
                                onStart = {
                                    navController.navigate(TimerRoute(topic.topicId, true))
                                },
                                onComplete = {
                                    completedTopicTitle = "${topic.topicName.take(24)} Marked as Revised!"
                                    showTopicExplosion = true
                                    viewModel.recordTimerSession(topic.topicId, true, topic.estimatedRevisionTimeMins * 60 * 1000L)
                                }
                            )
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
}

@Composable
fun SearchEmptyState(onClearSearch: () -> Unit) {
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
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No Results Found",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = RevTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No revisions match your search or filters.",
                fontSize = 14.sp,
                color = RevTextGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(containerColor = RevPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Clear Search",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }

}
}

@Composable
fun RevisionItemCard(topic: com.example.data.TopicWithSubjectAndChapter, onClick: () -> Unit, onStart: () -> Unit, onComplete: () -> Unit) {
    var isCompleting by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current
    
    val progress by animateFloatAsState(
        targetValue = if (isCompleting) 1f else if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) tween(durationMillis = 800, easing = LinearEasing) else tween(durationMillis = 300),
        label = "HoldProgress"
    )
    
    val cardScale by animateFloatAsState(
        targetValue = if (isCompleting) 1.05f else if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )
    
    LaunchedEffect(progress) {
        if (progress >= 1f && !isCompleting) {
            isCompleting = true
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            kotlinx.coroutines.delay(400)
            onComplete()
        }
    }
    
    AnimatedVisibility(
        visible = !isCompleting,
        exit = shrinkVertically(animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().scale(cardScale),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onClick() }
                    ) {
                        if (topic.subjectName.isNotBlank() && topic.chapterName.isNotBlank()) {
                            Text(
                                text = "${topic.subjectName} • ${topic.chapterName}",
                                fontSize = 12.sp,
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(topic.topicName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RevTextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rev #${topic.revisionCount} • ${formatEstimatedTime(topic.estimatedRevisionTimeMins)}", fontSize = 12.sp, color = RevTextGray)
                    }
                    var cardMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { cardMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = RevTextGray)
                        }
                        DropdownMenu(
                            expanded = cardMenuExpanded,
                            onDismissRequest = { cardMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Details") },
                                onClick = {
                                    cardMenuExpanded = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Start Timer") },
                                onClick = {
                                    cardMenuExpanded = false
                                    onStart()
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onStart, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }
                    
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF10B981),
                            trackColor = Color.Transparent,
                            strokeWidth = 3.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = if (isCompleting || progress == 1f) 1f else 0.1f))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isPressed = true
                                            tryAwaitRelease()
                                            isPressed = false
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = "Hold to Complete", 
                                tint = if (isCompleting || progress == 1f) Color.White else Color(0xFF10B981), 
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

}
}
