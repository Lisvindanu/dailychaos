// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/profile/ProfileUiState.kt
package com.dailychaos.project.presentation.ui.screen.auth.profile

import com.dailychaos.project.domain.model.UserProfile

/**
 * Profile Screen UI State
 *
 * "State management untuk profile adventurer"
 */
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Profile Screen Events
 */
sealed class ProfileEvent {
    data object Logout : ProfileEvent()
    data object EditProfile : ProfileEvent()
    data object GoToSettings : ProfileEvent()
    data object Retry : ProfileEvent()
}