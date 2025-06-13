package com.dailychaos.project.presentation.ui.screen.auth.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.presentation.ui.screen.home.Achievement
import com.dailychaos.project.presentation.ui.screen.home.AchievementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    // private val getCurrentUserUseCase: GetCurrentUserUseCase,
    // private val getAchievementsUseCase: GetAchievementsUseCase,
    // private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _logoutCompleteEvent = MutableSharedFlow<Unit>()
    val logoutCompleteEvent = _logoutCompleteEvent.asSharedFlow()

    init {
        loadProfileData()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Logout -> logout()
            is ProfileEvent.Retry -> loadProfileData()
            else -> { /* Navigation handled in UI */
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            kotlinx.coroutines.delay(1000) // Mock loading
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = createMockUser(),
                    achievements = createMockAchievements()
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(500) // Mock logout
            _logoutCompleteEvent.emit(Unit)
        }
    }

    private fun createMockUser(): User = User(
        id = "user_123",
        anonymousUsername = "Adventurer42",
        isAnonymous = false,
        email = "kazuma.satou@konosuba.world",
        chaosEntriesCount = 42,
        supportGivenCount = 120,
        supportReceivedCount = 85,
        streakDays = 14
    )

    private fun createMockAchievements(): List<Achievement> = listOf(
        Achievement("1", "First Chaos", "Record your first chaos entry", "🎉", isUnlocked = true, type = AchievementType.ENTRIES),
        Achievement("2", "Week Streak", "Maintain a 7-day streak", "🔥", isUnlocked = true, type = AchievementType.STREAK),
        Achievement("3", "Community Twin", "Find your first chaos twin", "🤝", isUnlocked = true, type = AchievementType.COMMUNITY),
        Achievement("4", "Giver", "Give 50 support reactions", "💙", isUnlocked = true, type = AchievementType.SUPPORT),
        Achievement("5", "Month Streak", "Maintain a 30-day streak", "🗓️", isUnlocked = false, progress = 14, maxProgress = 30, type = AchievementType.STREAK)
    )
}