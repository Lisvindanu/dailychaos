package com.dailychaos.project.presentation.ui.screen.chaos.detail

import com.dailychaos.project.domain.model.ChaosEntry

/**
 * UI State for the Chaos Detail Screen.
 */
data class ChaosDetailUiState(
    val entry: ChaosEntry? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val showShareConfirmDialog: Boolean = false,
)

/**
 * UI Events for the Chaos Detail Screen.
 */
sealed class ChaosDetailEvent {
    object Edit : ChaosDetailEvent()
    object Delete : ChaosDetailEvent()
    object ConfirmDelete : ChaosDetailEvent()
    object DismissDeleteDialog : ChaosDetailEvent()
    object Share : ChaosDetailEvent()
    object ConfirmShare : ChaosDetailEvent()
    object DismissShareDialog : ChaosDetailEvent()
    object Retry : ChaosDetailEvent()
}