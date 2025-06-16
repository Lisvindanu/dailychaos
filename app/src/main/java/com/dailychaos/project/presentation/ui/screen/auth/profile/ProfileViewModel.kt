/* app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/profile/ProfileViewModel.kt */
package com.dailychaos.project.presentation.ui.screen.auth.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UserProfile
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = authUseCases.getCurrentUser()
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        userProfile = mapDomainUserToUiProfile(user),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not load user profile. Please login again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred."
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCases.logout()
            // The navigation to the login screen is handled by the UI
            // after observing the auth state change.
        }
    }

    private fun mapDomainUserToUiProfile(user: User): UserProfile {
        return UserProfile(
            userId = user.id,
            username = user.anonymousUsername,
            displayName = user.anonymousUsername, // Assuming displayName is same as anonymousUsername for now
            email = user.email,
            chaosEntries = user.chaosEntriesCount,
            dayStreak = user.streakDays,
            supportGiven = user.supportGivenCount,
            joinDate = user.joinedAt.toString(), // Simplified conversion
            authType = if (user.isAnonymous) "username" else "email"
            // Other fields can be mapped here if the domain model is expanded
        )
    }
}