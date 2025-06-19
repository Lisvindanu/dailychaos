// Complete Fixed LoginViewModel.kt
package com.dailychaos.project.presentation.ui.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent: SharedFlow<Unit> = _loginSuccessEvent.asSharedFlow()

    private var validationJob: Job? = null

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.email, error = null) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password, error = null) }
            is LoginEvent.UsernameChanged -> onUsernameChanged(event.username)
            is LoginEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginEvent.LoginWithEmail -> loginWithEmail()
            is LoginEvent.LoginWithUsername -> loginWithUsername()
            is LoginEvent.LoginAnonymously -> loginAnonymously()
            is LoginEvent.SwitchLoginMode -> _uiState.update { it.copy(loginMode = event.mode, error = null, usernameError = null) }
            is LoginEvent.SuggestionClicked -> onUsernameChanged(event.suggestion)
            is LoginEvent.ClearError -> _uiState.update { it.copy(error = null, usernameError = null) }
        }
    }

    private fun onUsernameChanged(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameError = null,
                suggestions = emptyList(),
                // FIXED: Always set to true for login unless blank
                isUsernameValid = username.isNotBlank()
            )
        }

        // Background validation for suggestions only - doesn't block login
        validationJob?.cancel()
        if (username.isNotBlank()) {
            validationJob = viewModelScope.launch {
                delay(300)
                try {
                    validateUsername(username)
                } catch (e: Exception) {
                    // Ignore validation errors for login
                    Timber.w(e, "Username validation failed, but allowing login to proceed")
                }
            }
        }
    }

    private suspend fun validateUsername(username: String) {
        try {
            // Background validation for suggestions only
            val validation = authUseCases.validateUsername(username)
            _uiState.update {
                it.copy(
                    // FIXED: Always keep true for login - don't block login based on validation
                    isUsernameValid = true,
                    suggestions = validation.suggestions
                    // Don't set usernameError for login - let server handle it
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error validating username: $username")
            // On validation error, keep login available
            _uiState.update {
                it.copy(
                    isUsernameValid = true,
                    usernameError = null
                )
            }
        }
    }

    private fun loginWithEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authUseCases.loginWithEmail(
                email = _uiState.value.email,
                password = _uiState.value.password
            )
            result.onSuccess { user ->
                saveUserAndProceed(user)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Login failed") }
            }
        }
    }

    private fun loginWithUsername() {
        val state = _uiState.value

        // FIXED: Only check if blank - remove isUsernameValid check completely
        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Please enter your username.") }
            return
        }

        // Basic length check to prevent obvious errors
        if (state.username.length < 3) {
            _uiState.update { it.copy(usernameError = "Username must be at least 3 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, usernameError = null) }

            // Cancel any ongoing validation to prevent conflicts
            validationJob?.cancel()

            try {
                val result = authUseCases.loginWithUsername(state.username)

                result.onSuccess { user ->
                    saveUserAndProceed(user)
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Login failed"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error during username login")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "An unexpected error occurred. Please try again."
                    )
                }
            }
        }
    }

    private fun loginAnonymously() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Generate random username for anonymous user
                val randomUsername = generateRandomUsername()
                val displayName = randomUsername

                Timber.d("LoginViewModel: Starting anonymous login with username: $randomUsername")

                val result = authUseCases.registerWithUsername(
                    username = randomUsername,
                    displayName = displayName
                )

                result.onSuccess { user ->
                    Timber.d("LoginViewModel: Anonymous registration successful")
                    saveUserAndProceed(user)
                }.onFailure { error ->
                    Timber.e("LoginViewModel: Anonymous registration failed: ${error.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to start anonymous session"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "LoginViewModel: Unexpected error during anonymous login")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to create anonymous session. Please try again."
                    )
                }
            }
        }
    }

    private suspend fun saveUserAndProceed(user: User) {
        try {
            // Save user data to local preferences
            userPreferences.saveUserData(
                userId = user.id,
                username = user.anonymousUsername.takeIf { it.isNotEmpty() },
                displayName = user.displayName.ifBlank { user.anonymousUsername },
                email = user.email,
                authType = when {
                    user.isAnonymous -> "anonymous"
                    user.email.isNullOrBlank() -> "username"
                    else -> "email"
                }
            )

            _uiState.update { it.copy(isLoading = false) }
            _loginSuccessEvent.emit(Unit)

            Timber.d("Login successful for user: ${user.displayName}")
        } catch (e: Exception) {
            Timber.e(e, "Error saving user data after login")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Login successful but failed to save user data. Please try again."
                )
            }
        }
    }

    private fun generateRandomUsername(): String {
        val adjectives = listOf("Epic", "Chaos", "Brave", "Wild", "Cool", "Swift", "Bold", "Lucky", "Mighty", "Noble")
        val nouns = listOf("Adventurer", "Hero", "Warrior", "Mage", "Explorer", "Knight", "Rogue", "Wizard", "Champion", "Guardian")
        val number = (1000..9999).random()

        val adjective = adjectives.random()
        val noun = nouns.random()

        return "${adjective}${noun}${number}"
    }

    override fun onCleared() {
        super.onCleared()
        validationJob?.cancel()
    }
}