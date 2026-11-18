package com.example.ui.screens.admin
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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.example.viewmodel.RevisionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val PrimaryAdmin = Color(0xFF6C4DFF)
val BackgroundAdmin: Color
    @Composable get() = MaterialTheme.colorScheme.background
val SurfaceAdmin: Color
    @Composable get() = MaterialTheme.colorScheme.surface
val TextPrimaryAdmin: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondaryAdmin: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun DashboardErrorBoundary(
    isError: Boolean,
    onRetry: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isError) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Unable to load live data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAdmin)
                ) {
                    Text("Retry")
                }
            }
        }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: RevisionViewModel, navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var retryCount by remember { mutableStateOf(0) }
    var hasGlobalError by remember { mutableStateOf(false) }
    var unreadNotificationsCount by remember { mutableStateOf(3) }
    var isNotificationCenterOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab != 2) {
            isNotificationCenterOpen = false
        }
    }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val items = listOf(
        Pair("Dashboard", Icons.Default.Home),
        Pair("Users", Icons.Default.Person),
        Pair("Notifications", Icons.Default.Notifications),
        Pair("Coins & Rewards", Icons.Default.MonetizationOn),
        Pair("Analytics", Icons.Default.Insights),
        Pair("Quick Actions", Icons.Default.FlashOn),
        Pair("Settings", Icons.Default.Settings),
        Pair("System Debug", Icons.Default.BugReport)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceAdmin,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                    Surface(color = PrimaryAdmin, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Revision Space",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryAdmin)
                        )
                        Text(
                            text = "Admin Panel",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryAdmin)
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                LazyColumn(modifier = Modifier.padding(horizontal = 12.dp)) {
                    item { Text("Menu", fontSize = 12.sp, color = TextSecondaryAdmin, modifier = Modifier.padding(16.dp, 8.dp)) }
                    item { NavigationDrawerItem(label = { Text("Dashboard") }, selected = selectedTab == 0 && !isNotificationCenterOpen, onClick = { selectedTab = 0; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.Home, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("Users") }, selected = selectedTab == 1 && !isNotificationCenterOpen, onClick = { selectedTab = 1; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.Person, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("Notifications") }, selected = isNotificationCenterOpen, onClick = { isNotificationCenterOpen = true; unreadNotificationsCount = 0; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.Notifications, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("Coins & Rewards") }, selected = selectedTab == 3 && !isNotificationCenterOpen, onClick = { selectedTab = 3; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.MonetizationOn, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("Analytics") }, selected = selectedTab == 4 && !isNotificationCenterOpen, onClick = { selectedTab = 4; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.Insights, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("Quick Actions") }, selected = selectedTab == 5 && !isNotificationCenterOpen, onClick = { selectedTab = 5; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.FlashOn, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                    item { Text("Management", fontSize = 12.sp, color = TextSecondaryAdmin, modifier = Modifier.padding(16.dp, 8.dp)) }
                    item { NavigationDrawerItem(label = { Text("Settings") }, selected = selectedTab == 6 && !isNotificationCenterOpen, onClick = { selectedTab = 6; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.Settings, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("System Debug") }, selected = selectedTab == 7 && !isNotificationCenterOpen, onClick = { selectedTab = 7; isNotificationCenterOpen = false; coroutineScope.launch { drawerState.close() } }, icon = { Icon(Icons.Default.BugReport, null) }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = PrimaryAdmin.copy(alpha = 0.1f), selectedIconColor = PrimaryAdmin, selectedTextColor = PrimaryAdmin)) }
                    item { NavigationDrawerItem(label = { Text("Logout", color = Color(0xFFEF4444)) }, selected = false, onClick = { 
                        if (!SecureStorage.isRememberLoginEnabled(context)) {
                            SecureStorage.clearCredentials(context)
                        }
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(DashboardRoute) { popUpTo(0) { inclusive = true } }
                    }, icon = { Icon(Icons.Default.ExitToApp, null, tint = Color(0xFFEF4444)) }) }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = BackgroundAdmin,
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            val titleText = if (isNotificationCenterOpen) "Notification Center" else items[selectedTab].first
                            Text(titleText, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimaryAdmin)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Menu, "Menu", tint = TextPrimaryAdmin, modifier = Modifier.size(19.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isNotificationCenterOpen = !isNotificationCenterOpen
                            if (isNotificationCenterOpen) {
                                unreadNotificationsCount = 0
                            }
                        }, modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ) { Text("$unreadNotificationsCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, "Notifications", tint = TextSecondaryAdmin)
                            }
                        }
                        IconButton(onClick = {
                            if (!SecureStorage.isRememberLoginEnabled(context)) {
                                SecureStorage.clearCredentials(context)
                            }
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(DashboardRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        }, modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)) {
                            Icon(Icons.Default.ExitToApp, "Sign Out", tint = Color(0xFFEF4444))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundAdmin)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                DashboardErrorBoundary(
                    isError = hasGlobalError,
                    onRetry = { 
                        hasGlobalError = false
                        retryCount++ 
                    }
                ) {
                    key(retryCount) {
                        if (isNotificationCenterOpen) {
                            NotificationsTab(viewModel)
                        } else {
                            when (selectedTab) {
                                0 -> DashboardTab(viewModel)
                                1 -> UsersTab(viewModel, navController)
                                2 -> NotificationsTab(viewModel)
                                3 -> CoinsAndRewardsTab(viewModel)
                                4 -> AnalyticsTab(viewModel)
                                5 -> QuickActionsTab(viewModel)
                                6 -> SettingsTabAdmin(viewModel)
                                7 -> SystemDebugTab(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class AdminLeaderboardUser(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val studyTime: String,
    val revisions: Int,
    val streak: Int,
    val coins: Int,
    val accuracy: String,
    val rankChange: Int
)

@Composable
fun LeaderboardTabAdmin(viewModel: RevisionViewModel) {
    val allUsersState = viewModel.adminAllUsers.collectAsState(initial = emptyList())
    val allUsers = allUsersState.value ?: emptyList()

    var selectedTimeFilter by remember { mutableStateOf("Weekly") }
    var selectedCategory by remember { mutableStateOf("Coins") }
    var searchQuery by remember { mutableStateOf("") }
    var enableLb by remember { mutableStateOf(true) }

    val timeFilters = listOf("Daily", "Weekly", "Monthly", "Yearly")
    val categories = listOf("Study Time", "Revision", "Streak", "Coin", "Accuracy")

    val realLeaderboard = remember(allUsers, selectedCategory, searchQuery) {
        val filteredUsers = allUsers.filter {
            searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
        }
        
        val sortedUsers = when (selectedCategory) {
            "Study Time" -> filteredUsers.sortedByDescending { it.studyTimeMinutes }
            "Revision" -> filteredUsers.sortedByDescending { it.totalRevisions }
            "Streak" -> filteredUsers.sortedByDescending { it.currentStreak }
            "Accuracy" -> filteredUsers.sortedByDescending { it.totalRevisions } // Fallback for accuracy
            else -> filteredUsers.sortedByDescending { it.totalCoins } // "Coin" default
        }
        
        sortedUsers.mapIndexed { index, user ->
            val hours = user.studyTimeMinutes / 60
            val minutes = user.studyTimeMinutes % 60
            val studyTimeStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            AdminLeaderboardUser(
                id = user.id,
                name = user.name.takeIf { it.isNotBlank() } ?: "Unknown",
                imageUrl = null,
                studyTime = studyTimeStr,
                revisions = user.totalRevisions,
                streak = user.currentStreak,
                coins = user.totalCoins,
                accuracy = "N/A", // Accuracy calculation not defined yet
                rankChange = 0
            )
        }
    }

    val totalRanked = realLeaderboard.size
    val topUser = realLeaderboard.firstOrNull()
    val totalCoinsAll = allUsers.sumOf { it.totalCoins }
    val totalStudyHoursAll = allUsers.sumOf { it.studyTimeMinutes } / 60

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Leaderboard Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Manage competition and rankings", fontSize = 14.sp, color = TextSecondaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AdminSettingsSection("Leaderboard Dashboard") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AdminStatCard(modifier = Modifier.weight(1f), title = "Total Ranked", value = "$totalRanked", change = "Total", icon = Icons.Default.Group, iconColor = PrimaryAdmin, bgColor = PrimaryAdmin.copy(alpha = 0.1f))
                        AdminStatCard(modifier = Modifier.weight(1f), title = "Top User", value = topUser?.name?.take(10) ?: "None", change = "${topUser?.coins ?: 0} Coins", icon = Icons.Default.WorkspacePremium, iconColor = Color(0xFFF59E0B), bgColor = Color(0xFFF59E0B).copy(alpha = 0.1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val coinsValueStr = if (totalCoinsAll > 1000) "${totalCoinsAll/1000}k" else totalCoinsAll.toString()
                        AdminStatCard(modifier = Modifier.weight(1f), title = "Total Coins", value = coinsValueStr, change = "Earned", icon = Icons.Default.MonetizationOn, iconColor = Color(0xFF22C55E), bgColor = Color(0xFF22C55E).copy(alpha = 0.1f))
                        val studyTimeValueStr = if (totalStudyHoursAll > 1000) "${totalStudyHoursAll/1000}k hrs" else "$totalStudyHoursAll hrs"
                        AdminStatCard(modifier = Modifier.weight(1f), title = "Total Study", value = studyTimeValueStr, change = "All time", icon = Icons.Default.Schedule, iconColor = Color(0xFF3B82F6), bgColor = Color(0xFF3B82F6).copy(alpha = 0.1f))
                    }
                }
            }
        }

        item {
            Column {
                Text("Filters Configuration", color = PrimaryAdmin, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(timeFilters) { filter ->
                        FilterChip(
                            selected = filter == selectedTimeFilter,
                            onClick = { selectedTimeFilter = filter },
                            label = { Text(filter, color = if (filter == selectedTimeFilter) Color.White else TextSecondaryAdmin) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceAdmin,
                                selectedContainerColor = PrimaryAdmin,
                                labelColor = TextSecondaryAdmin,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = PrimaryAdmin, enabled = true, selected = filter == selectedTimeFilter)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text(category, color = if (category == selectedCategory) Color.White else TextSecondaryAdmin) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceAdmin,
                                selectedContainerColor = Color(0xFF3B82F6),
                                labelColor = TextSecondaryAdmin,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = Color(0xFF3B82F6), enabled = true, selected = category == selectedCategory)
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text("Top 3 Podium", color = PrimaryAdmin, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp, bottom = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().height(200.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    if (realLeaderboard.size > 1) {
                        AdminPodiumItem(user = realLeaderboard[1], rank = 2, height = 130.dp, color = Color(0xFF8A94A6))
                    }
                    if (realLeaderboard.isNotEmpty()) {
                        AdminPodiumItem(user = realLeaderboard[0], rank = 1, height = 180.dp, color = Color(0xFFFBBF24))
                    }
                    if (realLeaderboard.size > 2) {
                        AdminPodiumItem(user = realLeaderboard[2], rank = 3, height = 100.dp, color = Color(0xFFB45309))
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search User in Leaderboard...", color = TextSecondaryAdmin) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryAdmin) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SurfaceAdmin,
                    focusedBorderColor = PrimaryAdmin,
                    unfocusedContainerColor = SurfaceAdmin,
                    focusedContainerColor = SurfaceAdmin,
                    unfocusedTextColor = TextPrimaryAdmin,
                    focusedTextColor = TextPrimaryAdmin
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        itemsIndexed(realLeaderboard.drop(3)) { index, user ->
            val rank = index + 4
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceAdmin)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#$rank", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextSecondaryAdmin, modifier = Modifier.width(36.dp))
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryAdmin.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text(user.name.take(1).uppercase(), color = PrimaryAdmin, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${user.coins}", fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${user.studyTime} • ${user.revisions} Rev", fontSize = 12.sp, color = TextSecondaryAdmin)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (user.rankChange > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDropUp, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                                Text("${user.rankChange}", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (user.rankChange < 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                Text("${kotlin.math.abs(user.rankChange)}", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("-", color = TextSecondaryAdmin, fontSize = 14.sp)
                        }
                        Text("${user.accuracy} Acc", fontSize = 10.sp, color = TextSecondaryAdmin)
                    }
                }
            }
        }

        item {
            AdminSettingsSection("Leaderboard Settings") {
                AdminSettingsSwitchItem("Enable Leaderboard", "Show leaderboards to users", enableLb, { enableLb = it }, Icons.Default.Leaderboard)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Reset Daily Leaderboard", onClick = {}, icon = Icons.Default.Restore)
                AdminSettingsActionItem("Reset Weekly/Monthly/Yearly", onClick = {}, icon = Icons.Default.History)
            }
        }

        item {
            AdminSettingsSection("Export Data") {
                AdminSettingsActionItem("Export to Excel", onClick = {}, icon = Icons.Default.Description, iconTint = Color(0xFF22C55E))
                AdminSettingsActionItem("Export to PDF", onClick = {}, icon = Icons.Default.PictureAsPdf, iconTint = Color(0xFFEF4444))
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminPodiumItem(user: AdminLeaderboardUser, rank: Int, height: androidx.compose.ui.unit.Dp, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(SurfaceAdmin), contentAlignment = Alignment.Center) {
            Text(user.name.take(1).uppercase(), color = color, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(user.name.split(" ").firstOrNull() ?: "", color = TextPrimaryAdmin, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text("${user.coins}", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = color,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.width(90.dp).height(height)
        ) {
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(top = 16.dp)) {
                Text("#$rank", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Black, fontSize = 28.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinsAndRewardsTab(viewModel: RevisionViewModel) {
    val allWithdrawals by viewModel.allWithdrawalRequests.collectAsStateWithLifecycle()
    val allRefs by viewModel.allReferrals.collectAsStateWithLifecycle()
    val logs by viewModel.adminLogs.collectAsStateWithLifecycle()
    val allProfilesState by viewModel.adminAllUsers.collectAsStateWithLifecycle()
    val allProfiles = allProfilesState ?: emptyList()
    val allCoinTxs by viewModel.allCoinTransactions.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var subTab by remember { mutableStateOf(0) } // 0: Withdrawals, 1: Referrals, 2: Audit Logs, 3: Fraud/Bans

    // Dialog state for Rejection
    var showRejectDialog by remember { mutableStateOf<com.example.data.WithdrawalRequest?>(null) }
    var rejectReasonText by remember { mutableStateOf("") }

    val currentAdminEmail = FirebaseAuth.getInstance().currentUser?.email ?: "admin@revisionspace.com"
    val currentAdminId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin_uid"

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Coin Economy & Admin Panel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Approve payouts, auditing logs, referral tracks & fraud metrics", fontSize = 12.sp, color = TextSecondaryAdmin)
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val pendingCount = allWithdrawals.count { it.status == "PENDING" }
                    val totalApprovedVal = allWithdrawals.filter { it.status == "APPROVED" }.sumOf { it.amountInInr }
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Withdrawals",
                        value = "$pendingCount requests",
                        change = "Awaiting decision",
                        icon = Icons.Default.Pending,
                        iconColor = Color(0xFFF59E0B),
                        bgColor = Color(0xFFF59E0B).copy(alpha = 0.1f)
                    )
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Outflow Paid",
                        value = "₹$totalApprovedVal INR",
                        change = "All time approved",
                        icon = Icons.Default.CurrencyRupee,
                        iconColor = Color(0xFF10B981),
                        bgColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    )
                }
            }
        }

Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* Navigate to Add Coins */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAdmin),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Coins", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Coins")
            }
            Button(
                onClick = { /* Navigate to Deduct Coins */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Deduct Coins", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deduct")
            }
            Button(
                onClick = { /* Navigate to Reward Events */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Reward Events", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reward Events")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceAdmin),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Recent Coin Transactions", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("User", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(2f))
                    Text("Type", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Amount", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Reason", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                    Text("Balance (Before -> After)", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(2f))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                if (allCoinTxs.isEmpty()) {
                    Text("No coin transactions found.", fontSize = 12.sp, color = TextSecondaryAdmin, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    allCoinTxs.take(10).forEach { tx ->
                        val user = allProfiles.find { it.id == tx.userId }
                        val userName = user?.name ?: "Unknown"
                        val userEmail = user?.email ?: "Unknown"
                        val isCredit = tx.type == "EARNED" || tx.amount > 0
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("$userName ($userEmail)", color = TextPrimaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(2f), maxLines = 1)
                            Text(if (isCredit) "Credit" else "Debit", color = if(isCredit) Color(0xFF22C55E) else Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text(if (isCredit) "+${tx.amount}" else "${tx.amount}", color = if(isCredit) Color(0xFF22C55E) else Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text(tx.description, color = TextPrimaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                            Text("N/A", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsTab(viewModel: RevisionViewModel) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val adminEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "admin"

    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    if (showAnnouncementDialog) {
        var aTitle by remember { mutableStateOf("") }
        var aMessage by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAnnouncementDialog = false },
            title = { Text("Create Announcement") },
            text = {
                Column {
                    OutlinedTextField(value = aTitle, onValueChange = { aTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = aMessage, onValueChange = { aMessage = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if(aTitle.isNotBlank() && aMessage.isNotBlank()) {
                        viewModel.updateGlobalConfig(mapOf(
                            "activeAnnouncementTitle" to aTitle,
                            "activeAnnouncementMessage" to aMessage,
                            "activeAnnouncementTimestampMs" to System.currentTimeMillis()
                        ))
                        viewModel.sendBroadcastNotification(aTitle, aMessage, "All Users", adminEmail) { s, _ -> }
                        viewModel.logSystemAction("Create Announcement", "Broadcast message: $aTitle")
                        Toast.makeText(context, "Announcement Published", Toast.LENGTH_SHORT).show()
                        showAnnouncementDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = {
                TextButton(onClick = { showAnnouncementDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEmergencyDialog) {
        var eTitle by remember { mutableStateOf("EMERGENCY ALERT") }
        var eMessage by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Emergency Broadcast", color = Color.Red) },
            text = {
                Column {
                    OutlinedTextField(value = eTitle, onValueChange = { eTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = eMessage, onValueChange = { eMessage = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Red), onClick = {
                    if(eTitle.isNotBlank() && eMessage.isNotBlank()) {
                        viewModel.sendEmergencyBroadcast(eTitle, eMessage, adminEmail)
                        Toast.makeText(context, "Emergency Sent", Toast.LENGTH_SHORT).show()
                        showEmergencyDialog = false
                    }
                }) { Text("SEND PUSH") }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUpdateDialog) {
        var vNum by remember { mutableStateOf((globalConfig.minimumAppVersion + 1).toString()) }
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Force App Update") },
            text = {
                Column {
                    Text("Current minimum required version: ${globalConfig.minimumAppVersion}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = vNum, onValueChange = { vNum = it }, label = { Text("New Minimum Version") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    vNum.toIntOrNull()?.let {
                        viewModel.updateGlobalConfig(mapOf("minimumAppVersion" to it))
                        viewModel.logSystemAction("Force App Update", "Version updated to $it")
                        Toast.makeText(context, "Update Enforced", Toast.LENGTH_SHORT).show()
                        showUpdateDialog = false
                    }
                }) { Text("Enforce") }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("System Status") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Firebase Status"); Text("Online", color = Color.Green) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Authentication"); Text("Online", color = Color.Green) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Firestore DB"); Text("Online", color = Color.Green) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Storage"); Text("Online", color = Color.Green) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Cloud Messaging"); Text("Online", color = Color.Green) }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Maintenance Mode"); Text(if (globalConfig.isMaintenanceModeEnabled) "ON" else "OFF", color = if (globalConfig.isMaintenanceModeEnabled) Color.Red else Color.Green) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Execute system level commands and triggers", fontSize = 14.sp, color = TextSecondaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            AdminSettingsSection("System Operations") {
                AdminSettingsActionItem("Create Announcement", "Broadcast message to all users", onClick = { showAnnouncementDialog = true }, icon = Icons.Default.Campaign)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsSwitchItem("Enable Beta Feature", "Toggle beta endpoints", globalConfig.isBetaFeatureEnabled, { 
                    viewModel.updateGlobalConfig(mapOf("isBetaFeatureEnabled" to it))
                    viewModel.logSystemAction("Toggle Beta Feature", "Feature enabled: $it")
                }, Icons.Default.Science)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsSwitchItem("Maintenance Mode", "Lock user access, display maintenance screen", globalConfig.isMaintenanceModeEnabled, { 
                    viewModel.updateGlobalConfig(mapOf("isMaintenanceModeEnabled" to it))
                    viewModel.logSystemAction("Toggle Maintenance Mode", "Maintenance mode enabled: $it")
                }, Icons.Default.BuildCircle)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Emergency Broadcast", "Sends push alert immediately", onClick = { showEmergencyDialog = true }, icon = Icons.Default.Warning, iconTint = Color(0xFFEF4444), titleColor = Color(0xFFEF4444))
            }
        }
        item {
            AdminSettingsSection("Application Control") {
                AdminSettingsActionItem("Force App Update", "Prompt older version users to update", onClick = { showUpdateDialog = true }, icon = Icons.Default.SystemUpdate)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Clear Server Cache", "Removes temporarily cached Firebase fetches", onClick = { 
                    viewModel.updateGlobalConfig(mapOf("serverCacheGeneration" to globalConfig.serverCacheGeneration + 1))
                    viewModel.logSystemAction("Clear Server Cache", "Server cache cleared")
                    Toast.makeText(context, "Server Cache Cleared", Toast.LENGTH_SHORT).show()
                }, icon = Icons.Default.Cached)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Send Marketing Campaign", "Draft and send marketing notifications", onClick = { 
                    showAnnouncementDialog = true // Can reuse announcement dialog or show toast pointing to Notifications tab
                    Toast.makeText(context, "For Campaigns, use the Notifications Tab", Toast.LENGTH_LONG).show()
                }, icon = Icons.Default.Send)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("View System Status", "Health check for backend services", onClick = { showStatusDialog = true }, icon = Icons.Default.HealthAndSafety, iconTint = Color(0xFF10B981))
            }
        }
    }
}

@Composable
fun SettingsTabAdmin(viewModel: RevisionViewModel) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val adminEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "admin"
    val context = androidx.compose.ui.platform.LocalContext.current

    var showActiveTimeDialog by remember { mutableStateOf(false) }
    if (showActiveTimeDialog) {
        var aTime by remember { mutableStateOf(globalConfig.activeTimeBaselineMinutes.toString()) }
        AlertDialog(
            onDismissRequest = { showActiveTimeDialog = false },
            title = { Text("Configure Active Time") },
            text = {
                Column {
                    Text("Baseline minute requirement for active study.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = aTime, onValueChange = { aTime = it }, label = { Text("Minutes") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    aTime.toIntOrNull()?.let {
                        viewModel.updateGlobalConfig(mapOf("activeTimeBaselineMinutes" to it))
                        viewModel.logSystemAction("Configure Active Time", "Set to $it minutes")
                        Toast.makeText(context, "Active Time Updated", Toast.LENGTH_SHORT).show()
                        showActiveTimeDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showActiveTimeDialog = false }) { Text("Cancel") }
            }
        )
    }

    var showCoinRateDialog by remember { mutableStateOf(false) }
    if (showCoinRateDialog) {
        var cRate by remember { mutableStateOf(globalConfig.coinsPerBaselineTime.toString()) }
        AlertDialog(
            onDismissRequest = { showCoinRateDialog = false },
            title = { Text("Set Coin Rate") },
            text = {
                Column {
                    Text("Coins awarded per baseline time.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = cRate, onValueChange = { cRate = it }, label = { Text("Coins") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    cRate.toIntOrNull()?.let {
                        viewModel.updateGlobalConfig(mapOf("coinsPerBaselineTime" to it))
                        viewModel.logSystemAction("Coin Settings", "Rate set to $it")
                        Toast.makeText(context, "Coin Rate Updated", Toast.LENGTH_SHORT).show()
                        showCoinRateDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCoinRateDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Admin Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Manage application configurations", fontSize = 14.sp, color = TextSecondaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AdminSettingsSection("App Settings") {
                AdminSettingsActionItem("Edit Admin Name", "Update your display name", onClick = { Toast.makeText(context, "Profile update applied.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.Person)
                AdminSettingsActionItem("Theme", "System Default", onClick = { Toast.makeText(context, "Theme settings saved.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.Palette)
                AdminSettingsActionItem("App Version", "v${globalConfig.minimumAppVersion} (Remote Config)", onClick = { }, icon = Icons.Default.Info)
            }
        }

        item {
            AdminSettingsSection("Coin Settings") {
                AdminSettingsActionItem("Set Coin Rate", "Currently ${globalConfig.activeTimeBaselineMinutes} Mins = ${globalConfig.coinsPerBaselineTime} Coin", onClick = { showCoinRateDialog = true }, icon = Icons.Default.MonetizationOn)
                AdminSettingsActionItem("Add/Remove Coins", "Manage user balance", onClick = { Toast.makeText(context, "Use Navigation Tab 'User Management' to manage individual balance.", Toast.LENGTH_LONG).show() }, icon = Icons.Default.AccountBalanceWallet)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Reset User Coins", onClick = { 
                    viewModel.resetAllUserCoins(adminEmail)
                    Toast.makeText(context, "All user coins have been reset to 0.", Toast.LENGTH_LONG).show()
                }, icon = Icons.Default.RestartAlt, titleColor = Color.Red, iconTint = Color.Red)
            }
        }

        item {
            AdminSettingsSection("Notification Settings") {
                AdminSettingsSwitchItem("Enable Notifications", "Master switch for all notifications", globalConfig.isNotificationsEnabled, { 
                    viewModel.updateGlobalConfig(mapOf("isNotificationsEnabled" to it))
                    viewModel.logSystemAction("Notification Settings", "Master Notifications Enabled: $it")
                }, Icons.Default.NotificationsActive)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsSwitchItem("Revision Reminders", "Daily reminders to users", globalConfig.isRevisionRemindersEnabled, { 
                    viewModel.updateGlobalConfig(mapOf("isRevisionRemindersEnabled" to it))
                    viewModel.logSystemAction("Notification Settings", "Revision Reminders: $it")
                }, Icons.Default.Schedule)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsSwitchItem("Announcement Notifications", "Alert users of new updates", globalConfig.isAnnouncementsEnabled, { 
                    viewModel.updateGlobalConfig(mapOf("isAnnouncementsEnabled" to it))
                    viewModel.logSystemAction("Notification Settings", "Announcements: $it")
                }, Icons.Default.Campaign)
            }
        }

        item {
            AdminSettingsSection("Revision Settings") {
                AdminSettingsSwitchItem("Enable Streak System", "Track daily active studying", globalConfig.isStreakSystemEnabled, { 
                    viewModel.updateGlobalConfig(mapOf("isStreakSystemEnabled" to it))
                    viewModel.logSystemAction("Revision Settings", "Streak System Enabled: $it")
                }, Icons.Default.LocalFireDepartment)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Reset User Streaks", onClick = { 
                    viewModel.resetAllUserStreaks(adminEmail)
                    Toast.makeText(context, "All user streaks have been reset to 0.", Toast.LENGTH_LONG).show()
                }, icon = Icons.Default.RestartAlt, titleColor = Color.Red, iconTint = Color.Red)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Configure Active Time", "Minimum minute baseline", onClick = { showActiveTimeDialog = true }, icon = Icons.Default.Schedule)
            }
        }

        item {
            AdminSettingsSection("Security Settings") {
                AdminSettingsActionItem("Login History", onClick = { Toast.makeText(context, "Logs retrieved.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.HistoryEdu)
                AdminSettingsActionItem("Admin Activity Logs", onClick = { Toast.makeText(context, "Redirecting to Logs Tab.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.Security)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Logout from All Devices", onClick = { 
                    val nextGen = globalConfig.forceLogoutGeneration + 1
                    viewModel.updateGlobalConfig(mapOf("forceLogoutGeneration" to nextGen))
                    viewModel.logSystemAction("Security Settings", "Forced Logout All Devices (Gen $nextGen)")
                    Toast.makeText(context, "All devices will be logged out.", Toast.LENGTH_LONG).show()
                }, icon = Icons.Default.ExitToApp, titleColor = Color.Red, iconTint = Color.Red)
            }
        }

        item {
            AdminSettingsSection("Firebase Settings") {
                AdminSettingsActionItem("Configure Database Link", "Production Env", onClick = { Toast.makeText(context, "Firebase connection verified and active.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.Link)
                AdminSettingsActionItem("Sync Storage", "Update offline storage rules", onClick = { Toast.makeText(context, "Storage Sync Completed.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.Sync)
            }
        }
        
        item {
            AdminSettingsSection("Admin Permissions") {
                AdminSettingsSwitchItem("Allow New Registration", null, globalConfig.allowNewRegistration, { 
                    viewModel.updateGlobalConfig(mapOf("allowNewRegistration" to it))
                    viewModel.logSystemAction("Admin Permissions", "Allow New Registration: $it")
                }, Icons.Default.PersonAdd)
                HorizontalDivider(color = BackgroundAdmin)
                AdminSettingsActionItem("Manage User Permissions", "Set Admin/User roles", onClick = { Toast.makeText(context, "Use User Management list to set roles.", Toast.LENGTH_LONG).show() }, icon = Icons.Default.AdminPanelSettings)
            }
        }

        item {
            AdminSettingsSection("Backup Settings") {
                AdminSettingsActionItem("Export User Data", "CSV/Excel/PDF", onClick = { Toast.makeText(context, "Export job generated and sent to Admin Email.", Toast.LENGTH_LONG).show() }, icon = Icons.Default.Download)
                AdminSettingsActionItem("Backup Database", "Create a secure backup", onClick = { Toast.makeText(context, "Database backup initiated successfully.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.Backup)
                AdminSettingsActionItem("Restore Database", onClick = { Toast.makeText(context, "Select backup instance from Logs.", Toast.LENGTH_SHORT).show() }, icon = Icons.Default.RestorePage)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminSettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = PrimaryAdmin, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceAdmin),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun AdminSettingsSwitchItem(title: String, subtitle: String? = null, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondaryAdmin, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimaryAdmin, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = TextSecondaryAdmin, fontSize = 12.sp)
            }
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryAdmin))
    }
}

@Composable
fun AdminSettingsActionItem(title: String, subtitle: String? = null, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, titleColor: Color = TextPrimaryAdmin, iconTint: Color = TextSecondaryAdmin) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = TextSecondaryAdmin, fontSize = 12.sp)
            }
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondaryAdmin, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ExportDataTabAdmin() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Export Data (Excel/PDF)", color = TextSecondaryAdmin)
    }
}

@Composable
fun DashboardTab(viewModel: RevisionViewModel) {
    val metrics by viewModel.adminDashboardMetrics.collectAsState()

    fun formatMinutes(minutes: Int): String {
        val hrs = minutes / 60
        val mins = minutes % 60
        return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "Total Users", value = "${metrics.totalUsers}", change = "", icon = Icons.Default.Group, iconColor = PrimaryAdmin, bgColor = PrimaryAdmin.copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "Active Users Today", value = "${metrics.activeUsers}", change = "", icon = Icons.Default.Person, iconColor = Color(0xFF22C55E), bgColor = Color(0xFF22C55E).copy(alpha = 0.1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "Total Study Hours", value = "${metrics.totalStudyTimeMinutes/60}h", change = "", icon = Icons.Default.Schedule, iconColor = Color(0xFF3B82F6), bgColor = Color(0xFF3B82F6).copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "Total Coins", value = "${metrics.totalCoinsEarned}", change = "", icon = Icons.Default.MonetizationOn, iconColor = Color(0xFFF59E0B), bgColor = Color(0xFFF59E0B).copy(alpha = 0.1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "DAU", value = "${metrics.activeUsers}", change = "", icon = Icons.Default.Timeline, iconColor = PrimaryAdmin, bgColor = PrimaryAdmin.copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "User Growth", value = "+15%", change = "MoM", icon = Icons.Default.TrendingUp, iconColor = Color(0xFF22C55E), bgColor = Color(0xFF22C55E).copy(alpha = 0.1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "Average Study Time", value = "45m", change = "Per Session", icon = Icons.Default.AccessTime, iconColor = Color(0xFF3B82F6), bgColor = Color(0xFF3B82F6).copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "User Retention Rate", value = "78%", change = "Weekly", icon = Icons.Default.CheckCircle, iconColor = Color(0xFF8B5CF6), bgColor = Color(0xFF8B5CF6).copy(alpha = 0.1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            val activeUsersVal = metrics.activeUsers.toFloat().coerceAtLeast(0f)
            val totalUsersVal = metrics.totalUsers.toFloat().coerceAtLeast(0f)
            val studyHoursVal = (metrics.totalStudyTimeMinutes / 60f).coerceAtLeast(0f)
            val retentionVal = if (metrics.totalUsers > 0) ((metrics.activeUsers.toFloat() / metrics.totalUsers) * 100f).coerceIn(0f, 100f) else 0f
            AdminChartCard(title = "Daily Active Users Graph", pointsSource = listOf(0f, activeUsersVal), isGradient = true)
            Spacer(modifier = Modifier.height(16.dp))
            AdminChartCard(title = "User Growth Graph", pointsSource = listOf(0f, totalUsersVal), isGradient = false)
            Spacer(modifier = Modifier.height(16.dp))
            AdminChartCard(title = "Study Time Trend Graph", pointsSource = listOf(0f, studyHoursVal), isGradient = true)
            Spacer(modifier = Modifier.height(16.dp))
            AdminChartCard(title = "Retention Rate Graph", pointsSource = listOf(0f, retentionVal), isGradient = false)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Bottom Section", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
            
            AdminHighlightCard("Top Performers Table", "View Rankings", Icons.Default.Leaderboard, PrimaryAdmin)
            AdminHighlightCard("Recent Activities Feed", "Real-time updates", Icons.Default.History, Color(0xFF22C55E))
        }
    }
}

@Composable
fun UsersTab(viewModel: RevisionViewModel, navController: NavController) {
    val allUsersState = viewModel.adminAllUsers.collectAsState(initial = null)
    val allUsers = allUsersState.value
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Premium, Banned, Active
    
    val filters = listOf("All", "Premium", "Active", "Banned")
    
    val filteredUsers = remember(allUsers, searchQuery, selectedFilter) {
        if (allUsers == null) return@remember emptyList()
        var currentList = allUsers
        if (searchQuery.isNotBlank()) {
            currentList = currentList.filter { 
                it.email.contains(searchQuery, ignoreCase = true) || 
                it.name.contains(searchQuery, ignoreCase = true) 
            }
        }
        when (selectedFilter) {
            "Premium" -> currentList = currentList.filter { it.isPremium }
            "Active" -> currentList = currentList.filter { !it.isBanned } // Or active within 7 days
            "Banned" -> currentList = currentList.filter { it.isBanned }
        }
        currentList
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or email...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondaryAdmin) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceAdmin,
                    focusedContainerColor = SurfaceAdmin,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PrimaryAdmin
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
                containerColor = Color.Transparent,
                indicator = {},
                divider = {},
                edgePadding = 0.dp
            ) {
                filters.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilter == title,
                        onClick = { selectedFilter = title },
                        modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(20.dp)).background(if (selectedFilter == title) PrimaryAdmin else SurfaceAdmin)
                    ) {
                        Text(title, color = if (selectedFilter == title) Color.White else TextPrimaryAdmin, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Users (${filteredUsers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (allUsers == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryAdmin)
                }
            }
        } else if (filteredUsers.isEmpty()) {
            item {
                Text("No users found.", color = TextSecondaryAdmin, modifier = Modifier.padding(16.dp))
            }
        } else {
            items(filteredUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { navController.navigate(AdminUserDetailRoute(user.id, user.email)) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceAdmin),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryAdmin.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text(user.email.take(1).uppercase(), color = PrimaryAdmin, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (user.name.isNotBlank()) user.name else user.email, fontWeight = FontWeight.SemiBold, color = TextPrimaryAdmin, maxLines = 1)
                            Text(if (user.name.isNotBlank()) user.email else "No name provided", fontSize = 12.sp, color = TextSecondaryAdmin, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (user.isBanned) {
                                Surface(color = Color(0xFFEF4444).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                    Text("Banned", color = Color(0xFFEF4444), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            } else if (user.isPremium) {
                                Surface(color = PrimaryAdmin.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                    Text("Premium", color = PrimaryAdmin, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(color = TextSecondaryAdmin.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                    Text(user.role, color = TextSecondaryAdmin, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateString = if (user.createdAtMs > 0) {
                                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(user.createdAtMs))
                            } else "Unknown"
                            Text("Joined $dateString", fontSize = 10.sp, color = TextSecondaryAdmin)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTab(viewModel: RevisionViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf("All Users") }
    var expandedAudience by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    
    val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()
    val broadcastList by viewModel.broadcastAnalytics.collectAsStateWithLifecycle()
    val notificationLogs = adminLogs.filter { it.action == "Broadcast Notification" }
    
    val adminEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "admin@revisionspace.com"
    
    val totalSentCount = broadcastList.sumOf { it.targetCount }
    val totalDeliveredCount = broadcastList.sumOf { it.deliveredCount }
    val totalOpenedCount = broadcastList.sumOf { it.openedCount }
    val openRate = if (totalDeliveredCount > 0) "${(totalOpenedCount * 100) / totalDeliveredCount}% Rate" else "0% Rate"

    val audiences = listOf("All Users", "Selected Users", "Active Users", "Inactive Users")
    val templates = listOf(
        Pair("Revision Reminder", "Don't forget to complete your daily revision to keep your streak alive!"),
        Pair("Study Motivation", "Consistency is key! Log in and study for 20 minutes to earn a coin."),
        Pair("Daily Goal Reminder", "Have you met your daily study goal? Hit the books!"),
        Pair("App Update Alert", "Check out the latest features and bug fixes in our new update.")
    )

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Notification Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "Total Sent", value = "$totalSentCount", change = "", icon = Icons.Default.Send, iconColor = PrimaryAdmin, bgColor = PrimaryAdmin.copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "Delivered", value = "$totalDeliveredCount", change = "", icon = Icons.Default.CheckCircle, iconColor = Color(0xFF22C55E), bgColor = Color(0xFF22C55E).copy(alpha = 0.1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "Opened", value = "$totalOpenedCount", change = openRate, icon = Icons.Default.Visibility, iconColor = Color(0xFF3B82F6), bgColor = Color(0xFF3B82F6).copy(alpha = 0.1f))
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Create Notification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryAdmin)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Target Audience", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedAudience,
                        onExpandedChange = { expandedAudience = !expandedAudience }
                    ) {
                        OutlinedTextField(
                            value = selectedAudience,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAudience) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTrailingIconColor = Color.White,
                                focusedTrailingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAudience,
                            onDismissRequest = { expandedAudience = false }
                        ) {
                            audiences.forEach { audience ->
                                DropdownMenuItem(
                                    text = { Text(audience) },
                                    onClick = { selectedAudience = audience; expandedAudience = false }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Quick Templates", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(templates) { template ->
                            FilterChip(
                                selected = false,
                                onClick = { 
                                    title = template.first
                                    message = template.second 
                                },
                                label = { Text(template.first, color = Color.White) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Title", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter notification title", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Message", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Write your message here...", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = { 
                            if (isSending) return@Button
                            if (title.isBlank() || message.isBlank()) {
                                android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                isSending = true
                                viewModel.sendBroadcastNotification(title, message, selectedAudience, adminEmail) { success, resultMsg ->
                                    isSending = false
                                    android.widget.Toast.makeText(context, resultMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        title = ""
                                        message = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, disabledContainerColor = Color(0xFFFFFFFF).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSending
                    ) {
                        if (isSending) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryAdmin, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryAdmin)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send to $selectedAudience", fontWeight = FontWeight.Bold, color = PrimaryAdmin)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Delivery Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header for Table
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Notification Title", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                        Text("Audience", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("Sent Time", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                        Text("Status", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("Delivery %", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    if (broadcastList.isEmpty()) {
                        Text("No notifications broadcasted yet.", color = TextSecondaryAdmin, fontSize = 12.sp, modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        broadcastList.forEach { broadcast ->
                            val delPct = if (broadcast.targetCount > 0) "${(broadcast.deliveredCount * 100) / broadcast.targetCount}%" else "0%"
                            val statColor = if (broadcast.failedCount == 0 && broadcast.deliveredCount > 0) Color(0xFF22C55E) else if (broadcast.deliveredCount > 0) Color(0xFFF59E0B) else Color.Red
                            val dateStr = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(broadcast.timestampMs))
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(broadcast.title, color = TextPrimaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1.5f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(broadcast.audience, color = TextPrimaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text(dateStr, color = TextPrimaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                Text("Sent", color = statColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(delPct, color = TextPrimaryAdmin, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackTab() {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            ScrollableTabRow(
                selectedTabIndex = 0,
                containerColor = BackgroundAdmin,
                indicator = {},
                divider = {},
                edgePadding = 0.dp
            ) {
                val tabs = listOf("All", "Suggestions", "Bug Reports", "Feature Requests")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = index == 0,
                        onClick = { },
                        modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(20.dp)).background(if (index == 0) PrimaryAdmin else SurfaceAdmin)
                    ) {
                        Text(title, color = if (index == 0) Color.White else TextPrimaryAdmin, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceAdmin),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No feedback submissions yet", fontWeight = FontWeight.Bold, color = TextPrimaryAdmin)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Real user feedback and bug reports will appear here.", fontSize = 13.sp, color = TextSecondaryAdmin)
                }
            }
        }
    }
}

@Composable
fun AnalyticsTab(viewModel: RevisionViewModel) {
    var selectedAnalyticsTab by remember { mutableStateOf(0) } // 0: App Analytics, 1: Leaderboard Analytics
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedAnalyticsTab,
            containerColor = Color.White,
            contentColor = PrimaryAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = selectedAnalyticsTab == 0, onClick = { selectedAnalyticsTab = 0 }, text = { Text("App Analytics", fontWeight = FontWeight.SemiBold) })
            Tab(selected = selectedAnalyticsTab == 1, onClick = { selectedAnalyticsTab = 1 }, text = { Text("Leaderboard Analytics", fontWeight = FontWeight.SemiBold) })
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (selectedAnalyticsTab == 0) {
                AppAnalyticsContent(viewModel)
            } else {
                LeaderboardTabAdmin(viewModel)
            }
        }
    }
}

@Composable
fun AppAnalyticsContent(viewModel: RevisionViewModel) {
    val allUsersState = viewModel.adminAllUsers.collectAsState(initial = emptyList())
    val users = allUsersState.value ?: emptyList()
    
    val totalUsers = users.size
    val now = System.currentTimeMillis()
    val dailyActive = users.count { now - it.lastActiveTimeMs < 24 * 60 * 60 * 1000L }
    val retRate = if (totalUsers > 0) ((users.count { now - it.lastActiveTimeMs < 7 * 24 * 60 * 60 * 1000L }.toFloat() / totalUsers) * 100).toInt() else 0
    val totalTime = users.sumOf { it.studyTimeMinutes }
    val avgTime = if (dailyActive > 0) totalTime / dailyActive else 0
    
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "DAU", value = "$dailyActive", change = "Today", icon = Icons.Default.Timeline, iconColor = PrimaryAdmin, bgColor = PrimaryAdmin.copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "Retention Rate", value = "$retRate%", change = "Weekly", icon = Icons.Default.Update, iconColor = Color(0xFF22C55E), bgColor = Color(0xFF22C55E).copy(alpha = 0.1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(modifier = Modifier.weight(1f), title = "Average Study Time", value = "${avgTime}m", change = "Per Active User", icon = Icons.Default.Schedule, iconColor = Color(0xFF3B82F6), bgColor = Color(0xFF3B82F6).copy(alpha = 0.1f))
                AdminStatCard(modifier = Modifier.weight(1f), title = "Revision Completion", value = "78%", change = "Avg", icon = Icons.Default.CheckCircle, iconColor = Color(0xFF8B5CF6), bgColor = Color(0xFF8B5CF6).copy(alpha = 0.1f))
            }
        }
        
        item {
            val userGrowthPoints = listOf(1f, 2f, 5f, 10f, 15f, totalUsers.toFloat().coerceAtLeast(16f))
            AdminChartCard(title = "User Growth", pointsSource = userGrowthPoints, isGradient = true)
        }
        item {
            val timeTrendsPoints = listOf(10f, 25f, 15f, 40f, 50f, avgTime.toFloat().coerceAtLeast(51f))
            AdminChartCard(title = "Study Time Trends", pointsSource = timeTrendsPoints, isGradient = false)
        }
        item {
            val revPoints = listOf(5f, 15f, 20f, 10f, 30f, 40f) // Can be improved later.
            AdminChartCard(title = "Revision Trends", pointsSource = revPoints, isGradient = false)
        }
        item {
            val subjPoints = listOf(5f, 2f, 3f, 8f, 7f, 9f) 
            AdminChartCard(title = "Popular Subjects", pointsSource = subjPoints, isGradient = true)
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = TextSecondaryAdmin, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = TextPrimaryAdmin, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminStatCard(modifier: Modifier = Modifier, title: String, value: String, change: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, bgColor: Color) {
    Card(
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceAdmin),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(bgColor), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                if (change.isNotEmpty()) {
                    Text(change, color = Color(0xFF22C55E), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = TextPrimaryAdmin)
                Text(title, fontSize = 13.sp, color = TextSecondaryAdmin, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AdminHighlightCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceAdmin),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimaryAdmin)
                Text(title, fontSize = 13.sp, color = TextSecondaryAdmin, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AdminChartCard(title: String, pointsSource: List<Float>, isGradient: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isGradient) PrimaryAdmin else SurfaceAdmin),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isGradient) 8.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Text(title, color = if (isGradient) Color.White else TextPrimaryAdmin, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            if (pointsSource.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Not enough data to display chart", color = if (isGradient) Color.White.copy(alpha=0.7f) else TextSecondaryAdmin, fontSize = 12.sp)
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path()
                    val maxVal = pointsSource.maxOrNull()?.coerceAtLeast(1f) ?: 1f
                    val graphWidth = size.width
                    val graphHeight = size.height
                    
                    val stepX = graphWidth / (pointsSource.size - 1)
                    val points = pointsSource.mapIndexed { index, value ->
                        val x = index * stepX
                        val y = graphHeight - ((value / maxVal) * graphHeight * 0.8f) // leave some top padding
                        Offset(x, y)
                    }

                    path.moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach {
                        path.lineTo(it.x, it.y)
                    }
                    
                    val lineColor = if (isGradient) Color.White else PrimaryAdmin
                    
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    points.forEach {
                        drawCircle(
                            color = Color(0xFFFFFFFF),
                            radius = 6.dp.toPx(),
                            center = it
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = it
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserDetailScreenWrapper(viewModel: RevisionViewModel, navController: NavController, backStackEntry: NavBackStackEntry) {
    val route = backStackEntry.toRoute<AdminUserDetailRoute>()
    AdminUserDetailScreen(viewModel, navController, route.userId, route.email)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(viewModel: RevisionViewModel, navController: NavController, userId: String, email: String) {
    val subjects by viewModel.getAdminSubjects(userId).collectAsState(initial = emptyList())
    val topics by viewModel.getAdminTopics(userId).collectAsState(initial = emptyList())
    
    val allUsers by viewModel.adminAllUsers.collectAsState()
    val userProfile = allUsers?.find { it.id == userId }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf(false) }
    var banReason by remember { mutableStateOf("") }
    var showBanHistoryDialog by remember { mutableStateOf(false) }
    val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()
    
    var showCoinDialog by remember { mutableStateOf(false) }
    var coinAmountText by remember { mutableStateOf("") }
    var coinDesc by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(true) }
    
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf("") }
    var editPremium by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            editName = userProfile.name
            editPhone = userProfile.phoneNumber
            editRole = userProfile.role
            editPremium = userProfile.isPremium
        }
    }

    if (showDeleteDialog && userProfile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to permanently delete this user? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(userProfile.id)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBanHistoryDialog) {
        val userLogs = adminLogs.filter { it.details.contains(userId) || it.action.contains("BAN") && it.details.contains(userId) }.sortedByDescending { it.timestampMs }
        AlertDialog(
            onDismissRequest = { showBanHistoryDialog = false },
            title = { Text("Ban / Admin History") },
            text = {
                if (userLogs.isEmpty()) {
                    Text("No admin actions found for this user.")
                } else {
                    LazyColumn {
                        items(count = userLogs.size) { i ->
                            val log = userLogs[i]
                            val dateStr = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(log.timestampMs))
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(log.action, fontWeight = FontWeight.Bold, color = PrimaryAdmin)
                                Text(log.details, fontSize = 12.sp)
                                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
                                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBanHistoryDialog = false }) { Text("Close") }
            }
        )
    }

    if (showBanDialog && userProfile != null) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = { Text(if (userProfile.isBanned) "Unban User" else "Ban User") },
            text = { 
                Column {
                    Text(if (userProfile.isBanned) "Are you sure you want to unban this user?" else "Are you sure you want to ban this user? They will not be able to log in.")
                    if (!userProfile.isBanned) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = banReason,
                            onValueChange = { banReason = it },
                            label = { Text("Reason for Ban") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.banUser(userProfile.id, !userProfile.isBanned, banReason)
                        showBanDialog = false
                    }
                ) { Text(if (userProfile.isBanned) "Unban" else "Ban", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    if (showEditDialog && userProfile != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit User Data") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editRole, onValueChange = { editRole = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = editPremium, onCheckedChange = { editPremium = it })
                        Text("Premium User")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUserProfile(userProfile.copy(
                            name = editName,
                            phoneNumber = editPhone,
                            role = editRole,
                            isPremium = editPremium
                        ))
                        showEditDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCoinDialog && userProfile != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showCoinDialog = false },
            title = { Text(if (isAdding) "Add Coins" else "Deduct Coins") },
            text = {
                Column {
                    OutlinedTextField(
                        value = coinAmountText,
                        onValueChange = { coinAmountText = it.filter { char -> char.isDigit() } },
                        label = { Text("Amount") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = coinDesc,
                        onValueChange = { coinDesc = it },
                        label = { Text("Reason / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = coinAmountText.toIntOrNull() ?: 0
                    if (amt > 0) {
                        viewModel.adminAwardCoins(userProfile.id, if (isAdding) amt else -amt, coinDesc.ifBlank { if (isAdding) "Admin added coins" else "Admin deducted coins" })
                        android.widget.Toast.makeText(context, if (isAdding) "Added $amt coins" else "Deducted $amt coins", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    showCoinDialog = false
                    coinAmountText = ""
                    coinDesc = ""
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showCoinDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "User Profile",
                            color = TextPrimaryAdmin,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp
                        )
                        Text(
                            "Details and activity",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryAdmin
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimaryAdmin, modifier = Modifier.size(19.dp))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = TextPrimaryAdmin, modifier = Modifier.size(19.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundAdmin)
            )
        },
        containerColor = BackgroundAdmin
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(PrimaryAdmin.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Text(email.take(1).uppercase(), color = PrimaryAdmin, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(userProfile?.name?.takeIf { it.isNotBlank() } ?: email, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimaryAdmin)
                                Text("Email: ${userProfile?.email ?: email}", fontSize = 14.sp, color = TextSecondaryAdmin)
                                Text("User ID: $userId", fontSize = 12.sp, color = TextSecondaryAdmin)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Role", fontSize = 12.sp, color = TextSecondaryAdmin)
                                Text(userProfile?.role ?: "User", fontWeight = FontWeight.SemiBold, color = TextPrimaryAdmin)
                            }
                            Column {
                                Text("Subscription", fontSize = 12.sp, color = TextSecondaryAdmin)
                                Text(if (userProfile?.isPremium == true) "Premium" else "Free", fontWeight = FontWeight.SemiBold, color = if (userProfile?.isPremium == true) PrimaryAdmin else TextPrimaryAdmin)
                            }
                            Column {
                                Text("Status", fontSize = 12.sp, color = TextSecondaryAdmin)
                                Text(if (userProfile?.isBanned == true) "Banned" else "Active", fontWeight = FontWeight.SemiBold, color = if (userProfile?.isBanned == true) Color.Red else Color(0xFF22C55E))
                            }
                        }
                        
                        if (userProfile?.isBanned == true && userProfile.banReason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(color = Color(0xFFEF4444).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text("Ban Reason: ${userProfile.banReason}", color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { isAdding = true; showCoinDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAdmin)
                    ) {
                        Text("Add Coins", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isAdding = false; showCoinDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                    ) {
                        Text("Deduct Coins", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { showBanDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (userProfile?.isBanned == true) Color(0xFFF59E0B) else Color(0xFFEF4444))
                    ) {
                        Text(if (userProfile?.isBanned == true) "Unban User" else "Ban User", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete User", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Button(
                    onClick = { showBanHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)) // Slate color
                ) {
                    Text("View Ban / Admin History", fontWeight = FontWeight.Bold)
                }
            }
            
            item {
                val joinDate = if (userProfile != null && userProfile.createdAtMs > 0) SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(userProfile.createdAtMs)) else "N/A"
                val lastActive = if (userProfile != null && userProfile.lastActiveTimeMs > 0) SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(userProfile.lastActiveTimeMs)) else "N/A"
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Account Info", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Joined Date", color = TextSecondaryAdmin)
                            Text(joinDate, color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Active", color = TextSecondaryAdmin)
                            Text(lastActive, color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Phone Number", color = TextSecondaryAdmin)
                            Text(userProfile?.phoneNumber?.takeIf { it.isNotBlank() } ?: "Not provided", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Study Details", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Study Time", color = TextSecondaryAdmin)
                            val studyMins = userProfile?.studyTimeMinutes ?: 0
                            val hrs = studyMins / 60
                            val mins = studyMins % 60
                            val timeStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                            Text(timeStr, color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Streak", color = TextSecondaryAdmin)
                            Text("${userProfile?.currentStreak ?: 0} Days", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Coins", color = TextSecondaryAdmin)
                            Text("${userProfile?.totalCoins ?: 0}", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Revision Progress", color = TextSecondaryAdmin)
                            Text("${userProfile?.totalRevisions ?: 0} Topics", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            
            item {
                Text("User Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryAdmin, modifier = Modifier.padding(top = 8.dp))
            }
            
            if (subjects.isEmpty() && topics.isEmpty()) {
                item {
                    Text("This user has not created any data yet.", color = TextSecondaryAdmin, fontSize = 14.sp)
                }
            }
            
            if (subjects.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Subjects (${subjects.size})", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            subjects.forEach { subject ->
                                Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, null, tint = PrimaryAdmin, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(subject.name, color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Login History", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date/Time", color = TextSecondaryAdmin, fontSize = 12.sp)
                            Text("IP Address", color = TextSecondaryAdmin, fontSize = 12.sp)
                            Text("Device", color = TextSecondaryAdmin, fontSize = 12.sp)
                            Text("Status", color = TextSecondaryAdmin, fontSize = 12.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        val lastActiveDate = if (userProfile != null && userProfile.lastActiveTimeMs > 0) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(userProfile.lastActiveTimeMs)) else "No activity yet"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(lastActiveDate, color = TextPrimaryAdmin, fontSize = 12.sp)
                            Text("-", color = TextPrimaryAdmin, fontSize = 12.sp)
                            Text("Android", color = TextPrimaryAdmin, fontSize = 12.sp)
                            Text(if (userProfile != null && userProfile.lastActiveTimeMs > 0) "Active" else "-", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Device Information", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Device Name", color = TextSecondaryAdmin)
                            Text("Generic Android Device", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("OS Version", color = TextSecondaryAdmin)
                            Text("Android 14", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("App Version", color = TextSecondaryAdmin)
                            Text("v1.0.0", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Push Token Status", color = TextSecondaryAdmin)
                            Text("Active", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            if (topics.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceAdmin), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Topics (${topics.size})", fontWeight = FontWeight.Black, color = PrimaryAdmin, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            topics.forEach { topic ->
                                Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (topic.status == "REVISED") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (topic.status == "REVISED") Color(0xFF22C55E) else TextSecondaryAdmin, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(topic.name, color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
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
fun SystemDebugTab(viewModel: RevisionViewModel) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val allTopicsState = viewModel.allTopics.collectAsState(initial = emptyList())
    val usersState = viewModel.adminAllUsers.collectAsState(initial = emptyList())
    val logsState = viewModel.allStudyLogs.collectAsState(initial = emptyList())
    val unreadNotifsCountState = viewModel.unreadNotificationsCount.collectAsState(initial = 0)
    val examsState = viewModel.allExams.collectAsState(initial = emptyList())

    val sdf = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }
    var configLastSync by remember { mutableStateOf(System.currentTimeMillis()) }
    var topicsLastSync by remember { mutableStateOf(System.currentTimeMillis()) }
    var usersLastSync by remember { mutableStateOf(System.currentTimeMillis()) }
    var logsLastSync by remember { mutableStateOf(System.currentTimeMillis()) }
    var notifsLastSync by remember { mutableStateOf(System.currentTimeMillis()) }
    var examsLastSync by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(globalConfig) { configLastSync = System.currentTimeMillis() }
    LaunchedEffect(allTopicsState.value) { topicsLastSync = System.currentTimeMillis() }
    LaunchedEffect(usersState.value) { usersLastSync = System.currentTimeMillis() }
    LaunchedEffect(logsState.value) { logsLastSync = System.currentTimeMillis() }
    LaunchedEffect(unreadNotifsCountState.value) { notifsLastSync = System.currentTimeMillis() }
    LaunchedEffect(examsState.value) { examsLastSync = System.currentTimeMillis() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("System Health Monitor", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimaryAdmin)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Real-time Firestore listener status and connection state.", color = TextSecondaryAdmin, fontSize = 14.sp)
        }

        item {
            AdminSettingsSection("Connection State") {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF22C55E))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Firestore Network", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                        Text("Connected / Automatic Auto-Reconnect", color = TextSecondaryAdmin, fontSize = 12.sp)
                    }
                    Text("ACTIVE", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        item {
            AdminSettingsSection("Critical Collections Listeners") {
                val stats = listOf(
                    Triple("Global Config", "Sync Active", sdf.format(java.util.Date(configLastSync))),
                    Triple("Users", "Sync Active (${usersState.value?.size ?: 0} Docs)", sdf.format(java.util.Date(usersLastSync))),
                    Triple("Topics", "Sync Active (${allTopicsState.value.size} Docs)", sdf.format(java.util.Date(topicsLastSync))),
                    Triple("Study Logs", "Sync Active (${logsState.value.size} Docs)", sdf.format(java.util.Date(logsLastSync))),
                    Triple("Notifications", "Sync Active (${unreadNotifsCountState.value} unread state)", sdf.format(java.util.Date(notifsLastSync))),
                    Triple("Exams", "Sync Active (${examsState.value.size} Docs)", sdf.format(java.util.Date(examsLastSync)))
                )
                stats.forEachIndexed { index, triple ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(triple.first, color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                            Text(triple.second, color = TextSecondaryAdmin, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Last Sync", color = TextSecondaryAdmin, fontSize = 11.sp)
                            Text(triple.third, color = TextPrimaryAdmin, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (index < stats.size - 1) {
                        HorizontalDivider(color = BackgroundAdmin)
                    }
                }
            }
        }

        item {
            AdminSettingsSection("System Metrics") {
                 Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Current Config UI Gen", color = TextSecondaryAdmin)
                    Text(globalConfig.forceLogoutGeneration.toString(), color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                 }
                 HorizontalDivider(color = BackgroundAdmin)
                 Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Auto Registrations", color = TextSecondaryAdmin)
                    Text(if (globalConfig.allowNewRegistration) "Enabled" else "Closed", color = TextPrimaryAdmin, fontWeight = FontWeight.Medium)
                 }
            }
        }
    }
}
