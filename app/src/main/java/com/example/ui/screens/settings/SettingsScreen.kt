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
import android.content.Intent
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.BuildConfig
import com.example.data.backup.BackupFormat
import com.example.data.backup.BackupFormatException
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupPayload
import com.example.data.backup.ImportMode
import com.example.data.backup.ImportOutcome
import com.example.notification.ReminderScheduler
import com.example.services.SrsNotificationManager
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    // ---- Local file backup & restore (JSON via the system file picker) ----
    var exportInProgress by remember { mutableStateOf(false) }
    var importInProgress by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<PendingBackup?>(null) }
    var importOutcome by remember { mutableStateOf<ImportOutcome?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }
    var lastBackupStatus by remember {
        mutableStateOf(sharedPrefs.getString("last_backup_status", null))
    }
    // Newest automatic pre-import copy, used by the "Undo last import" row.
    var safetySnapshot by remember { mutableStateOf<BackupManager.SafetySnapshot?>(null) }
    var safetyLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        safetySnapshot = withContext(Dispatchers.IO) {
            runCatching { BackupManager.latestSafetySnapshot(context) }.getOrNull()
        }
    }

    fun setBackupStatus(text: String) {
        lastBackupStatus = text
        sharedPrefs.edit().putString("last_backup_status", text).apply()
    }

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

    // Export: the user picks where to save. We generate the JSON, verify that we
    // can parse what we are about to write, and only then write it to the file.
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(BackupFormat.MIME_TYPE)
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        exportInProgress = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val db = runCatching { com.example.MyApplication.database }.getOrNull()
                        ?: throw IllegalStateException("Local database is not ready. Please restart the app.")
                    val json = BackupManager.createBackup(context, db, BuildConfig.VERSION_NAME)
                    val rows = BackupFormat.decode(json).totalRows
                    BackupManager.writeBackupFile(context, uri, json)
                    rows
                }
            }
            exportInProgress = false
            result.onSuccess { rows ->
                setBackupStatus("Last backup: $rows records")
                Toast.makeText(context, "Backup saved — $rows records exported", Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                Toast.makeText(context, error.message ?: "Export failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Import step 1: read + validate the file. Nothing is written yet; the user
    // first sees what the file contains and chooses Merge or Replace.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importInProgress = true
        scope.launch {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val fileName = BackupManager.displayName(context, uri) ?: "backup.json"
                    val payload = BackupFormat.decode(BackupManager.readBackupFile(context, uri))
                    PendingBackup(fileName, payload)
                }
            }
            importInProgress = false
            result.onSuccess { pending -> pendingImport = pending }
                .onFailure { error ->
                    importError = when (error) {
                        is BackupFormatException -> error.message ?: "Invalid backup file"
                        else -> error.message ?: "Couldn't read the selected file"
                    }
                }
        }
    }

    // Import step 2: apply the already validated payload in one transaction.
    fun runImport(payload: BackupPayload, mode: ImportMode) {
        pendingImport = null
        importInProgress = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val db = runCatching { com.example.MyApplication.database }.getOrNull()
                        ?: throw IllegalStateException("Local database is not ready. Please restart the app.")
                    // Safety net: copy the current data before anything is touched.
                    // If this fails we still import — it must never block the user.
                    val safety = runCatching {
                        BackupManager.saveSafetySnapshot(context, db, BuildConfig.VERSION_NAME)
                    }.getOrNull()
                    val outcome = BackupManager.importBackup(context, db, payload, mode)
                    outcome to safety
                }
            }
            importInProgress = false
            result.onSuccess { (outcome, safety) ->
                safety?.let { safetySnapshot = it }
                // Settings live in SharedPreferences, so reflect them right away.
                notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", notificationsEnabled)
                runCatching {
                    viewModel.setDarkMode(sharedPrefs.getBoolean("dark_mode_enabled", darkModeEnabled))
                }
                // Imported reminder settings must actually schedule (or cancel) alarms.
                runCatching {
                    if (sharedPrefs.getBoolean("reminder_enabled", false)) {
                        ReminderScheduler.scheduleDailyReminder(
                            context,
                            sharedPrefs.getInt("reminder_hour", 20),
                            sharedPrefs.getInt("reminder_minute", 0)
                        )
                    } else {
                        ReminderScheduler.cancelReminder(context)
                    }
                    SrsNotificationManager.scheduleDailyMission(context)
                }
                setBackupStatus("Last import: ${outcome.totalImported} records")
                importOutcome = outcome
            }.onFailure { error ->
                importError = error.message ?: "Import failed. Your data was not changed."
            }
        }
    }

    // Opens the automatic pre-import copy in the same preview dialog used by a
    // normal import, so the user can review it before restoring anything.
    fun openSafetySnapshot() {
        val snapshot = safetySnapshot ?: return
        if (importInProgress || exportInProgress) return
        safetyLoading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { BackupManager.readSafetySnapshot(snapshot) }
            }
            safetyLoading = false
            result.onSuccess { payload ->
                pendingImport = PendingBackup(snapshot.name, payload)
            }.onFailure { error ->
                importError = error.message ?: "Couldn't read the safety copy."
            }
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

            // Local file backup & restore — works fully offline, no account needed.
            SectionLabel("Backup & Restore")
            SettingsItemRow(
                icon = Icons.Default.FileUpload,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "Export Data",
                subtitle = if (exportInProgress) {
                    "Preparing your backup file…"
                } else {
                    "Save all subjects, topics, history and settings to a file"
                },
                testTag = "settings_export_data_item",
                onClick = {
                    if (!exportInProgress && !importInProgress) {
                        exportLauncher.launch(BackupFormat.fileName())
                    }
                }
            )
            SettingsItemRow(
                icon = Icons.Default.FileDownload,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Import Data",
                subtitle = if (importInProgress) {
                    "Reading backup file…"
                } else {
                    "Restore from a Revision Space backup file"
                },
                testTag = "settings_import_data_item",
                onClick = {
                    if (!importInProgress && !exportInProgress) {
                        importLauncher.launch(
                            arrayOf(
                                "application/json",
                                "text/plain",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    }
                }
            )
            if (safetySnapshot != null) {
                SettingsItemRow(
                    icon = Icons.Default.Restore,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = if (safetyLoading) "Opening safety copy…" else "Undo last import",
                    subtitle = "Restore the copy saved before your last import (" +
                        formatBackupDate(safetySnapshot!!.modifiedAtMs) + ")",
                    testTag = "settings_restore_safety_item",
                    onClick = { openSafetySnapshot() }
                )
            }
            if (lastBackupStatus != null) {
                Text(
                    lastBackupStatus!!,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 22.dp, vertical = 4.dp)
                        .testTag("settings_last_backup_status")
                )
            }

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

            Spacer(modifier = Modifier.height(8.dp))
            SectionLabel("Account")

            // Logout — same behaviour as the drawer, with a confirmation first.
            SettingsItemRow(
                icon = Icons.Default.Logout,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Logout",
                subtitle = if (signedInEmail != null) {
                    "Sign out of $signedInEmail"
                } else {
                    "Sign out of this device"
                },
                testTag = "settings_logout_item",
                onClick = { showLogoutDialog = true }
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            modifier = Modifier.testTag("settings_logout_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            title = { Text("Logout?", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Text(
                    "You will be signed out of this device. Your study data stays safe and can be synced again after signing in.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(context) {
                            try {
                                navController.navigate(SignInRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Couldn't open the sign-in screen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_settings_logout_button")
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.testTag("cancel_settings_logout_button")
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ---- Import preview: nothing is written until the user picks a strategy ----
    pendingImport?.let { pending ->
        AlertDialog(
            onDismissRequest = { if (!importInProgress) pendingImport = null },
            modifier = Modifier.testTag("settings_import_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Restore backup?", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text(
                        pending.fileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Created ${formatBackupDate(pending.payload.createdAtMs)} • App v${pending.payload.appVersion}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 170.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        pending.payload.rowCounts().forEach { (table, count) ->
                            if (count > 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        BackupFormat.labelFor(table),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "$count",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "${pending.payload.totalRows} records in total. Merge keeps your current data; Replace deletes it first. A safety copy is saved automatically, so this can be undone.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { runImport(pending.payload, ImportMode.REPLACE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_import_replace_button")
                    ) {
                        Text(
                            "Replace all data",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { runImport(pending.payload, ImportMode.MERGE) },
                    modifier = Modifier.testTag("confirm_import_merge_button")
                ) {
                    Text("Merge", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingImport = null },
                    modifier = Modifier.testTag("cancel_import_button")
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ---- Import result ----
    importOutcome?.let { outcome ->
        AlertDialog(
            onDismissRequest = { importOutcome = null },
            modifier = Modifier.testTag("settings_import_result_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Import complete", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text(
                        "${outcome.totalImported} records restored on this device.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (outcome.clearedRows > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${outcome.clearedRows} older records were removed first.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "A safety copy of your previous data is kept on this device — use \"Undo last import\" in Settings to go back.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        outcome.importedRows.forEach { (table, count) ->
                            if (count > 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        BackupFormat.labelFor(table),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "$count",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    if (outcome.skippedTables.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Not supported by this app version: " +
                                outcome.skippedTables.joinToString { BackupFormat.labelFor(it) },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (outcome.failedRows > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${outcome.failedRows} records could not be restored.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { importOutcome = null },
                    modifier = Modifier.testTag("dismiss_import_result_button")
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // ---- Import error (bad file, unreadable, database not ready) ----
    importError?.let { message ->
        AlertDialog(
            onDismissRequest = { importError = null },
            modifier = Modifier.testTag("settings_import_error_dialog"),
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Can't restore this file", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your current data was not changed. Choose a backup file created by Revision Space (Export Data).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { importError = null },
                    modifier = Modifier.testTag("dismiss_import_error_button")
                ) {
                    Text("OK", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

/** A parsed backup waiting for the user to choose Merge or Replace. */
private data class PendingBackup(
    val fileName: String,
    val payload: BackupPayload
)

private fun formatBackupDate(createdAtMs: Long): String {
    if (createdAtMs <= 0L) return "unknown date"
    return try {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(createdAtMs))
    } catch (e: Exception) {
        "unknown date"
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
