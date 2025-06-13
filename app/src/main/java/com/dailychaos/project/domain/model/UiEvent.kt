package com.dailychaos.project.domain.model

/**
 * UI Event - Event untuk UI interactions
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class ShowDialog(val title: String, val message: String) : UiEvent()
    object HideDialog : UiEvent()
    data class ShowToast(val message: String) : UiEvent()
    object RefreshData : UiEvent()
}