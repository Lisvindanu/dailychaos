// File: app/src/main/java/com/dailychaos/project/presentation/ui/navigation/ChaosNavGraph.kt
package com.dailychaos.project.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import com.dailychaos.project.presentation.MainViewModel
import com.dailychaos.project.presentation.ui.screen.auth.login.LoginScreen
import com.dailychaos.project.presentation.ui.screen.auth.onboarding.OnboardingScreen
import com.dailychaos.project.presentation.ui.screen.auth.profile.ProfileScreen
import com.dailychaos.project.presentation.ui.screen.auth.register.RegisterScreen
import com.dailychaos.project.presentation.ui.screen.chaos.create.CreateChaosScreen
import com.dailychaos.project.presentation.ui.screen.chaos.detail.ChaosDetailScreen
import com.dailychaos.project.presentation.ui.screen.chaos.edit.EditChaosScreen
import com.dailychaos.project.presentation.ui.screen.chaos.history.ChaosHistoryScreen
import com.dailychaos.project.presentation.ui.screen.community.feed.CommunityFeedScreen
import com.dailychaos.project.presentation.ui.screen.community.twins.ChaosTwinsScreen
import com.dailychaos.project.presentation.ui.screen.home.HomeScreen
import com.dailychaos.project.presentation.ui.screen.settings.SettingsScreen
import com.dailychaos.project.presentation.ui.screen.splash.SplashScreen

/**
 * Main Navigation Graph for Daily Chaos
 *
 * "Navigation yang clean seperti party formation Kazuma!"
 */
@Composable
fun ChaosNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel? = null,
    konoSubaApiService: KonoSubaApiService? = null
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Cek apakah layar saat ini harus menampilkan navigasi bawah
    val showBottomNav = when {
        currentRoute == ChaosDestinations.SPLASH_ROUTE -> false
        currentRoute == ChaosDestinations.ONBOARDING_ROUTE -> false
        currentRoute == ChaosDestinations.LOGIN_ROUTE -> false
        currentRoute == ChaosDestinations.REGISTER_ROUTE -> false
        currentRoute == ChaosDestinations.SETTINGS_ROUTE -> false
        currentRoute == ChaosDestinations.CREATE_CHAOS_ROUTE -> false
        currentRoute?.startsWith(ChaosDestinations.CHAOS_DETAIL_ROUTE) == true -> false
        currentRoute?.startsWith(ChaosDestinations.EDIT_CHAOS_ROUTE) == true -> false
        currentRoute == ChaosDestinations.CHAOS_TWINS_ROUTE -> false
        else -> true
    }

    if (showBottomNav) {
        Scaffold(
            // TAMBAHKAN BARIS INI untuk membuat latar Scaffold transparan
            containerColor = Color.Transparent,
            bottomBar = {
                ChaosBottomNavigationBar(
                    currentRoute = currentRoute,
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
            ChaosNavHost(
                navController = navController,
                mainViewModel = mainViewModel,
                contentPadding = paddingValues,
                konoSubaApiService = konoSubaApiService
            )
        }

    } else {
        ChaosNavHost(
            navController = navController,
            mainViewModel = mainViewModel,
            contentPadding = PaddingValues(0.dp),
            konoSubaApiService = konoSubaApiService
        )
    }
}

@Composable
private fun ChaosBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            ChaosBottomNavItem(
                icon = if (currentRoute == ChaosDestinations.HOME_ROUTE) Icons.Filled.Home else Icons.Outlined.Home,
                isSelected = currentRoute == ChaosDestinations.HOME_ROUTE,
                onClick = { onNavigate(ChaosDestinations.HOME_ROUTE) }
            )

            // Journal/History
            ChaosBottomNavItem(
                icon = if (currentRoute == ChaosDestinations.JOURNAL_ROUTE) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook,
                isSelected = currentRoute == ChaosDestinations.JOURNAL_ROUTE,
                onClick = { onNavigate(ChaosDestinations.JOURNAL_ROUTE) }
            )

            // Create Chaos (Center FAB-style)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        if (currentRoute == ChaosDestinations.CREATE_CHAOS_ROUTE)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onNavigate(ChaosDestinations.CREATE_CHAOS_ROUTE) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Chaos",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Community
            ChaosBottomNavItem(
                icon = if (currentRoute == ChaosDestinations.COMMUNITY_ROUTE) Icons.Filled.People else Icons.Outlined.People,
                isSelected = currentRoute == ChaosDestinations.COMMUNITY_ROUTE,
                onClick = { onNavigate(ChaosDestinations.COMMUNITY_ROUTE) }
            )

            // Profile
            ChaosBottomNavItem(
                icon = if (currentRoute == ChaosDestinations.PROFILE_ROUTE) Icons.Filled.Person else Icons.Outlined.Person,
                isSelected = currentRoute == ChaosDestinations.PROFILE_ROUTE,
                onClick = { onNavigate(ChaosDestinations.PROFILE_ROUTE) }
            )
        }
    }
}

