// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/profile/ProfileViewModel.kt
package com.dailychaos.project.presentation.ui.screen.auth.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.UserProfile
// Import UserPreferences
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// Import 'first()'
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    // 1. Inject UserPreferences
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Cukup panggil loadUserProfile, pengecekan auth ada di dalamnya
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 2. Ambil UID dari UserPreferences sebagai sumber kebenaran
                val userId = userPreferences.userId.first()

                if (userId.isNullOrBlank()) {
                    // Jika tidak ada UID di preferences, berarti user belum login dengan benar
                    throw Exception("User ID tidak ditemukan. Silakan login kembali.")
                }

                // 3. Panggil service dengan UID yang sudah pasti benar
                val result = firebaseAuthService.getUserProfile(userId)

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
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Data profil kosong.")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Gagal memuat profil."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Terjadi kesalahan."
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

    /**
     * Parse user profile data from Firebase with safe type casting
     * Fix: Handle different data types that may come from Firebase
     */
    private fun parseUserProfile(data: Map<String, Any>): UserProfile {
        return UserProfile(
            userId = data["userId"] as? String ?: "",
            username = data["username"] as? String,
            displayName = data["displayName"] as? String ?: "Adventurer",
            email = data["email"] as? String,
            chaosEntries = when (val entries = data["chaosEntries"]) {
                is Long -> entries.toInt()
                is Int -> entries
                is Double -> entries.toInt()
                is String -> entries.toIntOrNull() ?: 0
                else -> 0
            },
            dayStreak = when (val streak = data["dayStreak"]) {
                is Long -> streak.toInt()
                is Int -> streak
                is Double -> streak.toInt()
                is String -> streak.toIntOrNull() ?: 0
                else -> 0
            },
            supportGiven = when (val support = data["supportGiven"]) {
                is Long -> support.toInt()
                is Int -> support
                is Double -> support.toInt()
                is String -> support.toIntOrNull() ?: 0
                else -> 0
            },
            joinDate = data["joinDate"] as? String ?: "",
            authType = data["authType"] as? String ?: "username",
            profilePicture = data["profilePicture"] as? String,
            bio = data["bio"] as? String ?: "",
            chaosLevel = when (val level = data["chaosLevel"]) {
                is Long -> level.toInt()
                is Int -> level
                is Double -> level.toInt()
                is String -> level.toIntOrNull() ?: 1
                else -> 1
            },
            partyRole = data["partyRole"] as? String ?: "Newbie Adventurer",
            isActive = data["isActive"] as? Boolean ?: true,
            lastLoginDate = data["lastLoginDate"] as? String,
            achievements = (data["achievements"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )
    }
}