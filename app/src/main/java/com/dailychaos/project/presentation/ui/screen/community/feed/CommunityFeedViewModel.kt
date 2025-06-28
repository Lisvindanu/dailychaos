// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/feed/CommunityFeedViewModel.kt
package com.dailychaos.project.presentation.ui.screen.community.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.dailychaos.project.domain.repository.CommunityRepositoryPagination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CommunityFeedViewModel @Inject constructor(
    private val communityRepository: CommunityRepositoryExtended,
    private val paginationRepository: CommunityRepositoryPagination,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityFeedUiState())
    val uiState = _uiState.asStateFlow()

    // Pagination extension
    private val paginationExtension = CommunityFeedPaginationExtension(
        paginationRepository = paginationRepository,
        baseViewModel = this
    )

    // Expose extension properties
    val filterState = paginationExtension.filterState
    val paginationState = paginationExtension.paginationState
    val metadataState = paginationExtension.metadataState
    val isFilterVisible = paginationExtension.isFilterVisible

    init {
        // ✅ CHANGED: Load via pagination instead of basic feed
        loadPaginatedData()
    }

    fun onEvent(event: CommunityFeedEvent) {
        when (event) {
            is CommunityFeedEvent.Refresh -> refreshFeed()
            is CommunityFeedEvent.Retry -> loadPaginatedData()
            is CommunityFeedEvent.GiveSupport -> giveSupport(event.postId, event.type)
            is CommunityFeedEvent.ReportPost -> reportPost(event.postId)
            CommunityFeedEvent.ClearError -> clearError()
        }
    }

    fun onFilterEvent(event: FilterEvent) {
        when (event) {
            FilterEvent.ToggleFilter -> paginationExtension.toggleFilter()
            is FilterEvent.UpdateTimeFilter -> paginationExtension.updateTimeFilter(event.timeFilter)
            is FilterEvent.UpdateChaosLevel -> paginationExtension.updateChaosLevelFilter(event.range)
            is FilterEvent.ToggleTag -> paginationExtension.toggleTag(event.tag)
            is FilterEvent.UpdateSort -> paginationExtension.updateSortBy(event.sortBy)
            is FilterEvent.UpdateSearch -> paginationExtension.updateSearchQuery(event.query)
            FilterEvent.ClearFilters -> paginationExtension.clearAllFilters()
            FilterEvent.LoadMore -> paginationExtension.loadMore()
            FilterEvent.Refresh -> paginationExtension.refresh()
        }
    }

    // ✅ NEW: Load data via pagination repository
    private fun loadPaginatedData() {
        viewModelScope.launch {
            try {
                Timber.d("🌍 ==================== LOADING PAGINATED COMMUNITY FEED ====================")

                _uiState.update { it.copy(isLoading = true, error = null) }

                val filter = filterState.value
                val result = paginationRepository.getPaginatedPosts(
                    page = 1,
                    pageSize = 15,
                    timeRange = filter.timeFilter,
                    chaosLevelRange = filter.chaosLevelRange,
                    tags = filter.selectedTags.takeIf { it.isNotEmpty() }?.toList(),
                    sortBy = filter.sortBy
                )

                result.fold(
                    onSuccess = { response ->
                        Timber.d("🌍 Received ${response.data.size} community posts via pagination")
                        response.data.forEach { post ->
                            Timber.d("  - Post: ${post.id} | ${post.title} | ${post.anonymousUsername}")
                        }

                        _uiState.update {
                            it.copy(
                                posts = response.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        Timber.e(error, "❌ Error loading paginated posts")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load posts: ${error.localizedMessage}"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading paginated feed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    // ✅ CHANGED: Refresh via pagination
    private fun refreshFeed() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true, error = null) }

                val filter = filterState.value
                val result = paginationRepository.getPaginatedPosts(
                    page = 1,
                    pageSize = 15,
                    timeRange = filter.timeFilter,
                    chaosLevelRange = filter.chaosLevelRange,
                    tags = filter.selectedTags.takeIf { it.isNotEmpty() }?.toList(),
                    sortBy = filter.sortBy
                )

                result.fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                posts = response.data,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = "Failed to refresh: ${error.localizedMessage}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = "Refresh error: ${e.message}"
                    )
                }
            }
        }
    }

    // ✅ NEW: Update posts from extension (called by pagination extension)
    fun updatePosts(posts: List<com.dailychaos.project.domain.model.CommunityPost>, append: Boolean = false) {
        _uiState.update { state ->
            state.copy(
                posts = if (append) {
                    state.posts + posts
                } else {
                    posts
                }
            )
        }
    }

    // ✅ ENHANCED: Support giving dengan same logic as detail screen
    private fun giveSupport(postId: String, supportType: SupportType) {
        viewModelScope.launch {
            try {
                Timber.d("💙 ==================== GIVING SUPPORT FROM FEED ====================")
                Timber.d("💙 Giving support to post: $postId (type: $supportType)")

                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot give support - user not authenticated")
                    _uiState.update {
                        it.copy(error = "Please login to give support")
                    }
                    return@launch
                }

                val currentSupportType = communityRepository.getUserSupportType(postId, currentUser.id)
                Timber.d("💙 Current user support type: $currentSupportType")

                // Optimistic UI update
                val currentPost = _uiState.value.posts.find { it.id == postId }
                if (currentPost != null) {
                    _uiState.update { state ->
                        val updatedPosts = state.posts.map { post ->
                            if (post.id == postId) {
                                when {
                                    currentSupportType == null -> {
                                        post.copy(supportCount = post.supportCount + 1)
                                    }
                                    currentSupportType == supportType -> {
                                        post.copy(supportCount = (post.supportCount - 1).coerceAtLeast(0))
                                    }
                                    else -> {
                                        post
                                    }
                                }
                            } else {
                                post
                            }
                        }
                        state.copy(posts = updatedPosts)
                    }
                }

                val result = communityRepository.giveSupport(postId, currentUser.id, supportType)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Support operation completed successfully from feed")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to give support from feed")

                        val errorMessage = when {
                            exception.message?.contains("SAME_SUPPORT_TYPE") == true -> {
                                "You already gave this support type"
                            }
                            exception.message?.contains("permission", ignoreCase = true) == true -> {
                                "Permission denied. Please check your access rights."
                            }
                            exception.message?.contains("network", ignoreCase = true) == true -> {
                                "Network error. Please check your connection."
                            }
                            else -> {
                                "Failed to give support: ${exception.message}"
                            }
                        }

                        // Revert optimistic update
                        _uiState.update { state ->
                            val revertedPosts = state.posts.map { post ->
                                if (post.id == postId) {
                                    when {
                                        currentSupportType == null -> {
                                            post.copy(supportCount = (post.supportCount - 1).coerceAtLeast(0))
                                        }
                                        currentSupportType == supportType -> {
                                            post.copy(supportCount = post.supportCount + 1)
                                        }
                                        else -> {
                                            post
                                        }
                                    }
                                } else {
                                    post
                                }
                            }
                            state.copy(
                                posts = revertedPosts,
                                error = errorMessage
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error giving support from feed")
                _uiState.update {
                    it.copy(error = "Unexpected error: ${e.message}")
                }
            }
        }
    }

    private fun reportPost(postId: String) {
        viewModelScope.launch {
            try {
                Timber.d("🚨 ==================== REPORTING POST ====================")
                Timber.d("🚨 Reporting post: $postId")

                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot report post - user not authenticated")
                    _uiState.update {
                        it.copy(error = "Please login to report posts")
                    }
                    return@launch
                }

                val result = communityRepository.reportPost(postId, currentUser.id, "Inappropriate content")

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Post reported successfully")
                        _uiState.update { state ->
                            val updatedPosts = state.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(isReported = true)
                                } else {
                                    post
                                }
                            }
                            state.copy(posts = updatedPosts)
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to report post")
                        _uiState.update {
                            it.copy(error = "Failed to report post: ${exception.message}")
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error reporting post")
                _uiState.update {
                    it.copy(error = "Unexpected error: ${e.message}")
                }
            }
        }
    }

    // Clear error state
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Utility methods
    fun canLoadMore(): Boolean {
        val pagination = paginationState.value
        return pagination.hasNextPage && !pagination.isLoadingMore
    }

    fun getActiveFilterCount(): Int {
        return filterState.value.getActiveFilterCount()
    }
}