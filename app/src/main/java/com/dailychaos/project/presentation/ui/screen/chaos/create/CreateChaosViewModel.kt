package com.dailychaos.project.presentation.ui.screen.chaos.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateChaosViewModel @Inject constructor(
    // private val createChaosEntryUseCase: CreateChaosEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChaosUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent = _saveSuccessEvent.asSharedFlow()

    fun onEvent(event: CreateChaosEvent) {
        when (event) {
            is CreateChaosEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is CreateChaosEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is CreateChaosEvent.ChaosLevelChanged -> _uiState.update { it.copy(chaosLevel = event.level) }
            is CreateChaosEvent.AddMiniWin -> _uiState.update { it.copy(miniWins = it.miniWins + event.win) }
            is CreateChaosEvent.RemoveMiniWin -> _uiState.update {
                it.copy(miniWins = it.miniWins.toMutableList().apply { removeAt(event.index) })
            }
            is CreateChaosEvent.AddTag -> _uiState.update { it.copy(tags = it.tags + event.tag) }
            is CreateChaosEvent.RemoveTag -> _uiState.update { it.copy(tags = it.tags - event.tag) }
            is CreateChaosEvent.ShareToggled -> _uiState.update { it.copy(shareToCommunity = event.share) }
            is CreateChaosEvent.SaveChaosEntry -> saveEntry()
            is CreateChaosEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveEntry() {
        if (!_uiState.value.isSavable) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            // Mock saving logic
            kotlinx.coroutines.delay(1500)
            // In a real app, call the use case:
            // val result = createChaosEntryUseCase(buildChaosEntryFromState())
            // handle result...
            _uiState.update { it.copy(isSaving = false) }
            _saveSuccessEvent.emit(Unit)
        }
    }
}