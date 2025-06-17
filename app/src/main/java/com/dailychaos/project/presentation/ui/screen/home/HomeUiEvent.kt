// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/home/HomeUiEvent.kt
package com.dailychaos.project.presentation.ui.screen.home

/**
 * Home UI Events - Enhanced with Quote System
 *
 * "Event-event yang bisa terjadi di Home Screen"
 */
sealed class HomeUiEvent {
    // Navigation events
    object NavigateToCreateChaos : HomeUiEvent()
    object NavigateToHistory : HomeUiEvent()
    object NavigateToCommunity : HomeUiEvent()
    data class NavigateToEntry(val entryId: String) : HomeUiEvent()

    // Data refresh events
    object Refresh : HomeUiEvent()
    object RetryLoadingEntries : HomeUiEvent()
    object RetryLoadingUser : HomeUiEvent()
    object RetryLoadingAchievements : HomeUiEvent()

    // Error handling
    object ClearError : HomeUiEvent()
    object ClearEntriesError : HomeUiEvent()

    // Quote system events - NEW
    object RefreshQuote : HomeUiEvent()
    object NextQuote : HomeUiEvent()
    object PreviousQuote : HomeUiEvent()
    data class SelectQuoteByCharacter(val character: com.dailychaos.project.util.KonoSubaQuotes.Character) : HomeUiEvent()
    data class SelectQuoteByContext(val context: com.dailychaos.project.util.KonoSubaQuotes.QuoteContext) : HomeUiEvent()

    // Achievement events
    data class UnlockAchievement(val achievementId: String) : HomeUiEvent()

    // Stats events
    object RefreshStats : HomeUiEvent()
    object IncrementTodayEntries : HomeUiEvent()
    object IncrementMiniWins : HomeUiEvent()
}