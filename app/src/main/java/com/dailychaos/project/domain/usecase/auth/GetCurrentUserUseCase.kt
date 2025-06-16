// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/GetCurrentUserUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Get current user use case
 * "Who's in the party right now?"
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}