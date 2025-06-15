// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/register/RegisterViewModel.kt
package com.dailychaos.project.presentation.ui.screen.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.preferences.UserPreferences
import com.dailychaos.project.util.ValidationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val userPreferences: UserPreferences,
    private val validationUtil: ValidationUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _registerSuccessEvent = MutableSharedFlow<Unit>()
    val registerSuccessEvent: SharedFlow<Unit> = _registerSuccessEvent.asSharedFlow()

    private var usernameValidationJob: Job? = null

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.DisplayNameChanged -> {
                _uiState.update { it.copy(displayName = event.displayName) }
            }
            is RegisterEvent.SwitchRegisterMode -> {
                _uiState.update {
                    it.copy(
                        registerMode = event.mode,
                        error = null,
                        usernameError = null,
                        emailError = null,
                        passwordError = null,
                        confirmPasswordError = null
                    )
                }
            }
            is RegisterEvent.UsernameChanged -> {
                _uiState.update {
                    it.copy(
                        username = event.username,
                        usernameError = null,
                        error = null
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
            is RegisterEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.email,
                        emailError = null,
                        error = null
                    )
                }
            }
            is RegisterEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = event.password,
                        passwordError = null,
                        error = null
                    )
                }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.confirmPassword,
                        confirmPasswordError = null,
                        error = null
                    )
                }
            }
            RegisterEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            RegisterEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            RegisterEvent.RegisterWithUsername -> registerWithUsername()
            RegisterEvent.RegisterWithEmail -> registerWithEmail()
            RegisterEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun validateUsernameWithDelay(username: String) {
        usernameValidationJob?.cancel()
        usernameValidationJob = viewModelScope.launch {
            delay(500) // Debounce validation
            validateUsername(username)
        }
    }

    private fun validateUsername(username: String) {
        if (username.isBlank()) {
            _uiState.update {
                it.copy(
                    isUsernameValid = false,
                    usernameError = null,
                    suggestions = emptyList()
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val validationResult = validationUtil.validateUsername(username)

                when {
                    validationResult.isValid -> {
                        // Check availability
                        val isAvailable = firebaseAuthService.checkUsernameAvailability(username)
                        if (isAvailable) {
                            _uiState.update {
                                it.copy(
                                    isUsernameValid = true,
                                    usernameError = null,
                                    suggestions = emptyList()
                                )
                            }
                        } else {
                            val suggestions = generateUsernameSuggestions(username)
                            _uiState.update {
                                it.copy(
                                    isUsernameValid = false,
                                    usernameError = "Username sudah digunakan!",
                                    suggestions = suggestions
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(
                                isUsernameValid = false,
                                usernameError = validationResult.message,
                                suggestions = emptyList()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUsernameValid = false,
                        usernameError = "Error validating username: ${e.message}",
                        suggestions = emptyList()
                    )
                }
            }
        }
    }

    private fun generateUsernameSuggestions(baseUsername: String): List<String> {
        val suggestions = mutableListOf<String>()
        val randomNumbers = (100..999).shuffled().take(3)

        randomNumbers.forEach { number ->
            suggestions.add("${baseUsername}$number")
        }

        // Add some creative variations
        val variations = listOf(
            "${baseUsername}_chaos",
            "${baseUsername}hero",
            "chaos_$baseUsername"
        )

        suggestions.addAll(variations.take(2))
        return suggestions.take(5)
    }

    private fun registerWithUsername() {
        val state = _uiState.value

        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username tidak boleh kosong!") }
            return
        }

        if (!state.isUsernameValid) {
            _uiState.update { it.copy(usernameError = "Username tidak valid atau sudah digunakan!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = firebaseAuthService.registerWithUsername(
                    username = state.username,
                    displayName = state.displayName.ifBlank { state.username }
                )

                if (result.isSuccess) {
                    // Mark first launch as completed after successful registration
                    userPreferences.setFirstLaunchCompleted()
                    _registerSuccessEvent.emit(Unit)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Registrasi gagal"
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
                        error = e.message ?: "Terjadi kesalahan saat registrasi"
                    )
                }
            }
        }
    }

    private fun registerWithEmail() {
        val state = _uiState.value

        // Validate email
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email tidak boleh kosong!") }
            return
        }

        if (!validationUtil.isValidEmail(state.email)) {
            _uiState.update { it.copy(emailError = "Format email tidak valid!") }
            return
        }

        // Validate password
        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password minimal 6 karakter!") }
            return
        }

        // Validate confirm password
        if (state.confirmPassword != state.password) {
            _uiState.update { it.copy(confirmPasswordError = "Password tidak sama!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = firebaseAuthService.registerWithEmail(
                    email = state.email,
                    password = state.password,
                    displayName = state.displayName.ifBlank { "Chaos Member" }
                )

                if (result.isSuccess) {
                    // Mark first launch as completed after successful registration
                    userPreferences.setFirstLaunchCompleted()
                    _registerSuccessEvent.emit(Unit)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Registrasi gagal"
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
                        error = e.message ?: "Terjadi kesalahan saat registrasi"
                    )
                }
            }
        }
    }
}