package com.dailychaos.project.presentation.ui.screen.chaos.edit

import com.dailychaos.project.domain.model.ChaosEntry

/**
 * Edit Chaos UI State
 * "State untuk mengedit petualangan chaos!"
 */
data class EditChaosUiState(
    // Loading states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,

    // Original entry
    val originalEntry: ChaosEntry? = null,

    // Form fields
    val title: String = "",
    val description: String = "",
    val chaosLevel: Int = 5,
    val miniWins: List<String> = emptyList(),
    val tags: List<String> = emptyList(),

    // Current input for mini wins and tags
    val currentMiniWin: String = "",
    val currentTag: String = "",

    // Error handling
    val error: String? = null
) {
    // Computed properties
    val isFormValid: Boolean
        get() = title.isNotBlank() && description.length >= 10

    val canSave: Boolean
        get() = isFormValid && !isSaving && !isLoading
}