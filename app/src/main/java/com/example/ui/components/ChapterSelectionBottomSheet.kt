package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Chapter
import com.example.data.Subject

/**
 * Chapter picker used by the Focus timer.
 *
 * Same behaviour as before (subject step -> chapter step, search, dismiss),
 * but restyled to match the premium minimal Focus screen: neutral background,
 * flat cards with a 1dp border, brand accent only for the small labels,
 * the letter tiles and the focused search border.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectionBottomSheet(
    allSubjects: List<Subject>,
    allChapters: List<Chapter>,
    onChapterSelected: (Subject, Chapter) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuerySubject by remember { mutableStateOf("") }
    var searchQueryChapter by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        val selectedSubject = allSubjects.find { it.id == selectedSubjectId }

        if (selectedSubject == null) {
            SubjectPickerStep(
                subjects = allSubjects,
                query = searchQuerySubject,
                onQueryChange = { searchQuerySubject = it },
                onSubjectSelected = { subject ->
                    selectedSubjectId = subject.id
                    searchQueryChapter = ""
                }
            )
        } else {
            ChapterPickerStep(
                subject = selectedSubject,
                chapters = allChapters.filter { it.subjectId == selectedSubject.id },
                query = searchQueryChapter,
                onQueryChange = { searchQueryChapter = it },
                onBack = {
                    selectedSubjectId = null
                    searchQuerySubject = ""
                },
                onChapterSelected = { chapter -> onChapterSelected(selectedSubject, chapter) }
            )
        }
    }
}

// ---------------------------------------------------------------- step 1

@Composable
private fun SubjectPickerStep(
    subjects: List<Subject>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSubjectSelected: (Subject) -> Unit
) {
    val filtered = subjects.filter { it.name.contains(query, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        PickerHeader(
            kicker = "STEP 1 OF 2",
            title = "Choose a subject",
            subtitle = if (filtered.size == 1) "1 subject" else "${filtered.size} subjects"
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search subjects"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filtered.isEmpty()) {
                item { PickerEmptyState("No subjects found") }
            }
            items(filtered, key = { it.id }) { subject ->
                PickerRow(
                    label = subject.name,
                    initial = subject.name,
                    showChevron = true,
                    onClick = { onSubjectSelected(subject) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------- step 2

@Composable
private fun ChapterPickerStep(
    subject: Subject,
    chapters: List<Chapter>,
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onChapterSelected: (Chapter) -> Unit
) {
    val filtered = chapters.filter { it.name.contains(query, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        PickerHeader(
            kicker = "STEP 2 OF 2",
            title = subject.name,
            subtitle = if (filtered.size == 1) "1 chapter" else "${filtered.size} chapters",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search chapters"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filtered.isEmpty()) {
                item { PickerEmptyState("No chapters found") }
            }
            items(filtered, key = { it.id }) { chapter ->
                PickerRow(
                    label = chapter.name,
                    initial = chapter.name,
                    showChevron = false,
                    onClick = { onChapterSelected(chapter) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------- pieces

@Composable
private fun PickerHeader(
    kicker: String,
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (onBack != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        kicker,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                kicker,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                title,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PickerSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}

@Composable
private fun PickerRow(
    label: String,
    initial: String,
    showChevron: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initial.trim().take(1).uppercase().ifBlank { "?" },
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (showChevron) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PickerEmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