@Composable
private fun ChaosBottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ChaosNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel? = null,
    contentPadding: PaddingValues,
    konoSubaApiService: KonoSubaApiService? = null
) {
    NavHost(
        navController = navController,
        startDestination = ChaosDestinations.SPLASH_ROUTE,
        modifier = Modifier.padding(contentPadding)
    ) {

        // Splash Screen - FIXED: Force ke onboarding untuk testing
        composable(ChaosDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(ChaosDestinations.ONBOARDING_ROUTE) {
                        popUpTo(ChaosDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(ChaosDestinations.HOME_ROUTE) {
                        popUpTo(ChaosDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                // FIXED: Force false untuk memastikan onboarding muncul
                isUserLoggedIn = false,  // ← CHANGED: Force ke onboarding
                apiService = konoSubaApiService
            )
        }

        // Onboarding
        composable(ChaosDestinations.ONBOARDING_ROUTE) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(ChaosDestinations.LOGIN_ROUTE) {
                        popUpTo(ChaosDestinations.ONBOARDING_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // Auth Graph
        composable(ChaosDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    // Update MainViewModel auth state
                    mainViewModel?.refreshAuthState()
                    navController.navigate(ChaosDestinations.HOME_ROUTE) {
                        popUpTo(ChaosDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(ChaosDestinations.REGISTER_ROUTE)
                }
            )
        }

        // Register flow - redirect ke LOGIN bukan HOME setelah registrasi
        composable(ChaosDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Setelah register berhasil, arahkan ke halaman login
                    navController.navigate(ChaosDestinations.LOGIN_ROUTE) {
                        popUpTo(ChaosDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Graph
        composable(ChaosDestinations.HOME_ROUTE) {
            HomeScreen(
                onNavigateToCreateChaos = {
                    navController.navigate(ChaosDestinations.CREATE_CHAOS_ROUTE)
                },
                onNavigateToHistory = {
                    navController.navigate(ChaosDestinations.JOURNAL_ROUTE)
                },
                onNavigateToCommunity = {
                    navController.navigate(ChaosDestinations.COMMUNITY_ROUTE)
                },
                onNavigateToEntry = { entryId ->
                    navController.navigate(ChaosDestinations.chaosDetailRoute(entryId))
                }
            )
        }

        composable(ChaosDestinations.JOURNAL_ROUTE) {
            ChaosHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { entryId ->
                    navController.navigate(ChaosDestinations.chaosDetailRoute(entryId))
                }
            )
        }

        composable(ChaosDestinations.COMMUNITY_ROUTE) {
            CommunityFeedScreen(
                onNavigateToPost = { postId ->
                    navController.navigate(ChaosDestinations.chaosDetailRoute(postId))
                },
                onNavigateToTwins = {
                    navController.navigate(ChaosDestinations.CHAOS_TWINS_ROUTE)
                }
            )
        }

        composable(ChaosDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(ChaosDestinations.SETTINGS_ROUTE)
                },
                onNavigateToLogin = {
                    mainViewModel?.userLoggedOut()
                    navController.navigate(ChaosDestinations.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Secondary Screens
        composable(ChaosDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ChaosDestinations.CREATE_CHAOS_ROUTE) {
            CreateChaosScreen(
                onNavigateBack = { navController.popBackStack() },
                onChaosSaved = {
                    mainViewModel?.chaosEntryCreated("new_chaos_entry")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = ChaosDestinations.CHAOS_DETAIL_WITH_ID,
            arguments = listOf(
                navArgument(ChaosDestinations.Args.ENTRY_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(ChaosDestinations.Args.ENTRY_ID) ?: ""
            ChaosDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(ChaosDestinations.editChaosRoute(id))
                }
            )
        }

        composable(
            route = ChaosDestinations.EDIT_CHAOS_WITH_ID,
            arguments = listOf(
                navArgument(ChaosDestinations.Args.ENTRY_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(ChaosDestinations.Args.ENTRY_ID) ?: ""
            EditChaosScreen(
                onNavigateBack = { navController.popBackStack() },
                onChaosUpdated = {
                    mainViewModel?.chaosEntryUpdated(entryId)
                    navController.popBackStack()
                }
            )
        }

        composable(ChaosDestinations.CHAOS_TWINS_ROUTE) {
            ChaosTwinsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPost = { postId ->
                    navController.navigate(ChaosDestinations.chaosDetailRoute(postId))
                }
            )
        }
    }
}