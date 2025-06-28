package com.dailychaos.project.presentation.ui.screen.chaos.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.usecase.chaos.GetChaosEntryUseCase
import com.dailychaos.project.domain.usecase.chaos.UpdateChaosEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Edit Chaos ViewModel
 * "Mengedit petualangan chaos yang sudah tertulis!"
 */
@HiltViewModel
class EditChaosViewModel @Inject constructor(
    private val getChaosEntryUseCase: GetChaosEntryUseCase,
    private val updateChaosEntryUseCase: UpdateChaosEntryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get entryId from navigation arguments
    private val entryId: String = checkNotNull(savedStateHandle["entryId"]) {
        "Entry ID is required for EditChaosScreen"
    }

    private val _uiState = MutableStateFlow(EditChaosUiState())
    val uiState: StateFlow<EditChaosUiState> = _uiState.asStateFlow()

    init {
        loadChaosEntry()
    }

    private fun loadChaosEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getChaosEntryUseCase(entryId)
                .catch { e ->
                    Timber.e(e, "Error loading chaos entry")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load chaos entry: ${e.message}"
                        )
                    }
                }
                .collect { entry ->
                    if (entry != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                originalEntry = entry,
                                title = entry.title,
                                description = entry.description,
                                chaosLevel = entry.chaosLevel,
                                miniWins = entry.miniWins,
                                tags = entry.tags,
                                currentMiniWin = "",
                                currentTag = "",
                                error = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Chaos entry not found"
                            )
                        }
                    }
                }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateChaosLevel(level: Int) {
        _uiState.update { it.copy(chaosLevel = level) }
    }

    fun updateCurrentMiniWin(miniWin: String) {
        _uiState.update { it.copy(currentMiniWin = miniWin) }
    }

    fun addMiniWin() {
        val miniWin = _uiState.value.currentMiniWin.trim()
        if (miniWin.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    miniWins = it.miniWins + miniWin,
                    currentMiniWin = ""
                )
            }
        }
    }

    fun removeMiniWin(index: Int) {
        _uiState.update {
            it.copy(miniWins = it.miniWins.filterIndexed { i, _ -> i != index })
        }
    }

    fun updateCurrentTag(tag: String) {
        _uiState.update { it.copy(currentTag = tag) }
    }

    fun addTag() {
        val tag = _uiState.value.currentTag.trim()
        if (tag.isNotEmpty() && !_uiState.value.tags.contains(tag)) {
            _uiState.update {
                it.copy(
                    tags = it.tags + tag,
                    currentTag = ""
                )
            }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update {
            it.copy(tags = it.tags.filter { it != tag })
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        val originalEntry = state.originalEntry ?: return

        // Validation
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title tidak boleh kosong!") }
            return
        }

        if (state.description.length < 10) {
            _uiState.update { it.copy(error = "Deskripsi minimal 10 karakter!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            // Create updated entry
            val updatedEntry = originalEntry.copy(
                title = state.title,
                description = state.description,
                chaosLevel = state.chaosLevel,
                miniWins = state.miniWins,
                tags = state.tags
            )

            updateChaosEntryUseCase(updatedEntry).fold(
                onSuccess = {
                    Timber.d("Chaos entry updated successfully")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true
                        )
                    }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to update chaos entry")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Failed to update: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value
        val original = state.originalEntry ?: return false

        return state.title != original.title ||
                state.description != original.description ||
                state.chaosLevel != original.chaosLevel ||
                state.miniWins != original.miniWins ||
                state.tags != original.tags
    }
}