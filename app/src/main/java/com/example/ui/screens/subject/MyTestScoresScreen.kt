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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Subject
import com.example.data.SubjectTest
import com.example.viewmodel.RevisionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTestScoresScreen(
    viewModel: RevisionViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val tests by viewModel.allSubjectTests.collectAsStateWithLifecycle(initialValue = emptyList())
    val subjects by viewModel.allSubjects.collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var testToEdit by remember { mutableStateOf<SubjectTest?>(null) }
    var selectedFilterSubjectId by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = ProfileBgLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Test Scores",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = ProfileTextDark
                        )
                        Text(
                            text = "Track your mock progress",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ProfileTextGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ProfileTextDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(ProfilePurple, CircleShape)
                            .testTag("add_test_score_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Score",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfileBgLight)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("test_scores_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Test Center Title
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text(
                        text = "Test Center",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfilePurple,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Performance & All Tests",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = ProfileTextDark
                    )
                }
            }

            // Summary Analytics Section
            if (tests.isNotEmpty()) {
                item {
                    TestPerformanceSummaryCard(tests = tests)
                }

                item {
                    SubjectWisePerformanceCard(tests = tests, subjects = subjects)
                }
            } else {
                item {
                    NoTestsEmptyState(onAddClick = { showAddDialog = true })
                }
            }

            // Test History Section Header & Subject Filter Tabs
            if (tests.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Test History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ProfileTextDark
                        )
                        
                        // Filter Scrollable Row
                        Box(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterTabItem(
                                        label = "All",
                                        isSelected = selectedFilterSubjectId == "All",
                                        onClick = { selectedFilterSubjectId = "All" }
                                    )
                                }
                                items(subjects) { subject ->
                                    FilterTabItem(
                                        label = subject.name,
                                        isSelected = selectedFilterSubjectId == subject.id,
                                        onClick = { selectedFilterSubjectId = subject.id }
                                    )
                                }
                            }
                        }
                    }
                }

                // Filtered List of Tests
                val filteredTests = if (selectedFilterSubjectId == "All") {
                    tests.sortedByDescending { it.dateMs }
                } else {
                    tests.filter { it.subjectId == selectedFilterSubjectId }.sortedByDescending { it.dateMs }
                }

                if (filteredTests.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tests submitted for this subject yet.",
                                    color = ProfileTextGray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTests, key = { it.id }) { test ->
                        val relatedSubject = subjects.find { it.id == test.subjectId }
                        TestHistoryItem(
                            test = test,
                            subject = relatedSubject,
                            onEdit = { testToEdit = test },
                            onDelete = {
                                viewModel.deleteSubjectTest(test.id)
                                Toast.makeText(context, "Test deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Test Score Dialog
    if (showAddDialog) {
        AddEditTestScoreDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onSave = { subjectId, testName, dateMs, marksObtained, totalMarks, remarks ->
                viewModel.addSubjectTest(subjectId, testName, dateMs, marksObtained, totalMarks, remarks)
                showAddDialog = false
                Toast.makeText(context, "Test score added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Test Score Dialog
    if (testToEdit != null) {
        AddEditTestScoreDialog(
            subjects = subjects,
            existingTest = testToEdit,
            onDismiss = { testToEdit = null },
            onSave = { subjectId, testName, dateMs, marksObtained, totalMarks, remarks ->
                val updatedTest = testToEdit!!.copy(
                    subjectId = subjectId,
                    testName = testName,
                    dateMs = dateMs,
                    marksObtained = marksObtained,
                    totalMarks = totalMarks,
                    remarks = remarks
                )
                viewModel.updateSubjectTest(updatedTest)
                testToEdit = null
                Toast.makeText(context, "Test score updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun TestPerformanceSummaryCard(tests: List<SubjectTest>) {
    val totalTests = tests.size
    val averageScore = if (totalTests > 0) {
        val totalMarksObtained = tests.sumOf { it.marksObtained.toDouble() }
        val totalMaxMarks = tests.sumOf { it.totalMarks.toDouble() }
        if (totalMaxMarks > 0) (totalMarksObtained / totalMaxMarks) * 100 else 0.0
    } else 0.0

    val highestScore = if (totalTests > 0) {
        tests.maxOf { (it.marksObtained / it.totalMarks) * 100 }
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ProfilePurpleSuperLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "Scores Summary",
                        tint = ProfilePurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Overall Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ProfileTextDark
                    )
                    Text(
                        text = "Based on $totalTests submitted tests",
                        fontSize = 12.sp,
                        color = ProfileTextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Average Score Metric
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", averageScore),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = ProfilePurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Avg Percentage",
                        fontSize = 11.sp,
                        color = ProfileTextGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterVertically)
                )

                // High Score Metric
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", highestScore),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = TrendGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Highest Score",
                        fontSize = 11.sp,
                        color = ProfileTextGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterVertically)
                )

                // Total Tests Metric
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalTests.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = ProfileTextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Tests",
                        fontSize = 11.sp,
                        color = ProfileTextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectWisePerformanceCard(tests: List<SubjectTest>, subjects: List<Subject>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Subject-wise Performance",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ProfileTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Average score per subject",
                fontSize = 12.sp,
                color = ProfileTextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            val subjectMap = subjects.associateBy { it.id }
            val testsBySubject = tests.groupBy { it.subjectId }

            val performanceList = testsBySubject.map { (subjectId, testList) ->
                val subjectName = subjectMap[subjectId]?.name ?: "Unknown Subject"
                val colorHex = subjectMap[subjectId]?.colorHex ?: "#6B4EFF"
                val subjectColor = try {
                    Color(android.graphics.Color.parseColor(colorHex))
                } catch (e: Exception) {
                    ProfilePurple
                }
                
                val totalObtained = testList.sumOf { it.marksObtained.toDouble() }
                val totalMax = testList.sumOf { it.totalMarks.toDouble() }
                val avgPercentage = if (totalMax > 0) (totalObtained / totalMax) * 100 else 0.0

                SubjectPerformanceItem(
                    name = subjectName,
                    percentage = avgPercentage,
                    color = subjectColor,
                    testCount = testList.size
                )
            }.sortedByDescending { it.percentage }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                performanceList.forEach { perf ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(perf.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = perf.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfileTextDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${perf.testCount} ${if (perf.testCount == 1) "test" else "tests"})",
                                    fontSize = 11.sp,
                                    color = ProfileTextGray
                                )
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", perf.percentage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileTextDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (perf.percentage / 100.0).toFloat().coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(perf.color)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SubjectPerformanceItem(
    val name: String,
    val percentage: Double,
    val color: Color,
    val testCount: Int
)

@Composable
fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ProfilePurple else Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else ProfileTextGray,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun TestHistoryItem(
    test: SubjectTest,
    subject: Subject?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = if (test.totalMarks > 0) (test.marksObtained / test.totalMarks) * 100 else 0f
    val ratingColor = when {
        percentage >= 75f -> TrendGreen
        percentage >= 40f -> TrendOrange
        else -> Color(0xFFEF4444)
    }

    val subjectName = subject?.name ?: "Unknown Subject"
    val colorHex = subject?.colorHex ?: "#6B4EFF"
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        ProfilePurple
    }

    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(subjectColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = subjectName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = test.testName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ProfileTextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(test.dateMs)),
                        fontSize = 12.sp,
                        color = ProfileTextGray
                    )
                }

                // Percentage Badge & Action Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ratingColor.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f%%", percentage),
                            color = ratingColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = ProfileTextGray
                            )
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Score") },
                                onClick = {
                                    expandedMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Score", color = Color.Red) },
                                onClick = {
                                    expandedMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MARKS OBTAINED",
                        fontSize = 10.sp,
                        color = ProfileTextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${test.marksObtained.toInt()} / ${test.totalMarks.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileTextDark
                    )
                }

                if (test.remarks.isNotEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "REMARKS",
                            fontSize = 10.sp,
                            color = ProfileTextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = test.remarks,
                            fontSize = 13.sp,
                            color = ProfileTextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoTestsEmptyState(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ProfilePurpleSuperLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = ProfilePurple,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Test Scores Yet",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ProfileTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Keep track of your academic performance by submitting your test scores and percentages.",
                fontSize = 13.sp,
                color = ProfileTextGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = ProfilePurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Test Score")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTestScoreDialog(
    subjects: List<Subject>,
    existingTest: SubjectTest? = null,
    onDismiss: () -> Unit,
    onSave: (subjectId: String, testName: String, dateMs: Long, marksObtained: Float, totalMarks: Float, remarks: String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.find { it.id == existingTest?.subjectId } ?: subjects.firstOrNull()) }
    var testName by remember { mutableStateOf(existingTest?.testName ?: "") }
    var marksObtainedStr by remember { mutableStateOf(existingTest?.marksObtained?.toInt()?.toString() ?: "") }
    var totalMarksStr by remember { mutableStateOf(existingTest?.totalMarks?.toInt()?.toString() ?: "") }
    var remarks by remember { mutableStateOf(existingTest?.remarks ?: "") }
    var testDateMs by remember { mutableStateOf(existingTest?.dateMs ?: System.currentTimeMillis()) }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingTest == null) "Submit Test Score" else "Edit Test Score",
                fontWeight = FontWeight.Bold,
                color = ProfileTextDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject Dropdown Selector
                if (subjects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.name ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubject = subject
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Please add subjects first in your Syllabus section.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                // Test Name Input
                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("Test Name / Topic") },
                    placeholder = { Text("e.g., Weekly Mock Test 3, Term Exam") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Marks Obtained Input
                    OutlinedTextField(
                        value = marksObtainedStr,
                        onValueChange = { marksObtainedStr = it },
                        label = { Text("Obtained") },
                        placeholder = { Text("Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                    )

                    // Total Marks Input
                    OutlinedTextField(
                        value = totalMarksStr,
                        onValueChange = { totalMarksStr = it },
                        label = { Text("Total Marks") },
                        placeholder = { Text("Max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                    )
                }

                // Remarks Input
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks (Optional)") },
                    placeholder = { Text("e.g., Strong in algebra, need practice in geometry") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sub = selectedSubject
                    if (sub == null) {
                        return@Button
                    }
                    if (testName.isBlank()) {
                        return@Button
                    }
                    val marksObtained = marksObtainedStr.toFloatOrNull() ?: -1f
                    val totalMarks = totalMarksStr.toFloatOrNull() ?: -1f
                    if (marksObtained < 0f || totalMarks <= 0f || marksObtained > totalMarks) {
                        return@Button
                    }
                    onSave(sub.id, testName, testDateMs, marksObtained, totalMarks, remarks)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProfilePurple),
                enabled = selectedSubject != null && testName.isNotBlank() &&
                        (marksObtainedStr.toFloatOrNull() ?: -1f) >= 0f &&
                        (totalMarksStr.toFloatOrNull() ?: -1f) > 0f &&
                        (marksObtainedStr.toFloatOrNull() ?: -1f) <= (totalMarksStr.toFloatOrNull() ?: -1f)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ProfileTextGray)
            ) {
                Text("Cancel")
            }
        }
    )
}
