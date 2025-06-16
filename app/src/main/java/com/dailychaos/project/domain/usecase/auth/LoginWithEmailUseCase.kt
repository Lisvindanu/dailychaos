package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.util.ValidationUtil
import javax.inject.Inject

/**
 * Login dengan email use case
 * "Traditional party entry method!"
 */
class LoginWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validationUtil: ValidationUtil
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validate email format
        if (!validationUtil.isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Email format tidak valid"))
        }

        // Validate password
        val passwordValidation = validationUtil.validatePassword(password)
        if (!passwordValidation.isValid) {
            return Result.failure(IllegalArgumentException(passwordValidation.errorMessage ?: "Password tidak valid"))
        }

        return authRepository.loginWithEmail(email, password)
    }
}
