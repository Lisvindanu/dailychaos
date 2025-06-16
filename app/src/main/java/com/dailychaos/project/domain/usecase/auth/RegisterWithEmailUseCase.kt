// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/RegisterWithEmailUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.util.ValidationUtil
import javax.inject.Inject

/**
 * Register dengan email use case
 * "Registrasi party cara tradisional dengan verifikasi email!"
 */
class RegisterWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val validationUtil: ValidationUtil
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        // Validate email format
        if (!validationUtil.isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Hei, ini bukan format mantra! Pastikan email-nya benar."))
        }

        // Validate password
        val passwordValidation = validationUtil.validatePassword(password)
        if (!passwordValidation.isValid) {
            val errorMessage = passwordValidation.errorMessage ?: "Password-nya jangan kosong, nanti guild-nya kebobolan!"
            return Result.failure(IllegalArgumentException(errorMessage))
        }

        // Validate display name
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Setiap petualang butuh nama panggilan! Jangan dikosongin."))
        }

        return authRepository.registerWithEmail(email, password, displayName)
    }
}