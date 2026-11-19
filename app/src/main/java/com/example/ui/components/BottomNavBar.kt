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

val DashPurplePrimaryShared = Color(0xFF7C3AED)
val DashTextGrayShared = Color(0xFF5B6478)

/**
 * Floating, pill-shaped bottom navigation — premium minimal redesign.
 *
 * - Slimmer 72dp bar with a soft hairline border and ambient shadow.
 * - Selected tab gets a tinted pill behind the icon + a small dot indicator.
 * - Central Timer action is a floating gradient circle that lifts above the bar.
 * - All colors are theme-aware so dark mode stays consistent.
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
    val barShape = RoundedCornerShape(24.dp)
    val surface = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = barShape,
                    clip = false,
                    ambientColor = primary.copy(alpha = 0.10f),
                    spotColor = primary.copy(alpha = 0.18f)
                )
                .background(surface, barShape)
                .border(1.dp, borderColor, barShape)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTabItem(
                    isSelected = selectedBottomNav == 0,
                    label = "Home",
                    activeIcon = Icons.Filled.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    onClick = onNavigateToHome,
                    testTag = "home_tab"
                )
                BottomTabItem(
                    isSelected = selectedBottomNav == 1,
                    label = "Subjects",
                    activeIcon = Icons.Filled.Layers,
                    inactiveIcon = Icons.Outlined.Layers,
                    onClick = onNavigateToSubjects,
                    testTag = "subjects_tab"
                )

                // Center slot reserved for the floating Timer action.
                Spacer(modifier = Modifier.weight(1f))

                BottomTabItem(
                    isSelected = selectedBottomNav == 3,
                    label = "To-Do",
                    activeIcon = Icons.Filled.Assignment,
                    inactiveIcon = Icons.Outlined.Assignment,
                    onClick = onNavigateToTodoList,
                    testTag = "todo_tab"
                )
                BottomTabItem(
                    isSelected = selectedBottomNav == 4,
                    label = "Profile",
                    activeIcon = Icons.Filled.Person,
                    inactiveIcon = Icons.Outlined.Person,
                    onClick = onNavigateToProfile,
                    testTag = "profile_tab"
                )
            }
        }

        // Floating gradient Timer button that sits above the bar.
        val scaleTimer by animateFloatAsState(
            targetValue = if (selectedBottomNav == 2) 1.06f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "scaleTimer"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = DashPurplePrimaryShared.copy(alpha = 0.35f),
                        spotColor = DashPurplePrimaryShared.copy(alpha = 0.45f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
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
                    .scale(scaleTimer)
                    .testTag("timer_tab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = "Timer Button",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Timer label under the floating button
            Text(
                text = "Timer",
                color = if (selectedBottomNav == 2) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = if (selectedBottomNav == 2) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 46.dp)
            )
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
        animationSpec = tween(durationMillis = 200),
        label = "tab_color"
    )
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "tab_pill"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
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
            .padding(vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(pillColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}