// File: app/src/main/java/com/dailychaos/project/presentation/MainViewModel.kt
package com.dailychaos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel - Global app state management
 *
 * "ViewModel utama untuk manage event global aplikasi - seperti guild master yang koordinasi semua quest!"
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _globalEvents = MutableSharedFlow<GlobalEvent>()
    val globalEvents = _globalEvents.asSharedFlow()

    // Authentication state untuk navigation
    private val _authState = MutableStateFlow<AuthenticationState>(AuthenticationState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthenticationState()
    }

    /**
     * Check if user is logged in - untuk navigation logic
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuthService.currentUser != null
    }

    /**
     * Check authentication state from multiple sources
     */
    private fun checkAuthenticationState() {
        viewModelScope.launch {
            try {
                val currentUser = firebaseAuthService.currentUser
                val isFirstLaunch = userPreferences.isFirstLaunch.first()
                val onboardingCompleted = userPreferences.onboardingCompleted.first()

                when {
                    currentUser == null -> {
                        _authState.value = if (!onboardingCompleted && isFirstLaunch) {
                            AuthenticationState.NeedsOnboarding
                        } else {
                            AuthenticationState.Unauthenticated
                        }
                    }
                    else -> {
                        _authState.value = AuthenticationState.Authenticated(currentUser.uid)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthenticationState.Error(e.message ?: "Auth check failed")
            }
        }
    }

    /**
     * Handle user logout globally
     */
    fun userLoggedOut() {
        viewModelScope.launch {
            try {
                firebaseAuthService.logout()
                userPreferences.clearUserData()
                _authState.value = AuthenticationState.Unauthenticated
                _globalEvents.emit(GlobalEvent.UserLoggedOut)
            } catch (e: Exception) {
                _globalEvents.emit(GlobalEvent.Error("Logout failed: ${e.message}"))
            }
        }
    }

    /**
     * Handle successful login - update auth state
     */
    fun userLoggedIn(userId: String) {
        viewModelScope.launch {
            _authState.value = AuthenticationState.Authenticated(userId)
            _globalEvents.emit(GlobalEvent.UserLoggedIn(userId))
        }
    }

    /**
     * Handle post creation (generic for any content)
     */
    fun postCreated(postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.PostCreated(postId))
        }
    }

    /**
     * Handle post update
     */
    fun postUpdated(postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.PostUpdated(postId))
        }
    }

    /**
     * Handle chaos entry creation
     */
    fun chaosEntryCreated(entryId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosEntryCreated(entryId))
        }
    }

    /**
     * Handle chaos entry update
     */
    fun chaosEntryUpdated(entryId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosEntryUpdated(entryId))
        }
    }

    /**
     * Force refresh authentication state
     */
    fun refreshAuthState() {
        checkAuthenticationState()
    }
}

/**
 * Authentication states for navigation logic
 */
sealed class AuthenticationState {
    data object Loading : AuthenticationState()
    data object NeedsOnboarding : AuthenticationState()
    data object Unauthenticated : AuthenticationState()
    data class Authenticated(val userId: String) : AuthenticationState()
    data class Error(val message: String) : AuthenticationState()
}

/**
 * Global Events untuk cross-screen communication
 */
sealed class GlobalEvent {
    data object UserLoggedOut : GlobalEvent()
    data class UserLoggedIn(val userId: String) : GlobalEvent()
    data class PostCreated(val postId: String) : GlobalEvent()
    data class PostUpdated(val postId: String) : GlobalEvent()
    data class ChaosEntryCreated(val entryId: String) : GlobalEvent()
    data class ChaosEntryUpdated(val entryId: String) : GlobalEvent()
    data class Error(val message: String) : GlobalEvent()
}