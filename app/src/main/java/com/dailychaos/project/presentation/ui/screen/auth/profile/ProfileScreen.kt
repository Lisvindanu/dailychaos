package com.dailychaos.project.presentation.ui.screen.auth.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.AnimatedCounter
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import com.dailychaos.project.presentation.ui.component.UserAvatar
import com.dailychaos.project.presentation.ui.screen.home.Achievement
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logoutCompleteEvent.collectLatest {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Adventurer Profile") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.user == null -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Loading your profile..."
                    )
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetryClick = { viewModel.onEvent(ProfileEvent.Retry) },
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.user != null -> {
                    val user = uiState.user!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(username = user.anonymousUsername, size = 96.dp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = user.anonymousUsername,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email ?: "Anonymous Adventurer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))

                        // Stats Section
                        StatsGrid(user = user)

                        Spacer(Modifier.height(24.dp))

                        // Achievements Section
                        AchievementsSection(achievements = uiState.achievements)

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.onEvent(ProfileEvent.Logout) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(user: com.dailychaos.project.domain.model.User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedCounter(count = user.chaosEntriesCount, label = "Entries")
        AnimatedCounter(count = user.streakDays, label = "Day Streak 🔥")
        AnimatedCounter(count = user.supportGivenCount, label = "Support Given")
    }
}

@Composable
private fun AchievementsSection(achievements: List<Achievement>) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🏆 Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement ->
                AchievementItem(achievement = achievement)
            }
        }
    }
}

@Composable
private fun AchievementItem(achievement: Achievement) {
    val alpha = if (achievement.isUnlocked) 1f else 0.5f
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = achievement.emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            if (!achievement.isUnlocked) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { achievement.progressPercentage / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    DailyChaosTheme {
        ProfileScreen(onNavigateToSettings = {}, onLogout = {})
    }
}