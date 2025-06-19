// File: app/src/main/java/com/dailychaos/project/domain/repository/CommunityRepository.kt
package com.dailychaos.project.domain.repository

import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import kotlinx.coroutines.flow.Flow

/**
 * Community Repository Interface
 * "Define community operations untuk sharing dan support!"
 */
interface CommunityRepository {

    // Community Feed operations
    fun getAllCommunityPosts(): Flow<List<CommunityPost>>
    fun getRecentCommunityPosts(limit: Int = 20): Flow<List<CommunityPost>>
    fun getCommunityPostsByTags(tags: List<String>): Flow<List<CommunityPost>>

    // Individual post operations
    fun getCommunityPost(postId: String): Flow<CommunityPost?>
    suspend fun createCommunityPost(post: CommunityPost): Result<String>
    suspend fun updateCommunityPost(postId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun deleteCommunityPost(postId: String): Result<Unit>

    // Support and interaction operations
    suspend fun giveSupport(postId: String, userId: String, supportType: SupportType): Result<Unit>
    suspend fun removeSupport(postId: String, userId: String): Result<Unit>
    suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit>

    // Chaos Twins operations
    fun findChaosTwins(userId: String, tags: List<String>, chaosLevel: Int): Flow<List<CommunityPost>>

    // Statistics
    suspend fun getCommunityStats(): Result<Map<String, Int>>
}