// File: app/src/main/java/com/dailychaos/project/domain/model/UsernameValidation.kt
package com.dailychaos.project.domain.model

/**
 * Username validation result untuk Daily Chaos
 * "Making sure party names are appropriate!"
 */
data class UsernameValidation(
    val isValid: Boolean,
    val message: String,
    val suggestions: List<String> = emptyList()
)