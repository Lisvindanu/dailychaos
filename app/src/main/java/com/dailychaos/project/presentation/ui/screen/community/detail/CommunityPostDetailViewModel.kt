// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/detail/CommunityPostDetailViewModel.kt
package com.dailychaos.project.presentation.ui.screen.community.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI State for Community Post Detail Screen
 */
data class CommunityPostDetailUiState(
    val post: CommunityPost? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isGivingSupport: Boolean = false,
    val showReportDialog: Boolean = false
)

/**
 * UI Events for Community Post Detail Screen
 */
sealed class CommunityPostDetailEvent {
    data class GiveSupport(val supportType: SupportType) : CommunityPostDetailEvent()
    object RemoveSupport : CommunityPostDetailEvent()
    object ShowReportDialog : CommunityPostDetailEvent()
    object DismissReportDialog : CommunityPostDetailEvent()
    data class ReportPost(val reason: String) : CommunityPostDetailEvent()
    object Retry : CommunityPostDetailEvent()
}

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CommunityPostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    private val _uiState = MutableStateFlow(CommunityPostDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CommunityPostDetailScreenEvent>()
    val events = _events.asSharedFlow()

    init {
        Timber.d("🌍 ==================== COMMUNITY POST DETAIL SCREEN INITIALIZED ====================")
        Timber.d("🌍 Post ID from navigation: '$postId'")

        if (postId.isBlank()) {
            Timber.e("❌ Post ID is blank - cannot proceed")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Invalid post ID: Post ID cannot be empty"
                )
            }
        } else {
            loadCommunityPost()
        }
    }

    fun onEvent(event: CommunityPostDetailEvent) {
        when (event) {
            is CommunityPostDetailEvent.GiveSupport -> {
                giveSupport(event.supportType)
            }
            is CommunityPostDetailEvent.RemoveSupport -> {
                removeSupport()
            }
            is CommunityPostDetailEvent.ShowReportDialog -> {
                _uiState.update { it.copy(showReportDialog = true) }
            }
            is CommunityPostDetailEvent.DismissReportDialog -> {
                _uiState.update { it.copy(showReportDialog = false) }
            }
            is CommunityPostDetailEvent.ReportPost -> {
                reportPost(event.reason)
            }
            is CommunityPostDetailEvent.Retry -> {
                loadCommunityPost()
            }
        }
    }

    private fun loadCommunityPost() {
        viewModelScope.launch {
            try {
                Timber.d("🌍 ==================== LOADING COMMUNITY POST DETAIL ====================")
                Timber.d("🌍 Loading community post with ID: '$postId'")

                _uiState.update { it.copy(isLoading = true, error = null) }

                // Load community post from repository
                communityRepository.getCommunityPost(postId)
                    .catch { exception ->
                        Timber.e(exception, "❌ Error loading community post from repository")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load community post: ${exception.message}"
                            )
                        }
                    }
                    .collect { post ->
                        if (post != null) {
                            Timber.d("✅ Community post loaded successfully:")
                            Timber.d("  - ID: ${post.id}")
                            Timber.d("  - Title: ${post.title}")
                            Timber.d("  - Anonymous Username: ${post.anonymousUsername}")
                            Timber.d("  - Content length: ${post.description.length}")
                            Timber.d("  - Chaos Level: ${post.chaosLevel}")
                            Timber.d("  - Support Count: ${post.supportCount}")
                            Timber.d("  - Tags: ${post.tags}")
                            Timber.d("  - Created At: ${post.createdAt}")

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    post = post,
                                    error = null
                                )
                            }
                        } else {
                            val error = "Community post not found with ID: $postId"
                            Timber.e("❌ $error")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = error
                                )
                            }
                        }
                    }

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading community post")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun giveSupport(supportType: SupportType) {
        viewModelScope.launch {
            try {
                Timber.d("💝 ==================== GIVING SUPPORT ====================")
                Timber.d("💝 Giving support to post: $postId, type: $supportType")

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot give support - user not authenticated")
                    _uiState.update {
                        it.copy(error = "You must be logged in to give support")
                    }
                    return@launch
                }

                _uiState.update { it.copy(isGivingSupport = true) }

                // Optimistically update support count
                _uiState.value.post?.let { post ->
                    _uiState.update {
                        it.copy(
                            post = post.copy(supportCount = post.supportCount + 1)
                        )
                    }
                }

                // Give support via repository
                val result = communityRepository.giveSupport(postId, currentUser.id, supportType)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Support given successfully")
                        _uiState.update { it.copy(isGivingSupport = false) }
                        _events.emit(CommunityPostDetailScreenEvent.SupportGiven)
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to give support")

                        // Revert optimistic update
                        _uiState.value.post?.let { post ->
                            _uiState.update {
                                it.copy(
                                    post = post.copy(supportCount = (post.supportCount - 1).coerceAtLeast(0)),
                                    isGivingSupport = false,
                                    error = "Failed to give support: ${exception.message}"
                                )
                            }
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error giving support")
                _uiState.update {
                    it.copy(
                        isGivingSupport = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun removeSupport() {
        viewModelScope.launch {
            try {
                Timber.d("🚫 ==================== REMOVING SUPPORT ====================")
                Timber.d("🚫 Removing support from post: $postId")

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot remove support - user not authenticated")
                    return@launch
                }

                // Remove support via repository
                val result = communityRepository.removeSupport(postId, currentUser.id)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Support removed successfully")
                        _events.emit(CommunityPostDetailScreenEvent.SupportRemoved)
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to remove support")
                        _uiState.update {
                            it.copy(error = "Failed to remove support: ${exception.message}")
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error removing support")
                _uiState.update {
                    it.copy(error = "Unexpected error: ${e.message}")
                }
            }
        }
    }

    private fun reportPost(reason: String) {
        viewModelScope.launch {
            try {
                Timber.d("🚨 ==================== REPORTING POST ====================")
                Timber.d("🚨 Reporting post: $postId, reason: $reason")

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot report post - user not authenticated")
                    return@launch
                }

                _uiState.update { it.copy(showReportDialog = false) }

                // Report post via repository
                val result = communityRepository.reportPost(postId, currentUser.id, reason)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Post reported successfully")
                        _events.emit(CommunityPostDetailScreenEvent.PostReported)
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

    // Helper function to refresh post data
    fun refreshPost() {
        Timber.d("🔄 Manual refresh triggered")
        loadCommunityPost()
    }

    // Clear error state
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    sealed class CommunityPostDetailScreenEvent {
        object SupportGiven : CommunityPostDetailScreenEvent()
        object SupportRemoved : CommunityPostDetailScreenEvent()
        object PostReported : CommunityPostDetailScreenEvent()
    }
}