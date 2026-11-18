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

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.UserProfile
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: RevisionViewModel, navController: NavController) {
    val usersListState = viewModel.adminAllUsers.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    val safeCurrentUserProfile = currentUserProfile
    if (safeCurrentUserProfile == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerBackground()
                )
            }
        }
        return
    }

    val rankedUsers = remember(usersListState.value) {
        (usersListState.value ?: emptyList())
            .filter { !it.isBanned && it.role != "Admin" }
            .sortedByDescending { it.studyTimeMinutes }
    }

    val myRank = remember(rankedUsers, currentUserProfile) {
        val index = rankedUsers.indexOfFirst { it.id == safeCurrentUserProfile.id }
        if (index != -1) index + 1 else -1
    }

    // Calculation for monthly countdown
    var countdownText by remember { mutableStateOf("Calculating...") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val nextMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMs = nextMonth.timeInMillis - now.timeInMillis
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60
            countdownText = "Resets in ${days}d ${hours}h ${minutes}m"
            delay(60000) // update every minute
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Leaderboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "See how your batch is doing",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8A94A6)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(19.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Countdown header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6C4DFF).copy(alpha = 0.06f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF6C4DFF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Monthly Competition — $countdownText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C4DFF)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Podiums
                if (rankedUsers.isNotEmpty()) {
                    item {
                        LeaderboardPodiumSection(rankedUsers.take(3))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Prizes Rule Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Monthly Reward Pool", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                PrizeBadge("🥇 Rank 1", "+1,000 Coins", Color(0xFFFBBF24))
                                PrizeBadge("🥈 Rank 2", "+500 Coins", Color(0xFF8A94A6))
                                PrizeBadge("🥉 Rank 3", "+200 Coins", Color(0xFFB45309))
                            }
                        }
                    }
                }

                // User Rows
                if (rankedUsers.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF8A94A6), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No users logged study hours yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    itemsIndexed(rankedUsers) { index, item ->
                        LeaderboardUserRow(index + 1, item, item.id == safeCurrentUserProfile.id)
                    }
                }
            }

            // Fixed User Position Bar
            if (myRank != -1 && currentUserProfile != null) {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#$myRank",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF6C4DFF),
                            modifier = Modifier.width(44.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6C4DFF).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                safeCurrentUserProfile.name.take(1).ifEmpty { safeCurrentUserProfile.email.take(1) }.uppercase(),
                                color = Color(0xFF6C4DFF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("You (${safeCurrentUserProfile.name.ifEmpty { "User" }})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Continuous Focus Session Tracker", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = String.format("%.1fh", safeCurrentUserProfile.studyTimeMinutes / 60.0),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardPodiumSection(topThree: List<UserProfile>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Rank #2 (Silver)
        if (topThree.size >= 2) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                PodiumColumn(topThree[1], 120.dp, "2", Color(0xFF8A94A6), MaterialTheme.colorScheme.outlineVariant)
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Rank #1 (Gold)
        if (topThree.isNotEmpty()) {
            Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.BottomCenter) {
                PodiumColumn(topThree[0], 150.dp, "1", Color(0xFFFFD700), Color(0xFFFEF3C7))
            }
        }

        // Rank #3 (Bronze)
        if (topThree.size >= 3) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                PodiumColumn(topThree[2], 100.dp, "3", Color(0xFFB45309), Color(0xFFFFEDD5))
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PodiumColumn(user: UserProfile, height: androidx.compose.ui.unit.Dp, rankText: String, medalColor: Color, bgContainerColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(medalColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(medalColor),
                contentAlignment = Alignment.Center
            ) {
                Text(rankText, color = if (medalColor == Color(0xFFFFD700)) MaterialTheme.colorScheme.onSurface else Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = user.name.ifEmpty { user.email.substringBefore("@") },
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = String.format("%.1fh", user.studyTimeMinutes / 60.0),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = Color(0xFF6C4DFF)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgContainerColor, bgContainerColor.copy(alpha = 0.4f))
                    )
                )
        )
    }
}

@Composable
fun PrizeBadge(rankLabel: String, coinsLabel: String, tintCol: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tintCol.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(rankLabel, fontWeight = FontWeight.Bold, color = tintCol, fontSize = 11.sp)
            Text(coinsLabel, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
        }
    }
}

@Composable
fun LeaderboardUserRow(rank: Int, user: UserProfile, isMe: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFF6C4DFF).copy(alpha = 0.04f) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isMe) Color(0xFF6C4DFF).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (rank <= 3) "" else "#$rank",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Start
            )
            if (rank == 1) Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp).padding(end = 4.dp))
            else if (rank == 2) Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF8A94A6), modifier = Modifier.size(18.dp).padding(end = 4.dp))
            else if (rank == 3) Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp).padding(end = 4.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isMe) Color(0xFF6C4DFF).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).ifEmpty { user.email.take(1) }.uppercase(),
                    color = if (isMe) Color(0xFF6C4DFF) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMe) "${user.name.ifEmpty { "Me" }} (You)" else user.name.ifEmpty { "User" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Weekly study sessions",
                    fontSize = 11.sp,
                    color = Color(0xFF8A94A6)
                )
            }

            Text(
                text = String.format("%.1fh", user.studyTimeMinutes / 60.0),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
