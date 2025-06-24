// File: app/src/main/java/com/dailychaos/project/domain/model/SupportComment.kt
package com.dailychaos.project.domain.model

import kotlinx.datetime.Instant

/**
 * Support Comment Model
 * "Komentar dukungan untuk community posts - like party members cheering each other!"
 */
data class SupportComment(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val anonymousUsername: String,
    val content: String,
    val supportType: SupportType, // Jenis dukungan yang diberikan
    val supportLevel: Int = 1, // 1-5, intensity of support
    val isAnonymous: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,

    // Metadata
    val likeCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val isReported: Boolean = false,
    val isModerated: Boolean = false,

    // Reply system (optional, for future expansion)
    val parentCommentId: String? = null,
    val replyCount: Int = 0
)

/**
 * Support Comment Request untuk creating new comment
 */
data class SupportCommentRequest(
    val postId: String,
    val content: String,
    val supportType: SupportType,
    val supportLevel: Int = 1,
    val isAnonymous: Boolean = true,
    val parentCommentId: String? = null
)