package com.dailychaos.project.presentation.ui.screen.community.feed

import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType

/**
 * Enhanced UI State for the Community Feed Screen
 * "State yang support advanced support operations seperti di detail screen"
 */
data class CommunityFeedUiState(
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * Enhanced UI Events for the Community Feed Screen
 */
sealed class CommunityFeedEvent {
    object Refresh : CommunityFeedEvent()
    object Retry : CommunityFeedEvent()
    object ClearError : CommunityFeedEvent()

    // Support operations
    data class GiveSupport(val postId: String, val type: SupportType) : CommunityFeedEvent()

    // Other operations
    data class ReportPost(val postId: String) : CommunityFeedEvent()
}