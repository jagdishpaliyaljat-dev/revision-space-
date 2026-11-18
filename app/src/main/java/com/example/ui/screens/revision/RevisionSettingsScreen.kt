package com.example.ui.screens.revision
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

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.RevisionSettings
import com.example.viewmodel.RevisionViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionSettingsScreen(viewModel: RevisionViewModel, navController: NavController) {
    val settingsState by viewModel.revisionSettings.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var hasInitializedSettings by remember { mutableStateOf(false) }
    var patternInput by remember { mutableStateOf("1, 3, 7, 14, 30") }
    var autoSchedule by remember { mutableStateOf(true) }
    
    LaunchedEffect(userProfile, settingsState) {
        if (!hasInitializedSettings && (userProfile != null || settingsState != RevisionSettings())) {
            val profilePattern = userProfile?.revisionPattern
            val patternStr = if (profilePattern != null && profilePattern.isNotEmpty()) {
                profilePattern.joinToString(", ")
            } else {
                val list = listOf(
                    settingsState.firstRevisionDays,
                    settingsState.secondRevisionDays,
                    settingsState.thirdRevisionDays,
                    settingsState.fourthRevisionDays,
                    settingsState.fifthRevisionDays
                )
                list.joinToString(", ")
            }
            patternInput = patternStr
            autoSchedule = settingsState.smartRevisionEnabled
            hasInitializedSettings = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Column {
                        Text(
                            "Revision Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Spacing rules that fit you",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }},
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(19.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = {
                        patternInput = "1, 3, 7, 14, 30"
                        autoSchedule = true
                    }) {
                        Icon(Icons.Default.Restore, contentDescription = "Reset Defaults")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val validationResult = com.example.services.RevisionEngine.validatePatternInput(patternInput)
                    if (!validationResult.first) {
                        Toast.makeText(context, validationResult.second, Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }
                    
                    val parsedPattern = patternInput.split(",").map { it.trim().toInt() }
                    
                    viewModel.updateRevisionPattern(parsedPattern)
                    
                    val updatedSettings = settingsState.copy(
                        firstRevisionDays = parsedPattern.getOrElse(0) { 1 },
                        secondRevisionDays = parsedPattern.getOrElse(1) { 3 },
                        thirdRevisionDays = parsedPattern.getOrElse(2) { 7 },
                        fourthRevisionDays = parsedPattern.getOrElse(3) { 14 },
                        fifthRevisionDays = parsedPattern.getOrElse(4) { 30 },
                        smartRevisionEnabled = autoSchedule
                    )
                    viewModel.updateRevisionSettings(updatedSettings)
                    
                    Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Settings", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Revision Pattern
            SettingsCard(title = "Revision Pattern (Days)") {
                OutlinedTextField(
                    value = patternInput, onValueChange = { patternInput = it },
                    label = { Text("Intervals (comma separated)") }, modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("e.g. 1, 3, 7, 14, 30 (इसके बाद हर 30 दिन में स्वतः रिपीट होगा)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("शुरुआती अंतराल 1, 3, 7, 14, 30 दिन। इसके बाद हर 30 दिन बाद नियमित रिवीजन होगा।", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            // 3. Auto Schedule
            SettingsCard(title = "Auto Schedule") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Schedule ON", fontWeight = FontWeight.SemiBold)
                        Text("Automatically schedule topics for revision based on pattern.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(checked = autoSchedule, onCheckedChange = { autoSchedule = it })
                }
            }
            
            // 4. Data Management
            SettingsCard(title = "Data Management") {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Reset Revision Data (Coming soon)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Revision History (Coming soon)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Revision Pattern (Coming soon)")
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
