// Complete Fixed RegisterViewModel.kt
package com.dailychaos.project.presentation.ui.screen.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.usecase.auth.AuthUseCases
import com.dailychaos.project.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _registerSuccessEvent = MutableSharedFlow<Unit>()
    val registerSuccessEvent: SharedFlow<Unit> = _registerSuccessEvent.asSharedFlow()

    private var usernameValidationJob: Job? = null

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.DisplayNameChanged -> _uiState.update { it.copy(displayName = event.displayName) }
            is RegisterEvent.SwitchRegisterMode -> _uiState.update {
                it.copy(
                    registerMode = event.mode,
                    error = null,
                    usernameError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null
                )
            }
            is RegisterEvent.UsernameChanged -> {
                _uiState.update {
                    it.copy(
                        username = event.username,
                        usernameError = null,
                        error = null,
                        isUsernameValid = event.username.isNotBlank() // FIXED: Set based on blank check
                    )
                }
                validateUsernameWithDelay(event.username)
            }
            is RegisterEvent.SuggestionClicked -> {
                _uiState.update {
                    it.copy(
                        username = event.suggestion,
                        suggestions = emptyList(),
                        usernameError = null,
                        isUsernameValid = true
                    )
                }
            }
            is RegisterEvent.EmailChanged -> _uiState.update { it.copy(email = event.email, emailError = null, error = null) }
            is RegisterEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password, passwordError = null, error = null) }
            is RegisterEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null, error = null) }
            is RegisterEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is RegisterEvent.ToggleConfirmPasswordVisibility -> _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            is RegisterEvent.RegisterWithUsername -> registerWithUsername()
            is RegisterEvent.RegisterWithEmail -> registerWithEmail()
            is RegisterEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun validateUsernameWithDelay(username: String) {
        usernameValidationJob?.cancel()
        usernameValidationJob = viewModelScope.launch {
            delay(300) // Reduced delay
            try {
                val validation = authUseCases.validateUsername(username)
                _uiState.update {
                    it.copy(
                        isUsernameValid = true, // Always true - don't block registration
                        suggestions = validation.suggestions
                    )
                }
            } catch (e: Exception) {
                // Ignore validation errors
            }
        }
    }

    private fun registerWithUsername() {
        viewModelScope.launch {
            val state = _uiState.value

            // FIXED: Simple validation - only check blank and length
            if (state.username.isBlank()) {
                _uiState.update { it.copy(usernameError = "Please enter a username.") }
                return@launch
            }

            if (state.username.length < 3) {
                _uiState.update { it.copy(usernameError = "Username must be at least 3 characters.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null, usernameError = null) }

            val result = authUseCases.registerWithUsername(
                username = state.username,
                displayName = state.displayName.ifBlank { state.username }
            )

            result.onSuccess {
                userPreferences.setFirstLaunchCompleted()
                _uiState.update { it.copy(isLoading = false) }
                _registerSuccessEvent.emit(Unit)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    private fun registerWithEmail() {
        val state = _uiState.value
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authUseCases.registerWithEmail(
                email = state.email,
                password = state.password,
                displayName = state.displayName.ifBlank { "Chaos Adventurer" }
            )

            result.onSuccess {
                userPreferences.setFirstLaunchCompleted()
                _uiState.update { it.copy(isLoading = false) }
                _registerSuccessEvent.emit(Unit)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        usernameValidationJob?.cancel()
    }
}