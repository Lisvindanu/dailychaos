package com.dailychaos.project.presentation.ui.screen.community.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * CommunityPostDetailViewModel - Enhanced dengan support type change dan Megumin modal
 * "ViewModel untuk detail community post dengan sistem support yang lebih canggih"
 */
@HiltViewModel
class CommunityPostDetailViewModel @Inject constructor(
    private val communityRepository: CommunityRepositoryExtended,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityPostDetailUiState())
    val uiState: StateFlow<CommunityPostDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CommunityPostDetailScreenEvent>()
    val events: SharedFlow<CommunityPostDetailScreenEvent> = _events.asSharedFlow()

    private var postId: String = ""
    private var currentUserId: String? = null

    // ============================================================================
    // PUBLIC METHODS
    // ============================================================================

    fun loadPost(postId: String) {
        this.postId = postId
        Timber.d("📄 ==================== LOADING POST ====================")
        Timber.d("📄 Loading post: $postId")

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                currentUserId = currentUser?.id

                communityRepository.getCommunityPost(postId)
                    .catch { exception ->
                        Timber.e(exception, "❌ Error loading post: $postId")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = getErrorMessage(exception)
                            )
                        }
                    }
                    .collect { post ->
                        if (post != null) {
                            Timber.d("✅ Post loaded successfully: ${post.title}")

                            // Check if current user has given support
                            val userSupportType = if (currentUserId != null) {
                                communityRepository.getUserSupportType(postId, currentUserId!!)
                            } else null

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    post = post,
                                    currentUserSupportType = userSupportType,
                                    error = null
                                )
                            }
                        } else {
                            Timber.w("⚠️ Post not found: $postId")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Post not found"
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading post")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun giveSupport(supportType: SupportType = SupportType.HEART) {
        viewModelScope.launch {
            try {
                Timber.d("💝 ==================== GIVING SUPPORT ====================")
                Timber.d("💝 Giving support to post: $postId, type: $supportType")

                // Validasi state
                if (_uiState.value.isGivingSupport) {
                    Timber.w("⚠️ Already giving support, ignoring request")
                    return@launch
                }

                if (postId.isBlank()) {
                    Timber.e("❌ Invalid post ID")
                    _uiState.update { it.copy(error = "Invalid post ID") }
                    return@launch
                }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot give support - user not authenticated")
                    _uiState.update {
                        it.copy(error = "You must be logged in to give support")
                    }
                    _events.emit(CommunityPostDetailScreenEvent.NavigateToLogin)
                    return@launch
                }

                // Check if user already gave support
                val currentSupportType = _uiState.value.currentUserSupportType

                if (currentSupportType != null) {
                    if (currentSupportType == supportType) {
                        // Same support type - show Megumin sad modal
                        Timber.d("😢 User trying to give same support type - showing Megumin modal")
                        _events.emit(CommunityPostDetailScreenEvent.ShowMeguminSadModal)
                        return@launch
                    } else {
                        // Different support type - change it
                        Timber.d("🔄 User changing support type from $currentSupportType to $supportType")
                        _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Support type changed to ${getSupportEmoji(supportType)}"))
                    }
                }

                _uiState.update { it.copy(isGivingSupport = true, error = null) }

                // Optimistically update support count dan user support type
                val currentPost = _uiState.value.post
                if (currentPost != null) {
                    val newSupportCount = if (currentSupportType == null) {
                        // New support - increment count
                        currentPost.supportCount + 1
                    } else {
                        // Change support type - count stays same
                        currentPost.supportCount
                    }

                    _uiState.update {
                        it.copy(
                            post = currentPost.copy(supportCount = newSupportCount),
                            currentUserSupportType = supportType
                        )
                    }
                }

                // Give support via repository
                val result = communityRepository.giveSupport(postId, currentUser.id, supportType)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Support given successfully")
                        _uiState.update {
                            it.copy(
                                isGivingSupport = false,
                                error = null
                            )
                        }

                        if (currentSupportType == null) {
                            _events.emit(CommunityPostDetailScreenEvent.SupportGiven)
                            _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Support given! ${getSupportEmoji(supportType)} 💙"))
                        } else {
                            _events.emit(CommunityPostDetailScreenEvent.SupportTypeChanged)
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to give support")

                        // Revert optimistic update
                        if (currentPost != null) {
                            _uiState.update {
                                it.copy(
                                    post = currentPost,
                                    currentUserSupportType = currentSupportType
                                )
                            }
                        }

                        val errorMessage = getErrorMessage(exception)
                        _uiState.update {
                            it.copy(
                                isGivingSupport = false,
                                error = errorMessage
                            )
                        }

                        // Handle specific error types
                        when {
                            exception.message?.contains("SAME_SUPPORT_TYPE") == true -> {
                                _events.emit(CommunityPostDetailScreenEvent.ShowMeguminSadModal)
                            }
                            exception.message?.contains("Permission denied") == true -> {
                                _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Access denied. Please check your permissions."))
                            }
                            exception.message?.contains("Post not found") == true -> {
                                _events.emit(CommunityPostDetailScreenEvent.ShowMessage("This post no longer exists."))
                            }
                            else -> {
                                _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Failed to give support. Please try again."))
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
                _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Something went wrong. Please try again."))
            }
        }
    }

    fun removeSupport() {
        viewModelScope.launch {
            try {
                Timber.d("🚫 ==================== REMOVING SUPPORT ====================")
                Timber.d("🚫 Removing support from post: $postId")

                if (postId.isBlank()) {
                    Timber.e("❌ Invalid post ID")
                    return@launch
                }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot remove support - user not authenticated")
                    _uiState.update {
                        it.copy(error = "You must be logged in to remove support")
                    }
                    return@launch
                }

                // Check if user actually has given support
                val currentSupportType = _uiState.value.currentUserSupportType
                if (currentSupportType == null) {
                    Timber.w("⚠️ User trying to remove support but hasn't given any")
                    _events.emit(CommunityPostDetailScreenEvent.ShowMessage("You haven't given support to this post yet."))
                    return@launch
                }

                // Show Megumin sad modal first for confirmation
                _events.emit(CommunityPostDetailScreenEvent.ShowMeguminSadModal)

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error in removeSupport")
                _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Something went wrong. Please try again."))
            }
        }
    }

    fun confirmRemoveSupport() {
        viewModelScope.launch {
            try {
                Timber.d("💔 ==================== CONFIRMING SUPPORT REMOVAL ====================")

                val currentUser = authRepository.getCurrentUser() ?: return@launch

                _uiState.update { it.copy(isGivingSupport = true, error = null) }

                // Optimistically update support count
                val currentPost = _uiState.value.post
                if (currentPost != null) {
                    _uiState.update {
                        it.copy(
                            post = currentPost.copy(
                                supportCount = (currentPost.supportCount - 1).coerceAtLeast(0)
                            ),
                            currentUserSupportType = null
                        )
                    }
                }

                // Remove support via repository
                val result = communityRepository.removeSupport(postId, currentUser.id)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Support removed successfully")
                        _uiState.update {
                            it.copy(
                                isGivingSupport = false,
                                error = null
                            )
                        }
                        _events.emit(CommunityPostDetailScreenEvent.SupportRemoved)
                        _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Support removed"))
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to remove support")

                        // Revert optimistic update
                        if (currentPost != null) {
                            _uiState.update {
                                it.copy(
                                    post = currentPost.copy(supportCount = currentPost.supportCount + 1),
                                    currentUserSupportType = _uiState.value.currentUserSupportType
                                )
                            }
                        }

                        val errorMessage = getErrorMessage(exception)
                        _uiState.update {
                            it.copy(
                                isGivingSupport = false,
                                error = errorMessage
                            )
                        }

                        _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Failed to remove support. Please try again."))
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error confirming support removal")
                _uiState.update {
                    it.copy(
                        isGivingSupport = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun reportPost(reason: String) {
        viewModelScope.launch {
            try {
                Timber.d("🚨 ==================== REPORTING POST ====================")
                Timber.d("🚨 Reporting post: $postId, reason: $reason")

                if (postId.isBlank()) {
                    Timber.e("❌ Invalid post ID")
                    return@launch
                }

                if (reason.isBlank()) {
                    _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Please provide a reason for reporting"))
                    return@launch
                }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot report post - user not authenticated")
                    _uiState.update {
                        it.copy(error = "You must be logged in to report posts")
                    }
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true, error = null) }

                // Report post via repository
                val result = communityRepository.reportPost(postId, currentUser.id, reason)

                result.fold(
                    onSuccess = {
                        Timber.d("✅ Post reported successfully")
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(CommunityPostDetailScreenEvent.PostReported)
                        _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Post reported. Thank you for helping keep our community safe."))
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to report post")
                        val errorMessage = getErrorMessage(exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                        _events.emit(CommunityPostDetailScreenEvent.ShowMessage("Failed to report post. Please try again."))
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error reporting post")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retry() {
        if (postId.isNotBlank()) {
            loadPost(postId)
        }
    }

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    private fun getSupportEmoji(supportType: SupportType): String {
        return when (supportType) {
            SupportType.HEART -> "💝"
            SupportType.HUG -> "🤗"
            SupportType.STRENGTH -> "💪"
            SupportType.HOPE -> "🌟"
        }
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        "Access denied. Please check your permissions or try logging in again."
                    }
                    FirebaseFirestoreException.Code.NOT_FOUND -> {
                        "Content not found."
                    }
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        "Service temporarily unavailable. Please try again."
                    }
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                        "Request timed out. Please check your connection and try again."
                    }
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                        "Please log in to continue."
                    }
                    else -> {
                        "Something went wrong: ${exception.message}"
                    }
                }
            }
            is IllegalArgumentException -> {
                exception.message ?: "Invalid input provided."
            }
            else -> {
                exception.message ?: "An unexpected error occurred. Please try again."
            }
        }
    }
}

// ============================================================================
// UI STATE DATA CLASSES
// ============================================================================

data class CommunityPostDetailUiState(
    val isLoading: Boolean = false,
    val post: CommunityPost? = null,
    val currentUserSupportType: SupportType? = null, // NEW: Track user's current support type
    val isGivingSupport: Boolean = false,
    val error: String? = null
)

// ============================================================================
// SCREEN EVENTS
// ============================================================================

sealed class CommunityPostDetailScreenEvent {
    data object SupportGiven : CommunityPostDetailScreenEvent()
    data object SupportTypeChanged : CommunityPostDetailScreenEvent() // NEW: For support type changes
    data object SupportRemoved : CommunityPostDetailScreenEvent()
    data object PostReported : CommunityPostDetailScreenEvent()
    data object NavigateToLogin : CommunityPostDetailScreenEvent()
    data object ShowMeguminSadModal : CommunityPostDetailScreenEvent() // NEW: Show Megumin sad modal
    data class ShowMessage(val message: String) : CommunityPostDetailScreenEvent()
}