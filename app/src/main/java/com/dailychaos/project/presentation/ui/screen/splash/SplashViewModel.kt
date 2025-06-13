package com.dailychaos.project.presentation.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userPreferences: UserPreferences
    // private val authRepository: AuthRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashDestination>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        decideNextScreen()
    }

    private fun decideNextScreen() {
        viewModelScope.launch {
            // Simulate a network/auth check
            delay(2000)

            // In a real app, you would check the actual login state
            // val isLoggedIn = authRepository.getCurrentUser() != null
            val isLoggedIn = true // Placeholder for dev
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