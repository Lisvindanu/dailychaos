package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.MainNavigationScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point aplikasi Daily Chaos
 *
 * "Seperti guild hall dimana semua petualangan dimulai!"
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DailyChaosTheme {
                DailyChaosApp()
            }
        }
    }
}

@Composable
fun DailyChaosApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // State untuk authentication (placeholder untuk sekarang)
    var isAuthenticated by remember { mutableStateOf(true) } // Set true untuk skip auth di development

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isAuthenticated) {
            // Main app navigation
            MainNavigationScreen(
                navController = navController,
                onNavigateToAuth = {
                    // Handle logout atau navigate ke auth screen
                    isAuthenticated = false
                }
            )
        } else {
            // Authentication screens (placeholder untuk sekarang)
            AuthenticationFlow(
                onAuthenticationSuccess = {
                    isAuthenticated = true
                }
            )
        }
    }
}

@Composable
private fun AuthenticationFlow(
    onAuthenticationSuccess: () -> Unit
) {
    // Placeholder authentication screen
    // Nanti akan diganti dengan proper auth screens
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🌪️",
                fontSize = 64.sp
            )
            Text(
                text = "Welcome to Daily Chaos!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your safe space for beautiful chaos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Temporary login buttons
            Button(
                onClick = onAuthenticationSuccess,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Continue Anonymously")
            }

            OutlinedButton(
                onClick = onAuthenticationSuccess,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Login with Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "\"Bahkan party paling disfungsional pun masih bisa selamatkan hari\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}