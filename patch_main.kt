package com.example

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.os.Bundle
import kotlinx.coroutines.flow.asStateFlow
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.RevisionRepository
import com.example.ui.AppNavigation
import com.example.ui.theme.RevisionSpaceTheme
import com.example.viewmodel.RevisionViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.viewmodel.RevisionViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
class MainActivity : ComponentActivity() {
  companion object {
      var isAppInForeground = false
          private set
  }

  override fun onStart() {
      super.onStart()
      isAppInForeground = true
  }

  override fun onStop() {
      super.onStop()
      isAppInForeground = false
  }

  private fun fetchFcmToken() {
      try {
          FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
              try {
                  if (!task.isSuccessful) return@addOnCompleteListener
                  val token = task.result
                  val uid = FirebaseAuth.getInstance().currentUser?.uid
                  if (uid != null && token != null) {
                      FirebaseFirestore.getInstance().collection("users").document(uid).update("fcmToken", token)
                  }
              } catch (ex: Exception) {
                  ex.printStackTrace()
              }
          }
      } catch (e: Exception) {
          e.printStackTrace()
      }
  }

  private val _intentFlow = kotlinx.coroutines.flow.MutableStateFlow<android.content.Intent?>(null)
  val intentFlow = _intentFlow.asStateFlow()

  override fun onNewIntent(intent: android.content.Intent) {
      super.onNewIntent(intent)
      setIntent(intent)
      _intentFlow.value = intent
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _intentFlow.value = intent
    
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
          android.util.Log.e("CrashHandler", "Uncaught Exception", e)
      }
    
    try {
         com.google.firebase.FirebaseApp.initializeApp(this)
     } catch (e: Exception) {
         e.printStackTrace()
     }
    
    enableEdgeToEdge()
    fetchFcmToken()
    
    try {
        setContent {
          val repository = androidx.compose.runtime.remember { RevisionRepository() }
          val viewModel: RevisionViewModel = viewModel(factory = RevisionViewModelFactory(repository))
          val isDarkMode by viewModel.isDarkMode.collectAsState()
          
          val context = androidx.compose.ui.platform.LocalContext.current
          LaunchedEffect(Unit) {
              val sharedPrefs = context.getSharedPreferences("revision_app_prefs", android.content.Context.MODE_PRIVATE)
              viewModel.setDarkMode(sharedPrefs.getBoolean("dark_mode_enabled", false))
          }

          RevisionSpaceTheme(darkTheme = isDarkMode) {
            Surface(modifier = Modifier.fillMaxSize()) {
                val globalConfig by viewModel.globalConfig.collectAsState(initial = null)
                val profile by viewModel.currentUserProfile.collectAsState(initial = null)
                val metrics by viewModel.dashboardMetrics.collectAsState(initial = null)
                
                LaunchedEffect(metrics) {
                    metrics?.let {
                        try {
                            me.leolin.shortcutbadger.ShortcutBadger.applyCount(context, it.todayDue.size)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                var showGoalReachedNotification by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    viewModel.goalReachedEvent.collect {
                        showGoalReachedNotification = true
                    }
                }

                if (showGoalReachedNotification) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showGoalReachedNotification = false },
                        title = { 
                             androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Star, 
                                     contentDescription = "Trophy", 
                                     tint = androidx.compose.ui.graphics.Color(0xFFF59E0B)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                                androidx.compose.material3.Text(
                                    "Daily Goal Reached!", 
                                     fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, 
                                     fontSize = 20.sp, 
                                     color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                                )
                            }
                        },
                        text = { 
                             androidx.compose.material3.Text(
                                "Amazing job! You have reached your daily study goal. Keep up the great consistency!", 
                                 fontSize = 15.sp, 
                                 color = androidx.compose.ui.graphics.Color(0xFF64748B)
                            ) 
                         },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = { showGoalReachedNotification = false },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF6366F1))
                            ) {
                                androidx.compose.material3.Text("Awesome")
                            }
                        }
                    )
                }

                if (globalConfig?.isMaintenanceModeEnabled == true && profile?.role != "Admin") {
                    com.example.ui.MaintenanceScreen()
                } else if (globalConfig != null && 1 < (globalConfig?.minimumAppVersion ?: 1)) {
                    com.example.ui.ForceUpdateScreen()
                } else {
                    AppNavigation(viewModel = viewModel)
                }
            }
          }
        }
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Launch crash", e)
        setContent {
            com.example.ui.ErrorScreen(errorText = e.message ?: "Unknown error")
        }
    }
  }
}
