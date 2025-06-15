// File: app/src/main/java/com/dailychaos/project/data/remote/dto/request/SupportReactionRequest.kt
package com.dailychaos.project.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * Support Reaction Request DTO
 * "Kasih dukungan ke party member - we're in this together!"
 */
@Serializable
data class SupportReactionRequest(
    val targetUserId: String,
    val targetContentId: String, // chaos entry ID atau community post ID
    val targetContentType: String, // "chaos_entry", "community_post", "comment"
    val reactionType: String, // "heart", "hug", "star", "fire", "thumbs_up"
    val message: String? = null, // Optional supportive message
    val isAnonymous: Boolean = false,
    val reactionLevel: Int = 1 // 1-3 untuk intensity of support
)

/**
 * Comment on Content Request
 */
@Serializable
data class CommentRequest(
    val targetContentId: String,
    val targetContentType: String, // "chaos_entry", "community_post"
    val content: String,
    val parentCommentId: String? = null, // For reply to comment
    val isAnonymous: Boolean = false,
    val supportLevel: Int = 1 // 1-5 untuk level dukungan dalam comment
)

/**
 * Share Content Request
 */
@Serializable
data class ShareContentRequest(
    val contentId: String,
    val contentType: String, // "chaos_entry", "community_post"
    val shareType: String, // "community_feed", "external_link"
    val message: String? = null,
    val visibility: String = "public" // "public", "friends", "private"
)
