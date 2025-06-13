package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.fillMaxSize
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
import com.dailychaos.project.presentation.ui.screen.auth.profile.ProfileScreen
import com.dailychaos.project.presentation.ui.screen.chaos.create.CreateChaosScreen
import com.dailychaos.project.presentation.ui.screen.chaos.detail.ChaosDetailScreen
import com.dailychaos.project.presentation.ui.screen.chaos.edit.EditChaosScreen
import com.dailychaos.project.presentation.ui.screen.chaos.history.ChaosHistoryScreen
import com.dailychaos.project.presentation.ui.screen.community.feed.CommunityFeedScreen
import com.dailychaos.project.presentation.ui.screen.community.twins.ChaosTwinsScreen
import com.dailychaos.project.presentation.ui.screen.home.HomeScreen
import com.dailychaos.project.presentation.ui.screen.settings.SettingsScreen

/**
 * Main Navigation Screen - Clean Design tanpa FAB
 *
 * "Navigation yang clean dengan fokus pada bottom navigation bar"
 */

@Composable
fun MainNavigationScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MainNavigationBar(
                selectedRoute = currentRoute ?: "home",
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToCreateChaos = { navController.navigate("create_chaos") },
                    onNavigateToHistory = { navController.navigate("journal") },
                    onNavigateToCommunity = { navController.navigate("community") },
                    onNavigateToEntry = { id -> navController.navigate("chaos_detail/$id") }
                )
            }

            composable("journal") {
                ChaosHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEntry = { id -> navController.navigate("chaos_detail/$id") }
                )
            }

            composable("community") {
                CommunityFeedScreen(
                    // FIX: Navigate to the correct detail route
                    onNavigateToPost = { id -> navController.navigate("chaos_detail/$id") },
                    onNavigateToTwins = { navController.navigate("chaos_twins") }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onLogout = onNavigateToAuth
                )
            }

            composable("create_chaos") {
                CreateChaosScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onChaosSaved = { navController.popBackStack() }
                )
            }

            composable("chaos_detail/{entryId}") { backStackEntry ->
                ChaosDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate("edit_chaos/$id") }
                )
            }

            composable("edit_chaos/{entryId}") {
                EditChaosScreen() // Using placeholder for now
            }

            composable("chaos_twins") {
                ChaosTwinsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    // FIX: Navigate to the correct detail route
                    onNavigateToPost = { id -> navController.navigate("chaos_detail/$id") }
                )
            }

            composable("settings") {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}