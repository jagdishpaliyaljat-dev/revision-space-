package com.example.ui.screens.profile
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
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Revision
import com.example.viewmodel.RevisionViewModel
import java.text.SimpleDateFormat
import java.util.*

private val CalPurple = Color(0xFF6C4DFF)
private val CalBg: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val CalTextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val CalTextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: RevisionViewModel,
    navController: NavController
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val allRevisions = metrics.allRevisions
    val context = LocalContext.current
    var showSuccessEffect by remember { mutableStateOf(false) }
    var overdueRevisionToComplete by remember { mutableStateOf<Revision?>(null) }
    var showImpactAnalysisDialog by remember { mutableStateOf<Revision?>(null) }

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    
    // We want to jump to today easily
    val today = remember { Calendar.getInstance() }
    val todayDay = today.get(Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(Calendar.MONTH)
    val todayYear = today.get(Calendar.YEAR)

    // selected day
    var selectedDay by remember { mutableIntStateOf(todayDay) }

    // If month changes, we might need to clamp selectedDay if it's out of bounds, but for now we can just let it be or select 1st.
    // Let's set selectedDay to 1 when changing month if we just want a simple UX, or keep it if valid.
    
    fun getDaysInMonth(month: Int, year: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    fun getStartOffset(month: Int, year: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Monday = 2. If we want Monday to be index 0:
        // dayOfWeek 2 (Mon) -> 0
        // dayOfWeek 3 (Tue) -> 1
        // dayOfWeek 1 (Sun) -> 6
        return if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    }

    val daysInMonthList = (1..getDaysInMonth(currentMonth, currentYear)).toList()
    val startOffset = getStartOffset(currentMonth, currentYear)

    // Group revisions by Date (Year, Month, Day)
    // We only map nextRevisionDateMs. Wait, if it's completed, should it still show on its completed date or next date?
    // Let's map everything based on nextRevisionDateMs.
    val revisionsByDate = remember(allRevisions, currentMonth, currentYear) {
        val map = mutableMapOf<Int, MutableList<Revision>>()
        for (rev in allRevisions) {
            val nextCal = Calendar.getInstance().apply { timeInMillis = rev.nextRevisionDateMs }
            val nextYear = nextCal.get(Calendar.YEAR)
            val nextMonth = nextCal.get(Calendar.MONTH)
            val nextDay = nextCal.get(Calendar.DAY_OF_MONTH)
            
            if (nextYear == currentYear && nextMonth == currentMonth) {
                map.getOrPut(nextDay) { mutableListOf() }.add(rev)
            }
            
            if (rev.lastRevisionDateMs > 0) {
                val lastCal = Calendar.getInstance().apply { timeInMillis = rev.lastRevisionDateMs }
                val lastYear = lastCal.get(Calendar.YEAR)
                val lastMonth = lastCal.get(Calendar.MONTH)
                val lastDay = lastCal.get(Calendar.DAY_OF_MONTH)
                
                if (lastYear == currentYear && lastMonth == currentMonth) {
                    if (lastYear != nextYear || lastMonth != nextMonth || lastDay != nextDay) {
                        val completedCopy = rev.copy(status = "Completed")
                        map.getOrPut(lastDay) { mutableListOf() }.add(completedCopy)
                    }
                }
            }
        }
        map
    }

    val selectedEvents = revisionsByDate[selectedDay] ?: emptyList()

    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            "Study Calendar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = CalTextDark
                        )
                        Text(
                            "Your revision rhythm",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CalTextDark.copy(alpha = 0.6f)
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
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = CalTextDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        currentMonth = todayMonth
                        currentYear = todayYear
                        selectedDay = todayDay
                    }) {
                        Text("Today", color = CalPurple, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = CalBg
                )
            )
        },
        containerColor = CalBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Month Header Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear -= 1
                    } else {
                        currentMonth -= 1
                    }
                    selectedDay = 1
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = CalTextDark)
                }
                Text(
                    text = "${monthNames[currentMonth]} $currentYear",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = CalTextDark
                )
                IconButton(onClick = {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear += 1
                    } else {
                        currentMonth += 1
                    }
                    selectedDay = 1
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = CalTextDark)
                }
            }

            // Days of the week row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontWeight = FontWeight.Bold,
                        color = CalTextGray,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid 5x7
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add empty boxes for offset
                items(startOffset) {
                    Box(modifier = Modifier.size(34.dp))
                }

                items(daysInMonthList) { day ->
                    val isSelected = day == selectedDay
                    val eventsForDay = revisionsByDate[day] ?: emptyList()
                    val hasEvents = eventsForDay.isNotEmpty()
                    val isToday = (day == todayDay && currentMonth == todayMonth && currentYear == todayYear)

                    // Event Status logic for indicator color
                    // Completed -> Green, Pending -> Orange, Overdue -> Red, Today -> Blue
                    var indicatorColor = CalPurple
                    if (hasEvents) {
                        val anyOverdue = eventsForDay.any { it.status != "Completed" && it.nextRevisionDateMs < startOfToday && !isToday }
                        val anyPending = eventsForDay.any { it.status != "Completed" && !anyOverdue }
                        val allCompleted = eventsForDay.all { it.status == "Completed" }

                        indicatorColor = when {
                            isToday -> Color(0xFF3B82F6) // Blue
                            allCompleted -> Color(0xFF22C55E) // Green
                            anyOverdue -> Color(0xFFEF4444) // Red
                            anyPending -> Color(0xFFF97316) // Orange
                            else -> CalPurple
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> indicatorColor
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDay = day }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(
                                text = day.toString(),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || hasEvents || isToday) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    isSelected -> Color.White
                                    isToday && !isSelected -> Color(0xFF3B82F6)
                                    hasEvents -> CalTextDark
                                    else -> CalTextDark
                                }
                            )
                            if (hasEvents && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(indicatorColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scheduled Sessions Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.EventNote,
                    contentDescription = null,
                    tint = CalPurple,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sessions Scheduled - ${monthNames[currentMonth]} $selectedDay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CalTextDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedEvents.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(selectedEvents, key = { it.id }) { rev ->
                        var showEditDialog by remember { mutableStateOf(false) }
                        var showRescheduleDialog by remember { mutableStateOf(false) }

                        val isCompleted = rev.status == "Completed" || rev.status == "Complete"
                        val eventColor = if (isCompleted) Color(0xFF22C55E) else if (rev.nextRevisionDateMs < startOfToday && (selectedDay != todayDay || currentMonth != todayMonth || currentYear != todayYear)) Color(0xFFEF4444) else Color(0xFFF97316)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(4.dp)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(eventColor)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${rev.subjectName} > ${rev.chapterName}",
                                        color = CalTextGray,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rev.topicName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CalTextDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = CalTextGray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${rev.duration} mins", color = CalTextGray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(eventColor.copy(alpha = 0.1f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (isCompleted) "Completed" else "Pending", color = eventColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                // Actions
                                Row {
                                    if (!isCompleted) {
                                        IconButton(onClick = { 
                                            if (rev.nextRevisionDateMs < startOfToday) {
                                                overdueRevisionToComplete = rev
                                            } else {
                                                AudioFeedback.playCompletionSound(context)
                                                showSuccessEffect = true
                                                viewModel.markRevisionComplete(rev.id)
                                                Toast.makeText(context, "Marked complete", Toast.LENGTH_SHORT).show()
                                            }
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = {
                                            navController.navigate(com.example.ui.TimerRoute(rev.topicId, true))
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = CalPurple, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Box {
                                        var menuExpanded by remember { mutableStateOf(false) }
                                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Options", tint = CalTextGray, modifier = Modifier.size(18.dp))
                                        }
                                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Reschedule") },
                                                onClick = { menuExpanded = false; showRescheduleDialog = true }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Edit Details") },
                                                onClick = { menuExpanded = false; showEditDialog = true }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete", color = Color.Red) },
                                                onClick = { 
                                                    menuExpanded = false
                                                    viewModel.deleteRevision(context, rev.id)
                                                    Toast.makeText(context, "Revision deleted", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Edit Dialog
                        if (showEditDialog) {
                            var editNotes by remember { mutableStateOf(rev.notes) }
                            AlertDialog(
                                onDismissRequest = { showEditDialog = false },
                                title = { Text("Edit Revision") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        viewModel.updateRevision(context, rev.copy(notes = editNotes))
                                        showEditDialog = false
                                        Toast.makeText(context, "Revision updated", Toast.LENGTH_SHORT).show()
                                    }) { Text("Save") }
                                },
                                dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
                            )
                        }

                        // Reschedule Dialog
                        if (showRescheduleDialog) {
                            // simple +1 day, +3 days, +7 days for reschedule
                            AlertDialog(
                                onDismissRequest = { showRescheduleDialog = false },
                                title = { Text("Reschedule Revision") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            val newTime = rev.nextRevisionDateMs + 86400000L // +1 day
                                            viewModel.updateRevision(context, rev.copy(nextRevisionDateMs = newTime))
                                            showRescheduleDialog = false
                                            Toast.makeText(context, "Rescheduled to Tomorrow", Toast.LENGTH_SHORT).show()
                                        }, modifier = Modifier.fillMaxWidth()) { Text("Tomorrow (+1 Day)") }
                                        Button(onClick = {
                                            val newTime = rev.nextRevisionDateMs + (3L * 86400000L) // +3 days
                                            viewModel.updateRevision(context, rev.copy(nextRevisionDateMs = newTime))
                                            showRescheduleDialog = false
                                            Toast.makeText(context, "Rescheduled by 3 Days", Toast.LENGTH_SHORT).show()
                                        }, modifier = Modifier.fillMaxWidth()) { Text("+3 Days") }
                                        Button(onClick = {
                                            val newTime = rev.nextRevisionDateMs + (7L * 86400000L) // +7 days
                                            viewModel.updateRevision(context, rev.copy(nextRevisionDateMs = newTime))
                                            showRescheduleDialog = false
                                            Toast.makeText(context, "Rescheduled by 7 Days", Toast.LENGTH_SHORT).show()
                                        }, modifier = Modifier.fillMaxWidth()) { Text("+7 Days") }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showRescheduleDialog = false }) { Text("Cancel") } }
                            )
                        }

                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Event,
                            contentDescription = null,
                            tint = CalTextGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No revisions scheduled",
                            fontWeight = FontWeight.Bold,
                            color = CalTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Select another date or add revisions in subject details.",
                            color = CalTextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    }

    if (showSuccessEffect) {
        CheckmarkExplosionEffect(
            modifier = Modifier.fillMaxSize(),
            onAnimationEnd = { showSuccessEffect = false }
        )
    }

    overdueRevisionToComplete?.let { rev ->
        AlertDialog(
            onDismissRequest = { overdueRevisionToComplete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Overdue Task Completed", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    "This task is overdue. Would you like to review the cumulative revision impact for your next scheduled session?",
                    color = CalTextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val temp = overdueRevisionToComplete
                        overdueRevisionToComplete = null
                        showImpactAnalysisDialog = temp
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CalPurple)
                ) {
                    Text("Review Impact")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val targetId = rev.id
                            overdueRevisionToComplete = null
                            AudioFeedback.playCompletionSound(context)
                            showSuccessEffect = true
                            viewModel.markRevisionComplete(targetId)
                            Toast.makeText(context, "Revision saved!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Just Complete", color = CalPurple)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { overdueRevisionToComplete = null }) {
                        Text("Cancel", color = CalTextGray)
                    }
                }
            }
        )
    }

    showImpactAnalysisDialog?.let { rev ->
        AlertDialog(
            onDismissRequest = { showImpactAnalysisDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cumulative Revision Impact", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Because this session is completed late, your learning system will dynamically calibrate to maintain high retention:",
                        color = CalTextGray,
                        fontSize = 13.sp
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Next Spacing Adjustment", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CalTextDark)
                                Text("+1 Day Shift", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                "Next session shifted outward to prevent mental fatigue and accommodate delayed active recall.",
                                fontSize = 11.sp,
                                color = CalTextGray
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Recall Calibration", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CalTextDark)
                                Text("Optimized (92%)", color = CalPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                "Slightly shorter subsequent interval scheduled to stabilize memory retention.",
                                fontSize = 11.sp,
                                color = CalTextGray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = rev.id
                        showImpactAnalysisDialog = null
                        AudioFeedback.playCompletionSound(context)
                        showSuccessEffect = true
                        viewModel.markRevisionComplete(targetId)
                        Toast.makeText(context, "Revision completed with adjusted impact!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Confirm & Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImpactAnalysisDialog = null }) {
                    Text("Cancel", color = CalTextGray)
                }
            }
        )
    }
}
