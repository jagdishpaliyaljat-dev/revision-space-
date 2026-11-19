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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.CoinTransaction
import com.example.data.WithdrawalRequest
import com.example.viewmodel.RevisionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinHistoryScreen(viewModel: RevisionViewModel, navController: NavController) {
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.coinTransactions.collectAsStateWithLifecycle()
    val withdrawalList by viewModel.withdrawalRequests.collectAsStateWithLifecycle()

    val totalCoins = userProfile?.totalCoins ?: 0
    val totalCoinsWithdrawn = userProfile?.totalCoinsWithdrawn ?: 0
    val totalEarned = totalCoins + totalCoinsWithdrawn

    val animatedCoins by animateIntAsState(
        targetValue = totalCoins,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "coinBalance"
    )

    val animatedCoinsWithdrawn by animateIntAsState(
        targetValue = totalCoinsWithdrawn,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "coinsWithdrawn"
    )

    val animatedEarned by animateIntAsState(
        targetValue = totalEarned,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "coinsEarned"
    )

    val inrEquivalent = animatedCoins / 100.0

    var selectedTab by remember { mutableStateOf(0) } // 0: Coin Logs, 1: Withdrawals

    val studyCoins = remember(transactions) {
        transactions.filter { it.amount > 0 && (it.description.contains("study", true) || it.description.contains("timer", true) || it.description.contains("session", true) || it.description.contains("revision", true)) }.sumOf { it.amount }
    }
    val refCoins = remember(transactions) {
        transactions.filter { it.amount > 0 && it.description.contains("referral", true) }.sumOf { it.amount }
    }
    val streakCoins = remember(transactions) {
        transactions.filter { it.amount > 0 && it.description.contains("streak", true) }.sumOf { it.amount }
    }
    val loginCoins = remember(transactions) {
        transactions.filter { it.amount > 0 && it.description.contains("login", true) }.sumOf { it.amount }
    }

    // Let's execute some evaluation when they enter wallet to reward referrals
    LaunchedEffect(Unit) {
        viewModel.evaluateReferralPayouts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Coin Wallet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Your earned coins and spends",
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Glass Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "COIN ECONOMY BALANCE",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("100 Coins = ₹1", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("₹", color = Color(0xFF7C3AED), fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = String.format("%,d", animatedCoins),
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("Rupee Equivalent: ₹%,.2f", inrEquivalent),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Totals Grid Rows
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Earned", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        Text(String.format("%,d", animatedEarned), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }

                                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.15f)))

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Cash Out", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        Text(String.format("%,d", animatedCoinsWithdrawn), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Breakthrough Grid
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    CoinBreakdownItem("Study", studyCoins)
                                    CoinBreakdownItem("Referrals", refCoins)
                                    CoinBreakdownItem("Streak", streakCoins)
                                    CoinBreakdownItem("Login", loginCoins)
                                }
                            }
                        }
                    }
                }

                // Core Quick Links
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Withdraw",
                            subtitle = "Bank/UPI Payout",
                            icon = Icons.Default.AccountBalanceWallet,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(WithdrawalRoute) }
                        )

                        QuickActionCard(
                            title = "Leaderboard",
                            subtitle = "Monthly Prizes",
                            icon = Icons.Default.Leaderboard,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(LeaderboardRoute) }
                        )
                    }
                }

                // Recent Activity Widget
                item {
                    val recentEarnings = remember(transactions) {
                        transactions.filter { tx ->
                            tx.amount > 0 && (
                                tx.description.contains("study", ignoreCase = true) ||
                                tx.description.contains("session", ignoreCase = true) ||
                                tx.description.contains("referral", ignoreCase = true) ||
                                tx.description.contains("invite", ignoreCase = true) ||
                                tx.description.contains("bonus", ignoreCase = true) ||
                                tx.description.contains("revision", ignoreCase = true) ||
                                tx.description.contains("minutes", ignoreCase = true)
                            )
                        }.take(5)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DailyEarningsChart(transactions)
                        RecentEarningWidget(recentEarnings = recentEarnings)
                    }
                }

                // Segmented Tabs Control for Lists
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF7C3AED),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF7C3AED)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Coin History", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Payout Records", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // List rendering based on selection
                if (selectedTab == 0) {
                    if (transactions.isEmpty()) {
                        item {
                            EmptyStateHelper("No coin transactions registered yet.")
                        }
                    } else {
                        items(transactions) { tx ->
                            CoinTransactionRow(tx)
                        }
                    }
                } else {
                    if (withdrawalList.isEmpty()) {
                        item {
                            EmptyStateHelper("No withdrawal entries submitted yet.")
                        }
                    } else {
                        items(withdrawalList) { req ->
                            WithdrawalRecordListItem(req)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoinBreakdownItem(label: String, amount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(String.format("%,d", amount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 9.sp, color = Color(0xFF8A94A6))
        }
    }
}

@Composable
fun CoinTransactionRow(tx: CoinTransaction) {
    val isDebit = tx.amount < 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDebit) Color(0xFFEF4444).copy(alpha = 0.08f) else Color(0xFF10B981).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDebit) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isDebit) Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestampMs)),
                    fontSize = 11.sp,
                    color = Color(0xFF8A94A6)
                )
            }

            Text(
                text = if (isDebit) "${tx.amount}" else "+${tx.amount}",
                fontWeight = FontWeight.Black,
                color = if (isDebit) Color(0xFFEF4444) else Color(0xFF10B981),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun WithdrawalRecordListItem(req: WithdrawalRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cashed Out ₹${req.amountInInr}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "${req.method} Transfer • ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.createdAtMs))}",
                    fontSize = 11.sp,
                    color = Color(0xFF8A94A6)
                )
            }

            val (colorStr, label) = when (req.status) {
                "APPROVED" -> Pair(Color(0xFF10B981), "COMPLETED")
                "REJECTED" -> Pair(Color(0xFFEF4444), "REJECTED")
                else -> Pair(Color(0xFFF59E0B), "PENDING")
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorStr.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(label, fontWeight = FontWeight.Black, fontSize = 9.sp, color = colorStr)
            }
        }
    }
}

