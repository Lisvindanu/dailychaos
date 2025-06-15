// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/register/RegisterScreen.kt
package com.dailychaos.project.presentation.ui.screen.auth.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailychaos.project.presentation.ui.theme.ChaosColors
import com.dailychaos.project.presentation.ui.component.PasswordStrengthIndicator
import com.dailychaos.project.util.PasswordStrength

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit, // FIX: Callback akan redirect ke login screen
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Observe register success event - FIX: Navigation akan ke login
    LaunchedEffect(Unit) {
        viewModel.registerSuccessEvent.collect {
            // Ketika register berhasil, arahkan ke login screen
            onRegisterSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header - UPDATED dengan pesan yang lebih jelas
            RegisterHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Register Mode Toggle
            RegisterModeToggle(
                selectedMode = uiState.registerMode,
                onModeSelected = { viewModel.onEvent(RegisterEvent.SwitchRegisterMode(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Form
            when (uiState.registerMode) {
                RegisterMode.USERNAME -> {
                    UsernameRegisterForm(
                        username = uiState.username,
                        displayName = uiState.displayName,
                        isUsernameValid = uiState.isUsernameValid,
                        usernameError = uiState.usernameError,
                        suggestions = uiState.suggestions,
                        isLoading = uiState.isLoading,
                        onUsernameChanged = { viewModel.onEvent(RegisterEvent.UsernameChanged(it)) },
                        onDisplayNameChanged = { viewModel.onEvent(RegisterEvent.DisplayNameChanged(it)) },
                        onSuggestionClicked = { viewModel.onEvent(RegisterEvent.SuggestionClicked(it)) },
                        onRegister = { viewModel.onEvent(RegisterEvent.RegisterWithUsername) },
                        focusManager = focusManager
                    )
                }
                RegisterMode.EMAIL -> {
                    EmailRegisterForm(
                        email = uiState.email,
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        displayName = uiState.displayName,
                        isPasswordVisible = uiState.isPasswordVisible,
                        isConfirmPasswordVisible = uiState.isConfirmPasswordVisible,
                        isLoading = uiState.isLoading,
                        emailError = uiState.emailError, // ADDED error handling
                        passwordError = uiState.passwordError, // ADDED error handling
                        confirmPasswordError = uiState.confirmPasswordError, // ADDED error handling
                        onEmailChanged = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
                        onPasswordChanged = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
                        onConfirmPasswordChanged = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                        onDisplayNameChanged = { viewModel.onEvent(RegisterEvent.DisplayNameChanged(it)) },
                        onTogglePasswordVisibility = { viewModel.onEvent(RegisterEvent.TogglePasswordVisibility) },
                        onToggleConfirmPasswordVisibility = { viewModel.onEvent(RegisterEvent.ToggleConfirmPasswordVisibility) },
                        onRegister = { viewModel.onEvent(RegisterEvent.RegisterWithEmail) },
                        focusManager = focusManager
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Redirect - UPDATED dengan pesan yang lebih jelas
            LoginRedirectSection(onNavigateToLogin = onNavigateToLogin)
        }
    }
}

@Composable
private fun RegisterHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Join the Party! 🎉",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ChaosColors.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Buat akun dan mulai petualangan chaos-mu bersama party yang solid!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        // ADDED: Info tentang flow registrasi
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Setelah registrasi, kamu akan diarahkan ke halaman login untuk masuk",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun RegisterModeToggle(
    selectedMode: RegisterMode,
    onModeSelected: (RegisterMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            RegisterModeButton(
                text = "Username",
                icon = Icons.Default.Person,
                isSelected = selectedMode == RegisterMode.USERNAME,
                onClick = { onModeSelected(RegisterMode.USERNAME) },
                modifier = Modifier.weight(1f)
            )
            RegisterModeButton(
                text = "Email",
                icon = Icons.Default.Email,
                isSelected = selectedMode == RegisterMode.EMAIL,
                onClick = { onModeSelected(RegisterMode.EMAIL) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RegisterModeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) ChaosColors.primary else Color.Transparent,
            contentColor = if (isSelected) ChaosColors.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun UsernameRegisterForm(
    username: String,
    displayName: String,
    isUsernameValid: Boolean,
    usernameError: String?,
    suggestions: List<String>,
    isLoading: Boolean,
    onUsernameChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSuggestionClicked: (String) -> Unit,
    onRegister: () -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Display Name Field
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text("Display Name (Optional)") },
            placeholder = { Text("Contoh: Kazuma si Petualang") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            placeholder = { Text("Contoh: kazuma_adventurer") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (username.isNotEmpty()) {
                    Icon(
                        imageVector = if (isUsernameValid) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isUsernameValid) ChaosColors.success else MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = usernameError != null,
            supportingText = if (usernameError != null) {
                { Text(usernameError) }
            } else null,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onRegister() }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Username Suggestions
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saran username:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { onSuggestionClicked(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = onRegister,
            enabled = isUsernameValid && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChaosColors.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = ChaosColors.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Join the Party!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun EmailRegisterForm(
    email: String,
    password: String,
    confirmPassword: String,
    displayName: String,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    isLoading: Boolean,
    emailError: String?, // ADDED
    passwordError: String?, // ADDED
    confirmPasswordError: String?, // ADDED
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegister: () -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Display Name Field
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text("Display Name (Optional)") },
            placeholder = { Text("Contoh: Kazuma si Petualang") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            placeholder = { Text("contoh@email.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            isError = emailError != null, // ADDED
            supportingText = if (emailError != null) { // ADDED
                { Text(emailError) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            placeholder = { Text("Minimal 6 karakter") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = passwordError != null, // ADDED
            supportingText = if (passwordError != null) { // ADDED
                { Text(passwordError) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Strength Indicator
        if (password.isNotEmpty()) {
            PasswordStrengthIndicator(
                password = password,
                strength = calculatePasswordStrength(password)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = { Text("Confirm Password") },
            placeholder = { Text("Ketik ulang password") },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = confirmPasswordError != null, // ADDED
            supportingText = if (confirmPasswordError != null) { // ADDED
                { Text(confirmPasswordError) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onRegister() }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = onRegister,
            enabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChaosColors.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = ChaosColors.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun LoginRedirectSection(
    onNavigateToLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.8f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah punya akun? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Login",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ChaosColors.primary
                ),
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

/**
 * Calculate password strength for UI display
 */
private fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0

    // Length bonus
    if (password.length >= 8) score += 1
    if (password.length >= 12) score += 1

    // Character variety
    if (password.any { it.isLowerCase() }) score += 1
    if (password.any { it.isUpperCase() }) score += 1
    if (password.any { it.isDigit() }) score += 1
    if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score += 1

    return when (score) {
        in 0..2 -> PasswordStrength.WEAK
        in 3..4 -> PasswordStrength.MEDIUM
        in 5..6 -> PasswordStrength.STRONG
        else -> PasswordStrength.VERY_STRONG
    }
}