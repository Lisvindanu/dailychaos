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
    val userId: String = "", // Added: ID of the original user
    val username: String = "", // Added: Username of the original poster (for internal use/moderation)
    val anonymousUsername: String = "",
    val title: String = "",
    val description: String = "",
    val chaosLevel: Int = 5,
    val miniWins: List<String> = emptyList(), // Added: List of mini wins
    val tags: List<String> = emptyList(),
    val supportCount: Int = 0,
    val twinCount: Int = 0, // Jumlah orang yang relate
    val createdAt: Instant = Instant.DISTANT_PAST,
    val isReported: Boolean = false,
    val isModerated: Boolean = false,
    val isAnonymous: Boolean = false // Added: Flag to indicate if the post is anonymous
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