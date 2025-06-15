// File: app/src/main/java/com/dailychaos/project/data/remote/dto/response/ModerationResponse.kt
package com.dailychaos.project.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Moderation Response DTO
 * "Response untuk moderation actions - keep the party safe and fun!"
 */
@Serializable
data class ModerationResponse(
    val actionId: String,
    val actionType: String, // "report", "block", "appeal", "review"
    val status: String, // "pending", "approved", "rejected", "escalated"
    val targetContentId: String,
    val targetContentType: String,
    val targetUserId: String,
    val reporterId: String,
    val reason: String,
    val severity: String,
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val reviewNotes: String? = null,
    val actionTaken: ModerationActionTakenResponse? = null,
    val createdAt: Long,
    val resolvedAt: Long? = null,
    val escalationLevel: Int = 0
)

@Serializable
data class ModerationActionTakenResponse(
    val action: String, // "no_action", "content_hidden", "user_warned", "user_suspended", "content_removed"
    val duration: String? = null, // "24h", "7d", "30d", "permanent"
    val reason: String,
    val appealable: Boolean,
    val appealDeadline: Long? = null,
    val additionalActions: List<String> = emptyList()
)

/**
 * Content Review Response
 */
@Serializable
data class ContentReviewResponse(
    val contentId: String,
    val contentType: String,
    val reviewStatus: String, // "approved", "rejected", "flagged", "needs_review"
    val confidence: Double, // 0.0 - 1.0
    val flags: List<ContentFlagResponse>,
    val recommendations: List<String>,
    val autoModerated: Boolean,
    val humanReviewRequired: Boolean,
    val reviewedAt: Long
)

@Serializable
data class ContentFlagResponse(
    val flagType: String, // "inappropriate", "spam", "violence", "hate_speech"
    val confidence: Double,
    val description: String,
    val severity: String,
    val autoAction: String? = null
)

/**
 * User Safety Response
 */
@Serializable
data class UserSafetyResponse(
    val userId: String,
    val safetyScore: Double, // 0.0 - 1.0
    val riskLevel: String, // "low", "medium", "high", "critical"
    val blockedUsers: List<BlockedUserResponse>,
    val reportHistory: ReportHistoryResponse,
    val safetySettings: SafetySettingsResponse,
    val recommendations: List<SafetyRecommendationResponse>
)

@Serializable
data class BlockedUserResponse(
    val blockedUserId: String,
    val blockedUsername: String,
    val blockedAt: Long,
    val reason: String,
    val blockType: String,
    val expiresAt: Long? = null
)

@Serializable
data class ReportHistoryResponse(
    val reportsSubmitted: Int,
    val reportsReceived: Int,
    val recentReports: List<ReportSummaryResponse>,
    val reputationScore: Double,
    val trustLevel: String // "new", "trusted", "verified", "flagged"
)

@Serializable
data class ReportSummaryResponse(
    val reportId: String,
    val type: String,
    val status: String,
    val createdAt: Long,
    val resolvedAt: Long? = null,
    val outcome: String? = null
)

@Serializable
data class SafetySettingsResponse(
    val autoBlockSpam: Boolean,
    val requireApprovalForComments: Boolean,
    val hideFromSearch: Boolean,
    val restrictDirectMessages: Boolean,
    val filterProfanity: Boolean,
    val notifyOnReports: Boolean
)

@Serializable
data class SafetyRecommendationResponse(
    val type: String, // "setting", "action", "awareness"
    val title: String,
    val description: String,
    val actionUrl: String? = null,
    val priority: String, // "low", "medium", "high"
    val dismissible: Boolean = true
)