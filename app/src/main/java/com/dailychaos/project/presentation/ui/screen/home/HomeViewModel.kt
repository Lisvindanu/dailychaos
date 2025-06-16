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
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Home ViewModel - Clean Architecture
 *
 * "ViewModel untuk Home Screen - sekarang terhubung dengan data asli, bukan mock!"
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

    init {
        loadInitialData()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refreshData()
            // ... (event lainnya tetap sama) ...
            else -> { /* Event lain ditangani di UI atau belum diimplementasikan */ }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUserLoading = true, isEntriesLoading = true) }

            // Load quote (tidak berubah)
            loadDailyQuote()

            // Ambil data user asli
            val userResult = authUseCases.getCurrentUser()
            if (userResult != null) {
                _uiState.update {
                    it.copy(
                        user = userResult,
                        currentStreak = userResult.streakDays, // Gunakan data asli
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
            loadInitialData() // Cukup panggil ulang fungsi load data utama
            _uiState.update { it.copy(isRefreshing = false, lastRefreshTime = Clock.System.now().toEpochMilliseconds()) }
        }
    }

    private fun loadDailyQuote() {
        val dailyQuote = KonoSubaQuotes.getDailyInspiration()
        _uiState.update { it.copy(dailyQuote = dailyQuote) }
    }

    // FUNGSI INI SUDAH TIDAK DIPERLUKAN KARENA DATA USER DIAMBIL DI loadInitialData
    // private fun loadUserData() { ... }

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
                        description = "7-day chaos recording streak!",
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
                        description = "Record your first chaos entry!",
                        emoji = "🌟",
                        isUnlocked = true,
                        type = AchievementType.ENTRIES
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

    // HAPUS SEMUA FUNGSI MOCK DATA DI BAWAH INI:
    // private fun createMockUser() { ... }
    // private fun createMockTodayStats() { ... }
    // private fun createMockRecentEntries() { ... }
    // private fun createMockAchievements() { ... }
    // private fun createMockCommunityHighlight() { ... }
}