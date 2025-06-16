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
            return Result.failure(IllegalArgumentException("Hei, ini bukan format mantra! Pastikan email-nya benar."))
        }

        // Validate password
        val passwordValidation = validationUtil.validatePassword(password)
        if (!passwordValidation.isValid) {
            // Di sini kita gunakan pesan error yang sudah ada dari ValidationUtil
            // yang juga sudah kita buat lucu sebelumnya.
            val errorMessage = passwordValidation.errorMessage ?: "Password-nya jangan kosong, nanti guild-nya kebobolan!"
            return Result.failure(IllegalArgumentException(errorMessage))
        }

        return authRepository.loginWithEmail(email, password)
    }
}