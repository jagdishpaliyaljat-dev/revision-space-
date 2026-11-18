package com.example.ui.components
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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import com.example.R

/**
 * Reusable Lottie-powered Congrats Badge Composable.
 * Renders an animated gold badge with stars and sparkles when daily study goals are completed.
 */
@Composable
fun CongratsBadgeLottieAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true,
    onFinished: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.congrats_badge))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying,
        speed = 1.0f
    )

    LaunchedEffect(progress) {
        if (progress >= 1f && iterations != LottieConstants.IterateForever) {
            onFinished()
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("lottie_congrats_badge"),
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback animated vector badge if Lottie composition is loading
            val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
            val badgeScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            Box(
                modifier = Modifier
                    .size(size * 0.75f)
                    .scale(badgeScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Congrats Badge",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.4f)
                )
            }
        }
    }
}

/**
 * Animated Dialog that triggers when a user completes their daily study goal.
 * Features the Lottie 'congrats' badge, celebratory text, streak information, and sound/replay options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalCongratsDialog(
    goalTarget: Int = 120, // Goal in minutes or sessions
    unitName: String = "minutes",
    streakDays: Int = 1,
    onDismiss: () -> Unit,
    onReplayAnimation: (() -> Unit)? = null
) {
    var replayCount by remember { mutableStateOf(0) }
    var showConfettiEffect by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.3f))
                .testTag("daily_goal_congrats_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFEF3C7).copy(alpha = 0.5f),
                                Color.White
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Tag
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DAILY GOAL COMPLETED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lottie Congrats Badge Animation
                    key(replayCount) {
                        CongratsBadgeLottieAnimation(
                            size = 180.dp,
                            iterations = LottieConstants.IterateForever
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = "Congrats! 🎉",
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Message
                    Text(
                        text = "You've crushed your daily goal of $goalTarget $unitName today! Consistent effort builds extraordinary success.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak Pill
                    if (streakDays > 0) {
                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFEDD5))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "$streakDays Day Streak Maintained!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                replayCount++
                                showConfettiEffect = true
                                onReplayAnimation?.invoke()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                "Replay 🎉",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C4DFF)
                            )
                        ) {
                            Text(
                                "Awesome!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact inline banner/card showcasing the Lottie Congrats Badge
 * suitable for embedding in Dashboard, Today's Revision, or Session Completion screens.
 */
@Composable
fun CongratsBadgeCard(
    goalTarget: Int = 120,
    unitName: String = "minutes",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.2f))
            .clickable { onClick() }
            .testTag("congrats_badge_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFEF3C7).copy(alpha = 0.6f),
                            Color.White
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CongratsBadgeLottieAnimation(
                size = 72.dp,
                iterations = LottieConstants.IterateForever
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Daily Goal Reached! 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You completed $goalTarget $unitName today. Tap to view your congrats badge!",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
