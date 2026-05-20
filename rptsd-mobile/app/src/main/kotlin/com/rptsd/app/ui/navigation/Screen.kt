package com.rptsd.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Stats : Screen("stats")
    object Feedback : Screen("feedback")
    object Profile : Screen("profile")
    object Subscription : Screen("subscription")
    object Payment : Screen("payment")
    object PaymentConfirm : Screen("payment_confirm")
    object Comments : Screen("comments")
    object Settings : Screen("settings")
    object Permissions : Screen("permissions")
}
