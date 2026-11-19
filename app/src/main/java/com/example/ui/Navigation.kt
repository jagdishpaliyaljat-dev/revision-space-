package com.example.ui
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.viewmodel.RevisionViewModel
import kotlinx.serialization.Serializable

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

// Routes
@Serializable object OfflineNotesRoute
@Serializable object SplashRoute
@Serializable object SignInRoute
//@Serializable object SignInRoute
@Serializable object DashboardRoute
@Serializable object CalendarRoute
@Serializable object TodoListRoute
@Serializable object SubjectsRoute
@Serializable data class ChapterListRoute(val subjectId: String, val subjectName: String)
@Serializable data class SubjectDetailsRoute(val subjectId: String)
@Serializable data class TopicListRoute(val chapterId: String, val chapterName: String)
@Serializable data class TopicDetailRoute(
    val topicId: String,
    val subjectId: String = "",
    val chapterId: String = "",
    val highlightStartRevision: Boolean = false,
    val showHistory: Boolean = false
)
@Serializable data class TimerRoute(
    val topicId: String = "",
    val isRevision: Boolean = false,
    val subjectId: String = "",
    val subjectName: String = "",
    val chapterId: String = "",
    val chapterName: String = ""
)
@Serializable data class SessionCompleteRoute(val studyTimeAddedMins: Int, val topicsRevised: Int, val coinsEarned: Int)
@Serializable data class TodayRevisionRoute(val initialView: String = "dashboard")
@Serializable object SyllabusRoute
@Serializable object AnalyticsRoute
@Serializable data class ProfileRoute(
    val openAchievements: Boolean = false,
    val openEditProfile: Boolean = false,
    val openExamGoal: Boolean = false
)
@Serializable object AdminDashboardRoute
@Serializable data class AdminUserDetailRoute(val userId: String, val email: String)
@Serializable object CoinHistoryRoute
@Serializable object LanguageRoute
@Serializable object HelpAndSupportRoute
@Serializable object LeaderboardRoute
@Serializable object WithdrawalRoute
@Serializable object StreakDetailsRoute
@Serializable object SettingsRoute
@Serializable object RevisionSettingsRoute
@Serializable object ReminderSettingsRoute
@Serializable object NotificationsRoute
@Serializable object MyTestScoresRoute

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: RevisionViewModel
) {
    val profile by viewModel.currentUserProfile.collectAsState(initial = null)
    val contextGlobal = androidx.compose.ui.platform.LocalContext.current
    val activity = contextGlobal as? com.example.MainActivity
    val intentFlowState = activity?.intentFlow?.collectAsStateWithLifecycle()

    var pendingDeepLinkTopicId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var pendingDeepLinkSubjectId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var pendingDeepLinkChapterId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(intentFlowState?.value) {
        val intent = intentFlowState?.value as? android.content.Intent
        if (intent != null) {
            val navigateTo = intent.getStringExtra("navigate_to")
            if (navigateTo == "today_revision") {
                intent.removeExtra("navigate_to")
                kotlinx.coroutines.delay(1000)
                navController.navigate(TodayRevisionRoute())
            }
            val topicId = intent.getStringExtra("open_topic_id")
            if (topicId != null && topicId.isNotEmpty()) {
                val subjectId = intent.getStringExtra("subjectId") ?: ""
                val chapterId = intent.getStringExtra("chapterId") ?: ""
                
                intent.removeExtra("open_topic_id") // Consume it
                
                // Add slight delay to allow auth state to resolve if opening cold
                kotlinx.coroutines.delay(1000)
                
                if (isTimerRunning) {
                    pendingDeepLinkTopicId = topicId
                    pendingDeepLinkSubjectId = subjectId
                    pendingDeepLinkChapterId = chapterId
                } else {
                    viewModel.handleDeepLinkNavigation(topicId, subjectId, chapterId, navController, contextGlobal)
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(profile) {
        if (profile?.isBanned == true && profile != null) {
             android.widget.Toast.makeText(contextGlobal, "Your account has been suspended by administrator.", android.widget.Toast.LENGTH_LONG).show()
        }
    }


    if (pendingDeepLinkTopicId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDeepLinkTopicId = null },
            title = { androidx.compose.material3.Text("A study session is already running") },
            text = { androidx.compose.material3.Text("You have an active timer. Do you want to end the current session and open this revision?") },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val tId = pendingDeepLinkTopicId!!
                        val sId = pendingDeepLinkSubjectId ?: ""
                        val cId = pendingDeepLinkChapterId ?: ""
                        pendingDeepLinkTopicId = null
                        
                        viewModel.resetTimer()
                        
                        viewModel.handleDeepLinkNavigation(
                            tId, sId, cId, navController, contextGlobal
                        )
                    }
                ) {
                    androidx.compose.material3.Text("End Current & Open")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { pendingDeepLinkTopicId = null }
                ) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && (
        currentRoute.contains("DashboardRoute") ||
        currentRoute.contains("SubjectsRoute") ||
        currentRoute.contains("TimerRoute") ||
        currentRoute.contains("TodoListRoute") ||
        currentRoute.contains("ProfileRoute") ||
        currentRoute.contains("AnalyticsRoute") ||
        currentRoute.contains("SessionCompleteRoute")
    )

    val selectedIndex = when {
        currentRoute?.contains("DashboardRoute") == true -> 0
        currentRoute?.contains("SubjectsRoute") == true -> 1
        currentRoute?.contains("TimerRoute") == true -> 2
        currentRoute?.contains("TodoListRoute") == true -> 3
        currentRoute?.contains("ProfileRoute") == true -> 4
        else -> -1
    }

    // App-wide hamburger drawer. Only the screens that actually render a
    // hamburger button get the drawer; the Dashboard keeps its own drawer so it
    // is skipped here to avoid composing two identical drawers on one screen.
    val drawerScreens = listOf("SubjectsRoute", "TimerRoute", "TodoListRoute", "ProfileRoute")
    val showAppDrawer = currentRoute?.let { route -> drawerScreens.any { route.contains(it) } } == true
    com.example.ui.components.AppDrawerHost(
        viewModel = viewModel,
        navController = navController,
        drawerEnabled = showAppDrawer
    ) {
    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    selectedBottomNav = selectedIndex,
                    onNavigateToHome = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(DashboardRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSubjects = {
                        navController.navigate(SubjectsRoute) {
                            popUpTo(DashboardRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTimer = {
                        navController.navigate(TimerRoute("", false)) {
                            popUpTo(DashboardRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTodoList = {
                        navController.navigate(TodoListRoute) {
                            popUpTo(DashboardRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(ProfileRoute()) {
                            popUpTo(DashboardRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
            enterTransition = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { 300 },
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            },
            exitTransition = {
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { -300 },
                    animationSpec = androidx.compose.animation.core.tween(250)
                )
            },
            popEnterTransition = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { -300 },
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            },
            popExitTransition = {
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { 300 },
                    animationSpec = androidx.compose.animation.core.tween(250)
                )
            }
        ) {
        composable<SplashRoute> {
            SplashScreen(
                isSetupComplete = true,
                onTransitionComplete = {
                    // Show the login screen on first launch so real Google sign-in can be used.
                    // If the user is already signed in (or previously continued as guest), go
                    // straight to the dashboard so they are never blocked by the login screen.
                    val destination = try {
                        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null ||
                            contextGlobal.getSharedPreferences("revision_space_prefs", android.content.Context.MODE_PRIVATE)
                                .getBoolean("guest_continue", false)
                        ) DashboardRoute else SignInRoute
                    } catch (e: Exception) {
                        SignInRoute
                    }
                    navController.navigate(destination) { popUpTo(SplashRoute) { inclusive = true } }
                }
            )
        }
        composable<SignInRoute> {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            var isLoading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            var error by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
            LoginScreen(
                onSignedIn = { idToken ->
                    isLoading = true
                    error = null
                    scope.launch {
                        try {
                            // Safety net: if any backend call stalls, stop the spinner and
                            // move on instead of leaving the user staring at "loading".
                            kotlinx.coroutines.withTimeoutOrNull(18000L) {
                                viewModel.signInWithGoogle(idToken)
                            }
                            isLoading = false
                            navController.navigate(DashboardRoute) { popUpTo(SignInRoute) { inclusive = true } }
                        } catch (e: Exception) {
                            isLoading = false
                            val msg = e.message.orEmpty()
                            if (msg.contains("offline", ignoreCase = true) ||
                                msg.contains("unavailable", ignoreCase = true)
                            ) {
                                // Auth already succeeded; Firestore will sync when back online.
                                navController.navigate(DashboardRoute) { popUpTo(SignInRoute) { inclusive = true } }
                            } else {
                                error = msg.ifBlank { "Sign in failed" }
                            }
                        }
                    }
                },
                onGuestContinue = {
                    contextGlobal.getSharedPreferences("revision_space_prefs", android.content.Context.MODE_PRIVATE)
                        .edit().putBoolean("guest_continue", true).apply()
                    navController.navigate(DashboardRoute) { popUpTo(SignInRoute) { inclusive = true } }
                },
                onSignInFailed = { message -> error = message },
                isLoading = isLoading,
                errorMessage = error
            )
        }
        composable<DashboardRoute> {
            val globalConfig by viewModel.globalConfig.collectAsState(initial = null)
            Column(modifier = Modifier.fillMaxSize()) {
                if (globalConfig?.activeAnnouncementTitle?.isNotBlank() == true) {
                    androidx.compose.material3.Surface(color = Color(0xFF3B82F6), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(globalConfig?.activeAnnouncementTitle ?: "", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(globalConfig?.activeAnnouncementMessage ?: "", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (globalConfig?.isBetaFeatureEnabled == true) {
                    androidx.compose.material3.Surface(color = Color(0xFF10B981), modifier = Modifier.fillMaxWidth()) {
                        Text("Beta Mode Enabled: Previewing Experimental Features", color = Color.White, modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSubjects = {
                            try {
                                navController.navigate(SubjectsRoute)
                            } catch (e: Exception) {
                                android.util.Log.e("Navigation", "Error navigating to SubjectsRoute", e)
                            }
                        },
                        onNavigateToToday = { navController.navigate(TodayRevisionRoute()) },
                        onNavigateToAnalytics = { navController.navigate(AnalyticsRoute) },
                        onNavigateToProfile = { navController.navigate(ProfileRoute()) },
                        onNavigateToTopic = { topicId -> navController.navigate(TopicDetailRoute(topicId)) },
                        onNavigateToTimer = { navController.navigate(TimerRoute("", false)) },
                        onNavigateToSyllabus = { navController.navigate(SyllabusRoute) },
                        onNavigateToCalendar = { navController.navigate(CalendarRoute) },
                        onNavigateToTodoList = { navController.navigate(TodoListRoute) },
                        onNavigateToWallet = { navController.navigate(CoinHistoryRoute) },
                        onNavigateToStreak = { navController.navigate(StreakDetailsRoute) },
                        onNavigateToSettings = { navController.navigate(SettingsRoute) },
                        onNavigateToOfflineNotes = { navController.navigate(OfflineNotesRoute) },
                        onNavigateToNotifications = { navController.navigate(NotificationsRoute) },
                        onLogout = {
                            // Logging out must return to the login screen, not the dashboard.
                            navController.navigate(SignInRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToLanguage = { navController.navigate(LanguageRoute) },
                        onNavigateToHelpAndSupport = { navController.navigate(HelpAndSupportRoute) }
                    )
                }
            }
        }
        composable<SubjectsRoute> {
            SubjectsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(DashboardRoute) { popUpTo(DashboardRoute) { inclusive = true } } },
                onNavigateToTimer = { navController.navigate(TimerRoute("", false)) },
                onNavigateToTodoList = { navController.navigate(TodoListRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute()) },
                onTopicClick = { subjectId -> navController.navigate(SubjectDetailsRoute(subjectId)) },
                onStartRevision = { topicId -> navController.navigate(TimerRoute(topicId, true)) }
            )
        }
        composable<ChapterListRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ChapterListRoute>()
            SubjectDetailsScreenWrapper(
                viewModel = viewModel,
                navController = navController,
                subjectId = route.subjectId
            )
        }
        composable<TopicListRoute> { backStackEntry ->
            TopicListScreenWrapper(viewModel, navController, backStackEntry)
        }
        composable<TopicDetailRoute> { backStackEntry ->
            TopicDetailScreenWrapper(viewModel, navController, backStackEntry)
        }
        composable<OfflineNotesRoute> {
            com.example.ui.screens.subject.OfflineNotesScreen(navController = navController)
        }
        composable<TimerRoute> { backStackEntry ->
            TimerScreenWrapper(viewModel, navController, backStackEntry)
        }
        composable<SessionCompleteRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SessionCompleteRoute>()
            SessionCompleteScreen(
                viewModel = viewModel,
                navController = navController,
                studyTimeAddedMins = route.studyTimeAddedMins,
                topicsRevised = route.topicsRevised,
                coinsEarned = route.coinsEarned,
                onBackToHome = {
                    try {
                        viewModel.resetTimer()
                        navController.navigate(DashboardRoute) {
                            popUpTo(DashboardRoute) { inclusive = false }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Navigation", "Error navigating to DashboardRoute", e)
                    }
                },
                onNavigateToSubjects = {
                    try {
                        navController.navigate(SubjectsRoute)
                    } catch (e: Exception) {
                        android.util.Log.e("Navigation", "Error navigating to SubjectsRoute from SessionCompleteScreen", e)
                    }
                },
                onNavigateToTimer = { navController.navigate(TimerRoute("", false)) },
                onNavigateToTodoList = { navController.navigate(TodoListRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute()) }
            )
        }
        composable<TodayRevisionRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TodayRevisionRoute>()
            TodayRevisionScreen(viewModel, navController, initialView = route.initialView)
        }
        composable<AnalyticsRoute> {
            AnalyticsScreen(viewModel, navController)
        }
        composable<ProfileRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProfileRoute>()
            ProfileScreen(
                viewModel,
                navController,
                openAchievements = route.openAchievements,
                openEditProfile = route.openEditProfile,
                openExamGoal = route.openExamGoal
            )
        }
        composable<AdminDashboardRoute> {
            AdminDashboardScreen(viewModel, navController)
        }
        composable<AdminUserDetailRoute> { backStackEntry ->
            AdminUserDetailScreenWrapper(viewModel, navController, backStackEntry)
        }
        composable<CoinHistoryRoute> {
            CoinHistoryScreen(viewModel, navController)
        }
        composable<LanguageRoute> {
            LanguageScreen(navController)
        }
        composable<HelpAndSupportRoute> {
            HelpAndSupportScreen(navController)
        }
        composable<LeaderboardRoute> {
            LeaderboardScreen(viewModel, navController)
        }
        composable<WithdrawalRoute> {
            WithdrawalScreen(viewModel, navController)
        }
        composable<SubjectDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SubjectDetailsRoute>()
            SubjectDetailsScreenWrapper(
                viewModel = viewModel,
                navController = navController,
                subjectId = route.subjectId
            )
        }
        composable<SyllabusRoute> {
            SyllabusScreen(
                viewModel = viewModel,
                navController = navController,
                onSubjectClick = { subjectId ->
                    navController.navigate(SubjectDetailsRoute(subjectId))
                }
            )
        }
        composable<CalendarRoute> {
            CalendarScreen(viewModel = viewModel, navController = navController)
        }
        composable<TodoListRoute> {
            TodoListScreen(viewModel = viewModel, navController = navController)
        }
        composable<StreakDetailsRoute> {
            StreakDetailsScreen(viewModel = viewModel, navController = navController)
        }
        composable<SettingsRoute> {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable<ReminderSettingsRoute> {
            com.example.ui.settings.ReminderSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<RevisionSettingsRoute> {
            RevisionSettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable<NotificationsRoute> {
            NotificationsScreen(viewModel = viewModel, navController = navController)
        }
        composable<MyTestScoresRoute> {
            MyTestScoresScreen(viewModel = viewModel, navController = navController)
        }
        }
        }
    }
}
