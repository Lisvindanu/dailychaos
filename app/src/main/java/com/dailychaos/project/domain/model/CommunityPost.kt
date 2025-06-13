package com.dailychaos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Community Post - Post yang dibagikan ke komunitas
 */
@Serializable
data class CommunityPost(
    val id: String = "",
    val chaosEntryId: String = "",
    val anonymousUsername: String = "",
    val title: String = "",
    val description: String = "",
    val chaosLevel: Int = 5,
    val tags: List<String> = emptyList(),
    val supportCount: Int = 0,
    val twinCount: Int = 0, // Jumlah orang yang relate
    val createdAt: Instant = Instant.DISTANT_PAST,
    val isReported: Boolean = false,
    val isModerated: Boolean = false
) {
    /**
     * Get chaos level as enum
     */
    fun getChaosLevelEnum(): ChaosLevel = ChaosLevel.fromValue(chaosLevel)

    /**
     * Check if post has good engagement
     */
    fun hasGoodEngagement(): Boolean = supportCount > 3 || twinCount > 1

    /**
     * Get engagement score
     */
    fun getEngagementScore(): Int = supportCount + (twinCount * 2)
}