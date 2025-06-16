package com.dailychaos.project.di

import com.dailychaos.project.domain.repository.AuthRepository
import com.dailychaos.project.domain.usecase.auth.*
import com.dailychaos.project.util.ValidationUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Auth Use Case DI Module
 * "Setting up the party management department!"
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthUseCaseModule {

    @Provides
    @Singleton
    fun provideGetAuthStateUseCase(
        authRepository: AuthRepository
    ): GetAuthStateUseCase = GetAuthStateUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLoginWithUsernameUseCase(
        authRepository: AuthRepository
    ): LoginWithUsernameUseCase = LoginWithUsernameUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLoginWithEmailUseCase(
        authRepository: AuthRepository,
        validationUtil: ValidationUtil
    ): LoginWithEmailUseCase = LoginWithEmailUseCase(authRepository, validationUtil)

    @Provides
    @Singleton
    fun provideRegisterWithUsernameUseCase(
        authRepository: AuthRepository
    ): RegisterWithUsernameUseCase = RegisterWithUsernameUseCase(authRepository)

    @Provides
    @Singleton
    fun provideRegisterWithEmailUseCase(
        authRepository: AuthRepository,
        validationUtil: ValidationUtil
    ): RegisterWithEmailUseCase = RegisterWithEmailUseCase(authRepository, validationUtil)

    @Provides
    @Singleton
    fun provideValidateUsernameUseCase(
        authRepository: AuthRepository
    ): ValidateUsernameUseCase = ValidateUsernameUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(
        authRepository: AuthRepository
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(authRepository)

    @Provides
    @Singleton
    fun provideIsAuthenticatedUseCase(
        authRepository: AuthRepository
    ): IsAuthenticatedUseCase = IsAuthenticatedUseCase(authRepository)

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(
        authRepository: AuthRepository
    ): UpdateUserProfileUseCase = UpdateUserProfileUseCase(authRepository)

    @Provides
    @Singleton
    fun provideDeleteAccountUseCase(
        authRepository: AuthRepository
    ): DeleteAccountUseCase = DeleteAccountUseCase(authRepository)

    @Provides
    @Singleton
    fun provideAuthUseCases(
        getAuthState: GetAuthStateUseCase,
        loginWithUsername: LoginWithUsernameUseCase,
        loginWithEmail: LoginWithEmailUseCase,
        registerWithUsername: RegisterWithUsernameUseCase,
        registerWithEmail: RegisterWithEmailUseCase,
        validateUsername: ValidateUsernameUseCase,
        logout: LogoutUseCase,
        getCurrentUser: GetCurrentUserUseCase,
        isAuthenticated: IsAuthenticatedUseCase,
        updateUserProfile: UpdateUserProfileUseCase,
        deleteAccount: DeleteAccountUseCase
    ): AuthUseCases = AuthUseCases(
        getAuthState = getAuthState,
        loginWithUsername = loginWithUsername,
        loginWithEmail = loginWithEmail,
        registerWithUsername = registerWithUsername,
        registerWithEmail = registerWithEmail,
        validateUsername = validateUsername,
        logout = logout,
        getCurrentUser = getCurrentUser,
        isAuthenticated = isAuthenticated,
        updateUserProfile = updateUserProfile,
        deleteAccount = deleteAccount
    )
}