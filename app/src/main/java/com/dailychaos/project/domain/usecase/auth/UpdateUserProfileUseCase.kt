// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/UpdateUserProfileUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Update user profile use case
 * "Customize your party member status!"
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(updates: Map<String, Any>): Result<Unit> {
        return authRepository.updateUserProfile(updates)
    }
}