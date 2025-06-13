package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Main Navigation Screen Component
 *
 * "Screen utama yang mengatur navigation dan bottom bar"
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

    // State untuk FAB menu
    var isFabExpanded by remember { mutableStateOf(false) }

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
        floatingActionButton = {
            FloatingActionMenu(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onNewChaosEntry = {
                    isFabExpanded = false
                    navController.navigate("create_chaos")
                },
                onShareToCommunity = {
                    isFabExpanded = false
                    navController.navigate("community")
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
                HomeScreenContent(
                    onNavigateToCreateChaos = {
                        navController.navigate("create_chaos")
                    },
                    onNavigateToHistory = {
                        navController.navigate("chaos_history")
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

// Placeholder content components - akan diganti dengan actual screens nanti
@Composable
private fun HomeScreenContent(
    onNavigateToCreateChaos: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏠 Home Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToCreateChaos) {
                Text("Create New Chaos Entry")
            }
        }
    }
}

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📝 Journal Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🤝 Community Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👤 Profile Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✍️ Create Chaos Entry",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = onNavigateBack) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📖 Chaos Entry Detail",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Entry ID: $entryId",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = onNavigateBack) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✏️ Edit Chaos Entry",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Editing Entry: $entryId",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = onNavigateBack) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚 Chaos History",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💬 Community Post",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Post ID: $postId",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🤝 Chaos Twins",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚙️ Settings",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}