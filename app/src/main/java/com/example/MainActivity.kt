package com.example
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
import com.example.ui.*


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
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
import com.google.firebase.auth.FirebaseAuth
import com.example.data.UserProfile

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
      // FCM Token logic removed
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
    configureAppCheck(this, intent)
    _intentFlow.value = intent
    
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
          android.util.Log.e("CrashHandler", "Uncaught Exception", e)
      }
    
    enableEdgeToEdge()
    fetchFcmToken()
    
    try {
        setContent {
          val repository = androidx.compose.runtime.remember { RevisionRepository() }
          val viewModel: RevisionViewModel = viewModel(factory = RevisionViewModelFactory(repository))
          val isDarkMode by viewModel.isDarkMode.collectAsState()
          
          RevisionSpaceTheme(darkTheme = isDarkMode) {
            Surface(modifier = Modifier.fillMaxSize()) {
                val context = androidx.compose.ui.platform.LocalContext.current
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
                    DailyGoalCongratsDialog(
                        goalTarget = profile?.focusSettings?.dailyFocusGoalMins ?: 120,
                        unitName = "minutes",
                        streakDays = profile?.currentStreak ?: 1,
                        onDismiss = { showGoalReachedNotification = false }
                    )
                }

                if (globalConfig?.isMaintenanceModeEnabled == true && profile?.role != "Admin") {
                    MaintenanceScreen()
                } else if (globalConfig != null && 1 < (globalConfig?.minimumAppVersion ?: 1)) {
                    ForceUpdateScreen()
                } else {
                    AppNavigation(viewModel = viewModel)
                }
            }
          }
        }
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Launch crash", e)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                androidx.compose.material3.Text(
                    text = e.message ?: "Unknown error",
                    modifier = androidx.compose.ui.Modifier.padding(16.dp),
                    color = androidx.compose.ui.graphics.Color.Red
                )
            }
        }
    }
  }
}
