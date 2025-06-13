package com.dailychaos.project.presentation.ui.screen.chaos.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.ChaosEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ChaosDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // private val getChaosEntryUseCase: GetChaosEntryUseCase,
    // private val deleteChaosEntryUseCase: DeleteChaosEntryUseCase,
    // private val shareChaosEntryUseCase: ShareChaosEntryUseCase
) : ViewModel() {

    private val entryId: String = savedStateHandle.get<String>("entryId") ?: ""

    private val _uiState = MutableStateFlow(ChaosDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DetailScreenEvent>()
    val events = _events.asSharedFlow()

    init {
        loadEntry()
    }

    fun onEvent(event: ChaosDetailEvent) {
        when (event) {
            is ChaosDetailEvent.Delete -> _uiState.update { it.copy(showDeleteConfirmDialog = true) }
            is ChaosDetailEvent.ConfirmDelete -> deleteEntry()
            is ChaosDetailEvent.DismissDeleteDialog -> _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            is ChaosDetailEvent.Share -> _uiState.update { it.copy(showShareConfirmDialog = true) }
            is ChaosDetailEvent.ConfirmShare -> shareEntry()
            is ChaosDetailEvent.DismissShareDialog -> _uiState.update { it.copy(showShareConfirmDialog = false) }
            is ChaosDetailEvent.Retry -> loadEntry()
            is ChaosDetailEvent.Edit -> { /* Navigation handled in UI */ }
        }
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            kotlinx.coroutines.delay(500) // Mock loading
            _uiState.update {
                it.copy(
                    isLoading = false,
                    entry = createMockEntry()
                )
            }
        }
    }

    private fun deleteEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteConfirmDialog = false, isLoading = true) }
            kotlinx.coroutines.delay(1000) // Mock delete
            _events.emit(DetailScreenEvent.DeleteSuccess)
        }
    }

    private fun shareEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(showShareConfirmDialog = false, isLoading = true) }
            kotlinx.coroutines.delay(1000) // Mock share
            _events.emit(DetailScreenEvent.ShareSuccess)
            loadEntry() // Reload to show updated shared status
        }
    }

    private fun createMockEntry(): ChaosEntry = ChaosEntry(
        id = entryId,
        title = "The Great Slime Incident",
        description = "Today was an absolute disaster, much like that one time Aqua tried to purify a lake full of alligators thinking they were cute turtles. I attempted to refactor a legacy module, and it ended up spawning bugs in every corner of the application. I feel like I just cast a curse on our own codebase. At least I didn't cry as much as Aqua... I think.",
        chaosLevel = 8,
        createdAt = Clock.System.now(),
        miniWins = listOf("Managed to revert before anyone noticed", "Drank coffee", "Didn't flip the table"),
        tags = listOf("work", "coding", "disaster", "technical-debt"),
        isSharedToCommunity = Random.nextBoolean()
    )

    sealed class DetailScreenEvent {
        object DeleteSuccess : DetailScreenEvent()
        object ShareSuccess : DetailScreenEvent()
    }
}