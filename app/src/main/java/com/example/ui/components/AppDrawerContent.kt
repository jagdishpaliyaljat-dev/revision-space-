package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.home.DashboardProfileAvatar
import com.example.viewmodel.RevisionViewModel

/** Every place the hamburger menu can take the user. */
enum class DrawerDestination {
    Home, Subjects, Syllabus, Focus, Tasks, Analytics, Profile, Coins, Streak, Settings, OfflineNotes, Language, Help
}

/**
 * Shared hamburger drawer content.
 *
 * Design goals: minimal (flat surfaces, one accent), premium (soft borders,
 * generous spacing) and mobile friendly (fixed 298dp sheet, single-column rows,
 * 44dp+ touch targets, no decorative art that pushes content down).
 */
@Composable
fun AppDrawerContent(
    viewModel: RevisionViewModel,
    currentRoute: String?,
    closeDrawer: () -> Unit,
    onNavigate: (DrawerDestination) -> Unit,
    onLogoutClick: () -> Unit
) {
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val allUsers by viewModel.adminAllUsers.collectAsStateWithLifecycle(initialValue = emptyList())

    val userName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "Aspirant"
    val streak = if (globalConfig.isStreakSystemEnabled) (userProfile?.currentStreak ?: 0) else 0
    val coins = userProfile?.totalCoins ?: 0

    val rank = remember(allUsers, userProfile) {
        val sorted = (allUsers ?: emptyList()).filter { !it.isBanned }
            .sortedByDescending { it.getOverallPerformanceScore() }
        val index = sorted.indexOfFirst { it.id == userProfile?.id }
        if (index != -1) index + 1 else null
    }

    val route = currentRoute.orEmpty()

    // Navigate first, then always close the sheet so the destination is visible.
    val goTo: (DrawerDestination) -> Unit = { destination ->
        onNavigate(destination)
        closeDrawer()
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp),
        modifier = Modifier.width(298.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---------- brand + close ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "REVISION SPACE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.3.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { closeDrawer() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // ---------- profile ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    DashboardProfileAvatar(
                        imageUrl = userProfile?.profilePictureUrl,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 26.dp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        userName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rank != null) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "AIR #$rank",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Keep learning, keep growing",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ---------- coins + streak ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DrawerStatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Paid,
                    iconTint = Color(0xFFF59E0B),
                    value = String.format("%,d", coins),
                    label = "Coins",
                    onClick = {
                        goTo(DrawerDestination.Coins)
                    }
                )
                DrawerStatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = Color(0xFFF97316),
                    value = streak.toString(),
                    label = "Day streak",
                    onClick = {
                        goTo(DrawerDestination.Streak)
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            DrawerDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // ---------- study ----------
            DrawerSectionLabel("STUDY")
            DrawerNavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = route.contains("DashboardRoute"),
                onClick = { goTo(DrawerDestination.Home) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.MenuBook,
                label = "Subjects",
                selected = route.contains("SubjectsRoute"),
                onClick = { goTo(DrawerDestination.Subjects) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Layers,
                label = "Syllabus Tracker",
                selected = route.contains("SyllabusRoute"),
                onClick = { goTo(DrawerDestination.Syllabus) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Timer,
                label = "Focus timer",
                selected = route.contains("TimerRoute"),
                onClick = { goTo(DrawerDestination.Focus) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Assignment,
                label = "Today's tasks",
                selected = route.contains("TodoListRoute"),
                onClick = { goTo(DrawerDestination.Tasks) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            DrawerDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // ---------- more ----------
            DrawerSectionLabel("MORE")
            DrawerNavItem(
                icon = Icons.Outlined.BarChart,
                label = "Analytics",
                selected = route.contains("AnalyticsRoute"),
                onClick = { goTo(DrawerDestination.Analytics) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Person,
                label = "Profile",
                selected = route.contains("ProfileRoute"),
                onClick = { goTo(DrawerDestination.Profile) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                selected = route.contains("SettingsRoute"),
                onClick = { goTo(DrawerDestination.Settings) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.SignalWifiOff,
                label = "Offline notes",
                selected = route.contains("OfflineNotesRoute"),
                onClick = { goTo(DrawerDestination.OfflineNotes) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Language,
                label = "Language",
                selected = route.contains("LanguageRoute"),
                onClick = { goTo(DrawerDestination.Language) }
            )
            DrawerNavItem(
                icon = Icons.Outlined.HeadsetMic,
                label = "Help & support",
                selected = route.contains("HelpAndSupportRoute"),
                onClick = { goTo(DrawerDestination.Help) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            DrawerDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // ---------- account ----------
            DrawerNavItem(
                icon = Icons.Outlined.Logout,
                label = "Logout",
                selected = false,
                danger = true,
                onClick = {
                    onLogoutClick()
                    closeDrawer()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

// ------------------------------------------------------------------ pieces

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, top = 6.dp, bottom = 4.dp)
    )
}

@Composable
private fun DrawerDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = when {
        danger -> MaterialTheme.colorScheme.error
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected || danger) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DrawerStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Column {
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
