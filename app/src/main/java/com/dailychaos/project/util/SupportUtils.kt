// File: app/src/main/java/com/dailychaos/project/util/SupportUtils.kt
package com.dailychaos.project.util

import com.dailychaos.project.domain.model.SupportType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Support Utilities
 * "Helper functions untuk support system dengan KonoSuba vibes"
 */
object SupportUtils {

    /**
     * Generate support notification message
     */
    fun generateSupportNotificationMessage(
        supportType: SupportType,
        senderUsername: String,
        postTitle: String
    ): String {
        val actionText = when (supportType) {
            SupportType.HEART -> "sent you love"
            SupportType.HUG -> "gave you a hug"
            SupportType.STRENGTH -> "shared strength"
            SupportType.HOPE -> "offered hope"
        }

        return "$senderUsername $actionText for your post \"$postTitle\""
    }

    /**
     * Generate support change message
     */
    fun generateSupportChangeMessage(
        oldType: SupportType,
        newType: SupportType,
        username: String
    ): String {
        return "$username changed their support from ${oldType.emoji} ${oldType.displayName} to ${newType.emoji} ${newType.displayName}"
    }

    /**
     * Get support animation duration based on type
     */
    fun getSupportAnimationDuration(supportType: SupportType): Long {
        return when (supportType) {
            SupportType.HEART -> 800L
            SupportType.HUG -> 1200L
            SupportType.STRENGTH -> 600L
            SupportType.HOPE -> 1000L
        }
    }

    /**
     * Get support color tema
     */
    fun getSupportColorHex(supportType: SupportType): String {
        return when (supportType) {
            SupportType.HEART -> "#FF6B9D" // Pink
            SupportType.HUG -> "#FFB347" // Orange
            SupportType.STRENGTH -> "#87CEEB" // Sky Blue
            SupportType.HOPE -> "#FFD700" // Gold
        }
    }

    /**
     * Generate KonoSuba-style support quote
     */
    fun getKonoSubaSupportQuote(supportType: SupportType): String {
        return when (supportType) {
            SupportType.HEART -> "\"Even Kazuma shows he cares in his own tsundere way!\" 💝"
            SupportType.HUG -> "\"Megumin may act tough, but she needs hugs too!\" 🤗"
            SupportType.STRENGTH -> "\"Darkness believes in the strength of enduring together!\" 💪"
            SupportType.HOPE -> "\"Aqua always says tomorrow will be better! (And she's right sometimes!)\" 🌟"
        }
    }

    /**
     * Check if support type combination is effective
     * Untuk suggest support types berdasarkan yang udah ada
     */
    fun getSuggestedNextSupport(currentSupports: List<SupportType>): List<SupportType> {
        val available = SupportType.getAllTypes().toMutableList()
        available.removeAll(currentSupports.toSet())

        return when {
            currentSupports.isEmpty() -> listOf(SupportType.HEART, SupportType.HUG)
            currentSupports.contains(SupportType.HEART) && !currentSupports.contains(SupportType.HUG) ->
                listOf(SupportType.HUG, SupportType.HOPE)
            currentSupports.contains(SupportType.HUG) && !currentSupports.contains(SupportType.STRENGTH) ->
                listOf(SupportType.STRENGTH, SupportType.HOPE)
            else -> available.take(2)
        }
    }

    /**
     * Format support count untuk display
     */
    fun formatSupportCount(count: Int): String {
        return when {
            count == 0 -> "No support yet"
            count == 1 -> "1 adventurer supports this"
            count < 10 -> "$count adventurers support this"
            count < 100 -> "$count fellow adventurers support this"
            count < 1000 -> "${count}+ adventurers in this chaos party!"
            else -> "${count/1000}k+ massive chaos support!"
        }
    }

    /**
     * Get support milestone message
     */
    fun getSupportMilestoneMessage(count: Int): String? {
        return when (count) {
            1 -> "🎉 First support! The adventure begins!"
            5 -> "🌟 5 supports! Building a small party!"
            10 -> "💪 10 supports! This is becoming a real party!"
            25 -> "🎊 25 supports! A whole guild is behind you!"
            50 -> "🚀 50 supports! You're inspiring the whole town!"
            100 -> "👑 100 supports! Legendary adventurer status!"
            else -> null
        }
    }

    /**
     * Calculate support score untuk ranking
     */
    fun calculateSupportScore(supports: Map<SupportType, Int>): Int {
        var score = 0
        supports.forEach { (type, count) ->
            val multiplier = when (type) {
                SupportType.HEART -> 1
                SupportType.HUG -> 2
                SupportType.STRENGTH -> 3
                SupportType.HOPE -> 4
            }
            score += count * multiplier
        }
        return score
    }

    /**
     * Get support distribution percentages
     */
    fun getSupportDistribution(supports: Map<SupportType, Int>): Map<SupportType, Float> {
        val total = supports.values.sum()
        if (total == 0) return mapOf()

        return supports.mapValues { (_, count) ->
            (count.toFloat() / total) * 100f
        }
    }

    /**
     * Generate support summary text
     */
    fun generateSupportSummary(supports: Map<SupportType, Int>): String {
        val total = supports.values.sum()
        if (total == 0) return "No support yet - be the first adventurer!"

        val topSupport = supports.maxByOrNull { it.value }
        return if (topSupport != null && topSupport.value > 0) {
            val percentage = (topSupport.value.toFloat() / total * 100).toInt()
            "Most appreciated: ${topSupport.key.emoji} ${topSupport.key.displayName} ($percentage%)"
        } else {
            "Mixed support from $total adventurers"
        }
    }

    /**
     * Validate support action
     */
    fun validateSupportAction(
        userId: String,
        postId: String,
        supportType: SupportType,
        postAuthorId: String
    ): SupportValidationResult {
        return when {
            userId.isBlank() -> SupportValidationResult.InvalidUser
            postId.isBlank() -> SupportValidationResult.InvalidPost
            userId == postAuthorId -> SupportValidationResult.SelfSupport
            else -> SupportValidationResult.Valid
        }
    }

    /**
     * Generate Megumin modal quotes untuk different scenarios
     */
    fun getMeguminModalQuote(scenario: MeguminModalScenario): String {
        return when (scenario) {
            MeguminModalScenario.DuplicateSupport ->
                "\"Oi oi oi! Kamu udah kasih support ${scenario.supportType?.emoji ?: ""} ini sebelumnya! Mau ganti ke yang lain?\""
            MeguminModalScenario.RemoveSupport ->
                "\"Kenapa mau batalin support untuk orang yang butuh bantuan moral? Apa kamu yakin ingin melakukan ini?\""
            MeguminModalScenario.SelfSupport ->
                "\"Heh?! Kamu mau support post sendiri? That's not how adventuring works!\""
        }
    }

    enum class SupportValidationResult {
        Valid,
        InvalidUser,
        InvalidPost,
        SelfSupport
    }

    enum class MeguminModalScenario(val supportType: SupportType? = null) {
        DuplicateSupport(),
        RemoveSupport,
        SelfSupport
    }
}

/**
 * Extension functions untuk easier usage
 */
fun SupportType.toDisplayString(): String = "${this.emoji} ${this.displayName}"

fun List<SupportType>.toEmojiString(): String = this.joinToString(" ") { it.emoji }

fun Int.toSupportMilestoneCheck(): String? = SupportUtils.getSupportMilestoneMessage(this)