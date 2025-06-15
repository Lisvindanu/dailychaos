// File: app/src/main/java/com/dailychaos/project/util/ValidationUtil.kt
package com.dailychaos.project.util

import android.util.Patterns
import com.dailychaos.project.domain.model.UsernameValidation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validation utilities untuk Daily Chaos
 * "Even in chaos, we need some rules!"
 */
@Singleton
class ValidationUtil @Inject constructor() {

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return if (email.isBlank()) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    /**
     * Validate password strength
     */
    fun validatePassword(password: String): PasswordValidation {
        return when {
            password.isBlank() -> PasswordValidation(
                isValid = false,
                errorMessage = "Password tidak boleh kosong!"
            )
            password.length < 6 -> PasswordValidation(
                isValid = false,
                errorMessage = "Password minimal 6 karakter!"
            )
            password.length > 50 -> PasswordValidation(
                isValid = false,
                errorMessage = "Password maksimal 50 karakter!"
            )
            else -> PasswordValidation(
                isValid = true,
                strength = calculatePasswordStrength(password)
            )
        }
    }

    /**
     * Validate username according to Daily Chaos rules
     * Keep errorMessage but return domain model UsernameValidation
     */
    fun validateUsername(username: String): UsernameValidation {
        return when {
            username.isBlank() -> UsernameValidation(
                isValid = false,
                message = "Username tidak boleh kosong!",
                suggestions = emptyList()
            )
            username.length < 3 -> UsernameValidation(
                isValid = false,
                message = "Username minimal 3 karakter! Contoh: 'Megumin'",
                suggestions = username.generateUsernameSuggestions()
            )
            username.length > 20 -> UsernameValidation(
                isValid = false,
                message = "Username maksimal 20 karakter! Singkat tapi berkesan!",
                suggestions = username.generateUsernameSuggestions()
            )
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> UsernameValidation(
                isValid = false,
                message = "Username hanya boleh huruf, angka, dan underscore! Contoh: 'kazuma_hero'",
                suggestions = username.generateUsernameSuggestions()
            )
            username.startsWith("_") || username.endsWith("_") -> UsernameValidation(
                isValid = false,
                message = "Username tidak boleh dimulai atau diakhiri dengan underscore!",
                suggestions = username.generateUsernameSuggestions()
            )
            username.contains("__") -> UsernameValidation(
                isValid = false,
                message = "Username tidak boleh ada double underscore!",
                suggestions = username.generateUsernameSuggestions()
            )
            username.lowercase() in FORBIDDEN_USERNAMES -> UsernameValidation(
                isValid = false,
                message = "Username '$username' tidak boleh digunakan! Coba yang lain.",
                suggestions = username.generateUsernameSuggestions()
            )
            else -> UsernameValidation(
                isValid = true,
                message = "Username tersedia!",
                suggestions = emptyList()
            )
        }
    }

    /**
     * Validate display name
     */
    fun validateDisplayName(displayName: String): DisplayNameValidation {
        return when {
            displayName.isBlank() -> DisplayNameValidation(isValid = true) // Optional field
            displayName.length > 30 -> DisplayNameValidation(
                isValid = false,
                errorMessage = "Display name maksimal 30 karakter!"
            )
            displayName.contains(Regex("[<>\"'&]")) -> DisplayNameValidation(
                isValid = false,
                errorMessage = "Display name tidak boleh mengandung karakter khusus!"
            )
            else -> DisplayNameValidation(isValid = true)
        }
    }

    /**
     * Calculate password strength
     */
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0

        // Length bonus
        if (password.length >= 8) score += 1
        if (password.length >= 12) score += 1

        // Character variety
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score += 1

        return when (score) {
            in 0..2 -> PasswordStrength.WEAK
            in 3..4 -> PasswordStrength.MEDIUM
            in 5..6 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }

    companion object {
        private val FORBIDDEN_USERNAMES = setOf(
            "admin", "administrator", "root", "system", "chaos", "dailychaos",
            "moderator", "mod", "support", "help", "api", "null", "undefined",
            "test", "demo", "example", "user", "username", "password",
            "guest", "anonymous", "bot", "service", "official"
        )
    }
}

/**
 * Data classes for validation results
 * Keep local ValidationResult classes, but use domain UsernameValidation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

data class PasswordValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val strength: PasswordStrength = PasswordStrength.WEAK
)

data class DisplayNameValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
)

enum class PasswordStrength(val displayName: String, val color: Long) {
    WEAK("Lemah", 0xFFE53E3E),
    MEDIUM("Sedang", 0xFFDD6B20),
    STRONG("Kuat", 0xFF38A169),
    VERY_STRONG("Sangat Kuat", 0xFF319795)
}