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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.BuildConfig
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: RevisionViewModel, navController: NavController) {
    val context = LocalContext.current
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val sharedPrefs = remember(context) { context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true))
    }
    val darkModeEnabled by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Dialog + transient state for the rows that need a confirmation or feedback.
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    // Notification permission (Android 13+) so the "turn on" switch actually
    // results in alerts instead of silently doing nothing.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.checkAndTriggerNotifications(context)
            Toast.makeText(context, "Notifications turned on", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Allow notifications in system settings to get alerts", Toast.LENGTH_LONG).show()
        }
    }

    fun applyNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
        if (!enabled) {
            Toast.makeText(context, "Notifications turned off", Toast.LENGTH_SHORT).show()
            return
        }
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.checkAndTriggerNotifications(context)
            Toast.makeText(context, "Notifications turned on", Toast.LENGTH_SHORT).show()
        }
    }

    // Every navigation goes through here so a bad/failed route can never look
    // like a dead button — the user always gets visible feedback.
    val navigateSafely: (Any) -> Unit = { route ->
        try {
            navController.navigate(route)
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't open that screen", Toast.LENGTH_SHORT).show()
        }
    }

    // Authoritative sign-in state (Firebase), not just the locally cached profile.
    val signedInEmail = remember {
        runCatching {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Personalise your study space",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .testTag("settings_back_button")
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
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel("Preferences & Accounts")

            // Google Sign-In item — becomes an account card once signed in.
            SettingsItemRow(
                icon = if (signedInEmail != null) Icons.Default.CheckCircle else Icons.Default.Login,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Google Sign-In",
                subtitle = if (signedInEmail != null) {
                    "Signed in as $signedInEmail"
                } else {
                    "Sign in to sync data to cloud"
                },
                testTag = "settings_google_signin_item",
                onClick = {
                    if (signedInEmail != null) {
                        showAccountDialog = true
                    } else {
                        Toast.makeText(context, "Choose a Google account to continue", Toast.LENGTH_SHORT).show()
                        navigateSafely(SignInRoute)
                    }
                }
            )

            // Profile settings item
            SettingsItemRow(
                icon = Icons.Default.Person,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Profile Settings",
                subtitle = "Manage name, email, credentials",
                testTag = "settings_profile_item",
                onClick = { navigateSafely(ProfileRoute()) }
            )

            // Notification settings item
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clickable { applyNotifications(!notificationsEnabled) }
                    .testTag("settings_notifications_toggle"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Notification Settings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text("Enables daily revision space alerts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isChecked -> applyNotifications(isChecked) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("settings_notifications_switch")
                    )
                }
            }

            // Revision settings item — opens the full revision settings screen.
            SettingsItemRow(
                icon = Icons.Default.Schedule,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "Revision Settings",
                subtitle = "Configure smart intervals and spacing rules",
                testTag = "settings_revision_item",
                onClick = { navigateSafely(RevisionSettingsRoute) }
            )

            // Daily Reminder Settings
            SettingsItemRow(
                icon = Icons.Default.Alarm,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Daily Reminder",
                subtitle = "Set a daily alarm to remind you to study",
                testTag = "settings_daily_reminder_item",
                onClick = { navigateSafely(ReminderSettingsRoute) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("System & Security")

            // Profile & Goals
            SettingsItemRow(
                icon = Icons.Default.Person,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Edit Profile",
                subtitle = "Update your name, bio, and contact info",
                testTag = "settings_edit_profile",
                onClick = { navigateSafely(ProfileRoute(openEditProfile = true)) }
            )
            SettingsItemRow(
                icon = Icons.Default.Flag,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Exam Goal",
                subtitle = "Set your target exam and date",
                testTag = "settings_exam_goal",
                onClick = { navigateSafely(ProfileRoute(openExamGoal = true)) }
            )

            // Backup & Sync
            SettingsItemRow(
                icon = Icons.Default.Backup,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Backup & Sync",
                subtitle = if (isSyncing) "Syncing your data…" else "Sync profile, revision goals, coins to cloud",
                testTag = "settings_backup_item",
                onClick = {
                    if (!isSyncing) {
                        isSyncing = true
                        viewModel.retrySync()
                        Toast.makeText(context, "Backup started", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(1400)
                            isSyncing = false
                            Toast.makeText(context, "Sync requested — changes will upload shortly", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            // Dark Mode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clickable {
                        val newValue = !darkModeEnabled
                        viewModel.setDarkMode(newValue)
                        sharedPrefs.edit().putBoolean("dark_mode_enabled", newValue).apply()
                        Toast.makeText(context, if (newValue) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
                    }
                    .testTag("settings_dark_mode_toggle"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Dark Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(1.dp))
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
            }

            // Privacy & Security
            SettingsItemRow(
                icon = Icons.Default.Lock,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Privacy & Security",
                subtitle = "Permissions, data privacy info",
                testTag = "settings_privacy_item",
                onClick = { showPrivacyDialog = true }
            )

            // Help & Support
            SettingsItemRow(
                icon = Icons.Default.HelpOutline,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "Help & Support",
                subtitle = "Faq, raise issues and contact us",
                testTag = "settings_help_item",
                onClick = { navigateSafely(HelpAndSupportRoute) }
            )

            // Delete All Data — always asks for confirmation first.
            SettingsItemRow(
                icon = Icons.Default.DeleteForever,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Delete All Data",
                subtitle = "Permanently erase all local study data",
                testTag = "settings_delete_data_item",
                onClick = { showDeleteDialog = true }
            )

            // About App
            SettingsItemRow(
                icon = Icons.Default.Info,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "About App",
                subtitle = "Version ${BuildConfig.VERSION_NAME} • Space Repetition Revision System",
                testTag = "settings_about_item",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ---------------- dialogs ----------------

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            modifier = Modifier.testTag("settings_account_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Google account", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text(
                        signedInEmail.orEmpty(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Your subjects, revisions, streak and coins are syncing to this account.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAccountDialog = false
                    navigateSafely(ProfileRoute())
                }) {
                    Text("Manage profile", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            modifier = Modifier.testTag("settings_delete_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete all data?", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Text(
                    "This permanently erases every subject, topic, revision history, note and streak stored on this device. This action cannot be undone.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.clearLocalData()
                        Toast.makeText(context, "All local data deleted", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.testTag("confirm_delete_data_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("cancel_delete_data_button")
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            modifier = Modifier.testTag("settings_privacy_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Privacy & Security", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrivacyLine("Your subjects, topics and revision history stay on this device by default.")
                    PrivacyLine("If you sign in with Google, your data is backed up to your private Firebase account.")
                    PrivacyLine("Notifications are local reminders — nothing is shared with advertisers.")
                    PrivacyLine("You can erase everything any time from Delete All Data.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Got it", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            modifier = Modifier.testTag("settings_about_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column {
                    Text("Revision Space", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text(
                        "Version ${BuildConfig.VERSION_NAME}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Text(
                    "A spaced-repetition planner for competitive exams. Plan subjects, track revisions, run focus sessions and keep your streak alive — all in one place.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
private fun PrivacyLine(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Small uppercase section label used to group settings rows. */
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 6.dp)
    )
}
