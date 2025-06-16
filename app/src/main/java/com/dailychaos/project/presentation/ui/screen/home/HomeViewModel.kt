// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/home/HomeViewModel.kt
package com.dailychaos.project.presentation.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.util.KonoSubaQuotes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
    // TODO: Inject use case lain saat sudah diimplementasikan
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUserLoading = true) }

            authUseCases.getAuthState().collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        val user = authState.user
                        _uiState.update {
                            it.copy(
                                user = user,
                                currentStreak = user.streakDays,
                                isUserLoading = false,
                                generalError = null
                            )
                        }
                        loadScreenData(user)
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.update {
                            it.copy(
                                isUserLoading = false,
                                generalError = "Sesi petualang tidak ditemukan. Coba login ulang."
                            )
                        }
                    }
                    is AuthState.Error -> {
                        _uiState.update {
                            it.copy(
                                isUserLoading = false,
                                generalError = authState.message
                            )
                        }
                    }
                    is AuthState.Loading -> {
                        _uiState.update { it.copy(isUserLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadScreenData(user: User) {
        val dailyQuote = KonoSubaQuotes.getDailyInspiration()
        _uiState.update { it.copy(dailyQuote = dailyQuote) }

        loadRecentEntries(user.id)
        loadAchievements(user)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refreshData()
            else -> {}
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, generalError = null) }
            val user = uiState.value.user
            if (user != null) {
                loadScreenData(user)
            } else {
                observeAuthState()
            }
            _uiState.update { it.copy(isRefreshing = false, lastRefreshTime = Clock.System.now().toEpochMilliseconds()) }
        }
    }

    private fun loadRecentEntries(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEntriesLoading = true, entriesError = null) }
            val mockEntries = emptyList<com.dailychaos.project.domain.model.ChaosEntry>()
            _uiState.update {
                it.copy(recentEntries = mockEntries, isEntriesLoading = false)
            }
        }
    }

    private fun loadAchievements(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAchievementsLoading = true) }

            val achievements = mutableListOf<Achievement>()

            // --- AWAL PERBAIKAN ---
            if (user.chaosEntriesCount > 0) {
                achievements.add(
                    Achievement(
                        id = "first_chaos",
                        title = "First Chaos!",
                        description = "Kamu telah mencatat kekacauan pertamamu!", // <-- DESKRIPSI DITAMBAHKAN
                        emoji = "🌟",
                        isUnlocked = true
                    )
                )
            }
            if (user.streakDays >= 7) {
                achievements.add(
                    Achievement(
                        id = "streak_7",
                        title = "Week Warrior",
                        description = "Berhasil mencatat chaos selama 7 hari berturut-turut!", // <-- DESKRIPSI DITAMBAHKAN
                        emoji = "🔥",
                        isUnlocked = true
                    )
                )
            }
            // --- AKHIR PERBAIKAN ---

            _uiState.update {
                it.copy(achievements = achievements, isAchievementsLoading = false)
            }
        }
    }
}