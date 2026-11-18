package com.example.ui.screens.auth
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun SplashScreen(isSetupComplete: Boolean, onTransitionComplete: () -> Unit) {
    var logoAppeared by remember { mutableStateOf(false) }
    var ringAppeared by remember { mutableStateOf(false) }
    var textAppeared by remember { mutableStateOf(false) }
    var progressStarted by remember { mutableStateOf(false) }
    var zoomStarted by remember { mutableStateOf(false) }

    // Initial appearance timers
    LaunchedEffect(Unit) {
        logoAppeared = true
        delay(400)
        ringAppeared = true
        delay(500)
        textAppeared = true
        progressStarted = true
    }

    // Trigger transition when setup is complete and minimum animations have played
    LaunchedEffect(isSetupComplete, textAppeared) {
        if (isSetupComplete && textAppeared) {
            delay(300) // Small pause before zooming out
            zoomStarted = true
            delay(600) // Wait for transition animation
            onTransitionComplete()
        }
    }

    // Logo Animations
    val logoScale by animateFloatAsState(
        targetValue = if (zoomStarted) 1.5f else if (logoAppeared) 1f else 0.4f,
        animationSpec = tween(
            durationMillis = if (zoomStarted) 600 else 800,
            easing = FastOutSlowInEasing
        )
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (zoomStarted) 0f else if (logoAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    // Ring Animation
    val ringAlpha by animateFloatAsState(
        targetValue = if (zoomStarted) 0f else if (ringAppeared) 1f else 0f,
        animationSpec = tween(500)
    )
    val infiniteTransition = rememberInfiniteTransition()
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Pulse Animation
    val logoPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Text Animations
    val textAlpha by animateFloatAsState(
        targetValue = if (zoomStarted) 0f else if (textAppeared) 1f else 0f,
        animationSpec = tween(600)
    )
    val textOffset by animateFloatAsState(
        targetValue = if (textAppeared) 0f else 20f,
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )

    // Progress Line
    val progressWidth by animateFloatAsState(
        targetValue = if (progressStarted) 1f else 0f,
        animationSpec = tween(500, easing = LinearEasing)
    )

    // Background Blur
    val bgBlur by animateFloatAsState(
        targetValue = if (zoomStarted) 16f else 0f,
        animationSpec = tween(500)
    )

    // Background Gradient: Deep Purple (#6C4DFF) to Royal Blue (#2196F3)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6C4DFF), Color(0xFF2196F3))
                )
            )
            .blur(bgBlur.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Particles (Subtle)
        Particles(alpha = logoAlpha)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale * if (ringAppeared) logoPulse else 1f)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Soft ambient glow behind logo
                Box(    
                    modifier = Modifier
                        .size(140.dp)
                        .blur(30.dp)
                        .background(Color(0xFF6C4DFF).copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                )
                
                // Thin glowing ring
                Canvas(modifier = Modifier
                    .size(160.dp)
                    .alpha(ringAlpha)
                    .rotate(ringRotation)
                ) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
                
                // Sparkles if ring appeared
                if (ringAppeared) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-16).dp, y = 16.dp)
                            .size(20.dp)
                            .alpha(ringAlpha)
                    )
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 16.dp, y = (-16).dp)
                            .size(14.dp)
                            .alpha(ringAlpha)
                    )
                }

                // Inner Image
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text section
            Column(
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REVISION SPACE",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Revise Smarter. Achieve Faster.",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Loading Indicator
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isSetupComplete,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut()
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
fun Particles(alpha: Float) {
    val particles = remember {
        List(20) {
            ParticleData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.02f + 0.005f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(alpha)) {
        val width = size.width
        val height = size.height
        
        particles.forEach { particle ->
            val py = (particle.y - (yOffset * particle.speed * 100f)) % 1f
            val actualY = if (py < 0) 1f + py else py
            
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = particle.size,
                center = Offset(particle.x * width, actualY * height)
            )
        }
    }
}

data class ParticleData(val x: Float, val y: Float, val size: Float, val speed: Float)
