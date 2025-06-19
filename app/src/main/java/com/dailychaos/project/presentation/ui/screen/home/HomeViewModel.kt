// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/home/HomeViewModel.kt
package com.dailychaos.project.presentation.ui.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import com.dailychaos.project.util.KonoSubaQuotes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

/**
 * Home ViewModel - Enhanced with Real Data Loading
 *
 * "ViewModel untuk Home Screen - dengan loading data chaos entries yang sesungguhnya!"
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chaosRepository: ChaosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Quote rotation system
    private var quoteRotationIndex = 0
    private val availableQuotes = mutableListOf<KonoSubaQuotes.Quote>()

    init {
        loadInitialData()
        setupQuoteRotation()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refreshData()
            is HomeUiEvent.RefreshQuote -> refreshQuote()
            is HomeUiEvent.NextQuote -> showNextQuote()
            is HomeUiEvent.NavigateToEntry -> {
                Timber.d("Navigate to entry: ${event.entryId}")
            }
            is HomeUiEvent.NavigateToCreateChaos -> {
                Timber.d("Navigate to create chaos")
            }
            is HomeUiEvent.NavigateToHistory -> {
                Timber.d("Navigate to history")
            }
            is HomeUiEvent.NavigateToCommunity -> {
                Timber.d("Navigate to community")
            }
            is HomeUiEvent.RetryLoadingEntries -> {
                val userId = uiState.value.user?.id
                if (userId != null) {
                    loadRecentEntries(userId)
                } else {
                    Timber.e("Cannot retry loading entries - no user ID available")
                }
            }
            is HomeUiEvent.ClearError -> {
                _uiState.update {
                    it.copy(generalError = null, entriesError = null)
                }
            }
            is HomeUiEvent.ClearEntriesError -> {
                _uiState.update { it.copy(entriesError = null) }
            }
            is HomeUiEvent.PreviousQuote -> showPreviousQuote()
            is HomeUiEvent.IncrementMiniWins -> incrementMiniWins()
            is HomeUiEvent.IncrementTodayEntries -> incrementTodayEntries()
            is HomeUiEvent.RefreshStats -> refreshStats()
            is HomeUiEvent.RetryLoadingAchievements -> retryLoadingAchievements()
            is HomeUiEvent.RetryLoadingUser -> loadCurrentUser()
            is HomeUiEvent.SelectQuoteByCharacter -> selectQuoteByCharacter(event.character)
            is HomeUiEvent.SelectQuoteByContext -> selectQuoteByContext(event.context)
            is HomeUiEvent.UnlockAchievement -> unlockAchievement(event.achievementId)
        }
    }

    private fun loadInitialData() {
        Timber.d("🏠 ==================== LOADING INITIAL HOME DATA ====================")
        loadCurrentUser()
        setupQuoteSystem()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                Timber.d("👤 Loading current user...")
                _uiState.update { it.copy(isUserLoading = true) }

                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    Timber.d("✅ Current user loaded: ${currentUser.id}")
                    _uiState.update {
                        it.copy(
                            user = currentUser,
                            isUserLoading = false,
                            generalError = null
                        )
                    }

                    // Load additional data based on user
                    loadRecentEntries(currentUser.id)
                    loadUserStats(currentUser)
                    loadAchievements(currentUser)
                } else {
                    Timber.e("❌ No current user found")
                    _uiState.update {
                        it.copy(
                            isUserLoading = false,
                            generalError = "User not authenticated"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "💥 Error loading current user")
                _uiState.update {
                    it.copy(
                        isUserLoading = false,
                        generalError = "Failed to load user: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadRecentEntries(userId: String) {
        viewModelScope.launch {
            try {
                Timber.d("📚 ==================== LOADING RECENT CHAOS ENTRIES ====================")
                Timber.d("📚 Loading recent entries for user: $userId")

                _uiState.update { it.copy(isEntriesLoading = true, entriesError = null) }

                chaosRepository.getRecentChaosEntries(userId, limit = 5)
                    .catch { exception ->
                        Timber.e(exception, "❌ Error loading recent entries")
                        _uiState.update {
                            it.copy(
                                isEntriesLoading = false,
                                entriesError = "Failed to load recent entries: ${exception.message}"
                            )
                        }
                    }
                    .collect { entries ->
                        Timber.d("📚 Received ${entries.size} recent entries")
                        entries.forEach { entry ->
                            Timber.d("  - Entry: ${entry.id} | ${entry.title}")
                        }

                        _uiState.update {
                            it.copy(
                                recentEntries = entries,
                                isEntriesLoading = false,
                                entriesError = null
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading recent entries")
                _uiState.update {
                    it.copy(
                        isEntriesLoading = false,
                        entriesError = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadUserStats(user: User) {
        viewModelScope.launch {
            try {
                Timber.d("📊 Loading user stats for: ${user.id}")
                _uiState.update { it.copy(isStatsLoading = true) }

                // For now, create stats based on user data
                // TODO: Implement proper stats calculation from chaos entries
                val todayStats = TodayStats(
                    entriesCount = 0, // Will be calculated from recent entries
                    miniWinsCount = 0,
                    supportGivenCount = user.supportGivenCount,
                    supportReceivedCount = user.supportReceivedCount,
                    averageChaosLevel = 5.0f,
                    completedGoals = 0,
                    totalGoals = 0
                )

                _uiState.update {
                    it.copy(
                        todayStats = todayStats,
                        currentStreak = user.streakDays,
                        isStatsLoading = false
                    )
                }

                Timber.d("✅ User stats loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading user stats")
                _uiState.update { it.copy(isStatsLoading = false) }
            }
        }
    }

    private fun loadAchievements(user: User) {
        viewModelScope.launch {
            try {
                Timber.d("🏆 Loading achievements for user: ${user.id}")
                _uiState.update { it.copy(isAchievementsLoading = true) }

                val achievements = mutableListOf<Achievement>()

                // First chaos entry achievement
                if (user.chaosEntriesCount > 0) {
                    achievements.add(
                        Achievement(
                            id = "first_chaos",
                            title = "First Chaos",
                            description = "Berhasil nulis chaos pertama!",
                            emoji = "🌟",
                            isUnlocked = true,
                            type = AchievementType.ENTRIES
                        )
                    )
                }

                // Streak achievements
                if (user.streakDays >= 3) {
                    achievements.add(
                        Achievement(
                            id = "streak_3",
                            title = "Consistency King",
                            description = "3 hari berturut-turut mantap!",
                            emoji = "⚡",
                            isUnlocked = true,
                            type = AchievementType.STREAK
                        )
                    )
                }

                if (user.streakDays >= 7) {
                    achievements.add(
                        Achievement(
                            id = "streak_7",
                            title = "Week Warrior",
                            description = "7 hari berturut-turut nulis chaos!",
                            emoji = "🔥",
                            isUnlocked = true,
                            type = AchievementType.STREAK
                        )
                    )
                }

                // Support achievements
                if (user.supportGivenCount >= 5) {
                    achievements.add(
                        Achievement(
                            id = "support_5",
                            title = "Caring Friend",
                            description = "Memberikan 5 dukungan kepada orang lain!",
                            emoji = "💙",
                            isUnlocked = true,
                            type = AchievementType.SUPPORT
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        achievements = achievements,
                        isAchievementsLoading = false
                    )
                }

                Timber.d("✅ Loaded ${achievements.size} achievements")
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading achievements")
                _uiState.update { it.copy(isAchievementsLoading = false) }
            }
        }
    }

    private fun setupQuoteSystem() {
        Timber.d("💬 Setting up quote system")
        loadQuotePool()
        loadDailyQuote()
    }

    private fun loadQuotePool() {
        availableQuotes.clear()

        // Get diverse quotes using available methods
        val timeBasedQuote = KonoSubaQuotes.getTimeBasedQuote()
        val inspirationalQuote = KonoSubaQuotes.getDailyInspiration()
        val multipleQuotes = KonoSubaQuotes.getMultipleQuotes(4)

        // Add to pool avoiding duplicates
        availableQuotes.add(timeBasedQuote)

        multipleQuotes.forEach { quote ->
            if (!availableQuotes.any { it.text == quote.text }) {
                availableQuotes.add(quote)
            }
        }

        if (!availableQuotes.any { it.text == inspirationalQuote.text }) {
            availableQuotes.add(inspirationalQuote)
        }

        // Add context-specific quotes
        val supportQuote = KonoSubaQuotes.getQuoteByContext(KonoSubaQuotes.QuoteContext.SUPPORT)
        val communityQuote = KonoSubaQuotes.getCommunityQuote()

        if (!availableQuotes.any { it.text == supportQuote.text }) {
            availableQuotes.add(supportQuote)
        }
        if (!availableQuotes.any { it.text == communityQuote.text }) {
            availableQuotes.add(communityQuote)
        }

        // Shuffle for variety
        availableQuotes.shuffle()
        quoteRotationIndex = 0
    }

    private fun loadDailyQuote() {
        val currentQuote = if (availableQuotes.isNotEmpty()) {
            availableQuotes[quoteRotationIndex]
        } else {
            KonoSubaQuotes.getDailyInspiration()
        }

        _uiState.update { it.copy(dailyQuote = currentQuote) }
    }

    private fun setupQuoteRotation() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // Rotate every 30 seconds
                showNextQuote()
            }
        }
    }

    private fun refreshData() {
        Timber.d("🔄 Refreshing home data")
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            try {
                loadCurrentUser()
                refreshQuote()

                delay(1000) // Minimum refresh time for UX

                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        lastRefreshTime = Clock.System.now().toEpochMilliseconds()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during refresh")
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        generalError = "Failed to refresh: ${e.message}"
                    )
                }
            }
        }
    }

    private fun refreshQuote() {
        Timber.d("💬 Refreshing quote")
        setupQuoteSystem()
    }

    private fun showNextQuote() {
        if (availableQuotes.isNotEmpty()) {
            quoteRotationIndex = (quoteRotationIndex + 1) % availableQuotes.size
            _uiState.update { it.copy(dailyQuote = availableQuotes[quoteRotationIndex]) }
            Timber.d("💬 Showing quote ${quoteRotationIndex + 1}/${availableQuotes.size}")
        }
    }

    private fun showPreviousQuote() {
        if (availableQuotes.isNotEmpty()) {
            quoteRotationIndex = if (quoteRotationIndex - 1 < 0) {
                availableQuotes.size - 1
            } else {
                quoteRotationIndex - 1
            }
            _uiState.update { it.copy(dailyQuote = availableQuotes[quoteRotationIndex]) }
            Timber.d("💬 Showing previous quote ${quoteRotationIndex + 1}/${availableQuotes.size}")
        }
    }

    // Additional event handlers
    private fun incrementMiniWins() {
        _uiState.update {
            it.copy(
                todayStats = it.todayStats.copy(
                    miniWinsCount = it.todayStats.miniWinsCount + 1
                )
            )
        }
    }

    private fun incrementTodayEntries() {
        _uiState.update {
            it.copy(
                todayStats = it.todayStats.copy(
                    entriesCount = it.todayStats.entriesCount + 1
                )
            )
        }
    }

    private fun refreshStats() {
        val user = uiState.value.user
        if (user != null) {
            loadUserStats(user)
        }
    }

    private fun retryLoadingAchievements() {
        val user = uiState.value.user
        if (user != null) {
            loadAchievements(user)
        }
    }

    private fun selectQuoteByCharacter(character: KonoSubaQuotes.Character) {
        val quote = KonoSubaQuotes.getQuoteByCharacter(character)
        _uiState.update { it.copy(dailyQuote = quote) }
    }

    private fun selectQuoteByContext(context: KonoSubaQuotes.QuoteContext) {
        val quote = KonoSubaQuotes.getQuoteByContext(context)
        _uiState.update { it.copy(dailyQuote = quote) }
    }

    private fun unlockAchievement(achievementId: String) {
        _uiState.update { currentState ->
            val updatedAchievements = currentState.achievements.map { achievement ->
                if (achievement.id == achievementId) {
                    achievement.copy(
                        isUnlocked = true,
                        unlockedAt = Clock.System.now().toEpochMilliseconds()
                    )
                } else {
                    achievement
                }
            }
            currentState.copy(achievements = updatedAchievements)
        }
    }

    // Method to manually trigger loading for testing
    fun triggerLoadEntries() {
        val userId = uiState.value.user?.id
        if (userId != null) {
            Timber.d("🧪 Manually triggering load entries for testing")
            loadRecentEntries(userId)
        } else {
            Timber.e("Cannot trigger load - no user ID available")
        }
    }

    // Clear errors
    fun clearErrors() {
        _uiState.update {
            it.copy(
                generalError = null,
                entriesError = null
            )
        }
    }
}