package com.example.ui.screens.revision

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Flashcard
import com.example.data.SrsRating
import com.example.services.SrsCardEngine
import com.example.ui.TimerRoute
import com.example.viewmodel.RevisionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyScreen(
    viewModel: RevisionViewModel,
    navController: NavController,
    topicId: String = "",
    subjectId: String = ""
) {
    val context = LocalContext.current
    val allCards by viewModel.allFlashcards.collectAsStateWithLifecycle()
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()

    val studyDeck = remember(allCards, dueCards, topicId, subjectId) {
        val filtered = when {
            topicId.isNotBlank() -> allCards.filter { it.topicId == topicId }
            subjectId.isNotBlank() -> allCards.filter { it.subjectId == subjectId }
            else -> if (dueCards.isNotEmpty()) dueCards else allCards
        }
        filtered.ifEmpty { emptyList() }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var reviewedCount by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Smooth 3D card flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlipRotation"
    )

    val currentCard = studyDeck.getOrNull(currentIndex)
    val isDeckFinished = studyDeck.isNotEmpty() && currentIndex >= studyDeck.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SRS Flashcards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.3).sp
                        )
                        if (studyDeck.isNotEmpty() && !isDeckFinished) {
                            Text(
                                "Card ${currentIndex + 1} of ${studyDeck.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Card")
                    }
                    IconButton(onClick = {
                        navController.navigate(TimerRoute(topicId, true))
                    }) {
                        Icon(Icons.Default.Timer, contentDescription = "Open Focus Timer")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                studyDeck.isEmpty() -> {
                    // Empty state: no cards in deck
                    EmptyFlashcardDeckState(
                        topicId = topicId,
                        onAddCard = { showAddDialog = true }
                    )
                }
                isDeckFinished -> {
                    // Completed state
                    FlashcardSessionSummary(
                        totalReviewed = reviewedCount,
                        correctCount = correctCount,
                        onRestart = {
                            currentIndex = 0
                            isFlipped = false
                            reviewedCount = 0
                            correctCount = 0
                        },
                        onFinish = { navController.popBackStack() }
                    )
                }
                currentCard != null -> {
                    // Active Flashcard
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { (currentIndex.toFloat()) / studyDeck.size.coerceAtLeast(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Flashcard surface with 3D Flip
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isFlipped = !isFlipped
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                // FRONT OF CARD
                                FlashcardFrontView(
                                    card = currentCard,
                                    showHint = showHint,
                                    onToggleHint = { showHint = !showHint }
                                )
                            } else {
                                // BACK OF CARD (mirrored rotationY so text renders right-side)
                                Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                                    FlashcardBackView(card = currentCard)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rating controls (only enabled when flipped or visible)
                        if (!isFlipped) {
                            Button(
                                onClick = { isFlipped = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Flip, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tap to Reveal Answer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        } else {
                            FlashcardRatingBar(
                                card = currentCard,
                                onRate = { rating ->
                                    viewModel.reviewFlashcard(currentCard, rating)
                                    reviewedCount++
                                    if (rating == SrsRating.GOOD || rating == SrsRating.EASY) {
                                        correctCount++
                                    }
                                    // Move to next card
                                    isFlipped = false
                                    showHint = false
                                    currentIndex++
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateFlashcardDialog(
            defaultTopicId = topicId,
            defaultSubjectId = subjectId,
            onDismiss = { showAddDialog = false },
            onSave = { card ->
                viewModel.saveFlashcard(card)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FlashcardFrontView(
    card: Flashcard,
    showHint: Boolean,
    onToggleHint: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        card.difficulty.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        SrsCardEngine.getMasteryStage(card),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                card.front.ifBlank { "No prompt available" },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (card.hint.isNotBlank()) {
                    TextButton(onClick = onToggleHint) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showHint) "Hide Hint" else "Show Hint", fontSize = 13.sp)
                    }
                    if (showHint) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                card.hint,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap anywhere on card to flip",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun FlashcardBackView(card: Flashcard) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "ANSWER",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                card.back.ifBlank { "No explanation recorded" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Interval: ${card.intervalDays}d",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Ease: ${String.format(java.util.Locale.US, "%.1f", card.easeFactor)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Reps: ${card.repetitionCount}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardRatingBar(
    card: Flashcard,
    onRate: (SrsRating) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // AGAIN
        val againDays = SrsCardEngine.predictInterval(card, SrsRating.AGAIN)
        Button(
            onClick = { onRate(SrsRating.AGAIN) },
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Again", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(SrsCardEngine.formatInterval(againDays), fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        // HARD
        val hardDays = SrsCardEngine.predictInterval(card, SrsRating.HARD)
        Button(
            onClick = { onRate(SrsRating.HARD) },
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hard", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(SrsCardEngine.formatInterval(hardDays), fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        // GOOD
        val goodDays = SrsCardEngine.predictInterval(card, SrsRating.GOOD)
        Button(
            onClick = { onRate(SrsRating.GOOD) },
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Good", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(SrsCardEngine.formatInterval(goodDays), fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        // EASY
        val easyDays = SrsCardEngine.predictInterval(card, SrsRating.EASY)
        Button(
            onClick = { onRate(SrsRating.EASY) },
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Easy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(SrsCardEngine.formatInterval(easyDays), fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun FlashcardSessionSummary(
    totalReviewed: Int,
    correctCount: Int,
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Deck Completed!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Great job! All spaced flashcards for this session are revised.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalReviewed", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Cards Revised", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val accuracy = if (totalReviewed > 0) ((correctCount.toDouble() / totalReviewed) * 100).toInt() else 100
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$accuracy%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Text("Retention", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Revisions", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Study Deck Again")
            }
        }
    }
}

@Composable
fun EmptyFlashcardDeckState(
    topicId: String,
    onAddCard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🗂️",
                fontSize = 44.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No Flashcards Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Create your first spaced repetition flashcard to memorize key formulas, facts, and definitions effortlessly.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddCard,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Flashcard")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlashcardDialog(
    defaultTopicId: String = "",
    defaultSubjectId: String = "",
    onDismiss: () -> Unit,
    onSave: (Flashcard) -> Unit
) {
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var hintText by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New SRS Flashcard", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = frontText,
                    onValueChange = {
                        frontText = it
                        errorMsg = null
                    },
                    label = { Text("Front (Question or Prompt)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = backText,
                    onValueChange = {
                        backText = it
                        errorMsg = null
                    },
                    label = { Text("Back (Answer or Explanation)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = hintText,
                    onValueChange = { hintText = it },
                    label = { Text("Hint (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Difficulty:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff) }
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (frontText.isBlank() || backText.isBlank()) {
                        errorMsg = "Both Question (Front) and Answer (Back) are required."
                        return@Button
                    }
                    val newCard = Flashcard(
                        id = java.util.UUID.randomUUID().toString(),
                        topicId = defaultTopicId,
                        subjectId = defaultSubjectId,
                        front = frontText.trim(),
                        back = backText.trim(),
                        hint = hintText.trim(),
                        difficulty = difficulty,
                        intervalDays = 1,
                        easeFactor = 2.5f,
                        repetitionCount = 0,
                        nextReviewDateMs = System.currentTimeMillis(),
                        createdAtMs = System.currentTimeMillis()
                    )
                    onSave(newCard)
                }
            ) {
                Text("Add Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
