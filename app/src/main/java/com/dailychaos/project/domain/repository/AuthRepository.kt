// File: app/src/main/java/com/dailychaos/project/domain/repository/AuthRepository.kt
package com.dailychaos.project.domain.repository

import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UserProfile
import com.dailychaos.project.domain.model.UsernameValidation
import kotlinx.coroutines.flow.Flow

/**
 * Authentication Repository Interface
 * "Define the party registration rules!"
 */
interface AuthRepository {
    // Auth state management
    fun getAuthState(): Flow<AuthState>
    fun isAuthenticated(): Boolean
    suspend fun getCurrentUser(): User?

    // Login methods
    suspend fun loginWithUsername(username: String): Result<User>
    suspend fun loginWithEmail(email: String, password: String): Result<User>

    // Registration methods
    suspend fun registerWithUsername(username: String, displayName: String): Result<User>
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<User>
    suspend fun registerAnonymous(username: String, displayName: String): Result<User>

    // Account management
    suspend fun logout(): Result<Unit>
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>

    // Profile management
    suspend fun getUserProfile(userId: String? = null): Result<UserProfile>

    // Validation and utility
    suspend fun validateUsername(username: String): UsernameValidation
    suspend fun checkUsernameAvailability(username: String): Boolean
    fun generateRandomUsername(): String
}