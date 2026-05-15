package com.example.solarradarapp.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.example.solarradarapp.ui.jobs.JobDetailScreen
import com.example.solarradarapp.ui.login.LoginScreen
import com.example.solarradarapp.ui.main.MainScreen
import com.example.solarradarapp.ui.report.EditReportScreen
import com.example.solarradarapp.ui.report.ReportFormScreen
import com.example.solarradarapp.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Default transitions for push screens (detail, report, settings)
    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(250)) { -it / 4 } + fadeOut(tween(250)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(250)) { it } + fadeOut(tween(250)) }
    ) {
        // Auth screens use crossfade — no directional hierarchy
        composable(
            route = "login",
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "main",
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(400)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            MainScreen(
                onJobClick = { firestoreId ->
                    navController.navigate("job_detail/$firestoreId")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Push screens inherit NavHost slide transitions
        composable(
            route = "job_detail/{firestoreId}",
            arguments = listOf(navArgument("firestoreId") { type = NavType.StringType })
        ) { backStack ->
            val firestoreId = backStack.arguments?.getString("firestoreId") ?: return@composable
            JobDetailScreen(
                firestoreId = firestoreId,
                onBack = { navController.popBackStack() },
                onSubmitReport = { id -> navController.navigate("report_form/$id") },
                onEditReport = { reportId -> navController.navigate("edit_report/$reportId") }
            )
        }

        composable(
            route = "report_form/{firestoreId}",
            arguments = listOf(navArgument("firestoreId") { type = NavType.StringType })
        ) { backStack ->
            val firestoreId = backStack.arguments?.getString("firestoreId") ?: return@composable
            ReportFormScreen(
                firestoreId = firestoreId,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "edit_report/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStack ->
            val reportId = backStack.arguments?.getString("reportId") ?: return@composable
            EditReportScreen(
                reportId = reportId,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
