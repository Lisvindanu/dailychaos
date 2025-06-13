package com.dailychaos.project.presentation.ui.screen.splash

/**
 * Splash Screen UI State
 *
 * "Menentukan ke mana petualangan akan dimulai setelah loading."
 */
data class SplashUiState(
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Navigation destination after splash.
 */
sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object Auth : SplashDestination()
    object Home : SplashDestination()
}