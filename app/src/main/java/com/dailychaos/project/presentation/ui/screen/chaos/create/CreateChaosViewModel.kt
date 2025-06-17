package com.dailychaos.project.presentation.ui.screen.chaos.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.SyncStatus
import com.dailychaos.project.domain.usecase.chaos.CreateChaosEntryUseCase
import com.dailychaos.project.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

/**
 * CreateChaosViewModel - Enhanced dengan Debug Logging
 *
 * "ViewModel untuk membuat chaos entry baru dengan debugging authentication!"
 */
@HiltViewModel
class CreateChaosViewModel @Inject constructor(
    private val createChaosEntryUseCase: CreateChaosEntryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChaosUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<String>() // Emit entryId on success
    val saveSuccessEvent = _saveSuccessEvent.asSharedFlow()

    private val _navigateBackEvent = MutableSharedFlow<Unit>()
    val navigateBackEvent = _navigateBackEvent.asSharedFlow()

    init {
        // Debug authentication status on init
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                val isAuthenticated = authRepository.isAuthenticated()
                val currentUser = authRepository.getCurrentUser()

                Timber.d("🔐 Authentication Status Check:")
                Timber.d("  - Is Authenticated: $isAuthenticated")
                Timber.d("  - Current User: ${currentUser?.id ?: "NULL"}")
                Timber.d("  - User Display Name: ${currentUser?.displayName ?: "NULL"}")
                Timber.d("  - User Email: ${currentUser?.email ?: "NULL"}")
                Timber.d("  - Is Anonymous: ${currentUser?.isAnonymous ?: "NULL"}")

                if (!isAuthenticated || currentUser == null) {
                    _uiState.update {
                        it.copy(error = "⚠️ Authentication Required: Please login first to create chaos entries")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error checking authentication status")
                _uiState.update {
                    it.copy(error = "Authentication check failed: ${e.message}")
                }
            }
        }
    }

    fun onEvent(event: CreateChaosEvent) {
        when (event) {
            is CreateChaosEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.title, error = null) }
            }
            is CreateChaosEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description, error = null) }
            }
            is CreateChaosEvent.ChaosLevelChanged -> {
                _uiState.update { it.copy(chaosLevel = event.level) }
            }
            is CreateChaosEvent.AddMiniWin -> {
                if (event.win.isNotBlank() && !_uiState.value.miniWins.contains(event.win)) {
                    _uiState.update { it.copy(miniWins = it.miniWins + event.win.trim()) }
                }
            }
            is CreateChaosEvent.RemoveMiniWin -> {
                val currentMiniWins = _uiState.value.miniWins.toMutableList()
                if (event.index in currentMiniWins.indices) {
                    currentMiniWins.removeAt(event.index)
                    _uiState.update { it.copy(miniWins = currentMiniWins) }
                }
            }
            is CreateChaosEvent.AddTag -> {
                val tag = event.tag.trim().lowercase()
                if (tag.isNotBlank() && !_uiState.value.tags.contains(tag)) {
                    _uiState.update { it.copy(tags = it.tags + tag) }
                }
            }
            is CreateChaosEvent.RemoveTag -> {
                _uiState.update { it.copy(tags = it.tags - event.tag) }
            }
            is CreateChaosEvent.ShareToggled -> {
                _uiState.update { it.copy(shareToCommunity = event.share) }
            }
            is CreateChaosEvent.SaveChaosEntry -> {
                saveEntry()
            }
            is CreateChaosEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun saveEntry() {
        val currentState = _uiState.value

        Timber.d("🎯 Save Entry Started")
        Timber.d("  - Title: '${currentState.title}'")
        Timber.d("  - Description length: ${currentState.description.length}")
        Timber.d("  - Chaos Level: ${currentState.chaosLevel}")
        Timber.d("  - Is Savable: ${currentState.isSavable}")

        // Validation
        if (!currentState.isSavable) {
            val error = "Please fill in all required fields properly"
            Timber.w("❌ Validation failed: $error")
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                // Check authentication before saving
                val isAuthenticated = authRepository.isAuthenticated()
                val currentUser = authRepository.getCurrentUser()

                Timber.d("🔐 Pre-save Authentication Check:")
                Timber.d("  - Is Authenticated: $isAuthenticated")
                Timber.d("  - Current User ID: ${currentUser?.id ?: "NULL"}")

                if (!isAuthenticated || currentUser == null) {
                    val error = "You must be logged in to create chaos entries"
                    Timber.e("❌ Authentication failed during save")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error
                        )
                    }
                    return@launch
                }

                // Build ChaosEntry from current state
                val chaosEntry = buildChaosEntryFromState(currentState)

                Timber.d("📝 Created ChaosEntry:")
                Timber.d("  - ID: '${chaosEntry.id}'")
                Timber.d("  - UserID: '${chaosEntry.userId}'")
                Timber.d("  - Title: '${chaosEntry.title}'")
                Timber.d("  - Description: '${chaosEntry.description.take(50)}...'")

                // Call UseCase
                Timber.d("🚀 Calling CreateChaosEntryUseCase...")
                val result = createChaosEntryUseCase(chaosEntry)

                result.fold(
                    onSuccess = { entryId ->
                        Timber.d("✅ Chaos entry created successfully!")
                        Timber.d("  - Entry ID: $entryId")
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = null
                            )
                        }
                        _saveSuccessEvent.emit(entryId)
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "❌ Error creating chaos entry")
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = "Failed to save: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "💥 Unexpected error in saveEntry")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "An unexpected error occurred: ${e.message}"
                    )
                }
            }
        }
    }

    private fun buildChaosEntryFromState(state: CreateChaosUiState): ChaosEntry {
        val now = Clock.System.now()

        val entry = ChaosEntry(
            id = "", // Will be generated by Firestore
            userId = "", // Will be set by UseCase from AuthRepository
            title = state.title.trim(),
            description = state.description.trim(),
            chaosLevel = state.chaosLevel,
            miniWins = state.miniWins.map { it.trim() }.filter { it.isNotBlank() },
            tags = state.tags.map { it.trim().lowercase() }.filter { it.isNotBlank() },
            createdAt = now,
            updatedAt = now,
            isSharedToCommunity = state.shareToCommunity,
            syncStatus = SyncStatus.PENDING, // Will be updated to SYNCED after successful upload
            localId = 0L // Not applicable for new entries
        )

        Timber.d("🏗️ Built ChaosEntry from state:")
        Timber.d("  - Title: '${entry.title}'")
        Timber.d("  - Description length: ${entry.description.length}")
        Timber.d("  - Chaos Level: ${entry.chaosLevel}")
        Timber.d("  - Mini Wins: ${entry.miniWins}")
        Timber.d("  - Tags: ${entry.tags}")
        Timber.d("  - Share to Community: ${entry.isSharedToCommunity}")

        return entry
    }

    fun resetForm() {
        _uiState.value = CreateChaosUiState()
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigateBackEvent.emit(Unit)
        }
    }

    // Add method to trigger authentication check manually
    fun recheckAuthentication() {
        checkAuthenticationStatus()
    }

    // Utility functions for better UX
    fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "Title cannot be empty"
            title.length < 3 -> "Title must be at least 3 characters"
            title.length > 100 -> "Title must be less than 100 characters"
            else -> null
        }
    }

    fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Description cannot be empty"
            description.length < 10 -> "Description must be at least 10 characters"
            description.length > 1000 -> "Description must be less than 1000 characters"
            else -> null
        }
    }

    fun validateChaosLevel(level: Int): String? {
        return when {
            level < 1 -> "Chaos level must be at least 1"
            level > 10 -> "Chaos level cannot exceed 10"
            else -> null
        }
    }
}