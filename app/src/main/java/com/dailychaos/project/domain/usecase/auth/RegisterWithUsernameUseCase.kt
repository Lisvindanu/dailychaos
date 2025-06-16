package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Register dengan username use case
 * "Quick party join - chaos style!"
 */
class RegisterWithUsernameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, displayName: String): Result<User> {
        // Validate username terlebih dahulu
        val usernameValidation = authRepository.validateUsername(username)
        if (!usernameValidation.isValid) {
            return Result.failure(IllegalArgumentException(usernameValidation.message))
        }

        // Validate display name tidak kosong
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Display name cannot be empty"))
        }

        return authRepository.registerWithUsername(username, displayName)
    }
}