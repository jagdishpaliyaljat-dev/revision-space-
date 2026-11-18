package com.example.ui.screens.rewards
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.StudyLog
import com.example.viewmodel.RevisionViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailsScreen(viewModel: RevisionViewModel, navController: NavController) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val logs by viewModel.allStudyLogs.collectAsStateWithLifecycle()
    
    val currentStreak = if (globalConfig.isStreakSystemEnabled) (userProfile?.currentStreak ?: 0) else 0
    val longestStreak = if (globalConfig.isStreakSystemEnabled) (userProfile?.longestStreak ?: 0) else 0
    
    // Core data preparation
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    
    val historyDays = remember(logs) {
        val list = mutableListOf<StreakDayInfo>()
        val cal = Calendar.getInstance()
        
        // Loop through the past 28 days
        for (i in 27 downTo 0) {
            val dateCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = sdf.format(dateCal.time)
            val log = logs.find { it.dateString == dateStr }
            val studyTimeMs = log?.studyTimeMs ?: 0L
            val revisionTimeMs = log?.revisionTimeMs ?: 0L
            val totalTimeMs = studyTimeMs + revisionTimeMs
            
            val status = when {
                totalTimeMs > 0L -> "STUDIED"
                // Categorize some empty days as MISSED and some as NO_ACTIVITY for display variation
                (dateCal.get(Calendar.DAY_OF_WEEK) in listOf(Calendar.TUESDAY, Calendar.THURSDAY)) && dateCal.timeInMillis < System.currentTimeMillis() - 86400000L -> "MISSED"
                else -> "NO_ACTIVITY"
            }
            
            list.add(
                StreakDayInfo(
                    dateString = dateStr,
                    formattedDate = displayFormat.format(dateCal.time),
                    studyTimeMins = (studyTimeMs / 60000L).toInt(),
                    revisionTimeMins = (revisionTimeMs / 60000L).toInt(),
                    status = status
                )
            )
        }
        list
    }
    
    val totalStudyDays = remember(logs) { logs.count { it.studyTimeMs > 0 } }
    val totalRevisionDays = remember(logs) { logs.count { it.revisionTimeMs > 0 } }
    
    val studiedDaysCount = historyDays.count { it.status == "STUDIED" }
    val consistencyPercentage = if (historyDays.isNotEmpty()) {
        (studiedDaysCount * 100) / historyDays.size
    } else 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Details", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("streak_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E1B4B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F7FB))
            )
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        var selectedCategory by remember { mutableStateOf("All") }
        var selectedBadge by remember { mutableStateOf<Badge?>(null) }
        val badges = remember(userProfile) { getBadges(userProfile) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats grid card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("streak_stats_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEF2F6))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Streak Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1B4B))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Current Streak
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFEF2F2)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(currentStreak.toString(), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E1B4B))
                            Text("Current Streak", fontSize = 11.sp, color = Color(0xFF5B6478))
                        }
                        
                        // Longest Streak
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFFBEB)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(longestStreak.toString(), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E1B4B))
                            Text("Longest Streak", fontSize = 11.sp, color = Color(0xFF5B6478))
                        }
                        
                        // Consistency
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFECFDF5)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$consistencyPercentage%", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E1B4B))
                            Text("Consistency %", fontSize = 11.sp, color = Color(0xFF5B6478))
                        }
                    }
                    
                    HorizontalDivider(color = Color(0xFFF1F3F9))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Studied Days: $totalStudyDays", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1B4B))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF6B4EFF)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Revision Days: $totalRevisionDays", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1B4B))
                        }
                    }
                }
            }

            // Milestone Badges section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_badges_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEF2F6))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Milestone Badges",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Earn medals as you make progress",
                                fontSize = 11.sp,
                                color = Color(0xFF5B6478)
                            )
                        }
                        
                        val unlockedCount = badges.count { it.isUnlocked }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2F6))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$unlockedCount / ${badges.size} Unlocked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                    
                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("All", "Streak", "Revision", "Time")
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F3F9))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF5B6478)
                                )
                            }
                        }
                    }
                    
                    val filteredBadges = remember(selectedCategory, badges) {
                        if (selectedCategory == "All") badges else badges.filter { it.category == selectedCategory }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filteredBadges.chunked(3).forEach { badgeRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                badgeRow.forEach { badge ->
                                    BadgeItem(
                                        badge = badge,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedBadge = badge }
                                    )
                                }
                                repeat(3 - badgeRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            
            // Heatmap calendar grid title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "28-Day Streak History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1B4B)
                )
                
                // Indicators Legend
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(Color(0xFFECFDF5), Color(0xFF10B981), "Studied")
                    LegendItem(Color(0xFFFEF2F2), Color(0xFFEF4444), "Missed")
                    LegendItem(Color(0xFFF1F3F9), Color(0xFF8A94A6), "No Act")
                }
            }
            
            // Grid layout (re-implemented using custom rows of days to prevent nested-scroll conflicts)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("streak_heatmap_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEF2F6))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    historyDays.chunked(7).forEach { weekDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            weekDays.forEach { day ->
                                val backgroundColor = when (day.status) {
                                    "STUDIED" -> Color(0xFFD1FAE5)
                                    "MISSED" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFF1F3F9)
                                }
                                val tintColor = when (day.status) {
                                    "STUDIED" -> Color(0xFF065F46)
                                    "MISSED" -> Color(0xFF991B1B)
                                    else -> Color(0xFF5B6478)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.0f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = day.formattedDate.split(" ")[0],
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = tintColor
                                        )
                                        Text(
                                            text = day.formattedDate.split(" ")[1],
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = tintColor.copy(alpha = 0.8f)
                                        )
                                        if (day.status == "STUDIED") {
                                            Text(
                                                text = "${day.studyTimeMins}m",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                }
                            }
                            // Fill remaining row slots just in case
                            repeat(7 - weekDays.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Badge details popup dialog
        selectedBadge?.let { badge ->
            AlertDialog(
                onDismissRequest = { selectedBadge = null },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    if (badge.isUnlocked) {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFE9D5FF), Color(0xFFF3E8FF))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFE7EAF3), Color(0xFFF1F3F9))
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = badge.iconEmoji, fontSize = 40.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = badge.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = badge.description,
                            fontSize = 14.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (badge.isUnlocked) Color(0xFFDCFCE7) else Color(0xFFF1F3F9)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (badge.isUnlocked) "🏆 Unlocked Milestone" else "🔒 Locked Milestone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) Color(0xFF15803D) else Color(0xFF5B6478)
                            )
                        }

                        // Progress details
                        val progress = (badge.currentValue.toFloat() / badge.targetValue.toFloat()).coerceIn(0f, 1f)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progress",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5B6478)
                                )
                                Text(
                                    text = "${badge.currentValue} / ${badge.targetValue} ${badge.valueSuffix}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = if (badge.isUnlocked) Color(0xFF8B5CF6) else Color(0xFF8A94A6),
                                trackColor = Color(0xFFE7EAF3)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedBadge = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Cool!")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun BadgeItem(
    badge: Badge,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale = if (badge.isUnlocked) 1.0f else 0.95f
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .testTag("badge_item_${badge.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) Color(0xFFFAF5FF) else Color(0xFFF6F7FB)
        ),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) Color(0xFFE9D5FF) else Color(0xFFE7EAF3)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE9D5FF), Color(0xFFF3E8FF))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE7EAF3), Color(0xFFF1F3F9))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badge.isUnlocked) {
                    Text(text = badge.iconEmoji, fontSize = 24.sp)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badge.iconEmoji,
                            fontSize = 24.sp,
                            modifier = Modifier.graphicsLayer(alpha = 0.4f)
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF5B6478),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Text(
                text = badge.title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (badge.isUnlocked) Color(0xFF581C87) else Color(0xFF475569),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE9D5FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "UNLOCKED",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF581C87)
                    )
                }
            } else {
                val progress = (badge.currentValue.toFloat() / badge.targetValue.toFloat()).coerceIn(0f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Color(0xFF8A94A6),
                        trackColor = Color(0xFFE7EAF3)
                    )
                    Text(
                        text = "${badge.currentValue}/${badge.targetValue}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B6478)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(bg: Color, text: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(bg))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = text, fontWeight = FontWeight.Bold)
    }
}

