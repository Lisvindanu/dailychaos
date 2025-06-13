package com.dailychaos.project.presentation.ui.screen.auth.profile

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.presentation.ui.screen.home.Achievement

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val achievements: List<Achievement> = emptyList()
)

sealed class ProfileEvent {
    object Logout : ProfileEvent()
    object EditProfile : ProfileEvent()
    object GoToSettings : ProfileEvent()
    object Retry : ProfileEvent()
}