// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/chaos/detail/ChaosDetailViewModel.kt
package com.dailychaos.project.presentation.ui.screen.chaos.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.repository.ChaosRepository
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

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ChaosDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chaosRepository: ChaosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val entryId: String = savedStateHandle.get<String>("entryId") ?: ""

    // ENHANCED: Check if this is a community post navigation
    private val isFromCommunity: Boolean = savedStateHandle.get<Boolean>("isFromCommunity") ?: false

    private val _uiState = MutableStateFlow(ChaosDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DetailScreenEvent>()
    val events = _events.asSharedFlow()

    init {
        Timber.d("🔍 ==================== CHAOS DETAIL SCREEN INITIALIZED ====================")
        Timber.d("🔍 Entry ID from navigation: '$entryId'")
        Timber.d("🔍 Is from community: $isFromCommunity")

        if (entryId.isBlank()) {
            Timber.e("❌ Entry ID is blank - cannot proceed")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Invalid entry ID: Entry ID cannot be empty"
                )
            }
        } else {
            loadEntry()
        }
    }

    fun onEvent(event: ChaosDetailEvent) {
        when (event) {
            is ChaosDetailEvent.Delete -> {
                Timber.d("🗑️ Delete button pressed")
                if (isFromCommunity) {
                    Timber.w("⚠️ Cannot delete community posts")
                    _uiState.update {
                        it.copy(error = "Community posts cannot be deleted")
                    }
                } else {
                    _uiState.update { it.copy(showDeleteConfirmDialog = true) }
                }
            }
            is ChaosDetailEvent.ConfirmDelete -> {
                Timber.d("🗑️ Delete confirmed")
                deleteEntry()
            }
            is ChaosDetailEvent.DismissDeleteDialog -> {
                Timber.d("🗑️ Delete dialog dismissed")
                _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            }
            is ChaosDetailEvent.Share -> {
                Timber.d("📤 Share button pressed")
                if (isFromCommunity) {
                    Timber.w("⚠️ Community posts are already shared")
                    _uiState.update {
                        it.copy(error = "This post is already shared in the community")
                    }
                } else {
                    _uiState.update { it.copy(showShareConfirmDialog = true) }
                }
            }
            is ChaosDetailEvent.ConfirmShare -> {
                Timber.d("📤 Share confirmed")
                shareEntry()
            }
            is ChaosDetailEvent.DismissShareDialog -> {
                Timber.d("📤 Share dialog dismissed")
                _uiState.update { it.copy(showShareConfirmDialog = false) }
            }
            is ChaosDetailEvent.Retry -> {
                Timber.d("🔄 Retry loading entry")
                loadEntry()
            }
            is ChaosDetailEvent.Edit -> {
                Timber.d("✏️ Edit button pressed - navigation handled in UI")
                if (isFromCommunity) {
                    Timber.w("⚠️ Cannot edit community posts")
                    _uiState.update {
                        it.copy(error = "Community posts cannot be edited")
                    }
                }
            }
        }
    }

    private fun loadEntry() {
        viewModelScope.launch {
            try {
                Timber.d("📚 ==================== LOADING CHAOS ENTRY DETAIL ====================")
                Timber.d("📚 Loading entry with ID: '$entryId'")
                Timber.d("📚 Is from community: $isFromCommunity")

                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    val error = "User not authenticated - cannot load entry"
                    Timber.e("❌ $error")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                    return@launch
                }

                Timber.d("✅ Current user found: ${currentUser.id}")

                if (isFromCommunity) {
                    // ENHANCED: Load from community feed
                    loadCommunityPost()
                } else {
                    // ORIGINAL: Load from user's personal chaos entries
                    loadPersonalChaosEntry(currentUser.id)
                }

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error loading chaos entry")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadPersonalChaosEntry(userId: String) {
        Timber.d("👤 Loading personal chaos entry for user: $userId")

        chaosRepository.getChaosEntry(userId, entryId)
            .catch { exception ->
                Timber.e(exception, "❌ Error loading personal chaos entry from repository")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load entry: ${exception.message}"
                    )
                }
            }
            .collect { entry ->
                handleLoadedEntry(entry, "personal chaos entry")
            }
    }

    private suspend fun loadCommunityPost() {
        Timber.d("🌍 Loading community post with ID: $entryId")

        // TODO: Implement community post loading
        // For now, show appropriate error
        _uiState.update {
            it.copy(
                isLoading = false,
                error = "Community post loading not yet implemented. Entry ID: $entryId"
            )
        }

        // Future implementation:
        // communityRepository.getCommunityPost(entryId)
        //     .catch { exception -> ... }
        //     .collect { post -> ... }
    }

    private fun handleLoadedEntry(entry: ChaosEntry?, entryType: String) {
        if (entry != null) {
            Timber.d("✅ $entryType loaded successfully:")
            Timber.d("  - ID: ${entry.id}")
            Timber.d("  - Title: ${entry.title}")
            Timber.d("  - Description length: ${entry.description.length}")
            Timber.d("  - Chaos Level: ${entry.chaosLevel}")
            Timber.d("  - Mini Wins: ${entry.miniWins.size}")
            Timber.d("  - Tags: ${entry.tags}")
            Timber.d("  - Created At: ${entry.createdAt}")
            Timber.d("  - Shared to Community: ${entry.isSharedToCommunity}")

            _uiState.update {
                it.copy(
                    isLoading = false,
                    entry = entry,
                    error = null
                )
            }
        } else {
            val error = "$entryType not found with ID: $entryId"
            Timber.e("❌ $error")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = error
                )
            }
        }
    }

    private fun deleteEntry() {
        if (isFromCommunity) {
            Timber.w("⚠️ Cannot delete community posts")
            return
        }

        viewModelScope.launch {
            try {
                Timber.d("🗑️ ==================== DELETING CHAOS ENTRY ====================")

                val entry = _uiState.value.entry
                if (entry == null) {
                    Timber.e("❌ Cannot delete - no entry loaded")
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        isLoading = true
                    )
                }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot delete - user not authenticated")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                    return@launch
                }

                Timber.d("🗑️ Deleting entry: ${entry.id} for user: ${currentUser.id}")

                // Delete from repository
                val deleteResult = chaosRepository.deleteChaosEntry(currentUser.id, entry.id)

                deleteResult.fold(
                    onSuccess = {
                        Timber.d("✅ Entry deleted successfully")
                        _events.emit(DetailScreenEvent.DeleteSuccess)
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to delete entry")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to delete entry: ${exception.message}"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error deleting entry")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun shareEntry() {
        if (isFromCommunity) {
            Timber.w("⚠️ Community posts are already shared")
            return
        }

        viewModelScope.launch {
            try {
                Timber.d("📤 ==================== SHARING CHAOS ENTRY ====================")

                val entry = _uiState.value.entry
                if (entry == null) {
                    Timber.e("❌ Cannot share - no entry loaded")
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        showShareConfirmDialog = false,
                        isLoading = true
                    )
                }

                // Get current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Cannot share - user not authenticated")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                    return@launch
                }

                Timber.d("📤 Sharing entry: ${entry.id} for user: ${currentUser.id}")

                // Update entry to mark as shared
                val updatedEntry = entry.copy(isSharedToCommunity = true)

                val updateResult = chaosRepository.updateChaosEntry(currentUser.id, updatedEntry)

                updateResult.fold(
                    onSuccess = {
                        Timber.d("✅ Entry shared successfully")
                        _events.emit(DetailScreenEvent.ShareSuccess)

                        // Update local state to reflect the change
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                entry = updatedEntry
                            )
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Failed to share entry")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to share entry: ${exception.message}"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error sharing entry")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    // Helper function to refresh entry data
    fun refreshEntry() {
        Timber.d("🔄 Manual refresh triggered")
        loadEntry()
    }

    // Clear error state
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    sealed class DetailScreenEvent {
        object DeleteSuccess : DetailScreenEvent()
        object ShareSuccess : DetailScreenEvent()
    }
}