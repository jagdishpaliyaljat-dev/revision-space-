package com.example.ui.screens.settings
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE) }
    var selectedLang by remember { mutableStateOf(sharedPrefs.getString("app_language", "English") ?: "English") }
    
    val languages = listOf("English", "Hindi", "Spanish", "French", "German")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(languages) { lang ->
                val isSelected = lang == selectedLang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedLang = lang
                            sharedPrefs.edit().putString("app_language", lang).apply()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(lang, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF6B4EFF))
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
