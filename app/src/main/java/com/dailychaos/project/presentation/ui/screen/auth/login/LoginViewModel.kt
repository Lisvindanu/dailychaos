/* app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/login/LoginViewModel.kt */
package com.dailychaos.project.presentation.ui.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()

    // Debounce job for username validation
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
                suggestions = emptyList()
            )
        }

        // Debounce validation
        validationJob?.cancel()
        if (username.isNotBlank()) {
            validationJob = viewModelScope.launch {
                delay(500) // Wait 500ms before validating
                validateUsername(username)
            }
        } else {
            _uiState.update { it.copy(isUsernameValid = false, usernameError = null) }
        }
    }

    private suspend fun validateUsername(username: String) {
        // We only perform basic format validation here on login.
        // The check for existence happens on the server.
        val validation = authUseCases.validateUsername(username)
        _uiState.update {
            it.copy(
                isUsernameValid = validation.isValid,
                usernameError = if (validation.isValid) null else validation.message,
                suggestions = validation.suggestions
            )
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
        if (state.username.isBlank() || !state.isUsernameValid) {
            _uiState.update { it.copy(usernameError = "Please enter a valid username.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // **FIXED**: Call the correct use case for logging in with a username.
            val result = authUseCases.loginWithUsername(state.username)

            result.onSuccess { user ->
                // After getting the user data, save it to preferences and proceed.
                saveUserAndProceed(user)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Login failed") }
            }
        }
    }

    private fun loginAnonymously() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // This flow is for creating a new anonymous user, so calling register is correct.
            val randomUsername = "Adventurer${(1000..9999).random()}"
            val result = authUseCases.registerWithUsername(
                username = randomUsername,
                displayName = randomUsername
            )

            result.onSuccess { user ->
                saveUserAndProceed(user)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to start anonymous session") }
            }
        }
    }

    private suspend fun saveUserAndProceed(user: User) {
        // Save the fetched user's data to local preferences to maintain the session
        userPreferences.saveUserData(
            userId = user.id,
            username = user.anonymousUsername.takeIf { it.isNotEmpty() },
            displayName = user.anonymousUsername, // Simplified for now
            email = user.email
        )
        _uiState.update { it.copy(isLoading = false) }
        _loginSuccessEvent.emit(Unit)
    }
}