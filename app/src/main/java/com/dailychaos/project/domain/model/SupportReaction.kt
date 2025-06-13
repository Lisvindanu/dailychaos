package com.dailychaos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Support Reaction - Reaksi dukungan untuk community post
 */
@Serializable
data class SupportReaction(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val type: SupportType = SupportType.HEART,
    val createdAt: Instant = Instant.DISTANT_PAST
)

/**
 * Tipe dukungan yang bisa diberikan
 */
enum class SupportType(val emoji: String, val description: String) {
    HEART("💙", "Sending virtual hug"),
    HUG("🤗", "Big warm hug"),
    SOLIDARITY("🤝", "I feel you, friend"),
    STRENGTH("💪", "You're stronger than you think"),
    HOPE("🌟", "Better days are coming")
}