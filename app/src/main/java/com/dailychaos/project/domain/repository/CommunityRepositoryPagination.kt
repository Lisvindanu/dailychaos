// File: app/src/main/java/com/dailychaos/project/domain/repository/CommunityRepositoryPagination.kt
package com.dailychaos.project.domain.repository

import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import kotlinx.coroutines.flow.Flow

/**
 * Extension interface untuk Pagination - tidak mengubah CommunityRepositoryExtended yang sudah ada
 * Bisa diimplementasikan sebagai extension atau wrapper
 */
interface CommunityRepositoryPagination {

    /**
     * Get paginated posts dengan filter sederhana
     */
    suspend fun getPaginatedPosts(
        page: Int = 1,
        pageSize: Int = 15,
        timeRange: TimeFilter? = null,
        chaosLevelRange: IntRange? = null,
        tags: List<String>? = null,
        sortBy: String = "createdAt_desc"
    ): Result<PaginatedResponse<CommunityPost>>

    /**
     * Search posts dengan pagination
     */
    suspend fun searchPaginatedPosts(
        query: String,
        page: Int = 1,
        pageSize: Int = 15
    ): Result<PaginatedResponse<CommunityPost>>

    /**
     * Get filter metadata
     */
    suspend fun getFilterMetadata(): Result<FilterMetadata>
}

data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    val totalPages: Int get() = (totalCount + pageSize - 1) / pageSize
}

data class FilterMetadata(
    val popularTags: List<String>,
    val chaosLevelRange: IntRange,
    val dateRange: Pair<Long, Long>
)

enum class TimeFilter(val displayName: String, val hoursAgo: Int?) {
    TODAY("Hari Ini", 24),
    WEEK("Minggu Ini", 168),
    MONTH("Bulan Ini", 720),
    ALL("Semua", null)
}