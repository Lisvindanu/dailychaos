// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/chaos/history/ChaosHistoryViewModel.kt
package com.dailychaos.project.presentation.ui.screen.chaos.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
import com.dailychaos.project.presentation.ui.component.ChaosFilter
import com.dailychaos.project.util.toFriendlyDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChaosHistoryViewModel @Inject constructor(
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChaosHistoryUiState())
    val uiState = _uiState.asStateFlow()

    // Store all entries fetched from the repository
    private var allEntries: List<ChaosEntry> = emptyList()

    init {
        loadChaosHistory()
    }

    fun onEvent(event: ChaosHistoryEvent) {
        when (event) {
            is ChaosHistoryEvent.Refresh -> loadChaosHistory()
            is ChaosHistoryEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterAndGroupEntries()
            }
            is ChaosHistoryEvent.FilterToggled -> {
                val currentFilters = _uiState.value.activeFilters.toMutableSet()
                if (event.filter in currentFilters) {
                    currentFilters.remove(event.filter)
                } else {
                    currentFilters.add(event.filter)
                }
                _uiState.update { it.copy(activeFilters = currentFilters) }
                filterAndGroupEntries()
            }
            is ChaosHistoryEvent.EntryClicked -> {
                // Navigation is handled in the UI
                Timber.d("Entry clicked: ${event.entryId}")
            }
            is ChaosHistoryEvent.Retry -> loadChaosHistory()
        }
    }

    private fun loadChaosHistory() {
        viewModelScope.launch {
            try {
                Timber.d("🔄 ==================== LOADING CHAOS HISTORY ====================")
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ No authenticated user found")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated. Please login again."
                        )
                    }
                    return@launch
                }

                Timber.d("✅ Current user found: ${currentUser.id}")

                // Collect chaos entries from repository
                chaosRepository.getAllChaosEntries(currentUser.id)
                    .catch { exception ->
                        Timber.e(exception, "❌ Error loading chaos entries from repository")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load chaos entries: ${exception.message}"
                            )
                        }
                    }
                    .collect { entries ->
                        Timber.d("📚 Received ${entries.size} chaos entries from repository")
                        allEntries = entries
                        filterAndGroupEntries()
                        _uiState.update { it.copy(isLoading = false, error = null) }
                    }

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error in loadChaosHistory")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun filterAndGroupEntries() {
        try {
            Timber.d("🔍 ==================== FILTERING AND GROUPING ENTRIES ====================")
            Timber.d("🔍 Total entries to filter: ${allEntries.size}")
            Timber.d("🔍 Search query: '${_uiState.value.searchQuery}'")
            Timber.d("🔍 Active filters: ${_uiState.value.activeFilters}")

            val filtered = allEntries.filter { entry ->
                val queryMatch = if (_uiState.value.searchQuery.isNotBlank()) {
                    entry.title.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                            entry.description.contains(_uiState.value.searchQuery, ignoreCase = true)
                } else true

                val filterMatch = if (_uiState.value.activeFilters.isNotEmpty()) {
                    _uiState.value.activeFilters.all { filter ->
                        when (filter) {
                            ChaosFilter.HIGH_CHAOS -> entry.chaosLevel > 7
                            ChaosFilter.LOW_CHAOS -> entry.chaosLevel < 4
                            ChaosFilter.WITH_WINS -> entry.miniWins.isNotEmpty()
                            ChaosFilter.SHARED -> entry.isSharedToCommunity
                            else -> true // Date filters would be handled differently
                        }
                    }
                } else true

                queryMatch && filterMatch
            }

            Timber.d("🔍 Filtered entries: ${filtered.size}")

            val grouped = filtered
                .sortedByDescending { it.createdAt }
                .groupBy {
                    try {
                        it.createdAt.toFriendlyDateString()
                    } catch (e: Exception) {
                        Timber.w("Failed to format date for entry ${it.id}: ${e.message}")
                        "Unknown Date"
                    }
                }

            Timber.d("🔍 Grouped by dates: ${grouped.keys}")
            grouped.forEach { (date, entries) ->
                Timber.d("  - $date: ${entries.size} entries")
            }

            _uiState.update { it.copy(entriesByDate = grouped) }
            Timber.d("✅ Filtering and grouping completed successfully")

        } catch (e: Exception) {
            Timber.e(e, "💥 Error in filterAndGroupEntries")
            _uiState.update {
                it.copy(
                    entriesByDate = emptyMap(),
                    error = "Error filtering entries: ${e.message}"
                )
            }
        }
    }

    // Keep this as fallback for testing/development
    private fun createMockHistory(): List<ChaosEntry> {
        Timber.d("🎭 Creating mock chaos history data")
        return List(10) {
            val now = Clock.System.now()
            ChaosEntry(
                id = "mock_entry_$it",
                userId = "mock_user",
                title = "Mock Chaos Day #${it + 1}",
                description = "This was a day of moderate chaos, inspired by Aqua's latest scheme. It involved a lot of running around and some minor property damage, but we survived and learned something new.",
                chaosLevel = Random.nextInt(1, 11),
                createdAt = now.minus(it.days),
                updatedAt = now.minus(it.days),
                miniWins = if (Random.nextBoolean()) listOf("Finished a task", "Ate cake", "Survived the day") else emptyList(),
                tags = listOf("daily", "adventure", "chaos"),
                isSharedToCommunity = Random.nextBoolean()
            )
        }
    }

    // Function to refresh data explicitly
    fun refreshData() {
        Timber.d("🔄 Manual refresh triggered")
        loadChaosHistory()
    }

    // Function to load mock data for testing
    fun loadMockData() {
        Timber.d("🎭 Loading mock data for testing")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(500) // Simulate loading
            allEntries = createMockHistory()
            filterAndGroupEntries()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}