package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.CrmViewModel

@Composable
fun MainScreen(viewModel: CrmViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Registration,
        Screen.FollowUps,
        Screen.Sales,
        Screen.Dashboard
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Registration.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Registration.route) { CallRegistrationScreen(viewModel) }
                composable(Screen.FollowUps.route) { FollowUpsScreen(viewModel) }
                composable(Screen.Sales.route) { SalesOpportunitiesScreen(viewModel) }
                composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Registration : Screen("registration", "ثبت تماس", Icons.Default.Call)
    object FollowUps : Screen("follow_ups", "پیگیری‌ها", Icons.Default.History)
    object Sales : Screen("sales", "فرصت‌های فروش", Icons.Default.Payments)
    object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.BarChart)
}
