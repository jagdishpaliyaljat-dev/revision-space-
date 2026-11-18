@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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


import androidx.compose.material3.ExperimentalMaterial3Api

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.RevisionViewModel
import com.example.data.Subject
import com.example.data.Chapter
import com.example.data.Revision
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.*

// Styling Color Constants
val ProfilePurple = Color(0xFF6C4DFF)
val ProfilePurpleLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
val ProfilePurpleSuperLight: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer
val ProfileBgLight: Color
    @Composable get() = MaterialTheme.colorScheme.background
val ProfileTextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val ProfileTextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

// Sparkline trends colors
val TrendGreen = Color(0xFF10B981)
val TrendOrange = Color(0xFFF59E0B)
val TrendBlue = Color(0xFF3B82F6)

@Composable
fun ProfileScreen(viewModel: RevisionViewModel, navController: NavController, openAchievements: Boolean = false) {
    val context = LocalContext.current
    var showAchievementsDialog by remember { mutableStateOf(openAchievements) }
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle()
    val logs by viewModel.allStudyLogs.collectAsStateWithLifecycle()
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle(emptyList())
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle(emptyList())
    val allRevisions by viewModel.allRevisions.collectAsStateWithLifecycle(emptyList())
    val profileStats by viewModel.profileStats.collectAsStateWithLifecycle()
    val referralsList by viewModel.referrals.collectAsStateWithLifecycle()

    val safeProfile = userProfile
    if (safeProfile == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .shimmerBackground()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerBackground()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerBackground()
            )
            Spacer(modifier = Modifier.height(24.dp))
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerBackground()
                )
            }
        }
        return
    }

    
    val currentRankInt = safeProfile.rankPosition ?: 999
    val myCoins = safeProfile.getOverallPerformanceScore() ?: 0
    val targetCoins = myCoins + 100 // Example logic
    val rankProgress = 0.5f
    val displayNextRank = if (currentRankInt > 1) currentRankInt - 1 else 1


    // Dialog state controllers
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showExamGoalDialog by remember { mutableStateOf(false) }
        var mockDarkModeEnabled by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showPhotoBottomSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var previewUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingAction by remember { mutableStateOf<String?>(null) }
    
    val sharedPrefs = remember(context) { context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE) }
    var showSavedTopicsDialog by remember { mutableStateOf(false) }
    var bookmarkedIdsSet by remember(allTopics) {
        mutableStateOf(allTopics.filter { sharedPrefs.getBoolean("topic_bookmark_${it.topicId}", false) }.map { it.topicId }.toSet())
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            previewUri = uri
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                previewUri = uri
            }
        } else {
            Toast.makeText(context, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }

        if (pendingAction == "camera") {
            if (cameraGranted) {
                try {
                    val uri = getTempImageUri(context)
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    Toast.makeText(context, "Camera not available: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Camera permission denied. Cannot take photo.", Toast.LENGTH_SHORT).show()
            }
        } else if (pendingAction == "gallery") {
            galleryLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        pendingAction = null
    }

    // Achievements system tab selection
    var selectedAchievementTab by remember { mutableStateOf("Study Hours") }

    // Dynamic stats derived from logs/DB with robust fallback
    val displayCoins = safeProfile.totalCoins ?: 0
    val displayStreak = if (globalConfig.isStreakSystemEnabled) (safeProfile.currentStreak ?: 0) else 0
    
    
    val displayStudyHours = profileStats.displayStudyHours
    val displayStudyTimeStr = profileStats.displayStudyTimeStr
    val displayRevisions = profileStats.displayRevisions
    val completedChaptersCount = profileStats.completedChaptersCount
    val displayMastery = profileStats.displayMastery

    // Modal Field drafts
    var nameField by remember { mutableStateOf("") }
    var bioField by remember { mutableStateOf("") }
    var emailField by remember { mutableStateOf("") }
    var phoneField by remember { mutableStateOf("") }
    
    val allExams by viewModel.allExams.collectAsStateWithLifecycle(initialValue = emptyList())
    val nearestExam = remember(allExams) {
        val now = System.currentTimeMillis()
        allExams.filter { (it.examDateMs ?: Long.MAX_VALUE) >= now }.minByOrNull { it.examDateMs ?: Long.MAX_VALUE }
    }
    
    var examGoalNameField by remember { mutableStateOf("") }
    var examGoalDateField by remember { mutableStateOf("") }
    
    LaunchedEffect(nearestExam) {
        if (nearestExam != null) {
            examGoalNameField = nearestExam.name
            examGoalDateField = nearestExam.examDateMs?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: ""
        } else {
            examGoalNameField = ""
            examGoalDateField = ""
        }
    }

    val daysLeft = remember(examGoalDateField) {
        if (examGoalDateField.isEmpty()) return@remember 0L
        try {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val targetDate = format.parse(examGoalDateField)
            if (targetDate != null) {
                val diff = targetDate.time - System.currentTimeMillis()
                (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    val maxDays = 365L // Arbitrary reference point for the circle visual
    val sweepAngle = if (daysLeft == 0L) 0f else ((maxDays - daysLeft.toFloat()) / maxDays * 260f).coerceIn(10f, 260f)

    // Sync input fields with current database profile
    LaunchedEffect(userProfile) {
        safeProfile.let {
            nameField = it.name.ifBlank { "User" }
            bioField = it.bio
            emailField = it.email.ifBlank { "" }
            phoneField = it.phoneNumber.ifBlank { "" }
        } ?: run {
            nameField = ""
            bioField = ""
            emailField = ""
            phoneField = ""
        }
    }

    

    Scaffold(
        containerColor = ProfileBgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate(com.example.ui.SettingsRoute) },
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(42.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = ProfileTextDark, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    // Coin pill
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .height(38.dp)
                            .clickable { navController.navigate(CoinHistoryRoute) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD700)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("₹", color = ProfileTextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%,d", displayCoins),
                                color = ProfileTextDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(ProfilePurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ProfileBgLight)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 0. Stats Hero (new)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileHeroStat(Icons.Default.LocalFireDepartment, Color(0xFFF97316), Color(0xFFFFEDD5), "Streak", "${displayStreak} days")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        ProfileHeroStat(Icons.Default.Paid, Color(0xFFF59E0B), Color(0xFFFEF3C7), "Coins", String.format("%,d", displayCoins))
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        ProfileHeroStat(Icons.Default.Timer, Color(0xFF3B82F6), Color(0xFFEFF6FF), "Study Time", displayStudyTimeStr)
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        ProfileHeroStat(Icons.Default.FactCheck, Color(0xFF10B981), Color(0xFFECFDF5), "Revisions", "$displayRevisions")
                    }
                }
            }

            // 1. Main Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(0.dp, RoundedCornerShape(24.dp)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (userProfile == null && nearestExam == null) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.PersonOutline, contentDescription = null, tint = ProfileTextGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Welcome!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ProfileTextDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Complete your profile setup.", color = ProfileTextGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { showExamGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ProfilePurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Select Target Exam")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        // Avatar Section
                        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(end = 16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { showPhotoBottomSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = ProfilePurple,
                                        strokeWidth = 2.dp
                                    )
                                } else if (userProfile != null && (!safeProfile.profileImageUrl.isNullOrBlank() || !safeProfile.profilePictureUrl.isNullOrBlank())) {
                                    val url = if (!safeProfile.profileImageUrl.isNullOrBlank()) safeProfile.profileImageUrl ?: "" else safeProfile.profilePictureUrl ?: ""
                                    if (url.startsWith("data:image")) {
                                        val bitmap = remember(url) {
                                            val base64String = url.substringAfter("base64,")
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
                                                modifier = Modifier.size(100.dp).clip(CircleShape),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(100.dp).clip(CircleShape).background(ProfilePurpleLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = "Profile App Icon", tint = ProfilePurple, modifier = Modifier.size(50.dp))
                                            }
                                        }
                                    } else {
                                        coil.compose.AsyncImage(
                                            model = url,
                                            contentDescription = "Profile Photo",
                                            modifier = Modifier.size(100.dp).clip(CircleShape),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.size(100.dp).clip(CircleShape).background(ProfilePurpleLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = "Profile App Icon", tint = ProfilePurple, modifier = Modifier.size(50.dp))
                                    }
                                }
                            }
                            // Edit Camera Badges
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = 4.dp, y = 4.dp) // Offset
                                    .clip(CircleShape)
                                    .background(ProfilePurple)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { showPhotoBottomSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Change Profile Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Middle Info Section
                        Column(modifier = Modifier.weight(1f)) {
                            val glowTransition = rememberInfiniteTransition(label = "glowTransition")
                            val glowScale by glowTransition.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 1.8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "glowScale"
                            )
                            val glowAlpha by glowTransition.animateFloat(
                                initialValue = 0.45f,
                                targetValue = 0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "glowAlpha"
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nameField,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ProfileTextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .scale(glowScale)
                                            .clip(CircleShape)
                                            .background(ProfilePurple.copy(alpha = glowAlpha))
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = ProfilePurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { showEditProfileDialog = true }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = ProfileTextGray, modifier = Modifier.size(14.dp))
                                }
                            }
                            
                            if (bioField.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bioField,
                                    fontSize = 13.sp,
                                    color = ProfileTextGray,
                                    lineHeight = 16.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (examGoalNameField.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(ProfilePurpleSuperLight)
                                        .border(BorderStroke(1.dp, ProfilePurpleLight.copy(alpha = 0.5f)), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = ProfilePurple,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = examGoalNameField,
                                            color = ProfilePurple,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Right Circular Progress Info
                        if (examGoalNameField.isNotBlank() && daysLeft > 0L) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawArc(
                                            color = Color.White,
                                            startAngle = -220f,
                                            sweepAngle = 260f,
                                            useCenter = false,
                                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        )
                                        drawArc(
                                            brush = Brush.horizontalGradient(listOf(ProfilePurple, Color(0xFFE11D48))),
                                            startAngle = -220f,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "$daysLeft", color = ProfileTextDark, fontWeight = FontWeight.Black, fontSize = 24.sp)
                                        Text(text = "Days Left", color = ProfileTextGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                    // Flame icon at top center-ish
                                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp), contentAlignment = Alignment.TopCenter) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = "Fire", tint = Color(0xFFF97316), modifier = Modifier.size(24.dp).offset(y = (-8).dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(
                                icon = Icons.Outlined.MenuBook,
                                iconBg = ProfilePurpleSuperLight,
                                iconTint = ProfilePurple,
                                title = "Chapters\nCompleted",
                                value = "$completedChaptersCount",
                                onClick = { navController.navigate(com.example.ui.SubjectsRoute) }
                            )
                            ProfileStatItem(
                                icon = Icons.Default.Refresh,
                                iconBg = Color(0xFFECFDF5),
                                iconTint = TrendGreen,
                                title = "Revisions\nDone",
                                value = "$displayRevisions",
                                onClick = { navController.navigate(com.example.ui.TodayRevisionRoute("today_revisions")) }
                            )
                            ProfileStatItem(
                                icon = Icons.Outlined.WatchLater,
                                iconBg = Color(0xFFEFF6FF),
                                iconTint = TrendBlue,
                                title = "Study\nTime",
                                value = displayStudyTimeStr,
                                onClick = { navController.navigate(com.example.ui.AnalyticsRoute) }
                            )
                            ProfileStatItem(
                                icon = Icons.Outlined.TrackChanges,
                                iconBg = Color(0xFFFFFBEB),
                                iconTint = TrendOrange,
                                title = "Syllabus\nCovered",
                                value = "${displayMastery}%",
                                onClick = { navController.navigate(com.example.ui.SyllabusRoute) }
                            )
                        }
                    }
                }
            }


            // 5. Quick Actions Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ProfileTextDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProfileQuickAction(Modifier.weight(1f), icon = Icons.Default.MenuBook, iconTint = ProfilePurple, iconBg = ProfilePurpleSuperLight, label = "Syllabus", onClick = { navController.navigate(SyllabusRoute) })
                            ProfileQuickAction(Modifier.weight(1f), icon = Icons.Default.EventAvailable, iconTint = TrendGreen, iconBg = Color(0xFFECFDF5), label = "Revision\nPlanner", onClick = { navController.navigate(CalendarRoute) })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProfileQuickAction(Modifier.weight(1f).testTag("my_test_scores_button"), icon = Icons.Default.Leaderboard, iconTint = TrendOrange, iconBg = Color(0xFFFFFBEB), label = "My Test\nScores", onClick = {
                                navController.navigate(MyTestScoresRoute)
                            })
                            ProfileQuickAction(Modifier.weight(1f), icon = Icons.Default.BarChart, iconTint = TrendBlue, iconBg = Color(0xFFEFF6FF), label = "Analytics", onClick = { navController.navigate(AnalyticsRoute) })
                        }
                    }
                }
            }


        }
    }

    // 5. Beautiful Edit Profile Modal Popup Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(nameField) }
        var tempBio by remember { mutableStateOf(bioField) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    "Edit Profile Details", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 18.sp, 
                    color = ProfileTextDark
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                    )
                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text("Bio") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        nameField = tempName
                        bioField = tempBio
                        
                        // Push immediately to DB profile!
                        safeProfile.let {
                            viewModel.updateUserProfile(
                                it.copy(
                                    name = tempName,
                                    bio = tempBio
                                )
                            )
                        }
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfilePurple)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditProfileDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ProfileTextGray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPhotoBottomSheet) {
        PhotoOptionsBottomSheet(
            onDismiss = { showPhotoBottomSheet = false },
            onTakePhotoClick = {
                pendingAction = "camera"
                val permissions = mutableListOf(android.Manifest.permission.CAMERA)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            },
            onChooseGalleryClick = {
                pendingAction = "gallery"
                val permissions = mutableListOf<String>()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            },
            onRemovePhotoClick = {
                isUploading = true
                viewModel.removeProfileImage(
                    onSuccess = {
                        isUploading = false
                        Toast.makeText(context, "Profile photo removed successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        isUploading = false
                        Toast.makeText(context, "Error removing photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            showRemoveOption = userProfile != null && (!safeProfile.profileImageUrl.isNullOrBlank() || !safeProfile.profilePictureUrl.isNullOrBlank())
        )
    }

    if (previewUri != null) {
        PreviewAndCropDialog(
            imageUri = previewUri,
            onDismiss = { previewUri = null },
            onConfirm = { bitmap ->
                previewUri = null
                isUploading = true
                val bytes = cropAndCompressProfileImage(bitmap)
                viewModel.uploadProfileImageBytes(
                    bytes = bytes,
                    onSuccess = { _ ->
                        isUploading = false
                        Toast.makeText(context, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        isUploading = false
                        Toast.makeText(context, "Error uploading photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // 6. Beautiful Exam & Goal Tracker Modal Popup Dialog
    if (showExamGoalDialog) {
        var tempExamName by remember { mutableStateOf(examGoalNameField) }
        var tempExamDate by remember { mutableStateOf(examGoalDateField) }

        AlertDialog(
            onDismissRequest = { showExamGoalDialog = false },
            title = {
                Text(
                    "Set Exam & Goal", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 18.sp, 
                    color = ProfileTextDark
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = tempExamName,
                        onValueChange = { tempExamName = it },
                        label = { Text("Target Exam Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                    )
                    OutlinedTextField(
                        value = tempExamDate,
                        onValueChange = { tempExamDate = it },
                        label = { Text("Exam Date (e.g., 15 Dec 2025)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProfilePurple)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        examGoalNameField = tempExamName
                        examGoalDateField = tempExamDate
                        showExamGoalDialog = false
                        Toast.makeText(context, "Goal updated to $tempExamName", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfilePurple)
                ) {
                    Text("Update Goal")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExamGoalDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ProfileTextGray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 7. Saved Topics Dialog
    if (showSavedTopicsDialog) {
        AlertDialog(
            modifier = Modifier.testTag("saved_topics_dialog"),
            onDismissRequest = { showSavedTopicsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Topics (${bookmarkedIdsSet.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showSavedTopicsDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                val bookmarkedList = remember(allTopics, bookmarkedIdsSet) {
                    allTopics.filter { bookmarkedIdsSet.contains(it.topicId) }
                }
                if (bookmarkedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No saved topics found",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Bookmark topics to find them here.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bookmarkedList, key = { it.topicId }) { topic ->
                            val topicColor = remember(topic.subjectColorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(topic.subjectColorHex))
                                } catch (e: Exception) {
                                    Color(0xFF6C4DFF)
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSavedTopicsDialog = false
                                        navController.navigate(TopicDetailRoute(topic.topicId, topic.subjectId, topic.chapterId))
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                topicColor.copy(alpha = 0.12f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = topic.subjectName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = topicColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topic.topicName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${topic.subjectName} • ${topic.chapterName}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val newSet = bookmarkedIdsSet.toMutableSet()
                                            newSet.remove(topic.topicId)
                                            bookmarkedIdsSet = newSet
                                            sharedPrefs.edit().putBoolean("topic_bookmark_${topic.topicId}", false).apply()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Unsave",
                                            tint = TrendOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showAchievementsDialog) {
        AlertDialog(
            onDismissRequest = { showAchievementsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Your Achievements", fontWeight = FontWeight.Bold, color = ProfileTextDark)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Track your milestones and unlock rewards as you study and revise consistently.",
                        fontSize = 12.sp,
                        color = ProfileTextGray
                    )
                    
                    // Study Hours Achievement
                    Column {
                        Text("Study Hours Milestone", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ProfileTextDark)
                        Text("Current: ${displayStudyHours} hrs", fontSize = 12.sp, color = ProfileTextGray)
                        AchievementsTimeline(
                            milestones = listOf(0, 10, 50, 100, 500),
                            currentValue = displayStudyHours.toInt(),
                            unit = "h",
                            completedColor = ProfilePurple,
                            lockedColor = Color(0xFF8A94A6)
                        )
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    
                    // Revisions Achievement
                    Column {
                        Text("Revisions Completed", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ProfileTextDark)
                        Text("Current: $displayRevisions", fontSize = 12.sp, color = ProfileTextGray)
                        AchievementsTimeline(
                            milestones = listOf(0, 10, 50, 100, 250),
                            currentValue = displayRevisions.toInt(),
                            unit = "revs",
                            completedColor = TrendGreen,
                            lockedColor = Color(0xFF8A94A6)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Coins Milestone
                    Column {
                        Text("Coins Earned", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ProfileTextDark)
                        Text("Current: $displayCoins", fontSize = 12.sp, color = ProfileTextGray)
                        AchievementsTimeline(
                            milestones = listOf(0, 100, 500, 1000, 5000),
                            currentValue = displayCoins.toInt(),
                            unit = "coins",
                            completedColor = TrendOrange,
                            lockedColor = Color(0xFF8A94A6)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAchievementsDialog = false }) {
                    Text("Done", color = ProfilePurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Sparkline Stat Components
@Composable
fun StatisticSparklineCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    title: String,
    trendData: List<Float>,
    trendColor: Color
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon layout
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = ProfileTextDark,
                maxLines = 1
            )
            
            Text(
                text = title,
                fontSize = 10.sp,
                color = ProfileTextGray,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Canvas Cubic Bezier Sparkline
            SparklineGraph(
                points = trendData,
                color = trendColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )
        }
    }
}

@Composable
fun SparklineGraph(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (points.size < 2) return@Canvas

        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * (width / (points.size - 1))
            val y = height - ((value - minVal) / range) * (height * 0.7f) - (height * 0.15f)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * (width / (points.size - 1))
                val prevY = height - ((points[index - 1] - minVal) / range) * (height * 0.7f) - (height * 0.15f)
                
                val controlX1 = prevX + (x - prevX) / 2
                val controlY1 = prevY
                val controlX2 = prevX + (x - prevX) / 2
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Beautiful under-trend vertical glow gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.25f), Color.Transparent)
            )
        )

        // Bold trend stroke line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun AchievementsTimeline(
    milestones: List<Int>,
    currentValue: Int,
    unit: String,
    completedColor: Color,
    lockedColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Track line
        Canvas(modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(4.dp)) {
            val width = size.width
            val height = size.height
            
            // Draw background track
            drawLine(
                color = Color(0xFFE7EAF3),
                start = androidx.compose.ui.geometry.Offset(0f, height / 2),
                end = androidx.compose.ui.geometry.Offset(width, height / 2),
                strokeWidth = 3.dp.toPx()
            )
            
            // Draw progress colored active track
            val completedCount = milestones.count { currentValue >= it }
            if (completedCount > 0) {
                val stepWidth = width / (milestones.size - 1)
                val fullSegments = completedCount - 1
                val partialFactor = if (completedCount < milestones.size) {
                    val prevMilestone = milestones[completedCount - 1]
                    val nextMilestone = milestones[completedCount]
                    ((currentValue - prevMilestone).toFloat() / (nextMilestone - prevMilestone).toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                val endX = stepWidth * fullSegments + stepWidth * partialFactor
                
                drawLine(
                    color = completedColor,
                    start = androidx.compose.ui.geometry.Offset(0f, height / 2),
                    end = androidx.compose.ui.geometry.Offset(endX, height / 2),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Timeline check/lock nodes with verified indicators
        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            milestones.forEach { valStep ->
                val isUnlocked = currentValue >= valStep
                val nodeColor = if (isUnlocked) completedColor else lockedColor
                
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) completedColor else Color.White)
                        .border(1.5.dp, if (isUnlocked) Color.Transparent else MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Achieved",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF8A94A6),
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }
        }
    }
}



// Settings list item component
@Composable
fun ProfileSettingRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = ProfileTextDark
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = ProfileTextGray
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = ProfileTextGray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ProfileStatItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stat_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(value, color = ProfileTextDark, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, color = ProfileTextGray, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

@Composable
fun ProfileQuickAction(modifier: Modifier = Modifier, icon: ImageVector, iconTint: Color, iconBg: Color, label: String, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = iconBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(36.dp))
            Text(label, color = ProfileTextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun ProfileHeroStat(icon: ImageVector, tint: Color, bg: Color, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 13.sp, color = ProfileTextDark)
        Text(label, fontSize = 10.sp, color = ProfileTextGray, fontWeight = FontWeight.Medium)
    }
}

fun getTempImageUri(context: android.content.Context): android.net.Uri {
    val cacheDir = context.cacheDir
    val tempFile = java.io.File.createTempFile("temp_avatar_", ".jpg", cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

fun loadBitmapFromUri(context: android.content.Context, uri: android.net.Uri): android.graphics.Bitmap? {
    return try {
        val loaded = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        
        if (loaded != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && loaded.config == android.graphics.Bitmap.Config.HARDWARE) {
            loaded.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        } else {
            loaded
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun cropAndCompressProfileImage(bitmap: android.graphics.Bitmap): ByteArray {
    // Prevent hardware bitmap manipulation errors
    val safeBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && bitmap.config == android.graphics.Bitmap.Config.HARDWARE) {
        bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
    } else {
        bitmap
    }
    val width = safeBitmap.width
    val height = safeBitmap.height
    val squareSize = if (width > height) height else width
    val x = (width - squareSize) / 2
    val y = (height - squareSize) / 2
    val squareBitmap = android.graphics.Bitmap.createBitmap(safeBitmap, x, y, squareSize, squareSize)
    val targetSize = 300
    val scaledBitmap = if (squareSize > targetSize) {
        android.graphics.Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
    } else {
        squareBitmap
    }
    val baos = java.io.ByteArrayOutputStream()
    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
    return baos.toByteArray()
}

@Composable
fun PhotoOptionsBottomSheet(
    onDismiss: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onChooseGalleryClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    showRemoveOption: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Update Profile Photo",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = ProfileTextDark,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            ProfileSettingRow(
                icon = Icons.Default.CameraAlt,
                iconBg = MaterialTheme.colorScheme.outlineVariant,
                iconTint = ProfilePurple,
                title = "Take Photo",
                onClick = {
                    onDismiss()
                    onTakePhotoClick()
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            ProfileSettingRow(
                icon = Icons.Default.Image,
                iconBg = MaterialTheme.colorScheme.outlineVariant,
                iconTint = ProfilePurple,
                title = "Choose from Gallery",
                onClick = {
                    onDismiss()
                    onChooseGalleryClick()
                }
            )
            
            if (showRemoveOption) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                ProfileSettingRow(
                    icon = Icons.Default.Delete,
                    iconBg = Color(0xFFFFE4E6),
                    iconTint = Color(0xFFEF4444),
                    title = "Remove Photo",
                    onClick = {
                        onDismiss()
                        onRemovePhotoClick()
                    }
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            ProfileSettingRow(
                icon = Icons.Default.Close,
                iconBg = MaterialTheme.colorScheme.outlineVariant,
                iconTint = ProfileTextGray,
                title = "Cancel",
                onClick = onDismiss
            )
        }
    }
}

@Composable
fun PreviewAndCropDialog(
    imageUri: android.net.Uri?,
    onDismiss: () -> Unit,
    onConfirm: (android.graphics.Bitmap) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            bitmap = loadBitmapFromUri(context, imageUri)
        }
    }
    
    if (bitmap != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Crop & Preview Photo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ProfileTextDark
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Adjust and preview your new profile picture:",
                        fontSize = 13.sp,
                        color = ProfileTextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(3.dp, ProfilePurple, CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This will be center-cropped to a perfect circle.",
                        fontSize = 11.sp,
                        color = ProfileTextGray,
                        fontWeight = FontWeight.Light
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        bitmap?.let { onConfirm(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfilePurple)
                ) {
                    Text("Confirm & Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = ProfileTextGray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ReferralSection(
    referralCode: String,
    totalReferrals: Int,
    totalEarnings: Int,
    referralsList: List<com.example.data.ReferralRecord>,
    context: Context
) {
    val successfulReferrals = referralsList.count { it.status == "COMPLETED" }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Refer & Earn", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Referrals", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("$totalReferrals", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text("Successful", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("$successfulReferrals", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                }
                Column {
                    Text("Coins Earned", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("$totalEarnings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF59E0B))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("My Referral Code", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = referralCode,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF6C4DFF),
                    letterSpacing = 2.sp
                )
                Row {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Referral Code", referralCode)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Referral code copied!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF6C4DFF))
                    }
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join Revision Space")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Elevate your study schedules and earn cash in Revision Space! Sign up using my referral code '$referralCode' to get Free Coins! Download now!"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Referral Link"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF6C4DFF))
                    }
                }
            }
        }
    }
}

