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

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun StreakSuccessEffect(modifier: Modifier = Modifier, onAnimationEnd: () -> Unit = {}) {
    val progress = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }
    var isAnimating by remember { mutableStateOf(true) }

    val particles = remember { List(15) { Sparkle() } }

    LaunchedEffect(Unit) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
            delay(500)
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500)
            )
            isAnimating = false
            onAnimationEnd()
        }
    }

    if (isAnimating) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Draw particles (Sparks)
            particles.forEach { particle ->
                val particleProgress = progress.value
                val currentRadius = particle.distance * particleProgress
                val cx = center.x + currentRadius * cos(particle.angle)
                val cy = center.y + currentRadius * sin(particle.angle)
                val particleAlpha = (1f - particleProgress) * alphaAnim.value
                
                drawCircle(
                    color = particle.color.copy(alpha = particleAlpha.coerceIn(0f, 1f)),
                    radius = particle.size * (1f - particleProgress),
                    center = Offset(cx, cy)
                )
            }

            // Draw Streak Flame
            translate(left = center.x, top = center.y) {
                scale(scale = scaleAnim.value) {
                    val path = Path().apply {
                        // A stylized flame / streak icon
                        moveTo(0f, -80f)
                        cubicTo(30f, -30f, 60f, 20f, 30f, 60f)
                        cubicTo(0f, 100f, -40f, 60f, -20f, 20f)
                        cubicTo(-10f, 0f, 0f, -30f, 0f, -80f)
                        close()
                    }
                    
                    val innerPath = Path().apply {
                        moveTo(10f, -20f)
                        cubicTo(20f, 10f, 40f, 30f, 20f, 50f)
                        cubicTo(0f, 70f, -20f, 50f, -10f, 30f)
                        cubicTo(-5f, 20f, 5f, 0f, 10f, -20f)
                        close()
                    }

                    // Outer flame
                    drawPath(
                        path = path,
                        color = Color(0xFFFF9800).copy(alpha = alphaAnim.value),
                        style = Stroke(
                            width = 12f * progress.value,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    
                    // Inner flame
                    drawPath(
                        path = innerPath,
                        color = Color(0xFFFFC107).copy(alpha = alphaAnim.value),
                        style = Stroke(
                            width = 8f * progress.value,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

class Sparkle {
    val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
    val distance = Random.nextFloat() * 300f + 100f
    val size = Random.nextFloat() * 15f + 8f
    val color = listOf(
        Color(0xFFFF9800), // Orange
        Color(0xFFFFC107), // Amber
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFF44336)  // Red
    ).random()
}
