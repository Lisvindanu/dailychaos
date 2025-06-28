// File: app/src/main/java/com/dailychaos/project/util/CommentValidationUtil.kt
package com.dailychaos.project.util

import com.dailychaos.project.domain.model.SupportType

/**
 * Comment Validation Utility
 * "Making sure comments are safe and appropriate for our adventurer community"
 */
object CommentValidationUtil {

    // ============================================================================
    // VALIDATION CONSTANTS
    // ============================================================================

    private const val MIN_COMMENT_LENGTH = 1
    private const val MAX_COMMENT_LENGTH = 1000
    private const val MIN_SUPPORT_LEVEL = 1
    private const val MAX_SUPPORT_LEVEL = 5

    // Prohibited words (can be expanded)
    private val PROHIBITED_WORDS = setOf(
        "spam", "scam", "hate", "toxic"
        // Add more as needed
    )

    // ============================================================================
    // VALIDATION METHODS
    // ============================================================================

    /**
     * Validate comment content
     */
    fun validateCommentContent(content: String): ValidationResult {
        val trimmedContent = content.trim()

        return when {
            trimmedContent.isBlank() -> ValidationResult.Error("Comment cannot be empty")
            trimmedContent.length < MIN_COMMENT_LENGTH -> ValidationResult.Error("Comment is too short")
            trimmedContent.length > MAX_COMMENT_LENGTH -> ValidationResult.Error("Comment is too long (max $MAX_COMMENT_LENGTH characters)")
            containsProhibitedWords(trimmedContent) -> ValidationResult.Error("Comment contains inappropriate content")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate support level
     */
    fun validateSupportLevel(level: Int): ValidationResult {
        return when {
            level < MIN_SUPPORT_LEVEL -> ValidationResult.Error("Support level too low")
            level > MAX_SUPPORT_LEVEL -> ValidationResult.Error("Support level too high")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate support type
     */
    fun validateSupportType(supportType: SupportType?): ValidationResult {
        return if (supportType != null) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Please select a support type")
        }
    }

    /**
     * Validate complete comment request
     */
    fun validateCommentRequest(
        content: String,
        supportType: SupportType?,
        supportLevel: Int,
        postId: String
    ): ValidationResult {

        if (postId.isBlank()) {
            return ValidationResult.Error("Invalid post")
        }

        val contentValidation = validateCommentContent(content)
        if (contentValidation is ValidationResult.Error) {
            return contentValidation
        }

        val typeValidation = validateSupportType(supportType)
        if (typeValidation is ValidationResult.Error) {
            return typeValidation
        }

        val levelValidation = validateSupportLevel(supportLevel)
        if (levelValidation is ValidationResult.Error) {
            return levelValidation
        }

        return ValidationResult.Success
    }

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    private fun containsProhibitedWords(content: String): Boolean {
        val lowercaseContent = content.lowercase()
        return PROHIBITED_WORDS.any { word ->
            lowercaseContent.contains(word)
        }
    }

    // ============================================================================
    // VALIDATION RESULT SEALED CLASS
    // ============================================================================

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    // ============================================================================
    // UTILITY EXTENSIONS
    // ============================================================================

    /**
     * Extension to clean comment content
     */
    fun String.cleanForComment(): String {
        return this.trim()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(MAX_COMMENT_LENGTH) // Truncate if too long
    }

    /**
     * Extension to get remaining character count
     */
    fun String.getRemainingCharacters(): Int {
        return maxOf(0, MAX_COMMENT_LENGTH - this.length)
    }

    /**
     * Extension to check if comment is valid length
     */
    fun String.isValidCommentLength(): Boolean {
        val trimmed = this.trim()
        return trimmed.length in MIN_COMMENT_LENGTH..MAX_COMMENT_LENGTH
    }
}