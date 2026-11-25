package com.example.ui.screens.home
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExamCountdown
import com.example.data.TopicWithSubjectAndChapter
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.airbnb.lottie.compose.*

val DashPurplePrimary = Color(0xFF7C3AED)
val DashPurpleLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
val DashBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
val DashTextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val DashTextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: RevisionViewModel,
    onNavigateToSubjects: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTopic: (String) -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToStreak: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOfflineNotes: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    
    onLogout: () -> Unit,
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToHelpAndSupport: () -> Unit = {},
    onNavigateToFlashcards: () -> Unit = {}
) {
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle(emptyList())
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())
    val allTopicsWithDetails by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val analyticsMetrics by viewModel.analyticsMetrics.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showAddExamDialog by remember { mutableStateOf(false) }
    var examToEdit by remember { mutableStateOf<ExamCountdown?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCongratsGoalDialog by remember { mutableStateOf(false) }
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle(initialValue = 0)
    var subjectSortOption by remember { mutableStateOf("Highest Progress") }
    var sortExpanded by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "DashboardPulse")
    val circlePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circlePulse"
    )
    val cardPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardPulse"
    )

    // LaunchedEffect removed for mock data

    // Identify closest exam dynamically
    val now = remember { System.currentTimeMillis() }
    val sortedExams = remember(allExams) {
        allExams.filter { (it.examDateMs ?: Long.MAX_VALUE) >= now }.sortedBy { it.examDateMs ?: Long.MAX_VALUE } +
        allExams.filter { (it.examDateMs ?: Long.MAX_VALUE) < now }.sortedByDescending { it.examDateMs ?: Long.MAX_VALUE }
    }
    val nearestExam = sortedExams.firstOrNull()

    val subjectNearestExamMap = remember(allExams, allTopicsWithDetails) {
        val map = mutableMapOf<String, Long>()
        allTopicsWithDetails.forEach { topic ->
            if (topic.examIds.isNotEmpty()) {
                val dates = topic.examIds.mapNotNull { examId ->
                    allExams.find { it.id == examId }?.examDateMs
                }.filter { it >= now }
                if (dates.isNotEmpty()) {
                    val minDate = dates.minOrNull() ?: Long.MAX_VALUE
                    val current = map[topic.subjectName] ?: Long.MAX_VALUE
                    if (minDate < current) {
                        map[topic.subjectName] = minDate
                    }
                }
            }
        }
        map
    }

    val sortedSubjectProgress = remember(analyticsMetrics.subjectProgress, subjectSortOption, subjectNearestExamMap) {
        if (subjectSortOption == "Upcoming Deadline") {
            analyticsMetrics.subjectProgress.sortedBy { prog ->
                subjectNearestExamMap[prog.name] ?: Long.MAX_VALUE
            }
        } else {
            analyticsMetrics.subjectProgress.sortedByDescending { it.completionPercentage }
        }
    }

    if (showAddExamDialog || examToEdit != null) {
        val isEdit = examToEdit != null
        AddExamDialog(
            initialName = examToEdit?.name ?: "",
            initialHours = examToEdit?.targetStudyHours?.toString() ?: "",
            initialDateMs = examToEdit?.examDateMs,
            onDismiss = {
                showAddExamDialog = false
                examToEdit = null
            },
            onSave = { name, dateMs, hours ->
                if (isEdit) {
                    examToEdit?.copy(name = name, examDateMs = dateMs, targetStudyHours = hours)?.let { updated ->
                        viewModel.updateExam(updated)
                    }
                } else {
                    viewModel.insertExam(name, dateMs, hours)
                }
                showAddExamDialog = false
                examToEdit = null
                Toast.makeText(context, "Exam saved successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(context) {
                            onLogout()
                        }
                    },
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Logout", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.testTag("cancel_logout_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                viewModel = viewModel,
                currentRoute = "DashboardRoute",
                closeDrawer = { scope.launch { drawerState.close() } },
                onNavigate = { destination ->
                    when (destination) {
                        DrawerDestination.Home -> Unit
                        DrawerDestination.Subjects -> onNavigateToSubjects()
                        DrawerDestination.Syllabus -> onNavigateToSyllabus()
                        DrawerDestination.Focus -> onNavigateToTimer()
                        DrawerDestination.Tasks -> onNavigateToTodoList()
                        DrawerDestination.Analytics -> onNavigateToAnalytics()
                        DrawerDestination.Profile -> onNavigateToProfile()
                        DrawerDestination.Coins -> onNavigateToWallet()
                        DrawerDestination.Streak -> onNavigateToStreak()
                        DrawerDestination.Settings -> onNavigateToSettings()
                        DrawerDestination.OfflineNotes -> onNavigateToOfflineNotes()
                        DrawerDestination.Language -> onNavigateToLanguage()
                        DrawerDestination.Help -> onNavigateToHelpAndSupport()
                    }
                },
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    ) {
        Scaffold(
            containerColor = DashBackground,
            topBar = {
                TopAppBar(
                    title = {
                        val greetName = userProfile?.name?.takeIf { it.isNotBlank() }?.split(" ")?.get(0) ?: "Aspirant"
                        Column {
                            Text(
                                text = "Hi, $greetName! \uD83D\uDC4B",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = DashTextDark,
                                letterSpacing = (-0.4).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ready for today's revision?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DashTextGray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = DashTextDark, modifier = Modifier.size(20.dp))
                        }
                    },
                    actions = {
                        // Notification bell
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                .clickable {
                                    onNavigateToNotifications()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = DashTextDark, modifier = Modifier.size(20.dp))
                            if (unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(5.dp)
                                        .size(15.dp) // Slightly larger to fit text
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(46.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(DashPurplePrimary)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            DashboardProfileAvatar(
                                imageUrl = userProfile?.profilePictureUrl,
                                modifier = Modifier.fillMaxSize(),
                                iconSize = 24.dp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DashBackground,
                        scrolledContainerColor = DashBackground
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {


                // SPACED REPETITION UI ALERT SYSTEM
                item {
                    val combinedDue = metrics.todayDue + metrics.pendingRevisions
                    if (combinedDue.isNotEmpty()) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_alert")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.02f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale_pulse"
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .scale(scale)
                                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFE65100).copy(alpha = 0.2f))
                                .bounceClick { onNavigateToToday() },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)), // Warm alert color
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFCC80))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFE0B2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Alert",
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Spaced Repetition Alert", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 15.sp)
                                    val combinedDueSize = metrics.todayDue.size + metrics.pendingRevisions.size
                                    Text(
                                        "You have ${combinedDueSize} topics due for review today to maintain optimal memory retention.",
                                        color = Color(0xFFF57C00),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = "Review",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // 1. EXAM COUNTDOWN CARD
                item {
                    if (nearestExam == null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = DashPurplePrimary.copy(alpha = 0.08f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = DashPurplePrimary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No Exam Selected", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DashTextDark)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Select your target exam to start tracking preparation.", color = DashTextGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { showAddExamDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DashPurplePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Select Exam", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            PremiumNearestExamCard(
                                exam = nearestExam,
                                onEdit = { examToEdit = nearestExam },
                                onDelete = { viewModel.deleteExam(nearestExam.id) }
                            )
                        }
                    }
                }

                // 2. TODAY'S PLAN CARD
                item {
                    val combinedDue = metrics.todayDue + metrics.pendingRevisions
                    val totalTodayTopics = combinedDue.size
                    val estimatedStudyTimeMins = combinedDue.sumOf { it.expectedRevisionTime }
                    val estimatedStudyTimeStr = formatEstimatedTime(estimatedStudyTimeMins)

                    var buttonPressed by remember { mutableStateOf(false) }
                    val buttonScale by animateFloatAsState(
                        targetValue = if (buttonPressed) 1.1f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "buttonScaleAnim"
                    )

                    var isNavigating by remember { mutableStateOf(false) }
                    val startRevisionAction = {
                        if (!isNavigating) {
                            isNavigating = true
                            scope.launch {
                                buttonPressed = true
                                delay(120)
                                buttonPressed = false
                                onNavigateToToday()
                                delay(500)
                                isNavigating = false
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .shadow(
                                elevation = 14.dp,
                                shape = RoundedCornerShape(24.dp),
                                clip = false,
                                spotColor = DashPurplePrimary.copy(alpha = 0.35f),
                                ambientColor = DashPurplePrimary.copy(alpha = 0.22f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                )
                            )
                            .bounceClick { startRevisionAction() }
                    ) {
                        // Soft decorative glow for depth
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 48.dp, y = (-52).dp)
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 30.dp, y = 40.dp)
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                        )

                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Today's Plan",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.2).sp
                                    )
                                    Text(
                                        text = "Small steps, every single day",
                                        color = Color.White.copy(alpha = 0.78f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Topics chip
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.14f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "$totalTodayTopics",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Topics",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                // Time chip
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.14f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = estimatedStudyTimeStr,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Study time",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { startRevisionAction() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .scale(buttonScale),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = DashPurplePrimary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Start Revision",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2A. FLASHCARDS DUE BANNER
                item {
                    val dueCardsCount by viewModel.dueFlashcardsCount.collectAsStateWithLifecycle(0)
                    val allFlashcardsList by viewModel.allFlashcards.collectAsStateWithLifecycle(emptyList())
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clickable { onNavigateToFlashcards() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🗂️", fontSize = 20.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        "Spaced Repetition Flashcards",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        if (dueCardsCount > 0) "$dueCardsCount cards due for revision today" else "${allFlashcardsList.size} cards saved • Tap to practice",
                                        fontSize = 12.sp,
                                        color = if (dueCardsCount > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (dueCardsCount > 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Open Flashcards",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 2B. TODAY'S TO-DO LIST
                item {
                    val combinedDue = metrics.todayDue + metrics.pendingRevisions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Today's To-Do List",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DashTextDark,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        if (combinedDue.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No revisions scheduled for today.",
                                        color = DashTextGray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                val combinedDue = metrics.todayDue + metrics.pendingRevisions
                if (combinedDue.isNotEmpty()) {
                    items(combinedDue, key = { it.topicId }) { topic ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .clickable { onNavigateToTopic(topic.topicId) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DashPurplePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = topic.subjectName,
                                        fontSize = 12.sp,
                                        color = DashPurplePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " • ${topic.chapterName}",
                                        fontSize = 12.sp,
                                        color = DashTextGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = topic.topicName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DashTextDark
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = DashTextGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${topic.expectedRevisionTime} min",
                                            fontSize = 12.sp,
                                            color = DashTextGray
                                        )
                                    }
                                    
                                    Button(
                                        onClick = { onNavigateToTopic(topic.topicId) },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DashPurplePrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Start Revision", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. STUDY TOOLS SECTION
                item {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(DashPurplePrimary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = DashPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Study Tools",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = DashTextDark,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    text = "Everything you need to revise smarter",
                                    fontSize = 12.sp,
                                    color = DashTextGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "3 tools",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DashPurplePrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(DashPurplePrimary.copy(alpha = 0.10f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                StudyToolCard(
                                    icon = Icons.Default.MenuBook,
                                    title = "Syllabus",
                                    subtitle = "Track Progress",
                                    accent = Color(0xFF7C3AED),
                                    softBg = MaterialTheme.colorScheme.primaryContainer,
                                    onClick = onNavigateToSyllabus
                                )
                            }
                            item {
                                StudyToolCard(
                                    icon = Icons.Default.CalendarToday,
                                    title = "Calendar",
                                    subtitle = "Exam Schedule",
                                    accent = Color(0xFF3B82F6),
                                    softBg = Color(0xFFEAF2FF),
                                    onClick = onNavigateToCalendar
                                )
                            }
                            item {
                                StudyToolCard(
                                    icon = Icons.Default.BarChart,
                                    title = "Analytics",
                                    subtitle = "Study Reports",
                                    accent = Color(0xFFD97706),
                                    softBg = Color(0xFFFFF6E5),
                                    onClick = onNavigateToAnalytics
                                )
                            }
                        }
                    }
                }

                // 5. WEEKLY VISUALIZATION
                item {
                    WeeklyVisualizationSection(analyticsMetrics)
                }

                // 5.5. RECENT SESSIONS
                item {
                    RecentSessionsListSection(analyticsMetrics)
                }

                // 6. SUBJECT BREAKDOWN
                item {
                    SubjectBreakdownVisualizationSection(analyticsMetrics)
                }


            }
        }
    }

    if (showCongratsGoalDialog) {
        DailyGoalCongratsDialog(
            goalTarget = userProfile?.focusSettings?.dailyFocusGoalMins ?: 120,
            unitName = "minutes of focus",
            streakDays = userProfile?.currentStreak ?: 1,
            onDismiss = { showCongratsGoalDialog = false }
        )
    }
}

@Composable
fun BulletRow(
    icon: ImageVector,
    text: String,
    iconTint: Color = Color.White.copy(alpha = 0.9f),
    textColor: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DividerDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun SyllabusMetricColumn(metric: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = metric,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = DashTextDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = DashTextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StudyToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    softBg: Color,
    pendingCount: Int = 0,
    progress: Float = 0f,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val tileBg = if (isDark) accent.copy(alpha = 0.22f) else softBg
    val cardBg = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(174.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = accent.copy(alpha = 0.16f))
            .bounceClick { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (isDark) 0.25f else 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = if (isDark) 0.10f else 0.05f), Color.Transparent)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(tileBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (progress > 0f) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                color = accent,
                                trackColor = Color.Transparent,
                                strokeWidth = 3.dp
                            )
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                        if (pendingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pendingCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DashTextDark,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DashTextGray
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeDTargetCanvas() {
    Canvas(
        modifier = Modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRad = size.minDimension / 2f

        // Shadow Outer
        drawCircle(
            color = Color.Black.copy(alpha = 0.08f),
            radius = maxRad,
            center = Offset(center.x + 4f, center.y + 4f)
        )

        // Outer white ring
        drawCircle(
            color = Color.White,
            radius = maxRad,
            center = center
        )

        // Purple outer ring
        drawCircle(
            color = Color(0xFFDCD6FF),
            radius = maxRad * 0.85f,
            center = center
        )

        // White ring
        drawCircle(
            color = Color.White,
            radius = maxRad * 0.65f,
            center = center
        )

        // Magenta/Purple inner
        drawCircle(
            color = Color(0xFFA855F7),
            radius = maxRad * 0.45f,
            center = center
        )

        // White mini ring
        drawCircle(
            color = Color.White,
            radius = maxRad * 0.28f,
            center = center
        )

        // Gold Bullseye
        drawCircle(
            color = Color(0xFFFFB300),
            radius = maxRad * 0.15f,
            center = center
        )

        // Draw targets cross lines
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(center.x, center.y - maxRad),
            end = Offset(center.x, center.y + maxRad),
            strokeWidth = 3f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(center.x - maxRad, center.y),
            end = Offset(center.x + maxRad, center.y),
            strokeWidth = 3f
        )

        // 3D dart hitting center
        drawLine(
            color = Color.White,
            start = Offset(center.x - maxRad, center.y - maxRad),
            end = Offset(center.x + maxRad * 0.1f, center.y + maxRad * 0.1f),
            strokeWidth = 6f
        )

        // Red flight of arrow
        drawCircle(
            color = Color(0xFFEF4444),
            radius = 6f,
            center = Offset(center.x - maxRad * 0.95f, center.y - maxRad * 0.95f)
        )
    }
}

@Composable
fun PremiumNearestExamCard(
    exam: ExamCountdown,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(exam.examDateMs) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val diff = (exam.examDateMs ?: 0L) - currentTimeMs
    val isLive = diff > 0 && (exam.examDateMs ?: 0L) > 0L

    val days = if (isLive) (diff / (1000 * 60 * 60 * 24)).toInt() else 0
    val hours = if (isLive) ((diff / (1000 * 60 * 60)) % 24).toInt() else 0
    val minutes = if (isLive) ((diff / (1000 * 60)) % 60).toInt() else 0
    val seconds = if (isLive) ((diff / 1000) % 60).toInt() else 0

    val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val dateString = if ((exam.examDateMs ?: 0L) > 0L) df.format(Date(exam.examDateMs ?: 0L)) else "No Date Set"

    val examProgressTarget = (exam.progress / 100f).coerceIn(0.001f, 1f)
    val animatedExamProgress by animateFloatAsState(
        targetValue = examProgressTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "premium_nearest_exam_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .shadow(5.dp, RoundedCornerShape(20.dp), spotColor = DashPurplePrimary.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Logo, Name, Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primaryContainer)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(19.dp))
                            Text(
                                text = if (exam.name.length >= 3) exam.name.take(3).uppercase() else "EXM",
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = DashTextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DashTextGray, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(dateString, color = DashTextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Countdown Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 13.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(13.dp))
                            Column {
                                Text(
                                    text = String.format("%dD %02dH %02dM %02dS", days, hours, minutes, seconds),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF7C3AED)
                                )
                                Text("Remaining", color = Color(0xFF7C3AED), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("LIVE", color = Color(0xFFEF4444), fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Banner
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Overall Progress", fontSize = 12.sp, color = DashTextDark, fontWeight = FontWeight.Bold)
                        Text("${exam.progress.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = DashPurplePrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.outlineVariant)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedExamProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                    )
                                )
                        )
                    }
                }
            }

            // Menu Options on Top-Right Corner
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(13.dp)) {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = DashTextGray, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { menuExpanded = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { menuExpanded = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStatItem(label: String, valStr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valStr, fontWeight = FontWeight.Black, fontSize = 13.sp, color = DashPurplePrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = DashTextGray)
    }
}

@Composable
fun PerformanceCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.03f)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = DashTextDark.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = DashPurplePrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = DashTextGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    description: String,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .bounceClick { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = DashTextDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            fontSize = 9.sp,
            color = DashTextGray,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(
    initialName: String = "",
    initialHours: String = "",
    initialDateMs: Long? = null,
    onDismiss: () -> Unit,
    onSave: (String, Long?, Int?) -> Unit
) {
    var examName by remember { mutableStateOf(initialName) }
    var targetHours by remember { mutableStateOf(initialHours) }
    var examDateMs by remember { mutableStateOf(initialDateMs) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = examDateMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    examDateMs = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK", color = DashPurplePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = DashTextGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialName.isEmpty()) "Add Target Exam" else "Edit Exam",
                fontWeight = FontWeight.Black,
                color = DashTextDark,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name (e.g. UPSC / SSC CGL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DashPurplePrimary,
                        cursorColor = DashPurplePrimary
                    )
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = DashPurplePrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = examDateMs?.let { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Select Exam Date",
                        color = if (examDateMs != null) DashTextDark else DashTextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    label = { Text("Target Study Hours (e.g. 500)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DashPurplePrimary,
                        cursorColor = DashPurplePrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (examName.isNotBlank() && examDateMs != null) {
                        onSave(examName.trim(), examDateMs, targetHours.toIntOrNull() ?: 300)
                    } else {
                        onSave(examName.trim(), examDateMs ?: System.currentTimeMillis() + 86400000L * 30, targetHours.toIntOrNull() ?: 300)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DashPurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DashTextGray)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DashboardDrawerContent(
    viewModel: RevisionViewModel,
    closeDrawer: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToStreak: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOfflineNotes: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToHelpAndSupport: () -> Unit
) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val allUsers by viewModel.adminAllUsers.collectAsStateWithLifecycle(initialValue = emptyList())
    val userName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "Jagdish Meena"
    val streak = if (globalConfig.isStreakSystemEnabled) (userProfile?.currentStreak ?: 15) else 0
    val coins = userProfile?.totalCoins ?: 1250
    
    val currentRankInt = remember(allUsers, userProfile) {
        val realAllUsers = allUsers ?: emptyList()
        val sorted = realAllUsers.filter { !it.isBanned }.sortedByDescending { it.getOverallPerformanceScore() }
        val index = sorted.indexOfFirst { it.id == userProfile?.id }
        if (index != -1) index + 1 else null
    }
    
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(340.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFF3E8FF).copy(alpha = 0.5f),
                    radius = 300.dp.toPx(),
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    color = Color(0xFFE4DBFF).copy(alpha = 0.25f),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width, 200.dp.toPx())
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button top right
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = closeDrawer,
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(1.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DashPurplePrimary, modifier = Modifier.size(20.dp))
                    }
                }

                // Avatar with camera badge
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(110.dp).border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape))
                    Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(DashPurpleLight), contentAlignment = Alignment.Center) {
                        DashboardProfileAvatar(imageUrl = userProfile?.profilePictureUrl, modifier = Modifier.fillMaxSize(), iconSize = 40.dp)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 6.dp, end = 6.dp)
                            .size(28.dp)
                            .background(DashPurplePrimary, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(userName, fontWeight = FontWeight.Black, fontSize = 22.sp, color = DashTextDark)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (currentRankInt != null) "AIR #${String.format("%,d", currentRankInt)}" else "Unranked",
                            color = DashPurplePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Cards Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onNavigateToWallet(); closeDrawer() }) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Paid, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(String.format("%,d", coins), fontWeight = FontWeight.Black, fontSize = 20.sp, color = DashPurplePrimary)
                            Text("Coins", fontSize = 13.sp, color = DashTextGray)
                        }
                        
                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onNavigateToStreak(); closeDrawer() }) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFEDD5)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(streak.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = DashPurplePrimary)
                            Text("Day Streak", fontSize = 13.sp, color = DashTextGray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Menu Items
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "ACCOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashTextGray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    DrawerMenuItem2(icon = Icons.Default.SignalWifiOff, iconTint = Color(0xFF10B981), iconBg = Color(0xFFD1FAE5), title = "Offline Notes", subtitle = "Access notes locally", onClick = { onNavigateToOfflineNotes(); closeDrawer() })
                    DrawerMenuItem2(icon = Icons.Default.Language, iconTint = DashPurplePrimary, iconBg = MaterialTheme.colorScheme.primaryContainer, title = "Language", subtitle = "English", onClick = { onNavigateToLanguage(); closeDrawer() })
                    DrawerMenuItem2(icon = Icons.Default.HeadsetMic, iconTint = Color(0xFF3B82F6), iconBg = Color(0xFFEFF6FF), title = "Help & Support", subtitle = "Get help and support", onClick = { onNavigateToHelpAndSupport(); closeDrawer() })

                    DrawerMenuItem2(icon = Icons.Default.Settings, iconTint = Color(0xFF64748B), iconBg = Color(0xFFF1F5F9), title = "Settings", subtitle = "App preferences", onClick = { onNavigateToSettings(); closeDrawer() })

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    Text(
                        "ACCOUNT ACTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashTextGray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    DrawerMenuItem2(icon = Icons.Default.Logout, iconTint = Color(0xFFEF4444), iconBg = Color(0xFFFFE4E6), title = "Logout", subtitle = "Sign out from your account", onClick = { onLogoutClick(); closeDrawer() })
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Bottom Illustration Area
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.BottomCenter) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color(0xFFE5E0FF).copy(alpha = 0.5f),
                            topLeft = Offset(20.dp.toPx(), size.height - 50.dp.toPx()),
                            size = Size(size.width - 40.dp.toPx(), 40.dp.toPx()),
                            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFFE5E0FF).copy(alpha = 0.3f),
                            topLeft = Offset(10.dp.toPx(), size.height - 25.dp.toPx()),
                            size = Size(size.width - 20.dp.toPx(), 20.dp.toPx()),
                            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            Icon(Icons.Default.School, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(50.dp).offset(y = 10.dp))
                            Box(modifier = Modifier.width(60.dp).height(14.dp).background(Color(0xFFA855F7), RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.width(70.dp).height(18.dp).background(Color(0xFF6D28D9), RoundedCornerShape(4.dp)))
                        }
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 12.dp)) {
                            Text("Keep Learning,", fontSize = 13.sp, color = DashTextDark, fontWeight = FontWeight.Bold)
                            Text("Keep Growing!", fontSize = 16.sp, color = DashPurplePrimary, fontWeight = FontWeight.Black)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
    }
}

@Composable
fun DrawerMenuItem2(icon: ImageVector, iconTint: Color, iconBg: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = DashTextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = DashTextGray)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, iconBg: Color, iconTint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DashTextDark)
            Text(subtitle, fontSize = 11.sp, color = DashTextGray)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DashTextGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun DashboardProfileAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    if (!imageUrl.isNullOrBlank()) {
        if (imageUrl.startsWith("data:image")) {
            val bitmap = remember(imageUrl) {
                val base64String = imageUrl.substringAfter("base64,")
                try {
                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val androidBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    androidBitmap?.asImageBitmap()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = "Profile Photo",
                    modifier = modifier,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                return
            }
        } else {
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = "Profile Photo",
                modifier = modifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            return
        }
    }
    
    // Fallback Icon
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = "Profile",
        tint = Color.White,
        modifier = Modifier.size(iconSize)
    )
}

@Composable
fun WeeklyVisualizationSection(metrics: com.example.data.AnalyticsMetrics) {
    val weeklyData = metrics.weeklyActivity
    val totalSessions = weeklyData.sumOf { it.sessions }
    val activeDays = weeklyData.count { it.sessions > 0 }
    val maxSessions = (weeklyData.maxOfOrNull { it.sessions } ?: 0).coerceAtLeast(1)

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(DashPurplePrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.InsertChart, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("This week", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DashTextDark, letterSpacing = (-0.2).sp)
                Text("Sessions completed over time", fontSize = 12.sp, color = DashTextGray, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(DashPurplePrimary.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("$totalSessions sessions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DashPurplePrimary)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                if (weeklyData.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("No study data for this period", color = DashTextGray, fontSize = 14.sp)
                    }
                    return@Column
                }

                // Summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total", fontSize = 11.sp, color = DashTextGray, fontWeight = FontWeight.Medium)
                        Text("$totalSessions", fontSize = 24.sp, fontWeight = FontWeight.Black, color = DashTextDark, letterSpacing = (-0.5).sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Active days", fontSize = 11.sp, color = DashTextGray, fontWeight = FontWeight.Medium)
                        Text("$activeDays / ${weeklyData.size}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = DashPurplePrimary, letterSpacing = (-0.5).sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bar chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { day ->
                        val fraction = (day.sessions.toFloat() / maxSessions).coerceIn(0f, 1f)
                        val animated by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(700, easing = FastOutSlowInEasing),
                            label = "bar_${day.day}"
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (day.sessions > 0) {
                                Text(
                                    text = day.sessions.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DashTextGray,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .height((120.dp * animated).coerceAtLeast(if (day.sessions > 0) 6.dp else 3.dp))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                    .background(
                                        if (day.sessions > 0)
                                            Brush.verticalGradient(listOf(DashPurplePrimary, Color(0xFFA855F7)))
                                        else
                                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day.day.take(3),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day.sessions > 0) DashTextDark else DashTextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectBreakdownVisualizationSection(metrics: com.example.data.AnalyticsMetrics) {
    if (metrics.studyTimeBreakdown.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.PieChart, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("STUDY HOURS PER SUBJECT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DashTextDark)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val totalPercentage = metrics.studyTimeBreakdown.sumOf { it.percentage }.coerceAtLeast(1)

                metrics.studyTimeBreakdown.take(5).forEach { subject ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val parsedColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorCode)) }.getOrDefault(Color(0xFF3B82F6))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(parsedColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(subject.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DashTextDark)
                            }
                            Text(subject.timeStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DashTextGray)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val fract = (subject.percentage.toFloat() / totalPercentage).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fract)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(parsedColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSubjectCard(
    subjectProgress: com.example.data.SubjectProgress,
    onClick: () -> Unit
) {
    val themeColor = runCatching { Color(android.graphics.Color.parseColor(subjectProgress.colorCode)) }.getOrDefault(DashPurplePrimary)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = themeColor.copy(alpha = 0.15f))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // Determine initial or icon mapping, using string since we don't have direct icon mappings here for all cases
                if (subjectProgress.name.lowercase().contains("math")) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = themeColor)
                } else if (subjectProgress.name.lowercase().contains("sci")) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = themeColor)
                } else {
                    Text(
                        subjectProgress.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = themeColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectProgress.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DashTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Completion:", fontSize = 12.sp, color = DashTextGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${subjectProgress.completionPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { 1f },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 4.dp
                )
                AnimatedCircularProgress(
                    progress = subjectProgress.completionPercentage / 100f,
                    color = themeColor,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 4.dp
                )
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DashTextGray, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun DashboardDeadlineCard(
    exam: ExamCountdown
) {
    val dateMs = exam.examDateMs ?: System.currentTimeMillis()
    val daysLeft = ((dateMs - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    val color = if (daysLeft < 7) Color(0xFFEF4444) else if (daysLeft < 30) Color(0xFFF59E0B) else DashPurplePrimary
    val dateFormatted = java.text.SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault()).format(java.util.Date(dateMs))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = color.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = daysLeft.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = color,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Days",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DashTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = DashTextGray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateFormatted, fontSize = 12.sp, color = DashTextGray)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = if (daysLeft < 7) color else color.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}


@Composable
fun RecentSessionsListSection(metrics: com.example.data.AnalyticsMetrics) {
    if (metrics.recentSessions.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = DashPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("RECENT SESSIONS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DashTextDark)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                metrics.recentSessions.take(5).forEachIndexed { index, session ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.topicName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DashTextDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(session.completedDateMs)),
                                fontSize = 12.sp,
                                color = DashTextGray
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DashPurplePrimary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "${session.durationMs / 60000}m",
                                color = DashPurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (index < minOf(metrics.recentSessions.size, 5) - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}
