/* app/src/main/java/com/dailychaos/project/presentation/ui/screen/splash/SplashViewModel.kt */
package com.dailychaos.project.presentation.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashDestination>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        decideNextScreen()
    }

    private fun decideNextScreen() {
        viewModelScope.launch {
            // Simulate a minimum splash time for better user experience
            delay(2500)

            val isLoggedIn = authUseCases.isAuthenticated()
            val onboardingCompleted = userPreferences.onboardingCompleted.first()

            val destination = when {
                !onboardingCompleted -> SplashDestination.Onboarding
                !isLoggedIn -> SplashDestination.Auth
                else -> SplashDestination.Home
            }
            _navigationEvent.emit(destination)
        }
    }
}