/* app/src/main/java/com/dailychaos/project/presentation/MainViewModel.kt */
package com.dailychaos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _globalEvents = MutableSharedFlow<GlobalEvent>()
    val globalEvents = _globalEvents.asSharedFlow()

    private val _authState = MutableStateFlow<AuthenticationState>(AuthenticationState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthenticationState()
    }

    fun isUserLoggedIn(): Boolean {
        return authUseCases.isAuthenticated()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            authUseCases.getAuthState().collect { domainAuthState ->
                val onboardingCompleted = userPreferences.onboardingCompleted.first()
                val newAuthState = when (domainAuthState) {
                    is AuthState.Authenticated -> AuthenticationState.Authenticated(domainAuthState.user.id)
                    is AuthState.Error -> AuthenticationState.Error(domainAuthState.message)
                    is AuthState.Loading -> AuthenticationState.Loading
                    is AuthState.Unauthenticated -> if (!onboardingCompleted) {
                        AuthenticationState.NeedsOnboarding
                    } else {
                        AuthenticationState.Unauthenticated
                    }
                }
                _authState.value = newAuthState
            }
        }
    }

    fun userLoggedOut() {
        viewModelScope.launch {
            try {
                authUseCases.logout()
                // Auth state flow will automatically update the UI
                _globalEvents.emit(GlobalEvent.UserLoggedOut)
            } catch (e: Exception) {
                _globalEvents.emit(GlobalEvent.Error("Logout failed: ${e.message}"))
            }
        }
    }

    fun userLoggedIn(userId: String) {
        viewModelScope.launch {
            _authState.value = AuthenticationState.Authenticated(userId)
            _globalEvents.emit(GlobalEvent.UserLoggedIn(userId))
        }
    }

    fun postCreated(postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.PostCreated(postId))
        }
    }

    fun postUpdated(postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.PostUpdated(postId))
        }
    }

    fun chaosEntryCreated(entryId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosEntryCreated(entryId))
        }
    }

    fun chaosEntryUpdated(entryId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosEntryUpdated(entryId))
        }
    }

    fun refreshAuthState() {
        checkAuthenticationState()
    }
}

sealed class AuthenticationState {
    data object Loading : AuthenticationState()
    data object NeedsOnboarding : AuthenticationState()
    data object Unauthenticated : AuthenticationState()
    data class Authenticated(val userId: String) : AuthenticationState()
    data class Error(val message: String) : AuthenticationState()
}

sealed class GlobalEvent {
    data object UserLoggedOut : GlobalEvent()
    data class UserLoggedIn(val userId: String) : GlobalEvent()
    data class PostCreated(val postId: String) : GlobalEvent()
    data class PostUpdated(val postId: String) : GlobalEvent()
    data class ChaosEntryCreated(val entryId: String) : GlobalEvent()
    data class ChaosEntryUpdated(val entryId: String) : GlobalEvent()
    data class Error(val message: String) : GlobalEvent()
}