package com.dailychaos.project.presentation.ui.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    // private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _uiState.update { it.copy(email = event.email) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password) }
            is LoginEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginEvent.LoginWithEmail -> loginWithEmail()
            is LoginEvent.LoginAnonymously -> loginAnonymously()
            is LoginEvent.ClearError -> _uiState.update { it.copy(error = null) }
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
            // Mock logic
            kotlinx.coroutines.delay(1500)
            // if (Random.nextBoolean()) { // Simulate success/failure
            _loginSuccessEvent.emit(Unit)
            // } else {
            //     _uiState.update { it.copy(isLoading = false, error = "Login failed. Please check your credentials.") }
            // }
        }
    }

    private fun loginAnonymously() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Mock logic
            kotlinx.coroutines.delay(1000)
            _loginSuccessEvent.emit(Unit)
        }
    }
}