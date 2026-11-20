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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BarHeight = 64.dp

/**
 * Premium floating bottom navigation — refined, mobile-friendly, minimal.
 *
 * Six equal tabs in a single row (Home · Subjects · Timer · To-Do · Profile · AI).
 * Keeping every tab the same width makes the bar symmetric on any screen size
 * and keeps the elevated-FAB asymmetry from crowding the sixth tab.
 *
 * Selected tab: filled icon + tinted pill + accent dot; unselected: outlined
 * icon, muted. All colors theme-aware for dark mode.
 */
@Composable
fun AppBottomNavigationBar(
    selectedBottomNav: Int,
    onNavigateToHome: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToTodoList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val barShape = RoundedCornerShape(24.dp)
    val surface = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .shadowSafe(
                    elevation = 16.dp,
                    shape = barShape,
                    ambientColor = primary.copy(alpha = 0.06f),
                    spotColor = primary.copy(alpha = 0.12f)
                )
                .clip(barShape)
                .background(surface.copy(alpha = 0.95f), barShape)
                .border(0.5.dp, borderColor, barShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTabItem(selectedBottomNav == 0, "Home", Icons.Filled.Home, Icons.Outlined.Home, onNavigateToHome, "home_tab")
                BottomTabItem(selectedBottomNav == 1, "Subjects", Icons.Filled.Layers, Icons.Outlined.Layers, onNavigateToSubjects, "subjects_tab")
                BottomTabItem(selectedBottomNav == 2, "Timer", Icons.Filled.Timer, Icons.Outlined.Timer, onNavigateToTimer, "timer_tab")
                BottomTabItem(selectedBottomNav == 3, "To-Do", Icons.Filled.Assignment, Icons.Outlined.Assignment, onNavigateToTodoList, "todo_tab")
                BottomTabItem(selectedBottomNav == 4, "Profile", Icons.Filled.Person, Icons.Outlined.Person, onNavigateToProfile, "profile_tab")
                BottomTabItem(selectedBottomNav == 5, "AI", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, onNavigateToChat, "chat_tab")
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
        animationSpec = tween(durationMillis = 180),
        label = "tab_color"
    )
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "tab_pill"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
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
            .padding(vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier
                    .size(20.dp)
                    .scale(iconScale)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .size(if (isSelected) 3.dp else 0.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}

/**
 * Shadow with the right import — androidx.compose.ui.draw.shadow needs its
 * own import. Kept as a tiny wrapper so the bar file stays clean.
 */
@Composable
private fun Modifier.shadowSafe(
    elevation: androidx.compose.ui.unit.Dp,
    shape: androidx.compose.ui.graphics.Shape,
    ambientColor: Color,
    spotColor: Color
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = ambientColor,
    spotColor = spotColor
)
