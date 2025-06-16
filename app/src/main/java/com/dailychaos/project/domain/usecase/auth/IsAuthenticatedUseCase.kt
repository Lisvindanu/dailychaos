// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/IsAuthenticatedUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Check if user is authenticated use case
 * "Quick party membership check!"
 */
class IsAuthenticatedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isAuthenticated()
    }
}