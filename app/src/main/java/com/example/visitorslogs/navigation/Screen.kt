package com.example.visitorslogs.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object ResidentDashboard : Screen("resident_dashboard")
    object GuardDashboard : Screen("guard_dashboard")
    object SuperAdminDashboard : Screen("super_admin_dashboard")
}
