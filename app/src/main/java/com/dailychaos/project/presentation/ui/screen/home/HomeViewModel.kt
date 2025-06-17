// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/home/HomeViewModel.kt
package com.dailychaos.project.presentation.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.util.KonoSubaQuotes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Home ViewModel - Enhanced with Dynamic Quote System
 *
 * "ViewModel untuk Home Screen - dengan quote system yang lebih engaging!"
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
    // TODO: Inject use case lain saat sudah diimplementasikan
    // private val getChaosEntriesUseCase: GetChaosEntriesUseCase,
    // private val getUserStatsUseCase: GetUserStatsUseCase,
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
            is HomeUiEvent.NavigateToEntry -> { /* Handle in UI */ }
            is HomeUiEvent.NavigateToCreateChaos -> { /* Handle in UI */ }
            is HomeUiEvent.NavigateToHistory -> { /* Handle in UI */ }
            is HomeUiEvent.NavigateToCommunity -> { /* Handle in UI */ }
            is HomeUiEvent.RetryLoadingEntries -> loadRecentEntries(uiState.value.user?.id ?: "")
            is HomeUiEvent.ClearError -> _uiState.update { it.copy(generalError = null, entriesError = null) }
            else -> { /* Event lain ditangani di UI atau belum diimplementasikan */ }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUserLoading = true, isEntriesLoading = true) }

            // Load initial quotes pool
            loadQuotePool()

            // Load daily quote (time-based + contextual)
            loadDailyQuote()

            // Ambil data user asli
            val userResult = authUseCases.getCurrentUser()
            if (userResult != null) {
                _uiState.update {
                    it.copy(
                        user = userResult,
                        currentStreak = userResult.streakDays,
                        isUserLoading = false
                    )
                }
                // Setelah mendapatkan user, kita bisa memuat data lain yang bergantung padanya
                loadRecentEntries(userResult.id)
                loadAchievements(userResult)
            } else {
                _uiState.update {
                    it.copy(
                        isUserLoading = false,
                        generalError = "Gagal memuat data petualang. Coba login ulang."
                    )
                }
            }
        }
    }

    private fun refreshData() {
        _uiState.update { it.copy(isRefreshing = true, generalError = null) }
        viewModelScope.launch {
            // Refresh quote pool with new selection
            loadQuotePool()
            loadDailyQuote()

            loadInitialData() // Cukup panggil ulang fungsi load data utama
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    lastRefreshTime = Clock.System.now().toEpochMilliseconds()
                )
            }
        }
    }

    private fun setupQuoteRotation() {
        viewModelScope.launch {
            while (true) {
                delay(25000) // Rotate every 25 seconds
                if (availableQuotes.size > 1) {
                    showNextQuote()
                }
            }
        }
    }

    private fun loadQuotePool() {
        // Create a diverse pool of quotes
        val timeBasedQuote = KonoSubaQuotes.getTimeBasedQuote()
        val inspirationalQuote = KonoSubaQuotes.getDailyInspiration()
        val randomQuotes = KonoSubaQuotes.getMultipleQuotes(4)

        // Create pool with variety, avoiding duplicates
        availableQuotes.clear()
        availableQuotes.add(timeBasedQuote)

        randomQuotes.forEach { quote ->
            if (!availableQuotes.any { it.text == quote.text }) {
                availableQuotes.add(quote)
            }
        }

        if (!availableQuotes.any { it.text == inspirationalQuote.text }) {
            availableQuotes.add(inspirationalQuote)
        }

        // Add some context-specific quotes
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

    private fun refreshQuote() {
        viewModelScope.launch {
            // Load completely new quote
            val freshQuote = KonoSubaQuotes.getRandomQuote()
            _uiState.update { it.copy(dailyQuote = freshQuote) }
        }
    }

    private fun showNextQuote() {
        if (availableQuotes.isNotEmpty()) {
            quoteRotationIndex = (quoteRotationIndex + 1) % availableQuotes.size
            val nextQuote = availableQuotes[quoteRotationIndex]
            _uiState.update { it.copy(dailyQuote = nextQuote) }
        }
    }

    // FUNGSI INI PERLU IMPLEMENTASI LEBIH LANJUT DI MASA DEPAN
    private fun loadRecentEntries(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEntriesLoading = true, entriesError = null) }
            // TODO: Ganti dengan pemanggilan use case asli saat sudah siap
            // val entries = getChaosEntriesUseCase.getRecent(userId, limit = 5)

            // Untuk sekarang, kita kembalikan daftar kosong agar konsisten
            val mockEntries = emptyList<com.dailychaos.project.domain.model.ChaosEntry>()

            _uiState.update {
                it.copy(
                    recentEntries = mockEntries,
                    isEntriesLoading = false
                )
            }
        }
    }

    // FUNGSI INI PERLU IMPLEMENTASI LEBIH LANJUT DI MASA DEPAN
    private fun loadAchievements(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAchievementsLoading = true) }
            // TODO: Ganti dengan pemanggilan use case asli saat sudah siap
            // val achievements = getAchievementsUseCase(user.id)

            // Untuk sekarang, kita buat logika sederhana berdasarkan data user
            val achievements = mutableListOf<Achievement>()
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

            _uiState.update {
                it.copy(
                    achievements = achievements,
                    isAchievementsLoading = false
                )
            }
        }
    }
}