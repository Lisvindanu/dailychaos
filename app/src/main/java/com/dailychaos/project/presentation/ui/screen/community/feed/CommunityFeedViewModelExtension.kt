// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/feed/CommunityFeedViewModelExtension.kt
package com.dailychaos.project.presentation.ui.screen.community.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.repository.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Extension state dan functions untuk pagination
 * Bisa digunakan alongside existing ViewModel tanpa mengubah struktur yang ada
 */

data class FeedFilterState(
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val chaosLevelRange: IntRange? = null,
    val selectedTags: Set<String> = emptySet(),
    val sortBy: String = "createdAt_desc",
    val searchQuery: String = ""
) {
    fun getActiveFilterCount(): Int {
        var count = 0
        if (timeFilter != TimeFilter.ALL) count++
        if (chaosLevelRange != null) count++
        if (selectedTags.isNotEmpty()) count++
        if (sortBy != "createdAt_desc") count++
        return count
    }
}

data class PaginationState(
    val currentPage: Int = 1,
    val pageSize: Int = 15,
    val hasNextPage: Boolean = true,
    val isLoadingMore: Boolean = false,
    val totalCount: Int = 0
)

data class FilterMetadataState(
    val popularTags: List<String> = emptyList(),
    val chaosLevelRange: IntRange = 1..10,
    val isLoading: Boolean = false
)

/**
 * Extension untuk existing ViewModel - bisa di-compose atau di-inherit
 */
