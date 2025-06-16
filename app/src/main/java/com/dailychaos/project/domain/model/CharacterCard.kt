package com.dailychaos.project.domain.model

/**
 * Character Card domain model
 */
data class CharacterCard(
    val id: String,
    val name: String,
    val imageUrl: String,
    val rarity: Int, // 1-4 stars
    val element: String? = null,
    val description: String? = null,
    val eventId: Int? = null,
    val characterId: String? = null
)

// Extension functions untuk utility
fun CharacterCard.isHighRarity(): Boolean = rarity >= 3

fun CharacterCard.getRarityStars(): String = "★".repeat(rarity)

fun CharacterCard.getDisplayName(): String = "$name ${getRarityStars()}"