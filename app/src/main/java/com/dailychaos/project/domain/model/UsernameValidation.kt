// File: app/src/main/java/com/dailychaos/project/domain/model/UsernameValidation.kt
package com.dailychaos.project.domain.model

/**
 * Username validation result untuk Daily Chaos
 * "Making sure party names are appropriate!"
 */
// Complete Validation Data Classes
// File: app/src/main/java/com/dailychaos/project/domain/model/ValidationModels.kt


/**
 * Username validation result
 */
data class UsernameValidation(
    val isValid: Boolean,
    val message: String? = null,
    val suggestions: List<String> = emptyList()
)

/**
 * Password validation result
 */
data class PasswordValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val strength: PasswordStrength = PasswordStrength.WEAK
)

/**
 * Display name validation result
 */
data class DisplayNameValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * General validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Password strength enum
 */
enum class PasswordStrength(val displayName: String, val color: Long) {
    WEAK("Lemah", 0xFFE53E3E),
    MEDIUM("Sedang", 0xFFDD6B20),
    STRONG("Kuat", 0xFF38A169),
    VERY_STRONG("Sangat Kuat", 0xFF319795)
}