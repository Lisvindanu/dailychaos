package com.dailychaos.project.presentation.ui.screen.chaos.history

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.presentation.ui.component.ChaosFilter

/**
 * UI State for Chaos History Screen
 */
data class ChaosHistoryUiState(
    val entriesByDate: Map<String, List<ChaosEntry>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val activeFilters: Set<ChaosFilter> = emptySet()
)

/**
 * UI Events for Chaos History Screen
 */
sealed class ChaosHistoryEvent {
    object Refresh : ChaosHistoryEvent()
    data class SearchQueryChanged(val query: String) : ChaosHistoryEvent()
    data class FilterToggled(val filter: ChaosFilter) : ChaosHistoryEvent()
    data class EntryClicked(val entryId: String) : ChaosHistoryEvent()
    object Retry : ChaosHistoryEvent()
}