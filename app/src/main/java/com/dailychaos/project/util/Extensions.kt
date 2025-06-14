package com.dailychaos.project.util

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Extension functions untuk Daily Chaos
 *
 * "Seperti skill tambahan untuk party - bikin hidup lebih mudah!"
 */

// ================================
// STRING EXTENSIONS
// ================================

/**
 * Capitalize first letter
 */
fun String.capitalizeFirstLetter(): String {
    return if (isEmpty()) this else first().uppercase() + drop(1)
}

/**
 * Truncate string with ellipsis
 */
fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else "${take(maxLength - 3)}..."
}

/**
 * Get word count
 */
fun String.wordCount(): Int {
    return if (isBlank()) 0 else trim().split("\\s+".toRegex()).size
}

/**
 * Clean and validate text input
 */
fun String.cleanInput(): String {
    return trim().replace("\\s+".toRegex(), " ")
}

/**
 * Check if string contains any of the given keywords
 */
fun String.containsAny(keywords: List<String>, ignoreCase: Boolean = true): Boolean {
    return keywords.any { keyword ->
        contains(keyword, ignoreCase)
    }
}

/**
 * Generate anonymous username
 */
fun String.Companion.generateAnonymousUsername(): String {
    val prefixes = Constants.ANONYMOUS_PREFIXES
    val randomPrefix = prefixes.random()
    val randomNumber = Random.nextInt(1000, 9999)
    return "$randomPrefix$randomNumber"
}

/**
 * Clean username by removing invalid characters
 */
fun String.cleanUsername(): String {
    return this.filter { it.isLetterOrDigit() || it == '_' }
}

/**
 * Generate display name from username
 */
fun String.toDisplayName(): String {
    return this.split("_")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
}

// ================================
// DATETIME EXTENSIONS
// ================================

/**
 * Get human readable time ago
 */
fun Instant.timeAgo(): String {
    val now = Clock.System.now()
    val duration = now - this

    return when {
        duration < 1.minutes -> "Baru saja"
        duration < 1.hours -> "${duration.inWholeMinutes} menit lalu"
        duration < 1.days -> "${duration.inWholeHours} jam lalu"
        duration < 7.days -> "${duration.inWholeDays} hari lalu"
        else -> {
            val localDate = toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${localDate.dayOfMonth}/${localDate.monthNumber}/${localDate.year}"
        }
    }
}

/**
 * Check if datetime is today
 */
fun Instant.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val thisDate = toLocalDateTime(TimeZone.currentSystemDefault()).date
    return thisDate == today
}

/**
 * Check if datetime is yesterday
 */
fun Instant.isYesterday(): Boolean {
    val yesterday = Clock.System.now().minus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date
    val thisDate = toLocalDateTime(TimeZone.currentSystemDefault()).date
    return thisDate == yesterday
}

/**
 * Get friendly date string
 */
fun Instant.toFriendlyDateString(): String {
    return when {
        isToday() -> "Hari Ini"
        isYesterday() -> "Kemarin"
        else -> {
            val localDate = toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${localDate.dayOfMonth}/${localDate.monthNumber}/${localDate.year}"
        }
    }
}

/**
 * Get time string (HH:mm)
 */
fun Instant.toTimeString(): String {
    val localTime = toLocalDateTime(TimeZone.currentSystemDefault()).time
    return "${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}"
}

/**
 * Start of day
 */
fun Instant.startOfDay(): Instant {
    val localDate = toLocalDateTime(TimeZone.currentSystemDefault()).date
    return localDate.atStartOfDayIn(TimeZone.currentSystemDefault())
}

/**
 * End of day
 */
fun Instant.endOfDay(): Instant {
    return startOfDay().plus(1.days).minus(1.minutes)
}

// ================================
// CONTEXT EXTENSIONS
// ================================

/**
 * Show toast extension
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show long toast
 */
fun Context.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}

// ================================
// COMPOSE EXTENSIONS
// ================================

/**
 * Vertical spacer
 */
@Composable
fun VerticalSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

/**
 * Horizontal spacer
 */
@Composable
fun HorizontalSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

/**
 * Convert Int to Dp
 */
@Composable
fun Int.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

/**
 * Convert Float to Dp
 */
@Composable
fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

// ================================
// COLLECTION EXTENSIONS
// ================================

/**
 * Safe get item from list
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in indices) this[index] else null
}

/**
 * Get random item or null if empty
 */
fun <T> List<T>.randomOrNull(): T? {
    return if (isEmpty()) null else random()
}

/**
 * Chunk list into smaller lists
 */
fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return if (size <= 0) listOf(this) else windowed(size, size, true)
}

/**
 * Update item in list by condition
 */
inline fun <T> List<T>.updateItem(predicate: (T) -> Boolean, update: (T) -> T): List<T> {
    return map { item ->
        if (predicate(item)) update(item) else item
    }
}

/**
 * Remove item by condition
 */
inline fun <T> List<T>.removeItem(predicate: (T) -> Boolean): List<T> {
    return filterNot(predicate)
}

