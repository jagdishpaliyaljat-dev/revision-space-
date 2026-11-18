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

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.viewmodel.RevisionViewModel
import com.airbnb.lottie.compose.LottieConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Redesigned premium colors to match prompt spec
private val ScreenBackground = Color(0xFFFFFFFF)
private val CardBackground = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1E1B4B)
private val TextGray = Color(0xFF64748B)

private val PurplePrimary = Color(0xFF7C4DFF)
private val PurpleGlow = Color(0xFF7C4DFF).copy(alpha = 0.15f)
private val PurpleLight = Color(0xFFF3E8FF)

private val TrophyBadgeGreen = Color(0xFF10B981)

@Composable
fun SessionCompleteScreen(
    viewModel: RevisionViewModel,
    navController: NavController,
    studyTimeAddedMins: Int = 25,
    topicsRevised: Int = 2,
    coinsEarned: Int = 1,
    onBackToHome: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isActionInProgress by remember { mutableStateOf(false) }
    
    // Animation triggers
    var startAnimation by remember { mutableStateOf(false) }
    var hasPlayedConfetti by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Trophy pulse and float loops
    val infiniteTransition = rememberInfiniteTransition(label = "trophyFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Staggered entering transitions for cards
    val cardAlpha1 by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 100),
        label = "cardAlpha1"
    )
    val cardOffsetY1 by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 24.dp,
        animationSpec = tween(500, delayMillis = 100),
        label = "cardOffsetY1"
    )

    val cardAlpha2 by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 250),
        label = "cardAlpha2"
    )
    val cardOffsetY2 by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 24.dp,
        animationSpec = tween(500, delayMillis = 250),
        label = "cardOffsetY2"
    )

    val cardAlpha3 by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 400),
        label = "cardAlpha3"
    )
    val cardOffsetY3 by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 24.dp,
        animationSpec = tween(500, delayMillis = 400),
        label = "cardOffsetY3"
    )

    val buttonsAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 550),
        label = "buttonsAlpha"
    )
    val buttonsOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 24.dp,
        animationSpec = tween(500, delayMillis = 550),
        label = "buttonsOffsetY"
    )

    // Animate Coin Count increasing (Coin Wallet card requirement)
    val targetCoins = userProfile?.totalCoins ?: 1250
    val startCoins = (targetCoins - coinsEarned).coerceAtLeast(0)
    var animatedCoins by remember { mutableStateOf(startCoins) }
    LaunchedEffect(targetCoins) {
        animate(
            initialValue = startCoins.toFloat(),
            targetValue = targetCoins.toFloat(),
            animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedCoins = value.toInt()
        }
    }

    Scaffold(
        containerColor = ScreenBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
        ) {
            // Confetti Burst Background
            if (startAnimation && !hasPlayedConfetti) {
                ConfettiAnimation(
                    onFinished = {
                        hasPlayedConfetti = true
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Top Hero Section
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .offset(y = floatOffset.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft glowing background aura
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(PurplePrimary.copy(alpha = 0.18f), Color.Transparent)
                            ),
                            radius = size.width / 1.4f
                        )
                    }

                    // Modern Lottie Animated Congrats Badge
                    CongratsBadgeLottieAnimation(
                        size = 160.dp,
                        iterations = LottieConstants.IterateForever
                    )

                    // Green Verified Badge overlapping the trophy
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-16).dp, y = (-16).dp)
                            .size(38.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(2.dp)
                            .background(TrophyBadgeGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎉 Session Complete!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Great job, Aspirant!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rounded motivational chip
                Box(
                    modifier = Modifier
                        .background(PurpleLight, RoundedCornerShape(100.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = PurplePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Consistency today, success tomorrow!",
                            color = PurplePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Cards and visual panels (spaced strictly at 20.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Card 1 — Session Summary
                    PremiumCard(
                        title = "Session Summary",
                        alpha = cardAlpha1,
                        offsetY = cardOffsetY1
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Schedule,
                                iconColor = PurplePrimary,
                                bgColor = Color(0xFFF5F3FF),
                                label = "Study Time",
                                value = "$studyTimeAddedMins min"
                            )
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.MenuBook,
                                iconColor = Color(0xFF3B82F6),
                                bgColor = Color(0xFFEFF6FF),
                                label = "Topics Revised",
                                value = "$topicsRevised"
                            )
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CheckCircle,
                                iconColor = Color(0xFF10B981),
                                bgColor = Color(0xFFECFDF5),
                                label = "Goal Status",
                                value = "Completed"
                            )
                            StatItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Stars,
                                iconColor = Color(0xFFFFB020),
                                bgColor = Color(0xFFFEF3C7),
                                label = "Coins Earned",
                                value = "+$coinsEarned"
                            )
                        }
                    }

                    // Card 2 — This Session Summary
                    PremiumCard(
                        title = "This Session Summary",
                        alpha = cardAlpha2,
                        offsetY = cardOffsetY2
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Minutes Studied",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$studyTimeAddedMins min",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(48.dp)
                                    .background(Color(0xFFE2E8F0))
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Coins Earned",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+$coinsEarned",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB020)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🪙", fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // Card 3 — Coin Wallet (Your Coin Update)
                    PremiumCard(
                        title = "Your Coin Update",
                        alpha = cardAlpha3,
                        offsetY = cardOffsetY3
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = "Total Coins",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = String.format("%,d", animatedCoins),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Today's Coins",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+35",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }

                            // Stack of gold coins illustration
                            PremiumCoinsIllustration(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(65.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Navigation Buttons with smooth entering staggered animation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = buttonsOffsetY)
                        .alpha(buttonsAlpha),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Primary start session button with high shadow and gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(100.dp),
                                clip = false,
                                ambientColor = PurplePrimary.copy(alpha = 0.4f),
                                spotColor = PurplePrimary.copy(alpha = 0.6f)
                            )
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF7C4DFF), Color(0xFF6C4DFF))
                                ),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    try {
                                        val currentTopic = viewModel.activeTimerTopicId.value
                                        val targetTopicId = if (currentTopic.isNotEmpty()) currentTopic else {
                                            viewModel.allTopicsWithDetails.value.firstOrNull()?.topicId ?: ""
                                        }
                                        
                                        val elapsedMs = viewModel.timerElapsedMs.value
                                        if (elapsedMs > 0 && currentTopic.isNotEmpty()) {
                                            val isRevisionActive = userProfile?.focusSettings?.selectedStudyMode == "Revision Mode"
                                            viewModel.recordTimerSession(currentTopic, isRevisionActive, elapsedMs)
                                        }
                                        
                                        viewModel.startTimer(targetTopicId)
                                        
                                        navController.navigate(TimerRoute(targetTopicId, false)) {
                                            popUpTo(DashboardRoute) { inclusive = false }
                                        }
                                        print("Navigation successful\n")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                            .testTag("start_next_session_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚀", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                  text = "Start Next Session",
                                  fontSize = 16.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = Color.White
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Secondary equal width buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!isActionInProgress) {
                                    isActionInProgress = true
                                    println("Take Break Pressed")
                                    println("Take Break pressed")
                                    coroutineScope.launch {
                                        try {
                                            println("Navigating...")
                                            val elapsedMs = viewModel.timerElapsedMs.value
                                            val currentTopic = viewModel.activeTimerTopicId.value
                                            if (elapsedMs > 0 && currentTopic.isNotEmpty()) {
                                                val isRevisionActive = userProfile?.focusSettings?.selectedStudyMode == "Revision Mode"
                                                viewModel.recordTimerSession(currentTopic, isRevisionActive, elapsedMs)
                                            }
                                            
                                            val settings = userProfile?.focusSettings ?: com.example.data.FocusSettings()
                                            val breakDuration = settings.shortBreakDuration
                                            viewModel.startBreakTimer(breakDuration)
                                            
                                            navController.navigate(TimerRoute("", false)) {
                                                popUpTo(DashboardRoute) { inclusive = false }
                                            }
                                            println("Navigation Success")
                                            println("Navigation successful")
                                        } catch (e: Exception) {
                                            println("Navigation failed: ${e.message}")
                                            e.printStackTrace()
                                        } finally {
                                            isActionInProgress = false
                                        }
                                    }
                                }
                            },
                            border = BorderStroke(1.5.dp, PurplePrimary.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurplePrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("take_break_button"),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("☕", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Take Break",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (!isActionInProgress) {
                                    isActionInProgress = true
                                    println("Back To Home Pressed")
                                    println("Back to Home pressed")
                                    try {
                                        println("Navigating...")
                                        onBackToHome()
                                        println("Navigation Success")
                                        println("Navigation successful")
                                    } catch (e: Exception) {
                                        println("Navigation failed: ${e.message}")
                                        e.printStackTrace()
                                    } finally {
                                        isActionInProgress = false
                                    }
                                }
                            },
                            border = BorderStroke(1.5.dp, PurplePrimary.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurplePrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("back_to_home_button"),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏠", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Back to Home",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun PremiumCard(
    title: String,
    alpha: Float,
    offsetY: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .alpha(alpha)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = PurplePrimary.copy(alpha = 0.05f),
                spotColor = PurplePrimary.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        PurplePrimary.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp, // Card Title 22 SemiBold is requested, but 20sp offers better density for multiple screen ratios
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            color = TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = if (label == "Goal Status") TrophyBadgeGreen else TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun PremiumCoinsIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val goldLight = Color(0xFFFFD700)
        val goldDark = Color(0xFFD4AF37)
        val goldOrange = Color(0xFFF59E0B)

        // Draw shadow base
        drawOval(
            color = Color(0x1F000000),
            topLeft = Offset(size.width * 0.4f, size.height * 0.72f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.2f)
        )

        // Stack of coins on the right
        val stackX = size.width * 0.45f
        for (i in 0 until 4) {
            val yOffset = (size.height * 0.58f) - (i * size.height * 0.12f)
            // Coin 3D edge/thickness
            drawOval(
                brush = Brush.verticalGradient(listOf(goldOrange, goldDark)),
                topLeft = Offset(stackX, yOffset + 2f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.42f, size.height * 0.2f)
            )
            // Coin flat face
            drawOval(
                brush = Brush.radialGradient(listOf(goldLight, goldOrange)),
                topLeft = Offset(stackX, yOffset),
                size = androidx.compose.ui.geometry.Size(size.width * 0.42f, size.height * 0.16f)
            )
        }

        // Leaning front coin on the left
        withTransform({
            translate(size.width * 0.1f, size.height * 0.36f)
            rotate(22f)
        }) {
            val coinSize = size.height * 0.52f
            // Coin flat shadow
            drawOval(
                color = Color(0x33000000),
                topLeft = Offset(2f, 2f),
                size = androidx.compose.ui.geometry.Size(coinSize, coinSize)
            )
            // 3D Rim thickness
            drawOval(
                brush = Brush.verticalGradient(listOf(goldOrange, goldDark)),
                topLeft = Offset(0f, 1f),
                size = androidx.compose.ui.geometry.Size(coinSize, coinSize)
            )
            // Coin face
            drawCircle(
                brush = Brush.radialGradient(listOf(goldLight, goldOrange)),
                radius = coinSize / 2f,
                center = Offset(coinSize / 2f, coinSize / 2f)
            )
            // Inner rim details
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = coinSize * 0.38f,
                center = Offset(coinSize / 2f, coinSize / 2f),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = goldDark,
                radius = coinSize * 0.22f,
                center = Offset(coinSize / 2f, coinSize / 2f)
            )
        }
    }
}

