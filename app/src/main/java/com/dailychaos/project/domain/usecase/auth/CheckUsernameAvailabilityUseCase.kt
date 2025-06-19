package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Check username availability use case
 * "Is this party name taken?"
 */
class CheckUsernameAvailabilityUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String): Boolean {
        return authRepository.checkUsernameAvailability(username)
    }
}