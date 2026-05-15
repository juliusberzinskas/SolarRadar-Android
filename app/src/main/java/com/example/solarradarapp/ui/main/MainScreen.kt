package com.example.solarradarapp.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.solarradarapp.ui.dashboard.DashboardScreen
import com.example.solarradarapp.ui.home.HomeScreen
import com.example.solarradarapp.ui.jobs.JobListScreen
import com.example.solarradarapp.ui.strings.AppStrings
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.*

private data class BottomNavItem(
    val route: String,
    val labelKey: (AppStrings) -> String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("dashboard", { it.navHome }, Icons.Default.Home),
    BottomNavItem("jobs", { it.navJobs }, Icons.Default.Work),
    BottomNavItem("home", { it.navProfile }, Icons.Default.Person)
)

@Composable
fun MainScreen(
    onJobClick: (String) -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit
) {
val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val strings = LocalAppStrings.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundNav,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.labelKey(strings)) },
                        label = {
                            Text(
                                item.labelKey(strings),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LightBlue,
                            selectedTextColor = TextOnDark,
                            unselectedIconColor = NavIconInactive,
                            unselectedTextColor = TextOnDarkSecondary,
                            indicatorColor = NavActiveIndicator
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(onJobClick = onJobClick)
            }
            composable("jobs") {
                JobListScreen(
                    onJobClick = onJobClick,
                    onLogout = onLogout
                )
            }
            composable("home") {
                HomeScreen(onOpenSettings = onOpenSettings)
            }
        }
    }
}
