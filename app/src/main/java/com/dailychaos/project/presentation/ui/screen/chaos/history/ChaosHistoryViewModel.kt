package com.dailychaos.project.presentation.ui.screen.chaos.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.presentation.ui.component.ChaosFilter
import com.dailychaos.project.util.toFriendlyDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@HiltViewModel
class ChaosHistoryViewModel @Inject constructor(
    // private val getChaosEntriesUseCase: GetChaosEntriesUseCase
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
            }
            is ChaosHistoryEvent.Retry -> loadChaosHistory()
        }
    }

    private fun loadChaosHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Mock Data
            kotlinx.coroutines.delay(1000)
            allEntries = createMockHistory()
            filterAndGroupEntries()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun filterAndGroupEntries() {
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

        val grouped = filtered
            .sortedByDescending { it.createdAt }
            .groupBy { it.createdAt.toFriendlyDateString() }

        _uiState.update { it.copy(entriesByDate = grouped) }
    }

    private fun createMockHistory(): List<ChaosEntry> {
        return List(20) {
            val now = Clock.System.now()
            ChaosEntry(
                id = "entry_$it",
                title = "Chaos Day #$it",
                description = "This was a day of moderate chaos, inspired by Aqua's latest scheme. It involved a lot of running around and some minor property damage, but we survived.",
                chaosLevel = Random.nextInt(1, 11),
                createdAt = now.minus(it.days),
                miniWins = if (Random.nextBoolean()) listOf("Finished a task", "Ate cake") else emptyList(),
                isSharedToCommunity = Random.nextBoolean()
            )
        }
    }
}