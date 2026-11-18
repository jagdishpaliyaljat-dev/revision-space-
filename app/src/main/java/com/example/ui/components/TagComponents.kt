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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// Beautiful Material 3 color tokens for tags
data class TagVisuals(
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val description: String = ""
)

/**
 * Resolves a tag string to a beautiful, modern pastel color scheme.
 * Includes explicit mappings for popular study tags to maximize visual polish.
 */
@Composable
fun getTagVisuals(tagName: String): TagVisuals {
    val lower = tagName.lowercase(Locale.getDefault()).trim()
    return when {
        // Red / Rose (Urgent, Important, High Priority)
        lower.contains("urgent") || lower.contains("important") || lower.contains("hard") || lower.contains("imp") -> {
            TagVisuals(
                backgroundColor = Color(0xFFFFF1F2),
                textColor = Color(0xFFE11D48),
                borderColor = Color(0xFFFECDD3),
                description = "Critical / Hard Topic"
            )
        }
        // Green / Emerald (Mastered, Easy, Done)
        lower.contains("easy") || lower.contains("strong") || lower.contains("done") || lower.contains("mastered") -> {
            TagVisuals(
                backgroundColor = Color(0xFFECFDF5),
                textColor = Color(0xFF059669),
                borderColor = Color(0xFFA7F3D0),
                description = "Mastered Topic"
            )
        }
        // Sky Blue / Formula (Math, Formula, Chemistry, Physics, Key concepts)
        lower.contains("formula") || lower.contains("equation") || lower.contains("math") || lower.contains("key") -> {
            TagVisuals(
                backgroundColor = Color(0xFFEFF6FF),
                textColor = Color(0xFF2563EB),
                borderColor = Color(0xFFBFDBFE),
                description = "Equations & Formulas"
            )
        }
        // Purple / Theory (Notes, Read, Literature, History, Theory)
        lower.contains("theory") || lower.contains("notes") || lower.contains("read") || lower.contains("book") -> {
            TagVisuals(
                backgroundColor = Color(0xFFF1EEFF),
                textColor = Color(0xFF6C4DFF),
                borderColor = Color(0xFFDCD6FF),
                description = "Theoretical Concepts"
            )
        }
        // Orange / Amber (Doubt, Pending, Review, Medium)
        lower.contains("doubt") || lower.contains("review") || lower.contains("medium") || lower.contains("pending") || lower.contains("verify") -> {
            TagVisuals(
                backgroundColor = Color(0xFFFFFBEB),
                textColor = Color(0xFFD97706),
                borderColor = Color(0xFFFEF3C7),
                description = "Needs Review / Doubts"
            )
        }
        // Indigo / Exam (Exam, Test, Mock, PYQ)
        lower.contains("exam") || lower.contains("test") || lower.contains("mock") || lower.contains("pyq") || lower.contains("board") -> {
            TagVisuals(
                backgroundColor = Color(0xFFF1EEFF),
                textColor = Color(0xFF4F46E5),
                borderColor = Color(0xFFC7D2FE),
                description = "Exam Target"
            )
        }
        // Pink / Magenta (Shortcuts, Tips, Tricks)
        lower.contains("trick") || lower.contains("shortcut") || lower.contains("tips") || lower.contains("mnemonics") -> {
            TagVisuals(
                backgroundColor = Color(0xFFFDF2F8),
                textColor = Color(0xFFDB2777),
                borderColor = Color(0xFFFBCFE8),
                description = "Tricks & Shortcuts"
            )
        }
        else -> {
            // Safe, deterministic hash-based theme assignment
            val index = Math.abs(tagName.hashCode()) % 7
            val bgs = listOf(
                Color(0xFFF1EEFF), Color(0xFFECFDF5), Color(0xFFEFF6FF),
                Color(0xFFFFF1F2), Color(0xFFFFFBEB), Color(0xFFF1EEFF), Color(0xFFFDF2F8)
            )
            val txts = listOf(
                Color(0xFF6C4DFF), Color(0xFF059669), Color(0xFF2563EB),
                Color(0xFFE11D48), Color(0xFFD97706), Color(0xFF4F46E5), Color(0xFFDB2777)
            )
            val borders = listOf(
                Color(0xFFDCD6FF), Color(0xFFA7F3D0), Color(0xFFBFDBFE),
                Color(0xFFFECDD3), Color(0xFFFEF3C7), Color(0xFFC7D2FE), Color(0xFFFBCFE8)
            )
            TagVisuals(bgs[index], txts[index], borders[index], "Custom Tag")
        }
    }
}