class CommunityFeedPaginationExtension(
    private val paginationRepository: CommunityRepositoryPagination,
    private val baseViewModel: CommunityFeedViewModel // Reference ke existing ViewModel
) {

    private val _filterState = MutableStateFlow(FeedFilterState())
    val filterState: StateFlow<FeedFilterState> = _filterState.asStateFlow()

    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    private val _metadataState = MutableStateFlow(FilterMetadataState())
    val metadataState: StateFlow<FilterMetadataState> = _metadataState.asStateFlow()

    private val _isFilterVisible = MutableStateFlow(false)
    val isFilterVisible: StateFlow<Boolean> = _isFilterVisible.asStateFlow()

    // Search debounce
    private val searchFlow = MutableStateFlow("")

    init {
        setupSearchDebounce()
        loadFilterMetadata()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        baseViewModel.viewModelScope.launch {
            searchFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    _filterState.value = _filterState.value.copy(searchQuery = query)
                    if (query.isNotBlank()) {
                        searchPosts(query)
                    } else {
                        loadFilteredPosts(resetPage = true)
                    }
                }
        }
    }

    // Public methods for UI
    fun toggleFilter() {
        _isFilterVisible.value = !_isFilterVisible.value
    }

    fun updateTimeFilter(timeFilter: TimeFilter) {
        _filterState.value = _filterState.value.copy(timeFilter = timeFilter)
        loadFilteredPosts(resetPage = true)
    }

    fun updateChaosLevelFilter(range: IntRange?) {
        _filterState.value = _filterState.value.copy(chaosLevelRange = range)
        loadFilteredPosts(resetPage = true)
    }

    fun toggleTag(tag: String) {
        val currentTags = _filterState.value.selectedTags.toMutableSet()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _filterState.value = _filterState.value.copy(selectedTags = currentTags)
        loadFilteredPosts(resetPage = true)
    }

    fun updateSortBy(sortBy: String) {
        _filterState.value = _filterState.value.copy(sortBy = sortBy)
        loadFilteredPosts(resetPage = true)
    }

    fun updateSearchQuery(query: String) {
        searchFlow.value = query
    }

    fun clearAllFilters() {
        _filterState.value = FeedFilterState()
        searchFlow.value = ""
        loadFilteredPosts(resetPage = true)
    }

    fun loadMore() {
        val pagination = _paginationState.value
        if (pagination.hasNextPage && !pagination.isLoadingMore) {
            loadFilteredPosts(page = pagination.currentPage + 1, append = true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() {
        loadFilteredPosts(resetPage = true)
    }

    private fun loadFilteredPosts(
        page: Int = 1,
        append: Boolean = false,
        resetPage: Boolean = false
    ) {
        baseViewModel.viewModelScope.launch {
            try {
                val currentPage = if (resetPage) 1 else page
                val filter = _filterState.value

                if (!append) {
                    _paginationState.value = _paginationState.value.copy(
                        isLoadingMore = currentPage > 1,
                        currentPage = currentPage
                    )
                } else {
                    _paginationState.value = _paginationState.value.copy(isLoadingMore = true)
                }

                val result = paginationRepository.getPaginatedPosts(
                    page = currentPage,
                    pageSize = _paginationState.value.pageSize,
                    timeRange = filter.timeFilter,
                    chaosLevelRange = filter.chaosLevelRange,
                    tags = filter.selectedTags.takeIf { it.isNotEmpty() }?.toList(),
                    sortBy = filter.sortBy
                )

                result.fold(
                    onSuccess = { response ->
                        // ✅ FIXED: Update base ViewModel's posts properly
                        baseViewModel.updatePosts(response.data, append)

                        Timber.d("📄 Loaded ${response.data.size} ${if (append) "more " else ""}posts (page $currentPage)")

                        _paginationState.value = _paginationState.value.copy(
                            currentPage = response.page,
                            hasNextPage = response.hasNext,
                            totalCount = response.totalCount,
                            isLoadingMore = false
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "❌ Error loading filtered posts")
                        _paginationState.value = _paginationState.value.copy(isLoadingMore = false)
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "❌ Error in loadFilteredPosts")
                _paginationState.value = _paginationState.value.copy(isLoadingMore = false)
            }
        }
    }

    private fun searchPosts(query: String) {
        baseViewModel.viewModelScope.launch {
            try {
                _paginationState.value = _paginationState.value.copy(currentPage = 1)

                val result = paginationRepository.searchPaginatedPosts(
                    query = query,
                    page = 1,
                    pageSize = _paginationState.value.pageSize
                )

                result.fold(
                    onSuccess = { response ->
                        // ✅ FIXED: Update base ViewModel's posts for search results
                        baseViewModel.updatePosts(response.data, append = false)

                        Timber.d("🔍 Found ${response.data.size} posts for query: '$query'")

                        _paginationState.value = _paginationState.value.copy(
                            currentPage = response.page,
                            hasNextPage = response.hasNext,
                            totalCount = response.totalCount,
                            isLoadingMore = false
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "❌ Error searching posts")
                        _paginationState.value = _paginationState.value.copy(isLoadingMore = false)
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "❌ Error in searchPosts")
                _paginationState.value = _paginationState.value.copy(isLoadingMore = false)
            }
        }
    }

    private fun loadFilterMetadata() {
        baseViewModel.viewModelScope.launch {
            try {
                _metadataState.value = _metadataState.value.copy(isLoading = true)

                val result = paginationRepository.getFilterMetadata()

                result.fold(
                    onSuccess = { metadata ->
                        _metadataState.value = FilterMetadataState(
                            popularTags = metadata.popularTags,
                            chaosLevelRange = metadata.chaosLevelRange,
                            isLoading = false
                        )
                        Timber.d("📋 Loaded filter metadata: ${metadata.popularTags.size} tags")
                    },
                    onFailure = { error ->
                        Timber.e(error, "❌ Error loading filter metadata")
                        _metadataState.value = _metadataState.value.copy(isLoading = false)
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "❌ Error in loadFilterMetadata")
                _metadataState.value = _metadataState.value.copy(isLoading = false)
            }
        }
    }
}

/**
 * Events untuk filter functionality
 */
sealed class FilterEvent {
    object ToggleFilter : FilterEvent()
    data class UpdateTimeFilter(val timeFilter: TimeFilter) : FilterEvent()
    data class UpdateChaosLevel(val range: IntRange?) : FilterEvent()
    data class ToggleTag(val tag: String) : FilterEvent()
    data class UpdateSort(val sortBy: String) : FilterEvent()
    data class UpdateSearch(val query: String) : FilterEvent()
    object ClearFilters : FilterEvent()
    object LoadMore : FilterEvent()
    object Refresh : FilterEvent()
}