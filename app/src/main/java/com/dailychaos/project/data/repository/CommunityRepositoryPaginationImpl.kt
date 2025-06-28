// File: app/src/main/java/com/dailychaos/project/data/repository/CommunityRepositoryPaginationImpl.kt
package com.dailychaos.project.data.repository

import com.dailychaos.project.data.mapper.toCommunityPost
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.repository.CommunityRepositoryExtended
import com.dailychaos.project.domain.repository.CommunityRepositoryPagination
import com.dailychaos.project.domain.repository.FilterMetadata
import com.dailychaos.project.domain.repository.PaginatedResponse
import com.dailychaos.project.domain.repository.TimeFilter
import com.dailychaos.project.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi Pagination sebagai extension/wrapper dari repository existing
 * Tidak mengubah CommunityRepositoryImpl yang sudah ada
 */
@Singleton
class CommunityRepositoryPaginationImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val baseRepository: CommunityRepositoryExtended
) : CommunityRepositoryPagination {

    // Cache sederhana untuk metadata
    private var cachedMetadata: FilterMetadata? = null
    private var metadataCacheTime: Long = 0
    private val cacheTimeout = 10 * 60 * 1000L // 10 minutes

    override suspend fun getPaginatedPosts(
        page: Int,
        pageSize: Int,
        timeRange: TimeFilter?,
        chaosLevelRange: IntRange?,
        tags: List<String>?,
        sortBy: String
    ): Result<PaginatedResponse<CommunityPost>> {
        return try {
            Timber.d("🔍 Loading paginated posts - page: $page, size: $pageSize")

            var query: Query = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)

            // Apply time filter
            timeRange?.hoursAgo?.let { hours ->
                val cutoffTime = System.currentTimeMillis() - (hours.toLong() * 60 * 60 * 1000L)
                query = query.whereGreaterThan("createdAt", cutoffTime)
            }

            // Apply chaos level filter
            chaosLevelRange?.let { range ->
                query = query.whereGreaterThanOrEqualTo("chaosLevel", range.first.toLong())
                    .whereLessThanOrEqualTo("chaosLevel", range.last.toLong())
            }

            // Apply tags filter
            if (!tags.isNullOrEmpty()) {
                query = query.whereArrayContainsAny("tags", tags)
            }

            // Apply sorting
            when (sortBy) {
                "createdAt_desc" -> query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                "createdAt_asc" -> query = query.orderBy("createdAt", Query.Direction.ASCENDING)
                "supportCount_desc" -> query = query.orderBy("supportCount", Query.Direction.DESCENDING)
                "chaosLevel_desc" -> query = query.orderBy("chaosLevel", Query.Direction.DESCENDING)
                "chaosLevel_asc" -> query = query.orderBy("chaosLevel", Query.Direction.ASCENDING)
                else -> query = query.orderBy("createdAt", Query.Direction.DESCENDING)
            }

            // Get total count estimate
            val totalSnapshot = query.limit(1000).get().await()
            val estimatedTotal = totalSnapshot.size()

            // Apply pagination using limit and skip simulation
            val itemsToSkip = (page - 1) * pageSize
            val paginatedQuery = query.limit((itemsToSkip + pageSize).toLong())

            val snapshot = paginatedQuery.get().await()
            val allPosts = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.toCommunityPost()
                } catch (e: Exception) {
                    Timber.e(e, "Error converting document ${doc.id}")
                    null
                }
            }

            // Skip items manually for pagination (Firestore limitation workaround)
            val posts = allPosts.drop(itemsToSkip).take(pageSize)

            val response = PaginatedResponse(
                data = posts,
                page = page,
                pageSize = pageSize,
                totalCount = estimatedTotal,
                hasNext = posts.size == pageSize && (itemsToSkip + pageSize) < estimatedTotal,
                hasPrevious = page > 1
            )

            Timber.d("✅ Loaded ${posts.size} posts for page $page")
            Result.success(response)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error loading paginated posts")
            Result.failure(e)
        }
    }

    override suspend fun searchPaginatedPosts(
        query: String,
        page: Int,
        pageSize: Int
    ): Result<PaginatedResponse<CommunityPost>> {
        return try {
            Timber.d("🔍 Searching posts: '$query'")

            // Simple search implementation
            val searchTerms = query.split(" ").filter { it.length >= 2 }

            if (searchTerms.isEmpty()) {
                return getPaginatedPosts(page, pageSize)
            }

            var firestoreQuery: Query = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            // Get more posts for filtering
            val itemsToSkip = (page - 1) * pageSize
            val searchLimit = (itemsToSkip + pageSize * 2).toLong() // Get extra for filtering

            val snapshot = firestoreQuery.limit(searchLimit).get().await()

            val filteredPosts = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.let { data ->
                        val title = data["title"] as? String ?: ""
                        val content = data["content"] as? String ?: ""
                        val tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                        // Simple text matching
                        val searchText = "$title $content ${tags.joinToString(" ")}".lowercase()
                        val matches = searchTerms.any { term ->
                            searchText.contains(term.lowercase())
                        }

                        if (matches) {
                            data.toCommunityPost()
                        } else null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error converting search result ${doc.id}")
                    null
                }
            }.drop(itemsToSkip).take(pageSize)

            val response = PaginatedResponse(
                data = filteredPosts,
                page = page,
                pageSize = pageSize,
                totalCount = filteredPosts.size + (if (filteredPosts.size == pageSize) pageSize else 0),
                hasNext = filteredPosts.size == pageSize,
                hasPrevious = page > 1
            )

            Timber.d("✅ Found ${filteredPosts.size} posts matching '$query'")
            Result.success(response)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error searching posts")
            Result.failure(e)
        }
    }

    override suspend fun getFilterMetadata(): Result<FilterMetadata> {
        return try {
            // Check cache first
            val now = System.currentTimeMillis()
            if (cachedMetadata != null && (now - metadataCacheTime) < cacheTimeout) {
                return Result.success(cachedMetadata!!)
            }

            Timber.d("📊 Loading filter metadata")

            // Get recent posts to analyze tags and ranges
            val recentPostsSnapshot = firestore.collection(Constants.COLLECTION_COMMUNITY_POSTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(500) // Reasonable sample size
                .get()
                .await()

            val tagCounts = mutableMapOf<String, Int>()
            var minChaosLevel = 10
            var maxChaosLevel = 1
            var earliestTime = Long.MAX_VALUE
            var latestTime = 0L

            recentPostsSnapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach

                // Analyze tags
                val tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                tags.forEach { tag ->
                    tagCounts[tag] = tagCounts.getOrDefault(tag, 0) + 1
                }

                // Analyze chaos levels
                val chaosLevel = (data["chaosLevel"] as? Long)?.toInt() ?: 1
                minChaosLevel = minOf(minChaosLevel, chaosLevel)
                maxChaosLevel = maxOf(maxChaosLevel, chaosLevel)

                // Analyze date range
                val timestamp = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                earliestTime = minOf(earliestTime, timestamp)
                latestTime = maxOf(latestTime, timestamp)
            }

            // Get top tags
            val popularTags = tagCounts.entries
                .sortedByDescending { it.value }
                .take(20)
                .map { it.key }

            val metadata = FilterMetadata(
                popularTags = popularTags,
                chaosLevelRange = minChaosLevel..maxChaosLevel,
                dateRange = earliestTime to latestTime
            )

            // Cache the result
            cachedMetadata = metadata
            metadataCacheTime = now

            Timber.d("✅ Loaded metadata: ${popularTags.size} tags, chaos range: $minChaosLevel-$maxChaosLevel")
            Result.success(metadata)

        } catch (e: Exception) {
            Timber.e(e, "❌ Error loading filter metadata")
            Result.failure(e)
        }
    }

    /**
     * Clear cache manually if needed
     */
    fun clearCache() {
        cachedMetadata = null
        metadataCacheTime = 0
        Timber.d("🗑️ Filter metadata cache cleared")
    }
}