@Composable
fun ConfettiAnimation(onFinished: () -> Unit) {
    val colors = listOf(
        Color(0xFF7C4DFF), // Purple
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Green
        Color(0xFFFFB020), // Yellow/Gold
        Color(0xFFF43F5E), // Pink/Red
        Color(0xFF06B6D4)  // Cyan
    )

    // Particle state representation
    class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var rotation: Float,
        val rotationSpeed: Float,
        val size: Float,
        val color: Color,
        val shape: Int, // 0 = Rect, 1 = Circle, 2 = Triangle
        val wobbleSpeed: Float,
        var wobbleTime: Float
    )

    // Keep particles in a remembered list
    val particles = remember {
        List(60) { // 60 particles for premium yet lightweight look
            val angle = Random.nextFloat() * Math.PI - Math.PI / 2 // Spread angle (-90 to 90 degrees)
            val speed = Random.nextFloat() * 15f + 10f // Initial velocity speed
            Particle(
                x = 0f, // Will be initialized to random horizontal positions on first draw layout
                y = Random.nextInt(-150, -30).toFloat(), // Spawn slightly above visible screen
                vx = (Math.sin(angle) * speed).toFloat() * 1.5f,
                vy = (Math.cos(angle) * speed).toFloat() + 5f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 8f - 4f,
                size = Random.nextFloat() * 14f + 12f,
                color = colors.random(),
                shape = Random.nextInt(0, 3),
                wobbleSpeed = Random.nextFloat() * 0.15f + 0.05f,
                wobbleTime = Random.nextFloat() * 100f
            )
        }
    }

    // Keep track of runtime
    var runtimeMs by remember { mutableStateOf(0L) }
    val durationMs = 3000L
    
    // Animate overall alpha to fade out at the end of 3 seconds
    val fadeProgress = if (runtimeMs > 2200L) {
        (1f - (runtimeMs - 2200L).toFloat() / 800L).coerceIn(0f, 1f)
    } else {
        1f
    }

    // Check for completion
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            val now = System.currentTimeMillis()
            runtimeMs = now - startTime
            if (runtimeMs >= durationMs) {
                onFinished()
                break
            }
            withFrameNanos { /* force recomposition/tick */ }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fadeProgress)
    ) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            // Initialize particles' actual horizontal screen spawn position if they are still at 0
            if (p.x == 0f) {
                p.x = Random.nextFloat() * width
            }
            
            // Physics simulation
            // Gravity
            p.vy += 0.25f // Gravity acceleration
            
            // Air resistance (drag)
            p.vx *= 0.98f
            
            // Lateral drift/wobble (simulate leaf-like falling effect)
            p.wobbleTime += p.wobbleSpeed
            val sway = Math.sin(p.wobbleTime.toDouble()).toFloat() * 1.2f
            
            // Apply velocities
            p.x += p.vx + sway
            p.y += p.vy

            // Draw particle based on its shape
            withTransform({
                translate(p.x, p.y)
                rotate(p.rotation)
            }) {
                when (p.shape) {
                    0 -> { // Rectangle
                        drawRect(
                            color = p.color,
                            topLeft = Offset(-p.size / 2f, -p.size / 4f),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size / 2f)
                        )
                    }
                    1 -> { // Circle
                        drawCircle(
                            color = p.color,
                            radius = p.size / 3.5f,
                            center = Offset.Zero
                        )
                    }
                    2 -> { // Triangle
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, -p.size / 2f)
                            lineTo(-p.size / 2f, p.size / 2f)
                            lineTo(p.size / 2f, p.size / 2f)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = p.color
                        )
                    }
                }
            }
        }
    }
}
