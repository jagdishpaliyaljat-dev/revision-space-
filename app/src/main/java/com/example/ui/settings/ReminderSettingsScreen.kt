package com.example.ui.settings
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
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.notification.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) { 
        context.getSharedPreferences("revision_app_prefs", Context.MODE_PRIVATE) 
    }

    var hour by remember { mutableIntStateOf(sharedPrefs.getInt("reminder_hour", 20)) }
    var minute by remember { mutableIntStateOf(sharedPrefs.getInt("reminder_minute", 0)) }
    var isReminderEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("reminder_enabled", false)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isReminderEnabled = true
            sharedPrefs.edit().putBoolean("reminder_enabled", true).apply()
            ReminderScheduler.scheduleDailyReminder(context, hour, minute)
            Toast.makeText(context, "Reminder enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show()
            isReminderEnabled = false
            sharedPrefs.edit().putBoolean("reminder_enabled", false).apply()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Reminder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
                    Text("Enable Daily Reminder", style = MaterialTheme.typography.titleMedium)
                }
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                isReminderEnabled = true
                                sharedPrefs.edit().putBoolean("reminder_enabled", true).apply()
                                ReminderScheduler.scheduleDailyReminder(context, hour, minute)
                                Toast.makeText(context, "Reminder enabled", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            isReminderEnabled = false
                            sharedPrefs.edit().putBoolean("reminder_enabled", false).apply()
                            ReminderScheduler.cancelReminder(context)
                            Toast.makeText(context, "Reminder disabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            if (isReminderEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    hour = selectedHour
                                    minute = selectedMinute
                                    sharedPrefs.edit()
                                        .putInt("reminder_hour", hour)
                                        .putInt("reminder_minute", minute)
                                        .apply()
                                    ReminderScheduler.rescheduleReminder(context, hour, minute)
                                },
                                hour,
                                minute,
                                false
                            ).show()
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Reminder Time", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        val amPm = if (hour >= 12) "PM" else "AM"
                        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        val displayMinute = minute.toString().padStart(2, '0')
                        Text(
                            text = "$displayHour:$displayMinute $amPm",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You will receive a notification every day at the selected time to remind you to complete your study revision sessions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
