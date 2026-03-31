package com.example.visitorslogs.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.visitorslogs.domain.model.UserRole
import com.example.visitorslogs.ui.auth.AuthViewModel
import com.example.visitorslogs.ui.auth.LoginScreen
import com.example.visitorslogs.ui.auth.SuperAdminDashboardScreen
import com.example.visitorslogs.ui.guard.GuardDashboardScreen
import com.example.visitorslogs.ui.resident.ResidentDashboardScreen

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    val user = authViewModel.authState.value.user
                    val route = when (user?.role) {
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                        UserRole.RESIDENT -> Screen.ResidentDashboard.route
                        UserRole.GUARD -> Screen.GuardDashboard.route
                        UserRole.SUPER_ADMIN -> Screen.SuperAdminDashboard.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.AdminDashboard.route) {
            val user = authViewModel.authState.value.user
            com.example.visitorslogs.ui.admin.AdminDashboardScreen(
                adminName = user?.name ?: "Admin",
                societyId = user?.societyId ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ResidentDashboard.route) {
            val user = authViewModel.authState.value.user
            ResidentDashboardScreen(
                flatNumber = user?.flatNumber ?: "Unknown",
                societyId = user?.societyId ?: "",
                currentUserId = user?.userId ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.GuardDashboard.route) {
            val user = authViewModel.authState.value.user
            GuardDashboardScreen(
                guardId = user?.userId ?: "Unknown",
                societyId = user?.societyId ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SuperAdminDashboard.route) {
            SuperAdminDashboardScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