/**
 * A beautiful, elevated custom Tag Chip adhering to Material Design 3 guidelines.
 */
@Composable
fun TopicTagChip(
    tag: String,
    modifier: Modifier = Modifier,
    showDelete: Boolean = false,
    onDeleteClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val visuals = getTagVisuals(tag)
    
    val chipModifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(visuals.backgroundColor)
        .border(BorderStroke(1.dp, visuals.borderColor), RoundedCornerShape(8.dp))
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )
        .padding(horizontal = 8.dp, vertical = 4.dp)

    Row(
        modifier = chipModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = tag,
            color = visuals.textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showDelete && onDeleteClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag",
                tint = visuals.textColor,
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .clickable { onDeleteClick() }
            )
        }
    }
}

/**
 * A comprehensive Tag Manager Dialog that enables users to:
 * 1. View current tags
 * 2. Delete tags
 * 3. Add custom text tags
 * 4. Choose from preset, beautiful study tag categories
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagEditDialog(
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onTagsUpdated: (List<String>) -> Unit
) {
    var tagsList by remember { mutableStateOf(currentTags.distinct()) }
    var customTagText by remember { mutableStateOf("") }
    
    // Curated default revision tags with preset styling
    val presets = listOf(
        "Important", "Formula", "Theory", "Doubt", "PYQ / Exam", "Trick", "Mastered"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = Color(0xFF6C4DFF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Organize Tags",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Current Tags",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (tagsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tags added yet. Add one below!",
                            fontSize = 12.sp,
                            color = Color(0xFF8A94A6),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagsList.forEach { tag ->
                            TopicTagChip(
                                tag = tag,
                                showDelete = true,
                                onDeleteClick = {
                                    tagsList = tagsList.filter { it != tag }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Add Custom Tag",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customTagText,
                        onValueChange = { customTagText = it },
                        placeholder = { Text("e.g. Physics", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C4DFF),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val clean = customTagText.trim()
                            if (clean.isNotEmpty() && !tagsList.contains(clean)) {
                                tagsList = tagsList + clean
                                customTagText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = Color(0xFF6C4DFF)
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add tag")
                    }
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                Text(
                    text = "Quick Presets (Tap to Add)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val isAlreadyAdded = tagsList.contains(preset)
                        val visuals = getTagVisuals(preset)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isAlreadyAdded) visuals.backgroundColor else MaterialTheme.colorScheme.background
                                )
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isAlreadyAdded) visuals.borderColor else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    tagsList = if (isAlreadyAdded) {
                                        tagsList.filter { it != preset }
                                    } else {
                                        tagsList + preset
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(visuals.textColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAlreadyAdded) visuals.textColor else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTagsUpdated(tagsList)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C4DFF)
                ),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

/**
 * A delightful animated text component that draws a strike-through line dynamically when marked complete.
 * Implements smooth spring translation simulating CSS keyframe effects.
 */
@Composable
fun AnimatedStrikethroughText(
    text: String,
    isCompleted: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    color: Color,
    completedColor: Color = Color(0xFF8A94A6),
    modifier: Modifier = Modifier
) {
    val progress = animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "StrikethroughProgress"
    )
    
    val currentTextColor = if (isCompleted) completedColor else color

    Box(
        modifier = modifier.width(IntrinsicSize.Max),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = currentTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Canvas(modifier = Modifier.matchParentSize()) {
            val progressVal = progress.value
            if (progressVal > 0f) {
                val canvasSize = this.size
                val y = canvasSize.height / 2f
                drawLine(
                    color = completedColor.copy(alpha = 0.8f),
                    start = Offset(0f, y),
                    end = Offset(canvasSize.width * progressVal, y),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

