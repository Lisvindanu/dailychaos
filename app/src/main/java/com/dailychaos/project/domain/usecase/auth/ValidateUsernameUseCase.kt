// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/ValidateUsernameUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Validate username use case
 * "Make sure party names are appropriate!"
 */
class ValidateUsernameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String): UsernameValidation {
        return authRepository.validateUsername(username)
    }
}