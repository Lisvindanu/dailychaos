// File: app/src/main/java/com/dailychaos/project/presentation/ui/navigation/ChaosNavGraph.kt
package com.dailychaos.project.presentation.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.dailychaos.project.presentation.ui.screen.community.detail.CommunityPostDetailScreen
import com.dailychaos.project.presentation.ui.screen.community.support.SupportScreen
import timber.log.Timber

/**
 * Main Navigation Graph for Daily Chaos
 *
 * "Navigation yang clean seperti party formation Kazuma!"
 */
@RequiresApi(Build.VERSION_CODES.O)
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
        currentRoute?.startsWith(ChaosDestinations.COMMUNITY_POST_ROUTE) == true -> false // ADDED
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
        // DIUBAH: Menggunakan warna primer (WoodBrown) yang lebih gelap
        containerColor = MaterialTheme.colorScheme.primary
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
                        // DIUBAH: Menggunakan warna onPrimary yang lebih terang untuk border
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        if (currentRoute == ChaosDestinations.CREATE_CHAOS_ROUTE)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
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
                        // DIUBAH: Menggunakan warna onPrimaryContainer untuk kontras
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 200), label = "icon_color_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple effect
                onClick = onClick
            )
            .height(64.dp) // Fixed height to prevent layout jumps
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedColor,
            modifier = Modifier.size(24.dp)
        )

        // Animated dot indicator
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(150)) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary, // Dot color
                        shape = CircleShape
                    )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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
            Timber.d("🔐 LoginScreen COMPOSABLE CREATED")
            LoginScreen(
                onLoginSuccess = {
                    Timber.d("🏃 LoginScreen onLoginSuccess callback triggered!")
                    Timber.d("🏃 About to navigate to HOME_ROUTE")

                    // Update MainViewModel auth state
                    mainViewModel?.refreshAuthState()

                    navController.navigate(ChaosDestinations.HOME_ROUTE) {
                        popUpTo(ChaosDestinations.LOGIN_ROUTE) { inclusive = true }
                    }

                    Timber.d("🏃 Navigation to HOME_ROUTE completed")
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
                    navController.navigate(ChaosDestinations.LOGIN_ROUTE) {
                        // Hapus RegisterScreen dari back stack saat kembali ke Login
                        popUpTo(ChaosDestinations.REGISTER_ROUTE) {
                            inclusive = true
                        }
                        // Pastikan tidak ada duplikat LoginScreen di atas stack
                        launchSingleTop = true
                    }
                }
            )
        }

        // Main App Graph - UPDATED: Removed test navigation
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
                },
                konoSubaApiService = konoSubaApiService
            )
        }

        composable(ChaosDestinations.JOURNAL_ROUTE) {
            ChaosHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { entryId ->
                    // FIXED: Personal chaos entries from journal
                    navController.navigate(ChaosDestinations.chaosDetailRoute(entryId))
                }
            )
        }

        composable(ChaosDestinations.COMMUNITY_ROUTE) {
            CommunityFeedScreen(
                onNavigateToPost = { postId ->
                    // FIXED: Community posts should use community post route
                    navController.navigate(ChaosDestinations.communityPostRoute(postId))
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
            LaunchedEffect(Unit) {
                Timber.d("🎯 CreateChaosScreen COMPOSABLE CREATED!")
            }
            CreateChaosScreen(
                onNavigateBack = { navController.popBackStack() },
                onChaosSaved = {
                    mainViewModel?.chaosEntryCreated("new_chaos_entry")
                    navController.popBackStack()
                }
            )
        }

        // PERSONAL CHAOS ENTRY DETAIL (for user's own entries)
        composable(
            route = ChaosDestinations.CHAOS_DETAIL_WITH_ID,
            arguments = listOf(
                navArgument(ChaosDestinations.Args.ENTRY_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(ChaosDestinations.Args.ENTRY_ID) ?: ""
            Timber.d("🔍 PERSONAL Chaos Detail Screen - Entry ID: $entryId")
            ChaosDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(ChaosDestinations.editChaosRoute(id))
                }
            )
        }

        // COMMUNITY POST DETAIL (for community posts)
        composable(
            route = ChaosDestinations.COMMUNITY_POST_WITH_ID,
            arguments = listOf(
                navArgument(ChaosDestinations.Args.POST_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString(ChaosDestinations.Args.POST_ID) ?: ""
            Timber.d("🌍 COMMUNITY Post Detail Screen - Post ID: $postId")

            CommunityPostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSupport = { supportPostId ->
                    // ✅ FIXED: Navigate to support screen
                    Timber.d("💬 Navigating to Support Screen - Post ID: $supportPostId")
                    navController.navigate(ChaosDestinations.supportRoute(supportPostId))
                }
            )
        }

        // ✅ NEW: SUPPORT/COMMENT SCREEN - ADD THIS ROUTE
        composable(
            route = ChaosDestinations.SUPPORT_WITH_POST_ID,
            arguments = listOf(
                navArgument(ChaosDestinations.Args.POST_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString(ChaosDestinations.Args.POST_ID) ?: ""
            Timber.d("💬 Support Screen - Post ID: $postId")

            SupportScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() }
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
                    // FIXED: Navigate to community post route
                    navController.navigate(ChaosDestinations.communityPostRoute(postId))
                }
            )
        }
    }
}