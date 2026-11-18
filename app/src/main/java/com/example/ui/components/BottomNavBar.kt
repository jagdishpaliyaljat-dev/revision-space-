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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DashPurplePrimaryShared = Color(0xFF6C4DFF)
val DashTextGrayShared = Color(0xFF64748B)

/**
 * Custom Shape for the Bottom Navigation Bar.
 * It draws a smooth cubic Bezier notch/wave in the top center to cradle the floating button.
 */
class CurvedBottomNavShape(
    private val cornerRadius: Float,
    private val notchWidth: Float,
    private val notchDepth: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            reset()
            val centerX = size.width / 2
            
            // Start at bottom-left corner
            moveTo(0f, size.height)
            
            // Line up to top-left corner start
            lineTo(0f, cornerRadius)
            
            // Smoothly round top-left corner
            quadraticTo(0f, 0f, cornerRadius, 0f)
            
            // Line to the start of the notch
            val notchStart = centerX - notchWidth / 2
            lineTo(notchStart, 0f)
            
            // Curve down smoothly into the notch (first half of cubic Bezier)
            val cp1X = notchStart + notchWidth * 0.2f
            val cp1Y = 0f
            val cp2X = centerX - notchWidth * 0.2f
            val cp2Y = notchDepth
            cubicTo(cp1X, cp1Y, cp2X, cp2Y, centerX, notchDepth)
            
            // Curve up smoothly out of the notch (second half of cubic Bezier)
            val cp3X = centerX + notchWidth * 0.2f
            val cp3Y = notchDepth
            val notchEnd = centerX + notchWidth / 2
            val cp4X = notchEnd - notchWidth * 0.2f
            val cp4Y = 0f
            cubicTo(cp3X, cp3Y, cp4X, cp4Y, notchEnd, 0f)
            
            // Line to top-right corner start
            lineTo(size.width - cornerRadius, 0f)
            
            // Smoothly round top-right corner
            quadraticTo(size.width, 0f, size.width, cornerRadius)
            
            // Line to bottom-right corner
            lineTo(size.width, size.height)
            
            // Close path
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun AppBottomNavigationBar(
    selectedBottomNav: Int,
    onNavigateToHome: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    // Convert Dp measurements to Px for the custom shape
    val notchWidthPx = with(density) { 96.dp.toPx() }
    val notchDepthPx = with(density) { 26.dp.toPx() }
    val cornerRadiusPx = with(density) { 32.dp.toPx() }
    
    val curvedShape = remember(density) {
        CurvedBottomNavShape(
            cornerRadius = cornerRadiusPx,
            notchWidth = notchWidthPx,
            notchDepth = notchDepthPx
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(), // Clean SafeArea support across all devices
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main bottom nav bar container with top rounded corners and notch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = curvedShape,
                    clip = false,
                    spotColor = DashPurplePrimaryShared.copy(alpha = 0.25f),
                    ambientColor = DashPurplePrimaryShared.copy(alpha = 0.12f)
                )
                .background(Color.White, shape = curvedShape)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Tab 0: Home
                BottomTabItem(
                    isSelected = selectedBottomNav == 0,
                    label = "Home",
                    activeIcon = Icons.Default.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    onClick = onNavigateToHome,
                    testTag = "home_tab"
                )
                
                // Tab 1: Subjects
                BottomTabItem(
                    isSelected = selectedBottomNav == 1,
                    label = "Subjects",
                    activeIcon = Icons.Default.Layers,
                    inactiveIcon = Icons.Outlined.Layers,
                    onClick = onNavigateToSubjects,
                    testTag = "subjects_tab"
                )
                
                // Tab 2: Timer (Center layout spacing + "Timer" label)
                val timerTextColor by animateColorAsState(
                    targetValue = if (selectedBottomNav == 2) Color(0xFF7C4DFF) else DashTextGrayShared,
                    animationSpec = tween(durationMillis = 250),
                    label = "timerColor"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToTimer()
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(bottom = 12.dp)
                        .testTag("timer_tab"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Timer",
                        color = timerTextColor,
                        fontSize = 11.sp,
                        fontWeight = if (selectedBottomNav == 2) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
                
                // Tab 3: To-Do
                BottomTabItem(
                    isSelected = selectedBottomNav == 3,
                    label = "To-Do",
                    activeIcon = Icons.Default.Assignment,
                    inactiveIcon = Icons.Outlined.Assignment,
                    onClick = onNavigateToTodoList,
                    testTag = "todo_tab"
                )
                
                // Tab 4: Profile
                BottomTabItem(
                    isSelected = selectedBottomNav == 4,
                    label = "Profile",
                    activeIcon = Icons.Default.Person,
                    inactiveIcon = Icons.Outlined.Person,
                    onClick = onNavigateToProfile,
                    testTag = "profile_tab"
                )
            }
        }
        
        // Floating timer button positioned perfectly in the notch
        val scaleTimer by animateFloatAsState(
            targetValue = if (selectedBottomNav == 2) 1.15f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "scaleTimer"
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp) // Offset to float beautifully inside the notch
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = Color(0xFF7C4DFF).copy(alpha = 0.5f),
                        spotColor = Color(0xFF5B3DF5).copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF7C4DFF), Color(0xFF5B3DF5))
                        ),
                        shape = CircleShape
                    )
                    .border(3.dp, Color.White, CircleShape)
                    .clickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToTimer()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .scale(scaleTimer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer Button",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomTabItem(
    isSelected: Boolean,
    label: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val haptic = LocalHapticFeedback.current
    
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) DashPurplePrimaryShared else DashTextGrayShared,
        animationSpec = tween(durationMillis = 250),
        label = "tab_color"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tab_scale"
    )
    
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(bottom = 12.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Icon(
            imageVector = if (isSelected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