// ================================
// VALIDATION EXTENSIONS
// ================================

/**
 * Validate email format
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Check if string is valid password
 */
fun String.isValidPassword(): Boolean {
    return length >= Constants.MIN_PASSWORD_LENGTH
}

/**
 * Check if chaos entry description is valid
 */
fun String.isValidChaosDescription(): Boolean {
    return cleanInput().length >= Constants.MIN_CHAOS_ENTRY_LENGTH
}

/**
 * Check if string is not blank and not empty
 */
fun String?.isNotBlankOrEmpty(): Boolean {
    return !isNullOrBlank()
}

/**
 * Validate username format (basic rules)
 */
fun String.isValidUsernameFormat(): Boolean {
    return this.isNotBlank() &&
            this.length in 3..20 &&
            this.matches(Regex("^[a-zA-Z0-9_]+$"))
}

/**
 * Check if username contains forbidden words
 */
fun String.containsForbiddenWords(): Boolean {
    val forbiddenWords = listOf("admin", "root", "moderator", "system")
    return forbiddenWords.any {
        this.contains(it, ignoreCase = true)
    }
}

/**
 * Get fun validation message for username errors
 * "KonoSuba-themed validation messages!"
 */
fun String.getUsernameErrorMessage(): String? {
    return when {
        this.isBlank() -> "Username tidak boleh kosong!"
        this.length < 3 -> "Username minimal 3 karakter - ini bukan nama explosion spell!"
        this.length > 20 -> "Username maksimal 20 karakter - ini bukan nama jutsu Naruto!"
        !this.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username hanya boleh huruf, angka, dan underscore"
        this.containsForbiddenWords() -> "Nice try! Username '$this' dilarang"
        this.contains("aqua", ignoreCase = true) && this.contains("useless", ignoreCase = true) ->
            "Hey! Aqua might be useless but she's still a goddess!"
        else -> null // No error
    }
}

/**
 * Generate username suggestions based on base name
 */
fun String.generateUsernameSuggestions(): List<String> {
    val suggestions = mutableListOf<String>()
    val cleanBase = this.cleanUsername().take(15)

    if (cleanBase.isNotEmpty()) {
        // KonoSuba inspired suggestions
        val konoSubaSuffixes = listOf("Adventurer", "Hero", "Mage", "Crusader", "Thief", "Priest")
        val numbers = (1..99).shuffled().take(3)
        val adjectives = listOf("Epic", "Chaos", "Lucky", "Brave", "Wild", "Cool")

        // Add number variations
        numbers.forEach { num ->
            suggestions.add("${cleanBase}$num")
        }

        // Add adjective + base
        adjectives.take(2).forEach { adj ->
            if ((adj + cleanBase).length <= 20) {
                suggestions.add("$adj$cleanBase")
            }
        }

        // Add base + suffix
        konoSubaSuffixes.take(2).forEach { suffix ->
            if ((cleanBase + suffix).length <= 20) {
                suggestions.add("$cleanBase$suffix")
            }
        }
    }

    return suggestions.distinct().take(6)
}

// ================================
// RANDOM UTILITIES
// ================================

/**
 * Get random KonoSuba character name
 */
fun getRandomCharacterName(): String {
    val names = listOf("Kazuma", "Aqua", "Megumin", "Darkness", "Wiz", "Yunyun", "Chris")
    return names.random()
}

/**
 * Get random chaos level with some weighting toward middle values
 */
fun getRandomChaosLevel(): Int {
    // Weighted toward 3-7 range (more common chaos levels)
    val weights = listOf(1, 2, 4, 5, 6, 5, 4, 3, 2, 1)
    val totalWeight = weights.sum()
    val randomValue = Random.nextInt(totalWeight)

    var accumulator = 0
    for (i in weights.indices) {
        accumulator += weights[i]
        if (randomValue < accumulator) {
            return i + 1
        }
    }
    return 5 // Fallback
}

// ================================
// FORMATTING UTILITIES
// ================================

/**
 * Format number with K, M suffix
 */
fun Int.formatCount(): String {
    return when {
        this >= 1_000_000 -> "${this / 1_000_000}M"
        this >= 1_000 -> "${this / 1_000}K"
        else -> toString()
    }
}

/**
 * Format streak days
 */
fun Int.formatStreakDays(): String {
    return when (this) {
        0 -> "Belum ada streak"
        1 -> "1 hari"
        else -> "$this hari berturut-turut"
    }
}

// ================================
// ANALYTICS EXTENSIONS
// ================================

/**
 * Create event parameters map
 */
fun createEventParams(vararg pairs: Pair<String, Any?>): Map<String, Any> {
    return pairs.mapNotNull { (key, value) ->
        value?.let { key to it }
    }.toMap()
}

/**
 * Sanitize string for analytics
 */
fun String.sanitizeForAnalytics(): String {
    return replace("[^a-zA-Z0-9_]".toRegex(), "_")
        .take(40) // Limit length
        .lowercase()
}