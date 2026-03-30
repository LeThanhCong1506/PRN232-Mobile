package com.example.scamazon_frontend.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scamazon_frontend.core.utils.CartBadgeManager
import com.example.scamazon_frontend.ui.components.BottomNavItem
import com.example.scamazon_frontend.ui.components.LafyuuBottomNavBar
import com.example.scamazon_frontend.ui.screens.chatbot.ChatbotWidget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

/**
 * Main Screen with Bottom Navigation
 * Wraps the NavGraph with bottom navigation bar
 * Supports both Customer and Admin role-based navigation
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Observe global cart count — updated by CartViewModel.fetchCart() and addToCart
    val cartItemCount by CartBadgeManager.cartItemCount.collectAsState()

    // Customer screens that should show bottom navigation
    val customerBottomNavScreens = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Cart.route,
        Screen.Account.route
    )

    // Admin screens that should show bottom navigation
    val adminBottomNavScreens = listOf(
        Screen.AdminDashboard.route,
        Screen.AdminProducts.route,
        Screen.AdminCategories.route,
        Screen.AdminAccount.route
    )

    // Normalize route: "explore?query={query}" template → "explore" so the tab is recognized
    val normalizedRoute = when {
        currentRoute?.startsWith(Screen.Explore.route) == true -> Screen.Explore.route
        else -> currentRoute
    }

    val showCustomerBottomNav = normalizedRoute in customerBottomNavScreens
    val showAdminBottomNav = normalizedRoute in adminBottomNavScreens

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            when {
                showAdminBottomNav -> {
                    LafyuuBottomNavBar(
                        items = adminBottomNavItems,
                        currentRoute = normalizedRoute ?: Screen.AdminDashboard.route,
                        onItemClick = { item ->
                            navController.navigate(item.route) {
                                popUpTo(Screen.AdminDashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                showCustomerBottomNav -> {
                    LafyuuBottomNavBar(
                        currentRoute = normalizedRoute ?: Screen.Home.route,
                        onItemClick = { item ->
                            if (item.route == Screen.Home.route) {
                                // Navigate to Home and clear the entire back stack above it
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        cartBadgeCount = cartItemCount
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NavGraph(
                navController = navController,
                startDestination = Screen.Login.route
            )
            // Floating AI chatbot widget — visible on all customer main tabs
            if (showCustomerBottomNav) {
                ChatbotWidget()
            }
        }
    }
}

/**
 * Check if the given route matches bottom nav item route
 */
fun getBottomNavItem(route: String?): BottomNavItem? {
    return when (route) {
        Screen.Home.route -> BottomNavItem.Home
        Screen.Explore.route -> BottomNavItem.Explore
        Screen.Cart.route -> BottomNavItem.Cart
        Screen.Account.route -> BottomNavItem.Account
        else -> null
    }
}
