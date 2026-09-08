package com.finsetpacer.app.ui.navigation

sealed class Screen(val route: String, val label: String) {
    object Dashboard : Screen("dashboard", "Pacer")
    object Transactions : Screen("transactions", "Transactions")
    object Settings : Screen("settings", "Settings")

    companion object {
        val bottomBarScreens = listOf(Dashboard, Transactions, Settings)
    }
}
