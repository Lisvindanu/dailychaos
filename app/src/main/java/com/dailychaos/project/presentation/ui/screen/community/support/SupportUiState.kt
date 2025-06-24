// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/support/SupportUiState.kt
package com.dailychaos.project.presentation.ui.screen.community.support

import com.dailychaos.project.domain.model.SupportComment
import com.dailychaos.project.domain.model.SupportType

/**
 * Support Screen UI State
 * "State untuk comment support system pada community posts"
 */
data class SupportUiState(
    // Comments data
    val comments: List<SupportComment> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // New comment creation
    val commentText: String = "",
    val selectedSupportType: SupportType = SupportType.HEART,
    val selectedSupportLevel: Int = 1,
    val isPostingComment: Boolean = false,
    val isAnonymous: Boolean = true,

    // Comment statistics
    val totalComments: Int = 0,
    val supportTypeBreakdown: Map<SupportType, Int> = emptyMap(),

    // UI state
    val showCommentDialog: Boolean = false,
    val expandedCommentId: String? = null
)

/**
 * Support Screen Events
 */
sealed class SupportEvent {
    // Screen lifecycle
    object LoadComments : SupportEvent()
    object RefreshComments : SupportEvent()
    object ClearError : SupportEvent()

    // Comment creation
    data class UpdateCommentText(val text: String) : SupportEvent()
    data class SelectSupportType(val type: SupportType) : SupportEvent()
    data class SelectSupportLevel(val level: Int) : SupportEvent()
    data class ToggleAnonymous(val isAnonymous: Boolean) : SupportEvent()
    object ShowCommentDialog : SupportEvent()
    object HideCommentDialog : SupportEvent()
    object PostComment : SupportEvent()

    // Comment interactions
    data class LikeComment(val commentId: String) : SupportEvent()
    data class ReportComment(val commentId: String, val reason: String) : SupportEvent()
    data class ExpandComment(val commentId: String) : SupportEvent()
    data class CollapseComment(val commentId: String) : SupportEvent()

    // Reply system (for future)
    data class ReplyToComment(val parentCommentId: String) : SupportEvent()
}