@Composable
fun EmptyStateHelper(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.ContentPasteOff, contentDescription = null, tint = Color(0xFF8A94A6), modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

@Composable
fun RecentEarningWidget(recentEarnings: List<CoinTransaction>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_activity_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rewards",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB8860B)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (recentEarnings.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.SentimentDissatisfied,
                        contentDescription = null,
                        tint = Color(0xFF8A94A6),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No recent study or referral rewards logged.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Complete your first study focus block to unlock!",
                        fontSize = 10.sp,
                        color = Color(0xFF8A94A6),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentEarnings.forEach { tx ->
                        val isReferral = tx.description.contains("Referral", ignoreCase = true) || tx.description.contains("Invite", ignoreCase = true)
                        val iconBg = if (isReferral) Color(0xFF7C3AED).copy(alpha = 0.08f) else Color(0xFF10B981).copy(alpha = 0.08f)
                        val iconTint = if (isReferral) Color(0xFF7C3AED) else Color(0xFF10B981)
                        val vectorIcon = if (isReferral) Icons.Default.GroupAdd else Icons.Default.Timer

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vectorIcon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.description,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestampMs)),
                                    fontSize = 10.sp,
                                    color = Color(0xFF8A94A6)
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "+${tx.amount}",
                                    fontWeight = FontWeight.Bold,
                                    color = iconTint,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("c", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyEarningsChart(transactions: List<CoinTransaction>) {
    val last7Days = remember(transactions) {
        (0..6).map { i ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -(6 - i))
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val end = cal.timeInMillis
            val label = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(java.util.Date(start))
            Triple(start, end, label)
        }
    }

    val dailyEarnings = remember(transactions, last7Days) {
        last7Days.map { (start, end, _) ->
            transactions.filter { it.amount > 0 && it.timestampMs in start until end }.sumOf { it.amount }
        }
    }
    val labels = last7Days.map { it.third }
    val maxEarnings = dailyEarnings.maxOrNull()?.coerceAtLeast(10) ?: 10

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_earnings_chart"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Weekly Earnings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyEarnings.forEachIndexed { index, amount ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (amount > 0) {
                            Text(
                                text = amount.toString(),
                                fontSize = 9.sp,
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        val heightFraction = amount.toFloat() / maxEarnings.toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(heightFraction.coerceAtLeast(0.02f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED))))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = labels[index],
                            fontSize = 10.sp,
                            color = Color(0xFF8A94A6),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
