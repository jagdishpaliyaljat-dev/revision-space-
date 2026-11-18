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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subject
import com.example.data.Chapter

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
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            if (selectedSubjectId == null) {
                Text("Select Subject", fontWeight = FontWeight.Black, fontSize = 20.sp, color = FocusTextDark)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuerySubject,
                    onValueChange = { searchQuerySubject = it },
                    placeholder = { Text("Search subjects...", color = FocusTextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FocusTextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                val filteredSubjects = allSubjects.filter { it.name.contains(searchQuerySubject, ignoreCase = true) }
                
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    if (filteredSubjects.isEmpty()) {
                        item {
                            Text("No subjects found.", color = FocusTextGray, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                    items(filteredSubjects) { subject ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    selectedSubjectId = subject.id
                                    searchQueryChapter = ""
                                }
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF3F0FF)), contentAlignment = Alignment.Center) {
                                  Text(subject.name.take(1), fontWeight = FontWeight.Bold, color = FocusPurple)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(subject.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FocusTextDark, modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                val subject = allSubjects.find { it.id == selectedSubjectId }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        selectedSubjectId = null 
                        searchQuerySubject = ""
                    }, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FocusTextDark)
                    }
                    Text(subject?.name ?: "Select Chapter", fontWeight = FontWeight.Black, fontSize = 20.sp, color = FocusTextDark)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQueryChapter,
                    onValueChange = { searchQueryChapter = it },
                    placeholder = { Text("Search chapters...", color = FocusTextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FocusTextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                val subjectChapters = allChapters.filter { it.subjectId == selectedSubjectId }
                val filteredChapters = subjectChapters.filter { it.name.contains(searchQueryChapter, ignoreCase = true) }
                
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    if (filteredChapters.isEmpty()) {
                        item {
                            Text("No Chapters Found", color = FocusTextGray, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                    items(filteredChapters) { chapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    if (subject != null) {
                                        onChapterSelected(subject, chapter)
                                    }
                                }
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF3F0FF)), contentAlignment = Alignment.Center) {
                                Text(chapter.name.take(1), fontWeight = FontWeight.Bold, color = FocusPurple)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(chapter.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FocusTextDark, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
