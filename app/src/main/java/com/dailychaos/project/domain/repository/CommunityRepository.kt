// File: app/src/main/java/com/dailychaos/project/domain/repository/CommunityRepository.kt
package com.dailychaos.project.domain.repository

import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportComment
import com.dailychaos.project.domain.model.SupportCommentRequest
import com.dailychaos.project.domain.model.SupportType
import kotlinx.coroutines.flow.Flow

/**
 * Community Repository Interface - Enhanced dengan support type checking
 * "Repository interface untuk community features dengan support system yang enhanced"
 */
interface CommunityRepository {

    // ============================================================================
    // COMMUNITY FEED OPERATIONS
    // ============================================================================

    /**
     * Get all community posts ordered by creation date
     */
    fun getAllCommunityPosts(): Flow<List<CommunityPost>>

    /**
     * Get recent community posts with limit
     */
    fun getRecentCommunityPosts(limit: Int = 50): Flow<List<CommunityPost>>

    /**
     * Get community posts filtered by tags
     */
    fun getCommunityPostsByTags(tags: List<String>): Flow<List<CommunityPost>>

    // ============================================================================
    // INDIVIDUAL POST OPERATIONS
    // ============================================================================

    /**
     * Get a specific community post by ID
     */
    fun getCommunityPost(postId: String): Flow<CommunityPost?>

    /**
     * Create a new community post
     */
    suspend fun createCommunityPost(post: CommunityPost): Result<String>

    /**
     * Update an existing community post
     */
    suspend fun updateCommunityPost(postId: String, updates: Map<String, Any>): Result<Unit>

    /**
     * Delete a community post
     */
    suspend fun deleteCommunityPost(postId: String): Result<Unit>

    // ============================================================================
    // SUPPORT AND INTERACTION OPERATIONS - ENHANCED
    // ============================================================================

    /**
     * Give support to a community post
     * Enhanced: Bisa ganti tipe support kalau udah kasih support sebelumnya
     */
    suspend fun giveSupport(postId: String, userId: String, supportType: SupportType): Result<Unit>

    /**
     * Remove support from a community post
     */
    suspend fun removeSupport(postId: String, userId: String): Result<Unit>

    // ============================================================================
    // REPORT OPERATIONS
    // ============================================================================

    /**
     * Report a community post for inappropriate content
     */
    suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit>

    // ============================================================================
    // CHAOS TWINS OPERATIONS
    // ============================================================================

    /**
     * Find chaos twins - posts with similar tags and chaos level
     */
    fun findChaosTwins(userId: String, tags: List<String>, chaosLevel: Int): Flow<List<CommunityPost>>

    // ============================================================================
    // STATISTICS
    // ============================================================================

    /**
     * Get community statistics
     */
    suspend fun getCommunityStats(): Result<Map<String, Int>>
}

/**
 * Extended interface untuk implementation-specific methods
 * Ini untuk method yang cuma ada di implementation, bukan di interface utama
 */
interface CommunityRepositoryExtended : CommunityRepository {

    // ============================================================================
    // ENHANCED SUPPORT OPERATIONS
    // ============================================================================

    /**
     * Get user's current support type for a specific post
     * Returns null if user hasn't given support
     */
    suspend fun getUserSupportType(postId: String, userId: String): SupportType?

    /**
     * 🚨 NEW: Check if user is trying to give the same support type
     * Useful for ViewModel confirmation logic
     */
    suspend fun isSameSupportType(postId: String, userId: String, supportType: SupportType): Boolean

    // ============================================================================
    // REPORT OPERATIONS
    // ============================================================================

    /**
     * Report a community post
     */
    override suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit>

    // ============================================================================
    // COMMENT OPERATIONS - NEW
    // ============================================================================

    /**
     * Get comments for a specific post
     */
    fun getPostComments(postId: String): Flow<List<SupportComment>>

    /**
     * Post a new comment
     */
    suspend fun postComment(commentRequest: SupportCommentRequest): Result<String>

    /**
     * Like/unlike a comment
     */
    suspend fun likeComment(commentId: String, userId: String): Result<Unit>

    /**
     * Report a comment
     */
    suspend fun reportComment(commentId: String, userId: String, reason: String): Result<Unit>

    /**
     * Delete a comment (only owner or admin)
     */
    suspend fun deleteComment(commentId: String, userId: String): Result<Unit>

    /**
     * Get comment statistics for a post
     */
    suspend fun getCommentStats(postId: String): Result<Map<SupportType, Int>>
}
