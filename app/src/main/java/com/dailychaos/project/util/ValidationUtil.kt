package com.dailychaos.project.util

/**
 * Validation utilities untuk Daily Chaos
 * "Making sure our chaos is organized chaos!"
 */
class ValidationUtil {

    companion object {
        /**
         * Clean username by removing invalid characters
         */
        fun cleanUsername(username: String): String {
            return username.cleanUsername()
        }

        /**
         * Generate display name from username
         */
        fun generateDisplayName(username: String): String {
            return username.toDisplayName()
        }

        /**
         * Validate username format (basic rules)
         */
        fun isValidUsernameFormat(username: String): Boolean {
            return username.isValidUsernameFormat()
        }

        /**
         * Check if username contains forbidden words
         */
        fun containsForbiddenWords(username: String): Boolean {
            return username.containsForbiddenWords()
        }

        /**
         * Generate username suggestions based on base name
         */
        fun generateUsernameSuggestions(baseUsername: String): List<String> {
            return baseUsername.generateUsernameSuggestions()
        }

        /**
         * Get fun validation message for username errors
         * "KonoSuba-themed validation messages!"
         */
        fun getUsernameErrorMessage(username: String): String? {
            return username.getUsernameErrorMessage()
        }
    }
}