package com.dailychaos.project.presentation.ui.screen.community.feed

import com.dailychaos.project.domain.model.CommunityPost

/**
 * UI State for the Community Feed Screen
 */
data class CommunityFeedUiState(
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedSupportType: com.dailychaos.project.domain.model.SupportType? = null,
    val selectedPostId: String? = null
)

/**
 * UI Events for the Community Feed Screen
 */
sealed class CommunityFeedEvent {
    object Refresh : CommunityFeedEvent()
    object Retry : CommunityFeedEvent()
    data class GiveSupport(val postId: String, val type: com.dailychaos.project.domain.model.SupportType) : CommunityFeedEvent()
    data class ReportPost(val postId: String) : CommunityFeedEvent()
}