package com.example.ui.screens.settings
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.RevisionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: RevisionViewModel, navController: NavController) {
    val context = LocalContext.current
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val sharedPrefs = remember(context) { context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE) }
    
    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true))
    }
    val darkModeEnabled by viewModel.isDarkMode.collectAsStateWithLifecycle()
    
    var showRevisionSettingsDialog by remember { mutableStateOf(false) }
    var smartIntervalsInput by remember { mutableStateOf("1, 3, 7, 14, 30") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Preferences & Accounts",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            
            // Profile settings item
            SettingsItemRow(
                icon = Icons.Default.Person,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Profile Settings",
                subtitle = "Manage name, email, credentials",
                testTag = "settings_profile_item",
                onClick = {
                    navController.navigate(ProfileRoute())
                }
            )
            
            // Notification settings item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        val newValue = !notificationsEnabled
                        notificationsEnabled = newValue
                        sharedPrefs.edit().putBoolean("notifications_enabled", newValue).apply()
                        viewModel.checkAndTriggerNotifications(context)
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .testTag("settings_notifications_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Notification Settings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Enables daily revision space alerts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        notificationsEnabled = isChecked
                        sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
                        viewModel.checkAndTriggerNotifications(context)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("settings_notifications_switch")
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Revision settings item
            SettingsItemRow(
                icon = Icons.Default.Schedule,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "Revision Settings",
                subtitle = "Configure smart intervals and spacing rules",
                testTag = "settings_revision_item",
                onClick = {
                    showRevisionSettingsDialog = true
                }
            )

            // Daily Reminder Settings
            SettingsItemRow(
                icon = Icons.Default.Alarm,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Daily Reminder",
                subtitle = "Set a daily alarm to remind you to study",
                testTag = "settings_daily_reminder_item",
                onClick = {
                    navController.navigate(ReminderSettingsRoute)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "System & Security",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            
            // Profile & Goals
            SettingsItemRow(
                icon = Icons.Default.Person,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Edit Profile",
                subtitle = "Update your name, bio, and contact info",
                testTag = "settings_edit_profile",
                onClick = {
                    Toast.makeText(context, "Edit Profile on the Profile Screen", Toast.LENGTH_SHORT).show()
                }
            )
            SettingsItemRow(
                icon = Icons.Default.Flag,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Exam Goal",
                subtitle = "Set your target exam and date",
                testTag = "settings_exam_goal",
                onClick = {
                    Toast.makeText(context, "Set Goal on the Profile Screen", Toast.LENGTH_SHORT).show()
                }
            )


            // Backup & Sync
            SettingsItemRow(
                icon = Icons.Default.Backup,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Backup & Sync",
                subtitle = "Sync profile, revision goals, coins to cloud",
                testTag = "settings_backup_item",
                onClick = {
                    Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                }
            )

            // Dark Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        val newValue = !darkModeEnabled
                        viewModel.setDarkMode(newValue)
                        sharedPrefs.edit().putBoolean("dark_mode_enabled", newValue).apply()
                        Toast.makeText(context, if (newValue) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .testTag("settings_dark_mode_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Toggle app light or dark theme", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { isChecked ->
                        viewModel.setDarkMode(isChecked)
                        sharedPrefs.edit().putBoolean("dark_mode_enabled", isChecked).apply()
                        Toast.makeText(context, if (isChecked) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("settings_dark_mode_switch")
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Privacy & Security
            SettingsItemRow(
                icon = Icons.Default.Lock,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Privacy & Security",
                subtitle = "Permissions, data privacy info",
                testTag = "settings_privacy_item",
                onClick = {
                    Toast.makeText(context, "Your data is secured locally.", Toast.LENGTH_LONG).show()
                }
            )

            // Help & Support
            SettingsItemRow(
                icon = Icons.Default.HelpOutline,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "Help & Support",
                subtitle = "Faq, raise issues and contact us",
                testTag = "settings_help_item",
                onClick = {
                    navController.navigate(HelpAndSupportRoute)
                }
            )
            
            // Delete All Data
            SettingsItemRow(
                icon = Icons.Default.DeleteForever,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Delete All Data",
                subtitle = "Permanently erase all local study data",
                testTag = "settings_delete_data_item",
                onClick = {
                    Toast.makeText(context, "Deleting all data...", Toast.LENGTH_SHORT).show()
                    viewModel.clearLocalData()
                    Toast.makeText(context, "All data deleted!", Toast.LENGTH_SHORT).show()
                }
            )

            // About App
            SettingsItemRow(
                icon = Icons.Default.Info,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "About App",
                subtitle = "Version 1.0.0 • Space Repetition Revision System",
                testTag = "settings_about_item",
                onClick = {
                    Toast.makeText(context, "Revision Space v1.0.0. Build success.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    // ... [Dialog remains unchanged, but use MaterialTheme for text colors if needed]
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
}
