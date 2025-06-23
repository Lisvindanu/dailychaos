// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/feed/CommunityFeedViewModel.kt
package com.dailychaos.project.presentation.ui.screen.community.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CommunityFeedViewModel @Inject constructor(
    private val communityRepository: CommunityRepositoryExtended, // ✅ CHANGED: Use extended repository
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityFeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCommunityFeed(isInitialLoad = true)
    }

    fun onEvent(event: CommunityFeedEvent) {
        when (event) {
            is CommunityFeedEvent.Refresh -> loadCommunityFeed(isRefreshing = true)
            is CommunityFeedEvent.Retry -> loadCommunityFeed(isInitialLoad = true)
            is CommunityFeedEvent.GiveSupport -> giveSupport(event.postId, event.type)
            is CommunityFeedEvent.ReportPost -> reportPost(event.postId)
            CommunityFeedEvent.ClearError -> TODO()
        }
    }

    private fun loadCommunityFeed(isInitialLoad: Boolean = false, isRefreshing: Boolean = false) {
        viewModelScope.launch {
            try {
                Timber.d("🌍 ==================== LOADING COMMUNITY FEED ====================")

                if (isInitialLoad) {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                if (isRefreshing) {
                    _uiState.update { it.copy(isRefreshing = true, error = null) }
                }

                // Load community posts from repository
                communityRepository.getRecentCommunityPosts(limit = 50)
                    .catch { exception ->
                        Timber.e(exception, "❌ Error loading community posts")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = "Failed to load community posts: ${exception.message}"
                            )
                        }
                    }
                    .collect { posts ->
                        Timber.d("🌍 Received ${posts.size} community posts")
                        posts.forEach { post ->
                            Timber.d("  - Post: ${post.id} | ${post.title} | ${post.anonymousUsername}")
                        }

                        _uiState.update {
                            it.copy(
                                posts = posts,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading community feed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    // ✅ ENHANCED: Support giving dengan same logic as CommunityPostDetailViewModel
    private fun giveSupport(postId: String, supportType: SupportType) {
        viewModelScope.launch {
            try {
                Timber.d("💙 ==================== GIVING SUPPORT FROM FEED ====================")
                Timber.d("💙 Giving support to post: $postId (type: $supportType)")

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot give support - user not authenticated")
                    _uiState.update {
                        it.copy(error = "Please login to give support")
                    }
                    return@launch
                }

                // ✅ NEW: Check current user support type for this post
                val currentSupportType = communityRepository.getUserSupportType(postId, currentUser.id)
                Timber.d("💙 Current user support type: $currentSupportType")

                // ✅ NEW: Check if trying to give same support type
                if (currentSupportType == supportType) {
                    Timber.d("🔄 User trying to give same support type - this will remove support")
                    // For feed screen, we could show a confirmation or just toggle off
                    // For now, let's just proceed with the toggle behavior
                }

                // Optimistic UI update
                val currentPost = _uiState.value.posts.find { it.id == postId }
                if (currentPost != null) {
                    _uiState.update { state ->
                        val updatedPosts = state.posts.map { post ->
                            if (post.id == postId) {
                                when {
                                    currentSupportType == null -> {
                                        // New support - increment count
                                        post.copy(supportCount = post.supportCount + 1)
                                    }
                                    currentSupportType == supportType -> {
                                        // Same support type - remove support (decrement)
                                        post.copy(supportCount = (post.supportCount - 1).coerceAtLeast(0))
                                    }
                                    else -> {
                                        // Different support type - count stays same
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

                // ✅ ENHANCED: Give support via repository dengan proper error handling
                val result = communityRepository.giveSupport(postId, currentUser.id, supportType)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Support operation completed successfully from feed")
                        // UI already updated optimistically

                        // Show appropriate message based on operation
                        when {
                            currentSupportType == null -> {
                                Timber.d("✅ New support given")
                                // Could show snackbar: "Support given!"
                            }
                            currentSupportType == supportType -> {
                                Timber.d("✅ Support removed (toggle)")
                                // Could show snackbar: "Support removed"
                            }
                            else -> {
                                Timber.d("✅ Support type changed")
                                // Could show snackbar: "Support changed!"
                            }
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to give support from feed")

                        // ✅ ENHANCED: Better error handling
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
                                            // Was new support - revert increment
                                            post.copy(supportCount = (post.supportCount - 1).coerceAtLeast(0))
                                        }
                                        currentSupportType == supportType -> {
                                            // Was remove support - revert decrement
                                            post.copy(supportCount = post.supportCount + 1)
                                        }
                                        else -> {
                                            // Was change support type - no count change to revert
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

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot report post - user not authenticated")
                    _uiState.update {
                        it.copy(error = "Please login to report posts")
                    }
                    return@launch
                }

                // Report post via repository
                val result = communityRepository.reportPost(postId, currentUser.id, "Inappropriate content")

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Post reported successfully")

                        // Optional: Remove from feed or mark as reported
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

    // Helper method to manually refresh for testing
    fun refreshFeed() {
        Timber.d("🔄 Manual refresh triggered")
        loadCommunityFeed(isRefreshing = true)
    }

    // Clear error state
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}