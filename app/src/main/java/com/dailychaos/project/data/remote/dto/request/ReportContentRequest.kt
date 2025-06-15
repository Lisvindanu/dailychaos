// File: app/src/main/java/com/dailychaos/project/data/remote/dto/request/ReportContentRequest.kt
package com.dailychaos.project.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * Report Content Request DTO
 * "Laporin konten yang gak sesuai - keep the party safe!"
 */
@Serializable
data class ReportContentRequest(
    val reportedContentId: String,
    val reportedContentType: String, // "chaos_entry", "community_post", "comment", "user_profile"
    val reportedUserId: String,
    val reportReason: String, // "spam", "harassment", "inappropriate", "violence", "hate_speech", "other"
    val description: String,
    val severity: String, // "low", "medium", "high", "critical"
    val evidence: List<EvidenceData> = emptyList(),
    val additionalInfo: Map<String, String> = emptyMap()
)

@Serializable
data class EvidenceData(
    val type: String, // "screenshot", "text", "url"
    val content: String,
    val timestamp: Long,
    val description: String? = null
)

/**
 * Block User Request
 */
@Serializable
data class BlockUserRequest(
    val blockedUserId: String,
    val reason: String, // "harassment", "spam", "inappropriate", "personal"
    val blockType: String = "full", // "full", "comments_only", "posts_only"
    val duration: String? = null // "permanent", "temporary_7d", "temporary_30d"
)

/**
 * Appeal Report Request
 */
@Serializable
data class AppealReportRequest(
    val reportId: String,
    val appealReason: String,
    val explanation: String,
    val evidence: List<EvidenceData> = emptyList()
)