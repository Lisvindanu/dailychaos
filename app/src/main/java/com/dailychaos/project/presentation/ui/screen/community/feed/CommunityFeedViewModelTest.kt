// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/feed/CommunityFeedViewModelTest.kt
package com.dailychaos.project.presentation.ui.screen.community.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Test ViewModel untuk pagination dan filter
 * Bisa digunakan untuk testing tanpa mengubah existing ViewModel
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CommunityFeedViewModelTest @Inject constructor(
    private val paginationRepository: CommunityRepositoryPagination,
    private val baseRepository: CommunityRepositoryExtended,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Basic UI State
    private val _uiState = MutableStateFlow(CommunityFeedUiState())
    val uiState: StateFlow<CommunityFeedUiState> = _uiState.asStateFlow()

    // Filter dan Pagination State
    private val _filterState = MutableStateFlow(SimpleFilterState())
    val filterState: StateFlow<SimpleFilterState> = _filterState.asStateFlow()

    private val _paginationState = MutableStateFlow(SimplePaginationState())
    val paginationState: StateFlow<SimplePaginationState> = _paginationState.asStateFlow()

    private val _metadataState = MutableStateFlow(SimpleMetadataState())
    val metadataState: StateFlow<SimpleMetadataState> = _metadataState.asStateFlow()

    private val _isFilterVisible = MutableStateFlow(false)
    val isFilterVisible: StateFlow<Boolean> = _isFilterVisible.asStateFlow()

    init {
        loadInitialData()
        loadFilterMetadata()
    }

    // ===== EVENT HANDLERS =====
    fun onEvent(event: CommunityFeedEvent) {
        when (event) {
            is CommunityFeedEvent.Refresh -> refreshFeed()
            is CommunityFeedEvent.Retry -> loadInitialData()
            is CommunityFeedEvent.GiveSupport -> giveSupport(event.postId, event.type)
            is CommunityFeedEvent.ReportPost -> reportPost(event.postId)
            CommunityFeedEvent.ClearError -> clearError()
        }
    }

    fun onFilterEvent(event: TestFilterEvent) {
        when (event) {
            TestFilterEvent.ToggleFilter -> toggleFilter()
            is TestFilterEvent.ApplyTimeFilter -> applyTimeFilter(event.timeFilter)
            is TestFilterEvent.ApplyChaosLevel -> applyChaosLevelFilter(event.range)
            is TestFilterEvent.ToggleTag -> toggleTag(event.tag)
            TestFilterEvent.ClearFilters -> clearAllFilters()
            TestFilterEvent.LoadMore -> loadMore()
        }
    }

    // ===== CORE METHODS =====
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val filter = _filterState.value
                val result = paginationRepository.getPaginatedPosts(
                    page = 1,
                    pageSize = 15,
                    timeRange = filter.timeFilter,
                    chaosLevelRange = filter.chaosLevelRange,
                    tags = filter.selectedTags.takeIf { it.isNotEmpty() }?.toList()
                )

                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            posts = response.data,
                            isLoading = false,
                            error = null
                        )
                        _paginationState.value = _paginationState.value.copy(
                            currentPage = response.page,
                            hasNextPage = response.hasNext,
                            totalCount = response.totalCount,
                            isLoading = false
                        )
                        Timber.d("✅ Loaded ${response.data.size} posts")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load posts: ${error.localizedMessage}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun loadMore() {
        val pagination = _paginationState.value
        if (!pagination.hasNextPage || pagination.isLoading) return

        viewModelScope.launch {
            try {
                _paginationState.value = _paginationState.value.copy(isLoading = true)

                val filter = _filterState.value
                val result = paginationRepository.getPaginatedPosts(
                    page = pagination.currentPage + 1,
                    pageSize = 15,
                    timeRange = filter.timeFilter,
                    chaosLevelRange = filter.chaosLevelRange,
                    tags = filter.selectedTags.takeIf { it.isNotEmpty() }?.toList()
                )

                result.fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            posts = _uiState.value.posts + response.data
                        )
                        _paginationState.value = _paginationState.value.copy(
                            currentPage = response.page,
                            hasNextPage = response.hasNext,
                            totalCount = response.totalCount,
                            isLoading = false
                        )
                        Timber.d("✅ Loaded ${response.data.size} more posts")
                    },
                    onFailure = { error ->
                        _paginationState.value = _paginationState.value.copy(isLoading = false)
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load more: ${error.localizedMessage}"
                        )
                    }
                )
            } catch (e: Exception) {
                _paginationState.value = _paginationState.value.copy(isLoading = false)
                _uiState.value = _uiState.value.copy(
                    error = "Error loading more: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadInitialData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    // ===== FILTER METHODS =====
    private fun toggleFilter() {
        _isFilterVisible.value = !_isFilterVisible.value
    }

    private fun applyTimeFilter(timeFilter: TimeFilter) {
        _filterState.value = _filterState.value.copy(timeFilter = timeFilter)
        loadInitialData()
    }

    private fun applyChaosLevelFilter(range: IntRange?) {
        _filterState.value = _filterState.value.copy(chaosLevelRange = range)
        loadInitialData()
    }

    private fun toggleTag(tag: String) {
        val currentTags = _filterState.value.selectedTags.toMutableSet()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _filterState.value = _filterState.value.copy(selectedTags = currentTags)
        loadInitialData()
    }

    private fun clearAllFilters() {
        _filterState.value = SimpleFilterState()
        loadInitialData()
    }

    private fun loadFilterMetadata() {
        viewModelScope.launch {
            try {
                _metadataState.value = _metadataState.value.copy(isLoading = true)

                val result = paginationRepository.getFilterMetadata()
                result.fold(
                    onSuccess = { metadata ->
                        _metadataState.value = SimpleMetadataState(
                            popularTags = metadata.popularTags,
                            chaosLevelRange = metadata.chaosLevelRange,
                            isLoading = false
                        )
                        Timber.d("✅ Loaded metadata: ${metadata.popularTags.size} tags")
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to load metadata")
                        _metadataState.value = _metadataState.value.copy(isLoading = false)
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading metadata")
                _metadataState.value = _metadataState.value.copy(isLoading = false)
            }
        }
    }

    // ===== SUPPORT METHODS (dari existing ViewModel) =====
    private fun giveSupport(postId: String, supportType: com.dailychaos.project.domain.model.SupportType) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(error = "Please login to give support")
                    return@launch
                }

                val result = baseRepository.giveSupport(postId, currentUser.id, supportType)
                result.fold(
                    onSuccess = {
                        // Refresh posts to show updated support
                        loadInitialData()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to give support: ${exception.localizedMessage}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error giving support: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun reportPost(postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(error = "Please login to report posts")
                    return@launch
                }

                val result = baseRepository.reportPost(postId, currentUser.id, "Inappropriate content")
                result.fold(
                    onSuccess = {
                        // Mark post as reported in UI
                        _uiState.value = _uiState.value.copy(
                            posts = _uiState.value.posts.map { post ->
                                if (post.id == postId) post.copy(isReported = true) else post
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to report post: ${exception.localizedMessage}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error reporting post: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ===== UTILITY METHODS =====
    fun canLoadMore(): Boolean {
        val pagination = _paginationState.value
        return pagination.hasNextPage && !pagination.isLoading
    }

    fun getActiveFilterCount(): Int {
        return _filterState.value.getActiveFilterCount()
    }
}

// ===== SIMPLE STATE CLASSES =====
data class SimpleFilterState(
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val chaosLevelRange: IntRange? = null,
    val selectedTags: Set<String> = emptySet()
) {
    fun getActiveFilterCount(): Int {
        var count = 0
        if (timeFilter != TimeFilter.ALL) count++
        if (chaosLevelRange != null) count++
        if (selectedTags.isNotEmpty()) count++
        return count
    }
}

data class SimplePaginationState(
    val currentPage: Int = 1,
    val hasNextPage: Boolean = true,
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

data class SimpleMetadataState(
    val popularTags: List<String> = emptyList(),
    val chaosLevelRange: IntRange = 1..10,
    val isLoading: Boolean = false
)

// ===== FILTER EVENTS =====
sealed class TestFilterEvent {
    object ToggleFilter : TestFilterEvent()
    data class ApplyTimeFilter(val timeFilter: TimeFilter) : TestFilterEvent()
    data class ApplyChaosLevel(val range: IntRange?) : TestFilterEvent()
    data class ToggleTag(val tag: String) : TestFilterEvent()
    object ClearFilters : TestFilterEvent()
    object LoadMore : TestFilterEvent()
}