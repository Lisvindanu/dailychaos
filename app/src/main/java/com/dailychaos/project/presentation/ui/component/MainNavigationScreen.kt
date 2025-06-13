package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dailychaos.project.presentation.ui.screen.home.HomeScreen

/**
 * Main Navigation Screen - Clean Design tanpa FAB
 *
 * "Navigation yang clean dengan fokus pada bottom navigation bar"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MainNavigationBar(
                selectedRoute = currentRoute ?: "home",
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to the start destination to avoid large back stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home Screen
            composable("home") {
                HomeScreen(
                    onNavigateToCreateChaos = {
                        navController.navigate("create_chaos")
                    },
                    onNavigateToHistory = {
                        navController.navigate("chaos_history")
                    },
                    onNavigateToCommunity = {
                        navController.navigate("community")
                    },
                    onNavigateToEntry = { entryId ->
                        navController.navigate("chaos_detail/$entryId")
                    }
                )
            }

            // Journal Screen
            composable("journal") {
                JournalScreenContent(
                    onNavigateToCreateChaos = {
                        navController.navigate("create_chaos")
                    },
                    onNavigateToEntry = { entryId ->
                        navController.navigate("chaos_detail/$entryId")
                    }
                )
            }

            // Community Screen
            composable("community") {
                CommunityScreenContent(
                    onNavigateToPost = { postId ->
                        navController.navigate("community_post/$postId")
                    },
                    onNavigateToTwins = {
                        navController.navigate("chaos_twins")
                    }
                )
            }

            // Profile Screen
            composable("profile") {
                ProfileScreenContent(
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToAuth = onNavigateToAuth
                )
            }

            // Create Chaos Entry
            composable("create_chaos") {
                CreateChaosScreenContent(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onChaosSaved = {
                        navController.popBackStack()
                    }
                )
            }

            // Chaos Detail
            composable("chaos_detail/{entryId}") { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                ChaosDetailScreenContent(
                    entryId = entryId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = {
                        navController.navigate("edit_chaos/$entryId")
                    }
                )
            }

            // Edit Chaos Entry
            composable("edit_chaos/{entryId}") { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                EditChaosScreenContent(
                    entryId = entryId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onChaosSaved = {
                        navController.popBackStack()
                    }
                )
            }

            // Chaos History
            composable("chaos_history") {
                ChaosHistoryScreenContent(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEntry = { entryId ->
                        navController.navigate("chaos_detail/$entryId")
                    }
                )
            }

            // Community Post Detail
            composable("community_post/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                CommunityPostDetailContent(
                    postId = postId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Chaos Twins
            composable("chaos_twins") {
                ChaosTwinsScreenContent(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPost = { postId ->
                        navController.navigate("community_post/$postId")
                    }
                )
            }

            // Settings
            composable("settings") {
                SettingsScreenContent(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Placeholder content components
@Composable
private fun JournalScreenContent(
    onNavigateToCreateChaos: () -> Unit,
    onNavigateToEntry: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📝",
                fontSize = 48.sp
            )
            Text(
                text = "Journal Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateToCreateChaos) {
                Text("Add New Entry")
            }
        }
    }
}

@Composable
private fun CommunityScreenContent(
    onNavigateToPost: (String) -> Unit,
    onNavigateToTwins: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🤝",
                fontSize = 48.sp
            )
            Text(
                text = "Community Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateToTwins) {
                Text("Find Chaos Twins")
            }
        }
    }
}

@Composable
private fun ProfileScreenContent(
    onNavigateToSettings: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "👤",
                fontSize = 48.sp
            )
            Text(
                text = "Profile Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateToSettings) {
                Text("Settings")
            }
        }
    }
}

@Composable
private fun CreateChaosScreenContent(
    onNavigateBack: () -> Unit,
    onChaosSaved: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "✍️",
                fontSize = 48.sp
            )
            Text(
                text = "Create Chaos Entry",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Cancel")
                }
                Button(onClick = onChaosSaved) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ChaosDetailScreenContent(
    entryId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📖",
                fontSize = 48.sp
            )
            Text(
                text = "Chaos Entry Detail",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Entry ID: $entryId",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Back")
                }
                Button(onClick = onNavigateToEdit) {
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun EditChaosScreenContent(
    entryId: String,
    onNavigateBack: () -> Unit,
    onChaosSaved: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "✏️",
                fontSize = 48.sp
            )
            Text(
                text = "Edit Chaos Entry",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Editing Entry: $entryId",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onNavigateBack) {
                    Text("Cancel")
                }
                Button(onClick = onChaosSaved) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
private fun ChaosHistoryScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📚",
                fontSize = 48.sp
            )
            Text(
                text = "Chaos History",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun CommunityPostDetailContent(
    postId: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "💬",
                fontSize = 48.sp
            )
            Text(
                text = "Community Post",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Post ID: $postId",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun ChaosTwinsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToPost: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🤝",
                fontSize = 48.sp
            )
            Text(
                text = "Chaos Twins",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun SettingsScreenContent(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚙️",
                fontSize = 48.sp
            )
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}