package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DashPurplePrimaryShared = Color(0xFF6C4DFF)
val DashTextGrayShared = Color(0xFF5B6478)

/**
 * Floating, pill-shaped bottom navigation.
 *
 * The bar hovers above the content with a soft shadow and a hairline border,
 * while the selected destination gets a tinted indicator that animates in.
 * The central Timer action stays prominent as a gradient action button.
 */
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
    val barShape = RoundedCornerShape(28.dp)
    val surface = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = barShape,
                    clip = false,
                    ambientColor = DashPurplePrimaryShared.copy(alpha = 0.14f),
                    spotColor = DashPurplePrimaryShared.copy(alpha = 0.20f)
                )
                .background(surface, barShape)
                .border(1.dp, borderColor, barShape)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                BottomTabItem(
                    isSelected = selectedBottomNav == 0,
                    label = "Home",
                    activeIcon = Icons.Default.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    onClick = onNavigateToHome,
                    testTag = "home_tab"
                )
                BottomTabItem(
                    isSelected = selectedBottomNav == 1,
                    label = "Subjects",
                    activeIcon = Icons.Default.Layers,
                    inactiveIcon = Icons.Outlined.Layers,
                    onClick = onNavigateToSubjects,
                    testTag = "subjects_tab"
                )

                // Center slot reserved for the floating Timer action.
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
                        .padding(bottom = 10.dp)
                        .testTag("timer_tab"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Timer",
                        color = if (selectedBottomNav == 2) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 10.sp,
                        fontWeight = if (selectedBottomNav == 2) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                }

                BottomTabItem(
                    isSelected = selectedBottomNav == 3,
                    label = "To-Do",
                    activeIcon = Icons.Default.Assignment,
                    inactiveIcon = Icons.Outlined.Assignment,
                    onClick = onNavigateToTodoList,
                    testTag = "todo_tab"
                )
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

        // Floating gradient Timer button that sits above the bar.
        val scaleTimer by animateFloatAsState(
            targetValue = if (selectedBottomNav == 2) 1.08f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "scaleTimer"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = Color(0xFF6C4DFF).copy(alpha = 0.45f),
                        spotColor = Color(0xFF4B32D6).copy(alpha = 0.45f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF6C4DFF), Color(0xFF4B32D6))
                        ),
                        shape = CircleShape
                    )
                    .border(3.dp, surface, CircleShape)
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
                    modifier = Modifier.size(26.dp)
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 220),
        label = "tab_color"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "tab_indicator"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.96f,
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
            .padding(bottom = 10.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .height(30.dp)
                .width(44.dp)
                .background(indicatorColor, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier
                    .size(21.dp)
                    .scale(iconScale)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.2.sp,
            maxLines = 1
        )
    }
}
