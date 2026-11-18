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

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import com.example.viewmodel.RevisionViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRevisionScreen(viewModel: RevisionViewModel, navController: NavController, onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    val context = LocalContext.current
    val allTopics by viewModel.allTopicsWithDetails.collectAsStateWithLifecycle()
    
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var expandedTopic by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Save Revision", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Topic", fontWeight = FontWeight.SemiBold)
            ExposedDropdownMenuBox(
                expanded = expandedTopic,
                onExpandedChange = { expandedTopic = !expandedTopic }
            ) {
                OutlinedTextField(
                    value = allTopics.find { it.topicId == selectedTopicId }?.topicName ?: "Select Topic",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTopic) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedTopic,
                    onDismissRequest = { expandedTopic = false }
                ) {
                    allTopics.forEach { topic ->
                        DropdownMenuItem(
                            text = { Text(topic.topicName) },
                            onClick = {
                                selectedTopicId = topic.topicId
                                duration = topic.estimatedRevisionTimeMins.toString()
                                expandedTopic = false
                            }
                        )
                    }
                }
            }

            Text("Duration (mins)", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { char -> char.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text("Notes (Optional)", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (isSaving) return@Button
                    val d = duration.toLongOrNull() ?: 0L
                    val safeTopicId = selectedTopicId
                    if (safeTopicId != null && d > 0) {
                        isSaving = true
                        scope.launch {
                            try {
                                viewModel.recordTimerSessionSuspend(
                                    topicId = safeTopicId,
                                    isRevision = true,
                                    durationMs = d * 60 * 1000L
                                )
                                Toast.makeText(context, "Revision Logged Successfully", Toast.LENGTH_SHORT).show()
                                onBack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to save revision: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please select topic and duration", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RevPrimary)
            ) {
                Text(if (isSaving) "Saving..." else "Save Revision")
            }
        }
    }
}
