// File: app/src/main/java/com/dailychaos/project/domain/repository/AuthRepository.kt
package com.dailychaos.project.domain.repository

import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UsernameValidation
import kotlinx.coroutines.flow.Flow

/**
 * Authentication Repository Interface
 * "Define the party registration rules!"
 */
interface AuthRepository {
    fun getAuthState(): Flow<AuthState>
    suspend fun loginWithUsername(username: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun validateUsername(username: String): UsernameValidation
    fun isAuthenticated(): Boolean
}

