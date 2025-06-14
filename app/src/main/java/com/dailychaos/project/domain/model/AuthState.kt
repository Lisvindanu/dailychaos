// File: app/src/main/java/com/dailychaos/project/domain/model/AuthState.kt
package com.dailychaos.project.domain.model

/**
 * Authentication State untuk Daily Chaos
 * "Track the party member status!"
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(
        val user: User,
        val isFirstTime: Boolean = false
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}