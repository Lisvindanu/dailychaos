// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/support/SupportViewModel.kt
package com.dailychaos.project.presentation.ui.screen.community.support

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.SupportComment
import com.dailychaos.project.domain.model.SupportCommentRequest
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
class SupportViewModel @Inject constructor(
    private val communityRepository: CommunityRepositoryExtended,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPostId: String = ""

    fun initialize(postId: String) {
        currentPostId = postId
        loadComments()
    }

    fun onEvent(event: SupportEvent) {
        when (event) {
            is SupportEvent.LoadComments -> loadComments()
            is SupportEvent.RefreshComments -> loadComments(isRefreshing = true)
            is SupportEvent.ClearError -> clearError()

            // Comment creation
            is SupportEvent.UpdateCommentText -> updateCommentText(event.text)
            is SupportEvent.SelectSupportType -> selectSupportType(event.type)
            is SupportEvent.SelectSupportLevel -> selectSupportLevel(event.level)
            is SupportEvent.ToggleAnonymous -> toggleAnonymous(event.isAnonymous)
            is SupportEvent.ShowCommentDialog -> showCommentDialog()
            is SupportEvent.HideCommentDialog -> hideCommentDialog()
            is SupportEvent.PostComment -> postComment()

            // Comment interactions
            is SupportEvent.LikeComment -> likeComment(event.commentId)
            is SupportEvent.ReportComment -> reportComment(event.commentId, event.reason)
            is SupportEvent.ExpandComment -> expandComment(event.commentId)
            is SupportEvent.CollapseComment -> collapseComment(event.commentId)
            is SupportEvent.ReplyToComment -> replyToComment(event.parentCommentId)
        }
    }

    private fun loadComments(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            try {
                Timber.d("💬 ==================== LOADING SUPPORT COMMENTS ====================")
                Timber.d("💬 Loading comments for post: $currentPostId")

                if (!isRefreshing) {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                } else {
                    _uiState.update { it.copy(isRefreshing = true, error = null) }
                }

                // ✅ REAL IMPLEMENTATION - Replace mock with actual repository call
                communityRepository.getPostComments(currentPostId)
                    .catch { exception ->
                        Timber.e(exception, "❌ Error loading comments")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = "Failed to load comments: ${exception.message}"
                            )
                        }
                    }
                    .collect { comments ->
                        Timber.d("✅ Loaded ${comments.size} comments")

                        // Calculate support type breakdown
                        val supportTypeBreakdown = comments.groupBy { it.supportType }
                            .mapValues { it.value.size }

                        // Get current user to check liked status
                        val currentUser = authRepository.getCurrentUser()
                        val commentsWithLikeStatus = if (currentUser != null) {
                            checkCommentsLikeStatus(comments, currentUser.id)
                        } else {
                            comments
                        }

                        _uiState.update {
                            it.copy(
                                comments = commentsWithLikeStatus,
                                totalComments = comments.size,
                                supportTypeBreakdown = supportTypeBreakdown,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading comments")
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

    private fun postComment() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { it.copy(error = "Please login to post comments") }
                    return@launch
                }

                val state = _uiState.value
                if (state.commentText.isBlank()) {
                    _uiState.update { it.copy(error = "Comment cannot be empty") }
                    return@launch
                }

                _uiState.update { it.copy(isPostingComment = true, error = null) }

                val commentRequest = SupportCommentRequest(
                    postId = currentPostId,
                    content = state.commentText.trim(),
                    supportType = state.selectedSupportType,
                    supportLevel = state.selectedSupportLevel,
                    isAnonymous = state.isAnonymous
                )

                Timber.d("💬 Posting comment: $commentRequest")

                // ✅ REAL IMPLEMENTATION - Replace mock with actual repository call
                val result = communityRepository.postComment(commentRequest)
                result.fold(
                    onSuccess = { commentId ->
                        Timber.d("✅ Comment posted successfully: $commentId")
                        clearCommentForm()
                        loadComments() // Reload to show new comment
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to post comment")
                        _uiState.update {
                            it.copy(
                                isPostingComment = false,
                                error = "Failed to post comment: ${exception.message}"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error posting comment")
                _uiState.update {
                    it.copy(
                        isPostingComment = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun likeComment(commentId: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { it.copy(error = "Please login to like comments") }
                    return@launch
                }

                Timber.d("👍 Toggling like for comment: $commentId")

                // Optimistically update UI first
                updateCommentLikeStatusOptimistically(commentId)

                // ✅ REAL IMPLEMENTATION
                val result = communityRepository.likeComment(commentId, currentUser.id)
                result.fold(
                    onSuccess = {
                        Timber.d("✅ Comment like toggled successfully")
                        // UI already updated optimistically
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to toggle comment like")
                        // Revert optimistic update
                        updateCommentLikeStatusOptimistically(commentId)
                        _uiState.update {
                            it.copy(error = "Failed to update like: ${exception.message}")
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "❌ Error liking comment")
                _uiState.update {
                    it.copy(error = "Failed to update like: ${e.message}")
                }
            }
        }
    }

    private fun reportComment(commentId: String, reason: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { it.copy(error = "Please login to report comments") }
                    return@launch
                }

                Timber.d("🚨 Reporting comment: $commentId, reason: $reason")

                // ✅ REAL IMPLEMENTATION
                val result = communityRepository.reportComment(commentId, currentUser.id, reason)
                result.fold(
                    onSuccess = {
                        Timber.d("✅ Comment reported successfully")
                        _uiState.update {
                            it.copy(error = null)
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to report comment")
                        _uiState.update {
                            it.copy(error = "Failed to report comment: ${exception.message}")
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "❌ Error reporting comment")
                _uiState.update {
                    it.copy(error = "Failed to report comment: ${e.message}")
                }
            }
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private suspend fun checkCommentsLikeStatus(
        comments: List<SupportComment>,
        userId: String
    ): List<SupportComment> {
        // For now, return comments as-is
        // In production, you'd check which comments the user has liked
        return comments
    }

    private fun updateCommentLikeStatusOptimistically(commentId: String) {
        _uiState.update { state ->
            val updatedComments = state.comments.map { comment ->
                if (comment.id == commentId) {
                    if (comment.isLikedByCurrentUser) {
                        // Unlike
                        comment.copy(
                            isLikedByCurrentUser = false,
                            likeCount = maxOf(0, comment.likeCount - 1)
                        )
                    } else {
                        // Like
                        comment.copy(
                            isLikedByCurrentUser = true,
                            likeCount = comment.likeCount + 1
                        )
                    }
                } else {
                    comment
                }
            }
            state.copy(comments = updatedComments)
        }
    }

    private fun clearCommentForm() {
        _uiState.update {
            it.copy(
                commentText = "",
                selectedSupportType = SupportType.HEART,
                selectedSupportLevel = 1,
                isPostingComment = false,
                showCommentDialog = false
            )
        }
    }

    private fun updateCommentText(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    private fun selectSupportType(type: SupportType) {
        _uiState.update { it.copy(selectedSupportType = type) }
    }

    private fun selectSupportLevel(level: Int) {
        _uiState.update { it.copy(selectedSupportLevel = level.coerceIn(1, 5)) }
    }

    private fun toggleAnonymous(isAnonymous: Boolean) {
        _uiState.update { it.copy(isAnonymous = isAnonymous) }
    }

    private fun showCommentDialog() {
        _uiState.update { it.copy(showCommentDialog = true) }
    }

    private fun hideCommentDialog() {
        _uiState.update { it.copy(showCommentDialog = false) }
    }

    private fun expandComment(commentId: String) {
        _uiState.update {
            it.copy(expandedCommentId = if (it.expandedCommentId == commentId) null else commentId)
        }
    }

    private fun collapseComment(commentId: String) {
        _uiState.update { it.copy(expandedCommentId = null) }
    }

    private fun replyToComment(parentCommentId: String) {
        // TODO: Implement reply functionality
        Timber.d("💭 Replying to comment: $parentCommentId")
        // For now, just show the comment dialog
        showCommentDialog()
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}