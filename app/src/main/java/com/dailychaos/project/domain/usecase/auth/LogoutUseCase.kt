// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/LogoutUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Logout use case
 * "Leave the party (temporarily)!"
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}