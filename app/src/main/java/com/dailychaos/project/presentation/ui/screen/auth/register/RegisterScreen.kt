package com.dailychaos.project.presentation.ui.screen.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.CustomTextField

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    // ViewModel and State would be implemented here similar to LoginScreen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🤝",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Join the Guild!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Create an account to save your adventures to the cloud.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField(value = "", onValueChange = {}, label = "Username (for display)")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(value = "", onValueChange = {}, label = "Email")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField(value = "", onValueChange = {}, label = "Password")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegisterSuccess, modifier = Modifier.fillMaxWidth()) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already in the party?")
            TextButton(onClick = onNavigateToLogin) {
                Text("Login Here")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    DailyChaosTheme {
        RegisterScreen({}, {})
    }
}