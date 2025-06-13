package com.dailychaos.project.presentation.ui.screen.chaos.create

/**
 * UI State for the Create Chaos Screen.
 *
 * "Manages all the data needed to craft a new chaos entry."
 */
data class CreateChaosUiState(
    val title: String = "",
    val description: String = "",
    val chaosLevel: Int = 5,
    val miniWins: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val shareToCommunity: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isSavable: Boolean
        get() = title.isNotBlank() && description.length >= 10 && !isSaving
}

/**
 * UI Events for the Create Chaos Screen.
 */
sealed class CreateChaosEvent {
    data class TitleChanged(val title: String) : CreateChaosEvent()
    data class DescriptionChanged(val description: String) : CreateChaosEvent()
    data class ChaosLevelChanged(val level: Int) : CreateChaosEvent()
    data class AddMiniWin(val win: String) : CreateChaosEvent()
    data class RemoveMiniWin(val index: Int) : CreateChaosEvent()
    data class AddTag(val tag: String) : CreateChaosEvent()
    data class RemoveTag(val tag: String) : CreateChaosEvent()
    data class ShareToggled(val share: Boolean) : CreateChaosEvent()
    object SaveChaosEntry : CreateChaosEvent()
    object ClearError : CreateChaosEvent()
}