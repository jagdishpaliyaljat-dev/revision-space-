package com.example.ui.components

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.AnalyticsRoute
import com.example.ui.DashboardRoute
import com.example.ui.CoinHistoryRoute
import com.example.ui.HelpAndSupportRoute
import com.example.ui.LanguageRoute
import com.example.ui.OfflineNotesRoute
import com.example.ui.ProfileRoute
import com.example.ui.SettingsRoute
import com.example.ui.SubjectsRoute
import com.example.ui.StreakDetailsRoute
import com.example.ui.TimerRoute
import com.example.ui.TodoListRoute
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.launch

/**
 * Small handle that lets any screen open the single, app-wide navigation drawer.
 * Screens simply call `LocalAppDrawer.current.open()` from their hamburger button,
 * so no extra callbacks have to be threaded through every screen signature.
 */
@Stable
class AppDrawerController internal constructor(private val openAction: () -> Unit) {
    fun open() = openAction()
}

val LocalAppDrawer = staticCompositionLocalOf<AppDrawerController> {
    AppDrawerController {}
}

/**
 * Hosts the shared hamburger drawer around the whole app.
 *
 * The drawer content is the same one already used on the Dashboard, so every
 * screen gets an identical menu (profile header, coins/streak, settings,
 * offline notes, language, help and logout) without duplicating logic.
 *
 * @param drawerEnabled when false (Dashboard), the drawer is not composed at all
 *        because the Dashboard already renders its own drawer.
 */
@Composable
fun AppDrawerHost(
    viewModel: RevisionViewModel,
    navController: NavHostController,
    drawerEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    val controller = remember(drawerState) {
        AppDrawerController { scope.launch { drawerState.open() } }
    }

    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    if (drawerEnabled) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    closeDrawer = closeDrawer,
                    onNavigate = { destination ->
                        when (destination) {
                            DrawerDestination.Home -> navController.navigateTab(DashboardRoute)
                            DrawerDestination.Subjects -> navController.navigateTab(SubjectsRoute)
                            DrawerDestination.Syllabus -> navController.navigateSafely(SyllabusRoute)
                            DrawerDestination.Focus -> navController.navigateTab(TimerRoute("", false))
                            DrawerDestination.Tasks -> navController.navigateTab(TodoListRoute)
                            DrawerDestination.Analytics -> navController.navigateSafely(AnalyticsRoute)
                            DrawerDestination.Profile -> navController.navigateSafely(ProfileRoute())
                            DrawerDestination.Coins -> navController.navigateSafely(CoinHistoryRoute)
                            DrawerDestination.Streak -> navController.navigateSafely(StreakDetailsRoute)
                            DrawerDestination.Settings -> navController.navigateSafely(SettingsRoute)
                            DrawerDestination.OfflineNotes -> navController.navigateSafely(OfflineNotesRoute)
                            DrawerDestination.Language -> navController.navigateSafely(LanguageRoute)
                            DrawerDestination.Help -> navController.navigateSafely(HelpAndSupportRoute)
                        }
                    },
                    onLogoutClick = { showLogoutDialog = true }
                )
            }
        ) {
            CompositionLocalProvider(LocalAppDrawer provides controller) {
                content()
            }
        }
    } else {
        CompositionLocalProvider(LocalAppDrawer provides controller) {
            content()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(context) {
                            navController.navigateFresh(com.example.ui.SignInRoute)
                        }
                    },
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Logout", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.testTag("cancel_logout_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** Navigation helper that never crashes on a duplicate/failed destination. */
private fun NavHostController.navigateSafely(route: Any) {
    try {
        navigate(route) { launchSingleTop = true }
    } catch (e: Exception) {
        Log.e("AppDrawer", "Navigation failed for $route", e)
    }
}

/**
 * Opens one of the four main tabs (Home / Subjects / Focus / Tasks) without
 * stacking duplicates, mirroring the bottom navigation behaviour.
 */
private fun NavHostController.navigateTab(route: Any) {
    try {
        navigate(route) {
            popUpTo(DashboardRoute) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: Exception) {
        Log.e("AppDrawer", "Tab navigation failed for $route", e)
    }
}

/** Used after logout: clears the whole back stack before showing the login screen. */
private fun NavHostController.navigateFresh(route: Any) {
    try {
        navigate(route) { popUpTo(0) { inclusive = true } }
    } catch (e: Exception) {
        Log.e("AppDrawer", "Navigation failed for $route", e)
    }
}
