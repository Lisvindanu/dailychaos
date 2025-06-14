package com.dailychaos.project.presentation.ui.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.domain.model.UsernameValidation
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
import com.dailychaos.project.util.isValidEmail
import com.dailychaos.project.util.isValidPassword
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
    private val firebaseAuthService: FirebaseAuthService,
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
            is LoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.email) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password) }
            is LoginEvent.UsernameChanged -> onUsernameChanged(event.username)
            is LoginEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginEvent.LoginWithEmail -> loginWithEmail()
            is LoginEvent.LoginWithUsername -> loginWithUsername()
            is LoginEvent.LoginAnonymously -> loginAnonymously()
            is LoginEvent.SwitchLoginMode -> _uiState.update { it.copy(loginMode = event.mode, error = null) }
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
        validationJob = viewModelScope.launch {
            delay(500) // Wait 500ms before validating
            if (username.isNotBlank()) {
                validateUsername(username)
            }
        }
    }

    private suspend fun validateUsername(username: String) {
        val validation = validateUsernameBasic(username)
        _uiState.update {
            it.copy(
                isUsernameValid = validation.isValid,
                usernameError = if (validation.isValid) null else validation.message,
                suggestions = validation.suggestions
            )
        }
    }

    private fun validateUsernameBasic(username: String): UsernameValidation {
        val errorMessage = ValidationUtil.getUsernameErrorMessage(username)
        return if (errorMessage == null) {
            UsernameValidation(true, "Username valid!")
        } else {
            UsernameValidation(
                false,
                errorMessage,
                if (!ValidationUtil.isValidUsernameFormat(username)) {
                    ValidationUtil.generateUsernameSuggestions(username)
                } else emptyList()
            )
        }
    }

    private fun loginWithEmail() {
        val state = _uiState.value
        if (!state.email.isValidEmail()) {
            _uiState.update { it.copy(error = "Invalid email format.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // TODO: Implement email login with Firebase
                // For now, mock implementation
                delay(1500)
                _loginSuccessEvent.emit(Unit)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Login failed. Please check your credentials."
                    )
                }
            }
        }
    }

    private fun loginWithUsername() {
        val state = _uiState.value

        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username tidak boleh kosong!") }
            return
        }

        if (!state.isUsernameValid) {
            _uiState.update { it.copy(usernameError = "Username tidak valid!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = firebaseAuthService.loginWithUsername(state.username)

                if (result.isSuccess) {
                    // Mark first launch as completed after successful login
                    userPreferences.setFirstLaunchCompleted()
                    _loginSuccessEvent.emit(Unit)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Login gagal"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Terjadi kesalahan saat login"
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
                val randomUsername = "Anonymous${(1000..9999).random()}"
                val result = firebaseAuthService.loginWithUsername(randomUsername)

                if (result.isSuccess) {
                    // Mark first launch as completed
                    userPreferences.setFirstLaunchCompleted()
                    _loginSuccessEvent.emit(Unit)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Anonymous login gagal"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Terjadi kesalahan saat login anonymous"
                    )
                }
            }
        }
    }
}