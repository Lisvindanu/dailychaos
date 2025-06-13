package com.dailychaos.project.presentation.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.util.KonoSubaQuotes
import com.dailychaos.project.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Home ViewModel
 *
 * "ViewModel untuk Home Screen - seperti guild master yang manage semua quest!"
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Inject use cases when implemented
    // private val getCurrentUserUseCase: GetCurrentUserUseCase,
    // private val getChaosEntriesUseCase: GetChaosEntriesUseCase,
    // private val getUserStatsUseCase: GetUserStatsUseCase,
    // private val getCommunityHighlightsUseCase: GetCommunityHighlightsUseCase,
    // private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refreshData()
            is HomeUiEvent.LoadMore -> loadMoreEntries()
            is HomeUiEvent.RetryLoadingEntries -> loadRecentEntries()
            is HomeUiEvent.RetryLoadingStats -> loadTodayStats()
            is HomeUiEvent.ClearError -> clearError()
            is HomeUiEvent.NavigateToEntry -> {
                // TODO: Handle navigation to entry detail
            }
            is HomeUiEvent.NavigateToCreateChaos -> {
                // TODO: Handle navigation to create chaos
            }
            is HomeUiEvent.NavigateToHistory -> {
                // TODO: Handle navigation to history
            }
            is HomeUiEvent.NavigateToCommunity -> {
                // TODO: Handle navigation to community
            }
            is HomeUiEvent.NavigateToProfile -> {
                // TODO: Handle navigation to profile
            }
            is HomeUiEvent.ShareEntry -> shareEntry(event.entryId)
            is HomeUiEvent.ToggleFavoriteEntry -> toggleFavoriteEntry(event.entryId)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load daily quote first (instant)
            loadDailyQuote()

            // Load all data concurrently
            launch { loadUserData() }
            launch { loadTodayStats() }
            launch { loadRecentEntries() }
            launch { loadAchievements() }
            launch { loadCommunityHighlights() }
        }
    }

    private fun refreshData() {
        _uiState.update { it.copy(isRefreshing = true, generalError = null) }

        viewModelScope.launch {
            try {
                // Refresh all data
                launch { loadUserData() }
                launch { loadTodayStats() }
                launch { loadRecentEntries() }
                launch { loadAchievements() }
                launch { loadCommunityHighlights() }

                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        lastRefreshTime = Clock.System.now().toEpochMilliseconds()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        generalError = e.message ?: "Failed to refresh data"
                    )
                }
            }
        }
    }

    private fun loadDailyQuote() {
        val dailyQuote = KonoSubaQuotes.getDailyInspiration()
        _uiState.update { it.copy(dailyQuote = dailyQuote) }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUserLoading = true) }

            try {
                // TODO: Replace with actual use case
                val mockUser = getMockUser()
                _uiState.update {
                    it.copy(
                        user = mockUser,
                        isUserLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUserLoading = false,
                        generalError = "Failed to load user data"
                    )
                }
            }
        }
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isStatsLoading = true) }

            try {
                // TODO: Replace with actual use case
                val mockStats = getMockTodayStats()
                _uiState.update {
                    it.copy(
                        todayStats = mockStats,
                        isStatsLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStatsLoading = false,
                        generalError = "Failed to load today's stats"
                    )
                }
            }
        }
    }

    private fun loadRecentEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEntriesLoading = true, entriesError = null) }

            try {
                // TODO: Replace with actual use case
                val mockEntries = getMockRecentEntries()
                _uiState.update {
                    it.copy(
                        recentEntries = mockEntries,
                        isEntriesLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isEntriesLoading = false,
                        entriesError = "Failed to load recent entries"
                    )
                }
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAchievementsLoading = true) }

            try {
                // TODO: Replace with actual use case
                val mockAchievements = getMockAchievements()
                val mockStreak = 7

                _uiState.update {
                    it.copy(
                        achievements = mockAchievements,
                        currentStreak = mockStreak,
                        isAchievementsLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAchievementsLoading = false,
                        generalError = "Failed to load achievements"
                    )
                }
            }
        }
    }

    private fun loadCommunityHighlights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommunityLoading = true) }

            try {
                // TODO: Replace with actual use case
                val mockHighlight = getMockCommunityHighlight()
                _uiState.update {
                    it.copy(
                        communityHighlight = mockHighlight,
                        isCommunityLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCommunityLoading = false,
                        generalError = "Failed to load community highlights"
                    )
                }
            }
        }
    }

    private fun loadMoreEntries() {
        // TODO: Implement pagination for entries
        viewModelScope.launch {
            // Load more entries logic
        }
    }

    private fun shareEntry(entryId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement share functionality
                // shareEntryUseCase(entryId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(generalError = "Failed to share entry")
                }
            }
        }
    }

    private fun toggleFavoriteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement favorite toggle
                // toggleFavoriteUseCase(entryId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(generalError = "Failed to update favorite")
                }
            }
        }
    }

    private fun clearError() {
        _uiState.update {
            it.copy(
                generalError = null,
                entriesError = null
            )
        }
    }

    // Mock data functions - TODO: Remove when real use cases are implemented
    private fun getMockUser(): User {
        return User(
            id = "mock_user_1",
            anonymousUsername = "Kazuma2025",
            chaosEntriesCount = 15,
            supportGivenCount = 23,
            supportReceivedCount = 18,
            streakDays = 7
        )
    }

    private fun getMockTodayStats(): TodayStats {
        return TodayStats(
            entriesCount = 2,
            miniWinsCount = 5,
            supportGivenCount = 3,
            supportReceivedCount = 1,
            averageChaosLevel = 6.5f,
            completedGoals = 3,
            totalGoals = 5
        )
    }

    private fun getMockRecentEntries(): List<ChaosEntry> {
        // Return mock chaos entries
        return listOf(
            // TODO: Create mock entries or use existing sample data
        )
    }

    private fun getMockAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "streak_7",
                title = "Week Warrior",
                description = "7-day chaos recording streak!",
                emoji = "🔥",
                isUnlocked = true,
                type = AchievementType.STREAK,
                unlockedAt = Clock.System.now().toEpochMilliseconds()
            ),
            Achievement(
                id = "entries_10",
                title = "Chaos Chronicle",
                description = "Record 10 chaos entries",
                emoji = "📚",
                progress = 15,
                maxProgress = 10,
                isUnlocked = true,
                type = AchievementType.ENTRIES
            ),
            Achievement(
                id = "support_20",
                title = "Community Helper",
                description = "Give 20 support reactions",
                emoji = "💙",
                progress = 23,
                maxProgress = 20,
                isUnlocked = true,
                type = AchievementType.SUPPORT
            )
        )
    }

    private fun getMockCommunityHighlight(): CommunityHighlight {
        return CommunityHighlight(
            id = "highlight_1",
            title = "Aqua Moment",
            content = "Today felt like carrying Aqua through a dungeon again... anyone else?",
            authorName = "Anonymous Kazuma",
            supportCount = 12,
            chaosLevel = 7,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            tags = listOf("work", "team", "frustration")
        )
    }
}