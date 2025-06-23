// File: app/src/main/java/com/dailychaos/project/domain/model/SupportType.kt
package com.dailychaos.project.domain.model

/**
 * Support Type Enum - Types of support yang bisa diberikan ke community posts
 * "Berbagai cara untuk support fellow adventurers dalam chaos mereka"
 */
enum class SupportType(
    val displayName: String,
    val emoji: String,
    val description: String,
    val konoSubaReference: String? = null
) {
    HEART(
        displayName = "Heart",
        emoji = "💝",
        description = "Send love and care to this adventurer",
        konoSubaReference = "Like Kazuma's hidden care for his party members"
    ),

    HUG(
        displayName = "Hug",
        emoji = "🤗",
        description = "Give a warm virtual hug of comfort",
        konoSubaReference = "Like Megumin's rare moments of vulnerability"
    ),

    STRENGTH(
        displayName = "Strength",
        emoji = "💪",
        description = "Share your strength and determination",
        konoSubaReference = "Like Darkness's unwavering resolve (minus the masochism)"
    ),

    HOPE(
        displayName = "Hope",
        emoji = "🌟",
        description = "Offer hope and encouragement for better days",
        konoSubaReference = "Like Aqua's optimism (but actually helpful)"
    );

    /**
     * Get support message untuk display
     */
    fun getSupportMessage(): String {
        return when (this) {
            HEART -> "sent you love and care 💝"
            HUG -> "gave you a warm hug 🤗"
            STRENGTH -> "shared their strength with you 💪"
            HOPE -> "offered you hope and encouragement 🌟"
        }
    }

    /**
     * Get action text untuk buttons
     */
    fun getActionText(): String {
        return when (this) {
            HEART -> "Send Love"
            HUG -> "Give Hug"
            STRENGTH -> "Share Strength"
            HOPE -> "Offer Hope"
        }
    }

    /**
     * Get notification text
     */
    fun getNotificationText(): String {
        return when (this) {
            HEART -> "Someone sent you love and care"
            HUG -> "Someone gave you a warm hug"
            STRENGTH -> "Someone shared their strength with you"
            HOPE -> "Someone offered you hope"
        }
    }

    companion object {
        /**
         * Get all support types as list
         */
        fun getAllTypes(): List<SupportType> = values().toList()

        /**
         * Get random support type untuk testing atau default
         */
        fun getRandomType(): SupportType = values().random()

        /**
         * Get support type from string dengan fallback
         */
        fun fromString(value: String?): SupportType? {
            return try {
                value?.let { valueOf(it.uppercase()) }
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * Get support types yang cocok untuk mood tertentu
         */
        fun getTypesForMood(mood: String): List<SupportType> {
            return when (mood.lowercase()) {
                "sad", "depressed", "down" -> listOf(HUG, HEART, HOPE)
                "angry", "frustrated", "mad" -> listOf(STRENGTH, HUG)
                "anxious", "worried", "stressed" -> listOf(HUG, HOPE, HEART)
                "overwhelmed", "burnt out" -> listOf(STRENGTH, HOPE)
                "lonely", "isolated" -> listOf(HUG, HEART)
                "hopeless", "lost" -> listOf(HOPE, HEART, STRENGTH)
                else -> getAllTypes() // Default: all types available
            }
        }
    }
}