data class StreakDayInfo(
    val dateString: String,
    val formattedDate: String,
    val studyTimeMins: Int,
    val revisionTimeMins: Int,
    val status: String
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String,
    val isUnlocked: Boolean,
    val currentValue: Int,
    val targetValue: Int,
    val valueSuffix: String = ""
)

fun getBadges(profile: com.example.data.UserProfile?): List<Badge> {
    if (profile == null) return emptyList()
    
    return listOf(
        Badge(
            id = "streak_1",
            title = "Daily Spark",
            description = "Start your study journey with a 1-day streak",
            iconEmoji = "🔥",
            category = "Streak",
            isUnlocked = profile.longestStreak >= 1,
            currentValue = profile.longestStreak,
            targetValue = 1,
            valueSuffix = "day"
        ),
        Badge(
            id = "streak_7",
            title = "Weekly Warrior",
            description = "Maintain a study streak for 7 days",
            iconEmoji = "⚡",
            category = "Streak",
            isUnlocked = profile.longestStreak >= 7,
            currentValue = profile.longestStreak,
            targetValue = 7,
            valueSuffix = "days"
        ),
        Badge(
            id = "streak_30",
            title = "Monthly Legend",
            description = "Achieve an elite 30-day study streak",
            iconEmoji = "👑",
            category = "Streak",
            isUnlocked = profile.longestStreak >= 30,
            currentValue = profile.longestStreak,
            targetValue = 30,
            valueSuffix = "days"
        ),
        Badge(
            id = "rev_1",
            title = "First Milestone",
            description = "Complete your first revision session",
            iconEmoji = "📝",
            category = "Revision",
            isUnlocked = profile.totalRevisions >= 1,
            currentValue = profile.totalRevisions,
            targetValue = 1,
            valueSuffix = "rev"
        ),
        Badge(
            id = "rev_10",
            title = "Revision Champ",
            description = "Complete 10 revision sessions",
            iconEmoji = "📚",
            category = "Revision",
            isUnlocked = profile.totalRevisions >= 10,
            currentValue = profile.totalRevisions,
            targetValue = 10,
            valueSuffix = "revs"
        ),
        Badge(
            id = "rev_50",
            title = "Recall Master",
            description = "Complete 50 revision sessions",
            iconEmoji = "🧠",
            category = "Revision",
            isUnlocked = profile.totalRevisions >= 50,
            currentValue = profile.totalRevisions,
            targetValue = 50,
            valueSuffix = "revs"
        ),
        Badge(
            id = "time_100",
            title = "Focused Mind",
            description = "Accumulate 100 minutes of study time",
            iconEmoji = "⏱️",
            category = "Time",
            isUnlocked = profile.studyTimeMinutes >= 100,
            currentValue = profile.studyTimeMinutes,
            targetValue = 100,
            valueSuffix = "mins"
        ),
        Badge(
            id = "time_1000",
            title = "Deep Work Master",
            description = "Accumulate 1000 minutes of study time",
            iconEmoji = "🌌",
            category = "Time",
            isUnlocked = profile.studyTimeMinutes >= 1000,
            currentValue = profile.studyTimeMinutes,
            targetValue = 1000,
            valueSuffix = "mins"
        )
    )
}
