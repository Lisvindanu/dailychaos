package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.UserProfile
import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Get user profile use case
 * "Check adventurer's detailed stats!"
 */
class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String? = null): Result<UserProfile> {
        return authRepository.getUserProfile(userId)
    }
}