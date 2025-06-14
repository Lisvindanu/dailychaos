// File: app/src/main/java/com/dailychaos/project/domain/usecase/auth/GetAuthStateUseCase.kt
package com.dailychaos.project.domain.usecase.auth

import com.dailychaos.project.domain.model.AuthState
import com.dailychaos.project.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get authentication state use case
 * "Check party membership status!"
 */
class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> {
        return authRepository.getAuthState()
    }
}