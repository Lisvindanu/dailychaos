
// File: app/src/main/java/com/dailychaos/project/data/remote/dto/response/CommunityPostResponse.kt
package com.dailychaos.project.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Community Post Response DTO
 * "Response untuk konten di community feed - share the chaos!"
 */
@Serializable
data class CommunityPostResponse(
    val postId: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorProfileImage: String? = null,
    val title: String,
    val content: String,
    val chaosLevel: Int,
    val mood: String,
    val tags: List<String>,
    val isAnonymous: Boolean,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val reactions: PostReactionsResponse,
    val comments: List<CommentResponse> = emptyList(),
    val attachments: List<AttachmentResponse> = emptyList(),
    val location: LocationResponse? = null,
    val visibility: String,
    val moderationStatus: String, // "approved", "pending", "rejected", "flagged"
    val viewCount: Int = 0,
    val shareCount: Int = 0
)

@Serializable
data class PostReactionsResponse(
    val total: Int,
    val breakdown: Map<String, Int>, // "heart" -> 5, "hug" -> 2, etc.
    val userReaction: String? = null // Current user's reaction
)

@Serializable
data class CommentResponse(
    val commentId: String,
    val authorId: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val content: String,
    val isAnonymous: Boolean,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val reactions: CommentReactionsResponse,
    val replies: List<CommentResponse> = emptyList(),
    val parentCommentId: String? = null,
    val supportLevel: Int
)

@Serializable
data class CommentReactionsResponse(
    val total: Int,
    val breakdown: Map<String, Int>,
    val userReaction: String? = null
)

@Serializable
data class AttachmentResponse(
    val id: String,
    val type: String,
    val url: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val thumbnail: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class LocationResponse(
    val city: String,
    val country: String,
    val timezone: String,
    val coordinates: CoordinateResponse? = null
)

@Serializable
data class CoordinateResponse(
    val latitude: Double,
    val longitude: Double
)

/**
 * Community Feed Response
 */
@Serializable
data class CommunityFeedResponse(
    val posts: List<CommunityPostResponse>,
    val pagination: PaginationResponse,
    val filters: FeedFiltersResponse,
    val totalCount: Int,
    val lastRefreshed: Long
)

@Serializable
data class PaginationResponse(
    val currentPage: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val nextPageToken: String? = null
)

@Serializable
data class FeedFiltersResponse(
    val appliedFilters: Map<String, String>,
    val availableFilters: Map<String, List<String>>,
    val sortBy: String,
    val timeRange: String
)