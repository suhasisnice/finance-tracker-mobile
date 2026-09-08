package com.finsetpacer.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finsetpacer.app.ui.dashboard.DashboardScreen
import com.finsetpacer.app.ui.settings.SettingsScreen
import com.finsetpacer.app.ui.transactions.TransactionsScreen

/** Post onboarding/permission-gate content: bottom-nav'd Dashboard / Transactions / Settings. */
@Composable
fun FinSetNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar {
                Screen.bottomBarScreens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(screen), contentDescription = screen.label) },
                        label = { androidx.compose.material3.Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Transactions.route) { TransactionsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

private fun iconFor(screen: Screen) = when (screen) {
    Screen.Dashboard -> Icons.Filled.Home
    Screen.Transactions -> Icons.Filled.List
    Screen.Settings -> Icons.Filled.Settings
}
