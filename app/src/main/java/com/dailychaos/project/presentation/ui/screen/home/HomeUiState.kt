package com.dailychaos.project.presentation.ui.screen.home

import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.util.KonoSubaQuotes
import com.dailychaos.project.util.Resource

/**
 * Home UI State
 *
 * "State management untuk Home Screen - seperti status party di guild!"
 */
data class HomeUiState(
    // User data
    val user: User? = null,
    val isUserLoading: Boolean = false,

    // Today's stats
    val todayStats: TodayStats = TodayStats(),
    val isStatsLoading: Boolean = false,

    // Recent chaos entries
    val recentEntries: List<ChaosEntry> = emptyList(),
    val isEntriesLoading: Boolean = false,
    val entriesError: String? = null,

    // Achievements & streaks
    val currentStreak: Int = 0,
    val achievements: List<Achievement> = emptyList(),
    val isAchievementsLoading: Boolean = false,

    // Community highlights
    val communityHighlight: CommunityHighlight? = null,
    val isCommunityLoading: Boolean = false,

    // Daily quote
    val dailyQuote: KonoSubaQuotes.Quote? = null,

    // General state
    val isRefreshing: Boolean = false,
    val generalError: String? = null,
    val lastRefreshTime: Long = 0L
) {
    // Computed properties
    val hasRecentEntries: Boolean get() = recentEntries.isNotEmpty()
    val hasAchievements: Boolean get() = achievements.isNotEmpty()
    val isLoading: Boolean get() = isUserLoading || isStatsLoading || isEntriesLoading || isAchievementsLoading || isCommunityLoading
    val hasError: Boolean get() = entriesError != null || generalError != null
    val errorMessage: String? get() = entriesError ?: generalError
}

/**
 * Today's Statistics
 */
data class TodayStats(
    val entriesCount: Int = 0,
    val miniWinsCount: Int = 0,
    val supportGivenCount: Int = 0,
    val supportReceivedCount: Int = 0,
    val averageChaosLevel: Float = 0f,
    val completedGoals: Int = 0,
    val totalGoals: Int = 0
) {
    val completionPercentage: Int get() = if (totalGoals > 0) (completedGoals * 100) / totalGoals else 0
}

/**
 * Achievement data
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 100,
    val type: AchievementType = AchievementType.GENERAL
) {
    val progressPercentage: Int get() = if (maxProgress > 0) (progress * 100) / maxProgress else 0
}

enum class AchievementType {
    STREAK,
    ENTRIES,
    COMMUNITY,
    SUPPORT,
    GENERAL
}

/**
 * Community highlight post
 */
data class CommunityHighlight(
    val id: String,
    val title: String,
    val content: String,
    val authorName: String,
    val supportCount: Int,
    val chaosLevel: Int,
    val createdAt: Long,
    val tags: List<String> = emptyList()
)

/**
 * UI Events for Home Screen
 */
sealed class HomeUiEvent {
    object Refresh : HomeUiEvent()
    object LoadMore : HomeUiEvent()
    object RetryLoadingEntries : HomeUiEvent()
    object RetryLoadingStats : HomeUiEvent()
    object ClearError : HomeUiEvent()
    data class NavigateToEntry(val entryId: String) : HomeUiEvent()
    object NavigateToCreateChaos : HomeUiEvent()
    object NavigateToHistory : HomeUiEvent()
    object NavigateToCommunity : HomeUiEvent()
    object NavigateToProfile : HomeUiEvent()
    data class ShareEntry(val entryId: String) : HomeUiEvent()
    data class ToggleFavoriteEntry(val entryId: String) : HomeUiEvent()
}