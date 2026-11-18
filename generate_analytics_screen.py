import os

content = """package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.AnalyticsMetrics
import com.example.viewmodel.RevisionViewModel
import com.example.data.UserProfile
import kotlin.math.cos
import kotlin.math.sin

val AnalyticsBg = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: RevisionViewModel, navController: NavController) {
    val analyticsMetrics by viewModel.analyticsMetrics.collectAsStateWithLifecycle()
    val timeFilter by viewModel.analyticsTimeFilter.collectAsStateWithLifecycle()
    val profile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDownloadMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AnalyticsBg,
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showDownloadMenu = true }) {
                            Icon(Icons.Outlined.Download, contentDescription = "Download Reports", tint = Color(0xFF1E293B))
                        }
                        DropdownMenu(
                            expanded = showDownloadMenu,
                            onDismissRequest = { showDownloadMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Download Analytics Report (PDF)") },
                                onClick = { 
                                    showDownloadMenu = false
                                    exportReport(context, "pdf", analyticsMetrics)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Download Study Progress (CSV)") },
                                onClick = { 
                                    showDownloadMenu = false
                                    exportReport(context, "csv", analyticsMetrics)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AnalyticsBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SegmentedTimeFilters(
                    selectedFilter = timeFilter,
                    onFilterSelected = { viewModel.setAnalyticsTimeFilter(it) }
                )
            }
            
            item {
                if (analyticsMetrics.totalStudyTimeStr == "0h 0m" && analyticsMetrics.totalRevisions == 0) {
                    EmptyAnalyticsState()
                } else {
                    StudyRecommendationsCard(analyticsMetrics)
                }
            }

            if (analyticsMetrics.totalStudyTimeStr != "0h 0m" || analyticsMetrics.totalRevisions != 0) {
                item { KeyStatsRow(analyticsMetrics, context) }
                item { GoalProgressCard(analyticsMetrics, profile) }
                item { FocusInsightsCard(analyticsMetrics) }
                item { DailyStudyHoursChart(analyticsMetrics) }
                item { SubjectTimeDonutChart(analyticsMetrics) }
                item { StreaksCard(metrics = analyticsMetrics, isStreakSystemEnabled = true) }
                item { RevisionStats(metrics = analyticsMetrics) }
                item { ExamReadinessCard(metrics = analyticsMetrics) }
                
                item {
                    Text(
                        "Subject Progress", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp, 
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                }
                
                items(analyticsMetrics.subjectProgress) { prog ->
                    PremiumSubjectCard(prog = prog) {
                        Toast.makeText(context, "Subject Details: ${prog.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedTimeFilters(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("Today", "Week", "Month", "Yearly")
    // Map existing strings to standard UI labels
    val mappedFilter = when(selectedFilter) {
        "Today" -> "Today"
        "Week", "This Week" -> "Week"
        "Month", "This Month" -> "Month"
        "Yearly", "This Year" -> "Yearly"
        "Overall" -> "Yearly" // fallback
        else -> if (selectedFilter.startsWith("Custom")) "Custom" else "Today"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        filters.forEach { filter ->
            val isSelected = mappedFilter == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun EmptyAnalyticsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Analytics,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No Analytics Yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Start studying, completing tasks, and running focus sessions to see your data here.",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StudyRecommendationsCard(metrics: AnalyticsMetrics) {
    val recommendations = mutableListOf<String>()
    
    // AI Rules Engine for Recommendations
    if (metrics.distractionCount > 5) {
        recommendations.add("Your distraction rate is high. Try a 25-minute Pomodoro session with Strict Mode.")
    }
    
    val avgSessionTokens = metrics.averageSessionStr.split(" ")
    val avgMins = avgSessionTokens.firstOrNull()?.toIntOrNull() ?: 0
    if (avgMins < 20 && metrics.focusSessions > 0) {
        recommendations.add("Increase your average session duration beyond $avgMins mins for deeper focus.")
    }
    
    if (metrics.retentionRate < 50 && metrics.totalRevisions > 0) {
        recommendations.add("Your retention rate is dropping. Prioritize overdue revisions today.")
    }
    
    if (metrics.readinessPercentage > 80) {
        recommendations.add("Excellent work! You are highly prepared for your upcoming exams.")
    }
    
    if (recommendations.isEmpty()) {
        recommendations.add("Keep up the great work! Consistency is key to achieving your goals.")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4F46E5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("AI Insights", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF3730A3))
                Spacer(modifier = Modifier.height(4.dp))
                Text(recommendations.first(), fontSize = 13.sp, color = Color(0xFF312E81), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun KeyStatsRow(metrics: AnalyticsMetrics, context: Context) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Syllabus Done",
                value = "${metrics.overallCompletionPercentage}%",
                valueColor = Color(0xFF1976D2),
                icon = Icons.Default.MenuBook,
                iconColor = Color.White,
                iconBg = Color(0xFF1976D2),
                bgColor = Color(0xFFE3F2FD),
                onClick = { }
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Productivity",
                value = "${metrics.productivityScore}%",
                valueColor = Color(0xFF388E3C),
                icon = Icons.Default.Sync,
                iconColor = Color.White,
                iconBg = Color(0xFF388E3C),
                bgColor = Color(0xFFE8F5E9),
                onClick = { }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Avg Score",
                value = "${metrics.averageScore}%",
                valueColor = Color(0xFFE65100),
                icon = Icons.Default.BarChart,
                iconColor = Color(0xFFE65100),
                iconBg = Color.White,
                bgColor = Color(0xFFFFF3E0),
                onClick = { }
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Readiness",
                value = "${metrics.readinessPercentage}%",
                valueColor = Color(0xFF512DA8),
                icon = Icons.Default.Adjust,
                iconColor = Color(0xFF512DA8),
                iconBg = Color.White,
                bgColor = Color(0xFFEDE7F6),
                onClick = { }
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(90.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
                Box(
                    modifier = Modifier.size(32.dp).background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
            }
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
        }
    }
}

@Composable
fun GoalProgressCard(metrics: AnalyticsMetrics, profile: UserProfile?) {
    val dailyGoalMins = profile?.focusSettings?.dailyFocusGoalMins ?: 120
    val todayHrsTokens = metrics.totalStudyTimeStr.split("h", "m")
    var todayMins = 0
    if (todayHrsTokens.size >= 2) {
        todayMins = (todayHrsTokens[0].trim().toIntOrNull() ?: 0) * 60 + (todayHrsTokens[1].trim().toIntOrNull() ?: 0)
    } else if (metrics.totalStudyTimeStr.contains("m")) {
        todayMins = metrics.totalStudyTimeStr.replace("m", "").trim().toIntOrNull() ?: 0
    }
    
    val progress = if (dailyGoalMins > 0) (todayMins.toFloat() / dailyGoalMins).coerceIn(0f, 1f) else 0f
    
    var isAnimate by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimate) progress else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "GoalProgress"
    )
    
    LaunchedEffect(Unit) { isAnimate = true }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color(0xFF0EA5E9))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Goal Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Today's Study Time", fontSize = 13.sp, color = Color(0xFF64748B))
                Text("${(progress * 100).toInt()}% Achieved", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF0EA5E9),
                trackColor = Color(0xFFF1F5F9)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$todayMins mins / $dailyGoalMins mins Goal",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun FocusInsightsCard(metrics: AnalyticsMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Insights, contentDescription = null, tint = Color(0xFF8B5CF6))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Focus & Session Insights", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    InsightItem(Icons.Outlined.Visibility, "Deep Focus", metrics.deepFocusTimeStr, Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightItem(Icons.Outlined.Timer, "Avg Session", metrics.averageSessionStr, Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightItem(Icons.Outlined.WarningAmber, "Distractions", metrics.distractionCount.toString(), Color(0xFFF43F5E))
                }
                Column(modifier = Modifier.weight(1f)) {
                    InsightItem(Icons.Outlined.Headphones, "Total Sessions", metrics.focusSessions.toString(), Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightItem(Icons.Outlined.WorkspacePremium, "Longest", metrics.longestSessionStr, Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightItem(Icons.Outlined.MonetizationOn, "Coins Earned", metrics.coinsEarned.toString(), Color(0xFFEAB308))
                }
            }
        }
    }
}

@Composable
fun InsightItem(icon: ImageVector, title: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun DailyStudyHoursChart(metrics: AnalyticsMetrics) {
    var isAnimate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isAnimate = true }
    
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Study Hours (This Week)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                metrics.dailyHours.forEachIndexed { index, hours ->
                    val heightRatio = if (metrics.maxDailyHours > 0) hours / metrics.maxDailyHours else 0f
                    val animatedRatio by animateFloatAsState(
                        targetValue = if (isAnimate) heightRatio else 0f,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "BarChartAnim"
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                        if (hours > 0) {
                            Text(String.format("%.1fh", hours), fontSize = 10.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(animatedRatio.coerceAtLeast(0.01f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8))))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(days.getOrNull(index) ?: "", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectTimeDonutChart(metrics: AnalyticsMetrics) {
    val subjects = metrics.subjectProgress.filter { it.studyTimeMins > 0 }
    val totalMins = subjects.sumOf { it.studyTimeMins }
    
    var isAnimate by remember { mutableStateOf(false) }
    val animatedSweep by animateFloatAsState(
        targetValue = if (isAnimate) 360f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "DonutChartAnim"
    )
    LaunchedEffect(Unit) { isAnimate = true }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Subject Time Distribution", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(24.dp))
            
            if (subjects.isEmpty() || totalMins == 0) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No subject data available", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            var currentAngle = -90f
                            subjects.forEach { subj ->
                                val sweepAngle = (subj.studyTimeMins.toFloat() / totalMins) * animatedSweep
                                val color = runCatching { Color(android.graphics.Color.parseColor(subj.colorCode)) }.getOrDefault(Color.Gray)
                                drawArc(
                                    color = color,
                                    startAngle = currentAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
                                )
                                currentAngle += sweepAngle
                            }
                        }
                        Text(
                            "${(totalMins / 60)}h ${(totalMins % 60)}m",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.take(5).forEach { subj ->
                            val color = runCatching { Color(android.graphics.Color.parseColor(subj.colorCode)) }.getOrDefault(Color.Gray)
                            val percent = if (totalMins > 0) (subj.studyTimeMins * 100) / totalMins else 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(subj.name, fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f), maxLines = 1)
                                Text("$percent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Keep the existing components but styled correctly
@Composable
fun StreaksCard(modifier: Modifier = Modifier, metrics: AnalyticsMetrics, isStreakSystemEnabled: Boolean = true) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Streaks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF7ED)).padding(12.dp)
                ) {
                    Column {
                        Text("Current Streak", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(if (isStreakSystemEnabled) metrics.currentStreak.toString() else "0", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("Days", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                        }
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.align(Alignment.End).size(28.dp))
                    }
                }
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF2F2)).padding(12.dp)
                ) {
                    Column {
                        Text("Best Streak", fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(metrics.bestStreak.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("Days", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                        }
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.align(Alignment.End).size(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExamReadinessCard(metrics: AnalyticsMetrics) {
    var isAnimate by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimate) (metrics.readinessPercentage / 100f) else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "ExamReadiness"
    )
    LaunchedEffect(Unit) { isAnimate = true }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.GpsFixed, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exam Readiness", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Target Exam", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(metrics.targetExamName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preparation Level", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFE11D48),
                        trackColor = Color(0xFFFFE4E6)
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        drawArc(Color(0xFFFFE4E6), 0f, 360f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                        drawArc(Color(0xFFE11D48), 270f, (metrics.readinessPercentage / 100f) * 360f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                    }
                    Text("${metrics.readinessPercentage}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
            }
        }
    }
}

@Composable
fun RevisionStats(modifier: Modifier = Modifier, metrics: AnalyticsMetrics) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Revision Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(16.dp))
            RevisionStatRow(icon = Icons.Outlined.DoneAll, iconColor = Color(0xFF10B981), iconBg = Color(0xFFECFDF5), title = "Total Revisions", value = metrics.totalRevisions.toString())
            RevisionStatRow(icon = Icons.Outlined.PlaylistAddCheck, iconColor = Color(0xFF3B82F6), iconBg = Color(0xFFEFF6FF), title = "Completed Topics", value = metrics.completedTopics.toString())
            RevisionStatRow(icon = Icons.Outlined.PendingActions, iconColor = Color(0xFFF59E0B), iconBg = Color(0xFFFEF3C7), title = "Pending Topics", value = metrics.pendingTopics.toString(), valueColor = Color(0xFFF59E0B))
            RevisionStatRow(icon = Icons.Outlined.Psychology, iconColor = Color(0xFF8B5CF6), iconBg = Color(0xFFF5F3FF), title = "Retention Rate", value = "${metrics.retentionRate}%", valueColor = Color(0xFF8B5CF6))
        }
    }
}

@Composable
fun RevisionStatRow(icon: ImageVector, iconColor: Color, iconBg: Color, title: String, value: String, valueColor: Color = Color(0xFF1E293B)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun PremiumSubjectCard(prog: com.example.data.SubjectProgress, onClick: () -> Unit = {}) {
    val themeColor = runCatching { Color(android.graphics.Color.parseColor(prog.colorCode)) }.getOrDefault(Color.Gray)
    var isAnimate by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimate) prog.progress else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "SubjProgress"
    )
    LaunchedEffect(Unit) { isAnimate = true }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(themeColor, CircleShape), contentAlignment = Alignment.Center) {
                    Text(prog.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(prog.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${prog.completedTopics} / ${prog.totalTopics} Topics", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Text(prog.percentage, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = themeColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = themeColor,
                trackColor = themeColor.copy(alpha = 0.1f)
            )
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/AnalyticsScreen.kt", "w") as f:
    f.write(content)
