package com.example.ui.components
import com.example.ui.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ConfettiParticle(
    val color: Color,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val shape: ParticleShape,
    val rotationSpeed: Float = Random.nextFloat() * 10f - 5f
) {
    enum class ParticleShape { Circle, Rectangle, Triangle }
}

/**
 * Task completion explosion effect combining particle graphics with Lottie animation feedback.
 * Triggered when a user completes or marks a study topic as revised.
 */
@Composable
fun CheckmarkExplosionEffect(
    modifier: Modifier = Modifier,
    title: String = "Topic Marked as Revised! 🎉",
    onAnimationEnd: () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }
    val circleScale = remember { Animatable(0f) }
    val checkmarkProgress = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }
        
    val particles = remember {
        val colors = listOf(
            Color(0xFF6C4DFF), // TodoPurple
            Color(0xFF10B981), // Emerald Green
            Color(0xFF3B82F6), // Blue
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6)  // Purple
        )
        List(40) {
            ConfettiParticle(
                color = colors.random(),
                angle = Random.nextFloat() * 2 * Math.PI.toFloat(),
                speed = Random.nextFloat() * 12f + 4f,
                size = Random.nextFloat() * 30f + 15f,
                shape = ConfettiParticle.ParticleShape.values().random()
            )
        }
    }

    LaunchedEffect(Unit) {
        launch {
            circleScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            checkmarkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
            )
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350)
            )
            onAnimationEnd()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f * alphaAnim.value)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val currentAlpha = alphaAnim.value

            // 1. Draw Confetti/Sparkles exploding outwards
            particles.forEach { particle ->
                val pProgress = progress.value
                val distance = particle.speed * 85f * pProgress
                   
                val gravityY = pProgress * pProgress * 150f
                val px = center.x + distance * cos(particle.angle)
                val py = center.y + distance * sin(particle.angle) + gravityY
                   
                val particleAlpha = ((1f - pProgress) * currentAlpha).coerceIn(0f, 1f)
                val currentSize = particle.size * (1f - pProgress * 0.5f)
                   
                translate(left = px, top = py) {
                    scale(scale = 1f, pivot = Offset.Zero) {
                        val path = Path()
                        when (particle.shape) {
                            ConfettiParticle.ParticleShape.Circle -> {
                                drawCircle(
                                    color = particle.color.copy(alpha = particleAlpha),
                                    radius = currentSize / 2,
                                    center = Offset.Zero
                                )
                            }
                            ConfettiParticle.ParticleShape.Rectangle -> {
                                val rectSize = Size(currentSize, currentSize * 0.5f)
                                drawRect(
                                    color = particle.color.copy(alpha = particleAlpha),
                                    topLeft = Offset(-rectSize.width / 2, -rectSize.height / 2),
                                    size = rectSize
                                )
                            }
                            ConfettiParticle.ParticleShape.Triangle -> {
                                path.moveTo(0f, -currentSize / 2)
                                path.lineTo(currentSize / 2, currentSize / 2)
                                path.lineTo(-currentSize / 2, currentSize / 2)
                                path.close()
                                drawPath(
                                    path = path,
                                    color = particle.color.copy(alpha = particleAlpha)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Lottie Animated Completion Badge & Celebration Title Banner
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(circleScale.value)
                .padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                CongratsBadgeLottieAnimation(
                    size = 190.dp,
                    iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color.White.copy(alpha = 0.96f * alphaAnim.value),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 12.dp,
                border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}
