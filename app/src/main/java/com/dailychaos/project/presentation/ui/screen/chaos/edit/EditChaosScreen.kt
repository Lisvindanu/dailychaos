package com.dailychaos.project.presentation.ui.screen.chaos.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.dailychaos.project.presentation.theme.DailyChaosTheme

/**
 * Edit Chaos Screen
 *
 * "Screen untuk mengedit chaos entry yang sudah ada"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChaosScreen(
    onNavigateBack: () -> Unit = {},
    onChaosUpdated: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: Implement EditChaosViewModel and proper state management
    // For now, this is a placeholder similar to CreateChaosScreen

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "✏️ Edit Chaos Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚧",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Edit Chaos Coming Soon!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fitur edit chaos entry sedang dalam development.\nSabar ya, seperti Kazuma menunggu Aqua selesai drama!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
                Button(
                    onClick = {
                        // Mock save action
                        onChaosUpdated()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mock Save")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditChaosScreenPreview() {
    DailyChaosTheme {
        EditChaosScreen()
    }
}