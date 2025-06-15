// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/profile/ProfileViewModel.kt
package com.dailychaos.project.presentation.ui.screen.auth.profile

import UserProfile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Check if user is authenticated
        if (!firebaseAuthService.isAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                error = "User not authenticated",
                isLoading = false
            )
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Get user profile from Firebase
                val result = firebaseAuthService.getUserProfile()

                if (result.isSuccess) {
                    val profileData = result.getOrNull()
                    if (profileData != null) {
                        val userProfile = parseUserProfile(profileData)
                        _uiState.value = _uiState.value.copy(
                            userProfile = userProfile,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Profile data is empty"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load profile"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                firebaseAuthService.logout()
                _uiState.value = ProfileUiState() // Reset state
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to logout: ${e.message}"
                )
            }
        }
    }

    private fun parseUserProfile(data: Map<String, Any>): UserProfile {
        return UserProfile(
            userId = data["userId"] as? String ?: "",
            username = data["username"] as? String ?: "Anonymous",
            displayName = data["displayName"] as? String ?: "Adventurer",
            email = data["email"] as? String,
            chaosEntries = (data["chaosEntries"] as? Long)?.toInt() ?: 0,
            dayStreak = (data["dayStreak"] as? Long)?.toInt() ?: 0,
            supportGiven = (data["supportGiven"] as? Long)?.toInt() ?: 0,
            joinDate = data["joinDate"] as? String ?: "",
            authType = data["authType"] as? String ?: "username"
        )
    }
}



