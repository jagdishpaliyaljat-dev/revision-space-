package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.Subject
import com.example.data.TopicWithSubjectAndChapter
import com.example.data.UserProfile
import com.example.data.chat.StudySnapshot
import com.example.data.chat.StudyTopicRef
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.ProfileStats
import com.example.viewmodel.RevisionViewModel
import com.example.viewmodel.StudyChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyChatScreen(
    revisionViewModel: RevisionViewModel,
    navController: NavController,
    chatViewModel: StudyChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isSending by chatViewModel.isSending.collectAsStateWithLifecycle()
    val error by chatViewModel.error.collectAsStateWithLifecycle()

    val subjects by revisionViewModel.allSubjects.collectAsStateWithLifecycle(emptyList())
    val topics by revisionViewModel.allTopicsWithDetails.collectAsStateWithLifecycle(emptyList())
    val revisions by revisionViewModel.allRevisions.collectAsStateWithLifecycle(emptyList())
    val profile by revisionViewModel.currentUserProfile.collectAsStateWithLifecycle()
    val stats by revisionViewModel.profileStats.collectAsStateWithLifecycle()

    val studyContext = remember(subjects, topics, revisions, profile, stats) {
        val pending = topics.filter { !it.status.startsWith("Complet", ignoreCase = true) }
        val now = System.currentTimeMillis()
        val overdue = revisions.filter {
            it.status != "Completed" && it.nextRevisionDateMs in 1L until now
        }
        com.example.data.chat.StudyChatPrompt.context(
            StudySnapshot(
                studentName = profile?.name.orEmpty(),
                currentStreak = profile?.currentStreak ?: 0,
                coins = profile?.totalCoins ?: 0,
                studyHours = stats.displayStudyHours,
                revisionsDone = stats.displayRevisions,
                subjects = subjects.map { it.name },
                pendingTopics = pending.take(25).map {
                    StudyTopicRef(
                        subject = it.subjectName,
                        chapter = it.chapterName,
                        topic = it.topicName,
                        status = it.status.ifBlank { "Pending" }
                    )
                },
                overdueTopics = overdue.take(10).map { it.topicName }
            )
        )
    }

    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    fun sendMessage() {
        val text = input.trim()
        if (text.isEmpty() || isSending) return
        chatViewModel.send(text, studyContext)
        input = ""
    }

    LaunchedEffect(messages.size, isSending) {
        val target = messages.size + if (isSending) 1 else 0
        if (target > 0) listState.animateScrollToItem(target - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Study Buddy", fontWeight = FontWeight.Bold)
                        Text(
                            "AI tutor for your revision",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { chatViewModel.clearChat() }) { Text("Clear") }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (messages.isEmpty()) {
                EmptyChatState(
                    onSuggestionClick = { chatViewModel.send(it, studyContext) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { message -> ChatBubble(message) }
                    if (isSending) item { TypingBubble() }
                }
            }

            error?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { chatViewModel.clearError() }) { Text("OK") }
                    }
                }
            }

            InputBar(
                value = input,
                onValueChange = { input = it },
                isSending = isSending,
                onSend = { sendMessage() }
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Text("Thinking…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything about your studies…") },
                maxLines = 4,
                shape = RoundedCornerShape(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSending,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun EmptyChatState(onSuggestionClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val suggestions = listOf(
        "Explain photosynthesis in simple words",
        "Make a 7-day revision plan for my subjects",
        "Give me 5 quick quiz questions on my weakest topic",
        "How can I improve my memory retention?"
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Hi! I'm Study Buddy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Ask me to explain concepts, make quizzes, or plan your revision.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        suggestions.forEach { s ->
            OutlinedButton(
                onClick = { onSuggestionClick(s) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(s, fontSize = 13.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
