package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Register anonymous user use case
 * "Quick anonymous party join!"
 */
class RegisterAnonymousUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, displayName: String): Result<User> {
        // Validate username first
        val usernameValidation = authRepository.validateUsername(username)
        if (!usernameValidation.isValid) {
            return Result.failure(IllegalArgumentException(usernameValidation.message))
        }

        // Validate display name
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Display name tidak boleh kosong"))
        }

        return authRepository.registerAnonymous(username, displayName)
    }
}