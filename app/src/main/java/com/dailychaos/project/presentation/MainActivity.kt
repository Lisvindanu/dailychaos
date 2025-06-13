package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.MainNavigationScreen
import com.dailychaos.project.presentation.ui.screen.auth.login.LoginScreen
import com.dailychaos.project.presentation.ui.screen.auth.onboarding.OnboardingScreen
import com.dailychaos.project.presentation.ui.screen.auth.register.RegisterScreen
import com.dailychaos.project.presentation.ui.screen.splash.SplashScreen
import com.dailychaos.project.presentation.ui.screen.splash.SplashDestination
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point aplikasi Daily Chaos
 *
 * "Seperti guild hall dimana semua petualangan dimulai!"
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DailyChaosTheme {
                DailyChaosApp()
            }
        }
    }
}

@Composable
fun DailyChaosApp() {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen { destination ->
                    val route = when (destination) {
                        SplashDestination.Onboarding -> "onboarding"
                        SplashDestination.Auth -> "auth"
                        SplashDestination.Home -> "main"
                    }
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            composable("onboarding") {
                OnboardingScreen {
                    navController.navigate("auth") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            }
            authGraph(navController)
            composable("main") {
                MainNavigationScreen(
                    onNavigateToAuth = {
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

fun androidx.navigation.NavGraphBuilder.authGraph(navController: NavHostController) {
    composable("auth") {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            },
            onNavigateToRegister = { navController.navigate("register") }
        )
    }
    composable("register") {
        RegisterScreen(
            onRegisterSuccess = {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            },
            onNavigateToLogin = { navController.popBackStack() }
        )
    }
}