package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Login dengan username use case
 * "Join the chaotic party!"
 */
class LoginWithUsernameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String): Result<User> {
        return authRepository.loginWithUsername(username)
    }
}