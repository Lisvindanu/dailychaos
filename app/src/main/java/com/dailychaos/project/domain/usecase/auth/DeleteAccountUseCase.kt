// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/DeleteAccountUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Delete account use case
 * "Leave the party permanently!"
 */
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.deleteAccount()
    }
}