package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Generate random username use case
 * "Create a random adventurer name!"
 */
class GenerateRandomUsernameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): String {
        return authRepository.generateRandomUsername()
    }
}