package com.rptsd.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rptsd.app.ui.screens.auth.login.LoginScreen
import com.rptsd.app.ui.screens.auth.register.RegisterScreen
import com.rptsd.app.ui.screens.feedback.FeedbackScreen
import com.rptsd.app.ui.screens.home.HomeScreen
import com.rptsd.app.ui.screens.permissions.AccessibilityPermissionScreen
import com.rptsd.app.ui.screens.permissions.NotificationPermissionScreen
import com.rptsd.app.ui.screens.profile.ProfileScreen
import com.rptsd.app.ui.screens.settings.SettingsScreen
import com.rptsd.app.ui.screens.splash.SplashScreen
import com.rptsd.app.ui.screens.stats.StatsScreen
import com.rptsd.app.ui.screens.subscription.SubscriptionScreen

private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Stats.route,
    Screen.Feedback.route,
    Screen.Profile.route,
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Register.route) { RegisterScreen(navController) }
            composable(Screen.Subscription.route) { SubscriptionScreen(navController) }
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Stats.route) { StatsScreen(navController) }
            composable(Screen.Feedback.route) { FeedbackScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
            composable(Screen.Permissions.route) { NotificationPermissionScreen(navController) }
            composable(Screen.AccessibilityPermissions.route) { AccessibilityPermissionScreen(navController) }
        }
    }
}
