package com.dailychaos.project.domain.usecase.auth

import javax.inject.Inject

/**
 * Auth Use Cases container
 * "All party management operations in one place!"
 */
data class AuthUseCases @Inject constructor(
    val getAuthState: GetAuthStateUseCase,
    val loginWithUsername: LoginWithUsernameUseCase,
    val loginWithEmail: LoginWithEmailUseCase,
    val registerWithUsername: RegisterWithUsernameUseCase,
    val registerWithEmail: RegisterWithEmailUseCase,
    val validateUsername: ValidateUsernameUseCase,
    val logout: LogoutUseCase,
    val getCurrentUser: GetCurrentUserUseCase,
    val isAuthenticated: IsAuthenticatedUseCase,
    val updateUserProfile: UpdateUserProfileUseCase,
    val deleteAccount: DeleteAccountUseCase
)