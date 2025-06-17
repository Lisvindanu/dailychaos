package com.dailychaos.project.presentation.ui.screen.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Focus management
    val usernameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loginSuccessEvent.collectLatest {
            Timber.d("🎉 LOGIN SUCCESS EVENT RECEIVED IN LOGINSCREEN!")
            Timber.d("🎉 About to call onLoginSuccess() callback")
            onLoginSuccess()
            Timber.d("🎉 onLoginSuccess() callback called - should navigate to HOME now")
        }
    }

    // Auto-focus username field when switching to username mode
    LaunchedEffect(uiState.loginMode) {
        if (uiState.loginMode == LoginMode.USERNAME) {
            try {
                usernameFocusRequester.requestFocus()
            } catch (e: Exception) {
                // Handle exception if focus fails
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "🌪️",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (uiState.loginMode) {
                    LoginMode.USERNAME -> "Join the Chaotic Party!"
                    LoginMode.EMAIL -> "Welcome Back, Adventurer!"
                    LoginMode.ANONYMOUS -> "Anonymous Adventure Awaits!"
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = when (uiState.loginMode) {
                    LoginMode.USERNAME -> "Choose your adventurer name and dive into the chaos!"
                    LoginMode.EMAIL -> "Your party awaits your return."
                    LoginMode.ANONYMOUS -> "No strings attached, just pure chaos."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Mode Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LoginModeButton(
                        text = "Username",
                        isSelected = uiState.loginMode == LoginMode.USERNAME,
                        onClick = { viewModel.onEvent(LoginEvent.SwitchLoginMode(LoginMode.USERNAME)) },
                        modifier = Modifier.weight(1f)
                    )
                    LoginModeButton(
                        text = "Email",
                        isSelected = uiState.loginMode == LoginMode.EMAIL,
                        onClick = { viewModel.onEvent(LoginEvent.SwitchLoginMode(LoginMode.EMAIL)) },
                        modifier = Modifier.weight(1f)
                    )
                    LoginModeButton(
                        text = "Anonymous",
                        isSelected = uiState.loginMode == LoginMode.ANONYMOUS,
                        onClick = { viewModel.onEvent(LoginEvent.SwitchLoginMode(LoginMode.ANONYMOUS)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Messages
            uiState.error?.let {
                ErrorMessage(message = it)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Login Forms based on mode
            when (uiState.loginMode) {
                LoginMode.USERNAME -> {
                    UsernameLoginForm(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        focusRequester = usernameFocusRequester,
                        onLogin = { viewModel.onEvent(LoginEvent.LoginWithUsername) }
                    )
                }
                LoginMode.EMAIL -> {
                    EmailLoginForm(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onLogin = { viewModel.onEvent(LoginEvent.LoginWithEmail) }
                    )
                }
                LoginMode.ANONYMOUS -> {
                    AnonymousLoginForm(
                        onLogin = { viewModel.onEvent(LoginEvent.LoginAnonymously) },
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigate to Register (only for email mode)
            AnimatedVisibility(
                visible = uiState.loginMode == LoginMode.EMAIL,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New to the guild?")
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Register Here")
                    }
                }
            }
        }

        if (uiState.isLoading) {
            LoadingIndicator(
                modifier = Modifier.align(Alignment.Center),
                message = when (uiState.loginMode) {
                    LoginMode.USERNAME -> "Registering adventurer..."
                    LoginMode.EMAIL -> "Authenticating..."
                    LoginMode.ANONYMOUS -> "Preparing anonymous adventure..."
                }
            )
        }
    }
}

@Composable
private fun LoginModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (isSelected) {
        ButtonDefaults.filledTonalButtonColors()
    } else {
        ButtonDefaults.textButtonColors()
    }

    Button(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 2.dp),
        colors = colors,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun UsernameLoginForm(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    focusRequester: FocusRequester,
    onLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column {
        // Username Field
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            supportingText = {
                uiState.usernameError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = uiState.usernameError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLogin()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        // Username Suggestions
        AnimatedVisibility(
            visible = uiState.suggestions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Suggestions:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.suggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { onEvent(LoginEvent.SuggestionClicked(suggestion)) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.username.isNotBlank()
        ) {
            Text("Join the Party! 🎉")
        }
    }
}

@Composable
private fun EmailLoginForm(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onLogin: () -> Unit
) {
    // --- FIX: Deklarasi focus manager dan requester di sini ---
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    Column {
        // Email Field
        // Menggunakan OutlinedTextField karena CustomTextField tidak punya semua parameter yg dibutuhkan
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                val image = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { onEvent(LoginEvent.TogglePasswordVisibility) }) {
                    Icon(imageVector = image, "Toggle password visibility")
                }
            },
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLogin()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester) // Terapkan requester di sini
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank()
        ) {
            Text("Login with Email")
        }
    }
}

@Composable
private fun AnonymousLoginForm(
    onLogin: () -> Unit,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎭",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Registration Required!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jump straight into the chaos with a random username. You can always upgrade later!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Start Anonymous Adventure! 🚀")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DailyChaosTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToRegister = {})
    }
}