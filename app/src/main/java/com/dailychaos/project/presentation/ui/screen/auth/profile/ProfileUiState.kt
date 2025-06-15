package com.dailychaos.project.presentation.ui.screen.auth.profile

import UserProfile
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.presentation.ui.screen.home.Achievement

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProfileEvent {
    object Logout : ProfileEvent()
    object EditProfile : ProfileEvent()
    object GoToSettings : ProfileEvent()
    object Retry : ProfileEvent()
}