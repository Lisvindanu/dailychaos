// File: app/src/main/java/com/dailychaos/project/data/repository/CommunityRepositoryImpl.kt
package com.dailychaos.project.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.dailychaos.project.data.mapper.toCommunityPost
import com.dailychaos.project.data.mapper.toFirestoreMap
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.domain.repository.CommunityRepository
import com.dailychaos.project.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Community Repository Implementation - Complete
 * "Implementasi lengkap untuk semua community operations dengan Firestore"
 */
@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommunityRepository {

    companion object {
        // Use Constants for consistency across the app
        private const val COLLECTION_SUPPORT_REACTIONS = "support_reactions"
    }

    // ============================================================================
    // COMMUNITY FEED OPERATIONS
    // ============================================================================

    override fun getAllCommunityPosts(): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting all community posts from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                Timber.d("📄 Retrieved ${snapshot.documents.size} documents from Firestore")
                val posts = snapshot.documents.mapNotNull { document ->
                    try {
                        Timber.d("🔄 Processing document: ${document.id}")
                        document.data?.let { data ->
                            Timber.d("📋 Document data keys: ${data.keys}")
                            data.toCommunityPost()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document to CommunityPost: ${document.id}")
                        null
                    }
                }
                Timber.d("✅ Successfully converted ${posts.size} community posts")
                posts
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community posts from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(emptyList())
            }
    }

    override fun getRecentCommunityPosts(limit: Int): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting recent community posts (limit: $limit) from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                Timber.d("📄 Retrieved ${snapshot.documents.size} recent documents from Firestore")
                val posts = snapshot.documents.mapNotNull { document ->
                    try {
                        Timber.d("🔄 Processing recent document: ${document.id}")
                        document.data?.let { data ->
                            Timber.d("📋 Document data keys: ${data.keys}")
                            data.toCommunityPost()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error converting document to CommunityPost: ${document.id}")
                        null
                    }
                }
                Timber.d("✅ Successfully converted ${posts.size} recent community posts")
                posts
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting recent community posts from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(emptyList())
            }
    }

    override fun getCommunityPostsByTags(tags: List<String>): Flow<List<CommunityPost>> {
        Timber.d("🌍 Getting community posts by tags: $tags from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .whereArrayContainsAny("tags", tags)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                val posts = snapshot.documents.mapNotNull { document ->
                    try {
                        document.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "Error converting document to CommunityPost: ${document.id}")
                        null
                    }
                }
                Timber.d("🌍 Retrieved ${posts.size} community posts with tags: $tags from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                posts
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community posts by tags from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(emptyList())
            }
    }

    // ============================================================================
    // INDIVIDUAL POST OPERATIONS
    // ============================================================================

    override fun getCommunityPost(postId: String): Flow<CommunityPost?> {
        Timber.d("🌍 Getting community post: $postId from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .document(postId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    try {
                        snapshot.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "Error converting document to CommunityPost: $postId")
                        null
                    }
                } else {
                    Timber.w("Community post not found: $postId")
                    null
                }
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error getting community post: $postId from ${Constants.COLLECTION_COMMUNITY_POSTS}")
                emit(null)
            }
    }

    override suspend fun createCommunityPost(post: CommunityPost): Result<String> {
        return try {
            Timber.d("🌍 ==================== CREATING COMMUNITY POST ====================")
            Timber.d("🌍 Creating community post in collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Timber.d("  - Title: ${post.title}")
            Timber.d("  - Description length: ${post.description.length}")
            Timber.d("  - Chaos Level: ${post.chaosLevel}")
            Timber.d("  - Anonymous Username: ${post.anonymousUsername}")
            Timber.d("  - Tags: ${post.tags}")

            val postMap = post.toFirestoreMap().toMutableMap()
            val documentRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document()

            // Add the auto-generated ID to the map
            postMap["id"] = documentRef.id

            documentRef.set(postMap).await()

            Timber.d("✅ Community post created successfully with ID: ${documentRef.id} in ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Result.success(documentRef.id)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating community post in ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Result.failure(e)
        }
    }

    override suspend fun updateCommunityPost(postId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            Timber.d("🌍 Updating community post: $postId in collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Timber.d("  - Updates: $updates")

            firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .document(postId)
                .update(updates)
                .await()

            Timber.d("✅ Community post updated successfully: $postId in ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating community post: $postId in ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Result.failure(e)
        }
    }

    override suspend fun deleteCommunityPost(postId: String): Result<Unit> {
        return try {
            Timber.d("🌍 Deleting community post: $postId from collection: ${Constants.COLLECTION_COMMUNITY_POSTS}")

            firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .document(postId)
                .delete()
                .await()

            Timber.d("✅ Community post deleted successfully: $postId from ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting community post: $postId from ${Constants.COLLECTION_COMMUNITY_POSTS}")
            Result.failure(e)
        }
    }

    // ============================================================================
    // SUPPORT AND INTERACTION OPERATIONS
    // ============================================================================

    override suspend fun giveSupport(postId: String, userId: String, supportType: SupportType): Result<Unit> {
        return try {
            Timber.d("💙 Giving support to post: $postId (type: $supportType)")

            // Create support reaction document
            val supportReaction = mapOf(
                "postId" to postId,
                "userId" to userId,
                "supportType" to supportType.name,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val batch = firestore.batch()

            // Add support reaction
            val supportRef = firestore.collection(COLLECTION_SUPPORT_REACTIONS).document()
            batch.set(supportRef, supportReaction)

            // Increment support count on post
            val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document(postId)
            batch.update(postRef, "supportCount", com.google.firebase.firestore.FieldValue.increment(1))

            batch.commit().await()

            Timber.d("✅ Support given successfully to post: $postId")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error giving support to post: $postId")
            Result.failure(e)
        }
    }

    override suspend fun removeSupport(postId: String, userId: String): Result<Unit> {
        return try {
            Timber.d("💔 Removing support from post: $postId for user: $userId")

            // Find and delete support reaction
            val supportQuery = firestore.collection(COLLECTION_SUPPORT_REACTIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!supportQuery.isEmpty) {
                val batch = firestore.batch()

                // Delete support reaction
                supportQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // Decrement support count on post
                val postRef = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).document(postId)
                batch.update(postRef, "supportCount", com.google.firebase.firestore.FieldValue.increment(-1))

                batch.commit().await()

                Timber.d("✅ Support removed successfully from post: $postId")
            } else {
                Timber.w("No support reaction found for user $userId on post $postId")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error removing support from post: $postId")
            Result.failure(e)
        }
    }

    override suspend fun reportPost(postId: String, userId: String, reason: String): Result<Unit> {
        return try {
            Timber.d("🚨 Reporting post: $postId (reason: $reason)")

            val reportData = mapOf(
                "postId" to postId,
                "reportedBy" to userId,
                "reason" to reason,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "status" to "pending"
            )

            firestore.collection(Constants.COLLECTION_REPORTS)
                .add(reportData)
                .await()

            Timber.d("✅ Post reported successfully: $postId")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error reporting post: $postId")
            Result.failure(e)
        }
    }

    // ============================================================================
    // CHAOS TWINS OPERATIONS
    // ============================================================================

    override fun findChaosTwins(userId: String, tags: List<String>, chaosLevel: Int): Flow<List<CommunityPost>> {
        Timber.d("🔍 Finding chaos twins for user: $userId (tags: $tags, chaosLevel: $chaosLevel)")

        // Find posts with similar tags and chaos level (+/- 2)
        val minChaosLevel = (chaosLevel - 2).coerceAtLeast(1)
        val maxChaosLevel = (chaosLevel + 2).coerceAtMost(10)

        return firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
            .whereArrayContainsAny("tags", tags)
            .whereGreaterThanOrEqualTo("chaosLevel", minChaosLevel)
            .whereLessThanOrEqualTo("chaosLevel", maxChaosLevel)
            .orderBy("chaosLevel")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .snapshots()
            .map { snapshot ->
                val posts = snapshot.documents.mapNotNull { document ->
                    try {
                        document.data?.toCommunityPost()
                    } catch (e: Exception) {
                        Timber.e(e, "Error converting document to CommunityPost: ${document.id}")
                        null
                    }
                }
                Timber.d("🔍 Found ${posts.size} chaos twins")
                posts
            }
            .catch { exception ->
                Timber.e(exception, "❌ Error finding chaos twins")
                emit(emptyList())
            }
    }

    // ============================================================================
    // STATISTICS
    // ============================================================================

    override suspend fun getCommunityStats(): Result<Map<String, Int>> {
        return try {
            Timber.d("📊 Getting community stats")

            val postsSnapshot = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS).get().await()
            val supportSnapshot = firestore.collection(COLLECTION_SUPPORT_REACTIONS).get().await()

            val stats = mapOf(
                "totalPosts" to postsSnapshot.size(),
                "totalSupport" to supportSnapshot.size(),
                "activeUsers" to postsSnapshot.documents.map { it.getString("userId") }.distinct().size
            )

            Timber.d("📊 Community stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error getting community stats")
            Result.failure(e)
        }
    }
}