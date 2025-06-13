package com.dailychaos.project.presentation.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.util.KonoSubaQuotes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Home ViewModel - Clean Architecture
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
                // Navigation handled by UI
            }
            is HomeUiEvent.NavigateToCreateChaos -> {
                // Navigation handled by UI
            }
            is HomeUiEvent.NavigateToHistory -> {
                // Navigation handled by UI
            }
            is HomeUiEvent.NavigateToCommunity -> {
                // Navigation handled by UI
            }
            is HomeUiEvent.NavigateToProfile -> {
                // Navigation handled by UI
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
                // val user = getCurrentUserUseCase()

                // Temporary mock data for development
                val mockUser = createMockUser()

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
                // val stats = getUserStatsUseCase.getTodayStats()

                // Temporary mock data for development
                val mockStats = createMockTodayStats()

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
                // val entries = getChaosEntriesUseCase.getRecent(limit = 5)

                // Temporary mock data for development
                val mockEntries = createMockRecentEntries()

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
                // val achievements = getAchievementsUseCase()
                // val streak = getUserStatsUseCase.getCurrentStreak()

                // Temporary mock data for development
                val mockAchievements = createMockAchievements()
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
                // val highlight = getCommunityHighlightsUseCase()

                // Temporary mock data for development
                val mockHighlight = createMockCommunityHighlight()

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

    // Temporary mock data functions - TODO: Remove when real use cases are implemented
    private fun createMockUser(): com.dailychaos.project.domain.model.User {
        return com.dailychaos.project.domain.model.User(
            id = "mock_user_1",
            anonymousUsername = "Kazuma2025",
            chaosEntriesCount = 15,
            supportGivenCount = 23,
            supportReceivedCount = 18,
            streakDays = 7
        )
    }

    private fun createMockTodayStats(): TodayStats {
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

    private fun createMockRecentEntries(): List<com.dailychaos.project.domain.model.ChaosEntry> {
        return emptyList() // Return empty for now, will be populated when data layer is implemented
    }

    private fun createMockAchievements(): List<Achievement> {
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

    private fun createMockCommunityHighlight(): CommunityHighlight {
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