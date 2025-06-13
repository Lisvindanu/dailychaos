package com.dailychaos.project.presentation.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.domain.model.SyncStatus
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.*
import com.dailychaos.project.util.KonoSubaQuotes
import kotlinx.datetime.Clock
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshState

// Also add these missing domain model imports:
import com.dailychaos.project.domain.model.User

/**
 * Home Screen - Main dashboard with proper MVVM
 *
 * "Dashboard utama seperti guild hall dimana adventurer berkumpul!"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateChaos: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToEntry: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle UI events
    LaunchedEffect(Unit) {
        // Screen initialized, data loading handled by ViewModel
    }

    // Error handling
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: Show snackbar or handle error display
        }
    }

    // Pull to refresh
    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.onEvent(HomeUiEvent.Refresh)
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                // Welcome Header
                WelcomeHeader(user = uiState.user)
            }

            item {
                // Today's Quick Stats
                TodayQuickStatsWithData(
                    stats = uiState.todayStats,
                    isLoading = uiState.isStatsLoading,
                    onRetry = { viewModel.onEvent(HomeUiEvent.RetryLoadingStats) }
                )
            }

            // Daily Quote (only show if available)
            uiState.dailyQuote?.let { quote ->
                item {
                    KonoSubaQuote(
                        quote = quote.text,
                        character = quote.character.displayName
                    )
                }
            }

            item {
                // Quick Actions
                QuickActionsSection(
                    onCreateChaos = {
                        viewModel.onEvent(HomeUiEvent.NavigateToCreateChaos)
                        onNavigateToCreateChaos()
                    },
                    onViewHistory = {
                        viewModel.onEvent(HomeUiEvent.NavigateToHistory)
                        onNavigateToHistory()
                    },
                    onVisitCommunity = {
                        viewModel.onEvent(HomeUiEvent.NavigateToCommunity)
                        onNavigateToCommunity()
                    }
                )
            }

            item {
                // Recent Chaos Entries
                RecentChaosSectionWithData(
                    recentEntries = uiState.recentEntries,
                    isLoading = uiState.isEntriesLoading,
                    error = uiState.entriesError,
                    onEntryClick = { entryId ->
                        viewModel.onEvent(HomeUiEvent.NavigateToEntry(entryId))
                        onNavigateToEntry(entryId)
                    },
                    onViewAll = {
                        viewModel.onEvent(HomeUiEvent.NavigateToHistory)
                        onNavigateToHistory()
                    },
                    onRetry = { viewModel.onEvent(HomeUiEvent.RetryLoadingEntries) },
                    onCreateFirst = {
                        viewModel.onEvent(HomeUiEvent.NavigateToCreateChaos)
                        onNavigateToCreateChaos()
                    }
                )
            }

            item {
                // Achievement/Streak Section
                AchievementSectionWithData(
                    achievements = uiState.achievements,
                    currentStreak = uiState.currentStreak,
                    isLoading = uiState.isAchievementsLoading
                )
            }

            // Community highlights (only show if available)
            uiState.communityHighlight?.let { highlight ->
                item {
                    CommunityHighlightsSectionWithData(
                        highlight = highlight,
                        isLoading = uiState.isCommunityLoading,
                        onViewCommunity = {
                            viewModel.onEvent(HomeUiEvent.NavigateToCommunity)
                            onNavigateToCommunity()
                        }
                    )
                }
            }
        }

        // Pull to refresh indicator
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
        )

        // Error handling UI
        if (uiState.hasError && !uiState.isLoading) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Something went wrong",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.onEvent(HomeUiEvent.ClearError) }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌪️",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (user != null) "Welcome back, ${user.anonymousUsername}!" else "Welcome to Daily Chaos!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ready for today's adventure?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TodayQuickStatsWithData(
    stats: TodayStats,
    isLoading: Boolean,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Today's Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                // Loading skeleton
                repeat(4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(4) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatsItem(
                        emoji = "📝",
                        value = stats.entriesCount.toString(),
                        label = "Entries"
                    )
                    StatsItem(
                        emoji = "🏆",
                        value = stats.miniWinsCount.toString(),
                        label = "Mini Wins"
                    )
                    StatsItem(
                        emoji = "💙",
                        value = stats.supportGivenCount.toString(),
                        label = "Support Given"
                    )
                    StatsItem(
                        emoji = "🎯",
                        value = "${stats.completionPercentage}%",
                        label = "Goals"
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentChaosSectionWithData(
    recentEntries: List<ChaosEntry>,
    isLoading: Boolean,
    error: String?,
    onEntryClick: (String) -> Unit,
    onViewAll: () -> Unit,
    onRetry: () -> Unit,
    onCreateFirst: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📚 Recent Chaos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (recentEntries.isNotEmpty() && !isLoading) {
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }
        }

        when {
            isLoading -> {
                // Loading state
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(3) {
                        Card(
                            modifier = Modifier.width(200.dp).height(120.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
            error != null -> {
                // Error state
                ErrorMessage(
                    message = error,
                    onRetryClick = onRetry
                )
            }
            recentEntries.isNotEmpty() -> {
                // Data state
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(recentEntries.take(5)) { entry ->
                        CompactChaosCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
            else -> {
                // Empty state
                EmptyState(
                    illustration = "📝",
                    title = "No chaos yet",
                    subtitle = "Start recording your daily adventures!",
                    actionText = "Create First Entry",
                    onActionClick = onCreateFirst
                )
            }
        }
    }
}

@Composable
private fun AchievementSectionWithData(
    achievements: List<Achievement>,
    currentStreak: Int,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Text(
                    text = "Loading achievements...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else if (currentStreak > 0) {
                Text(
                    text = "$currentStreak-day streak! You're on fire! 🔥",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                if (achievements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(achievements.take(3)) { achievement ->
                            AchievementChip(achievement = achievement)
                        }
                    }
                }

                Text(
                    text = "Keep recording your chaos to unlock more achievements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Start your chaos journey to unlock achievements!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun AchievementChip(achievement: Achievement) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (achievement.isUnlocked)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = achievement.emoji,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommunityHighlightsSectionWithData(
    highlight: CommunityHighlight,
    isLoading: Boolean,
    onViewCommunity: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤝 Community",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewCommunity) {
                    Text("Explore")
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "\"${highlight.content}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "- ${highlight.authorName} • ${highlight.supportCount} support reactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StatsItem(
    emoji: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickActionsSection(
    onCreateChaos: () -> Unit,
    onViewHistory: () -> Unit,
    onVisitCommunity: () -> Unit
) {
    Column {
        Text(
            text = "⚡ Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add,
                title = "New Chaos",
                subtitle = "Record today's adventure",
                onClick = onCreateChaos
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.History,
                title = "View History",
                subtitle = "Browse past entries",
                onClick = onViewHistory
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        QuickActionCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.People,
            title = "Visit Community",
            subtitle = "Find your chaos twins and share support",
            onClick = onVisitCommunity,
            isFullWidth = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFullWidth: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = if (isFullWidth) Modifier.weight(1f) else Modifier
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactChaosCard(
    entry: ChaosEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ChaosLevel.fromValue(entry.chaosLevel).emoji,
                    fontSize = 20.sp
                )
                Text(
                    text = entry.chaosLevel.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            if (entry.miniWins.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🏆 ${entry.miniWins.size} wins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

// Sample data for preview
private fun getSampleChaosEntries(): List<ChaosEntry> {
    return listOf(
        ChaosEntry(
            id = "1",
            title = "Kazuma Style Monday",
            description = "Hari ini seperti jadi leader party yang aneh tapi somehow berhasil",
            chaosLevel = 6,
            miniWins = listOf("Selesaikan presentation", "Bantu teman"),
            createdAt = Clock.System.now(),
            syncStatus = SyncStatus.SYNCED
        ),
        ChaosEntry(
            id = "2",
            title = "Megumin Energy",
            description = "All or nothing approach hari ini!",
            chaosLevel = 9,
            miniWins = listOf("Explosion success!"),
            createdAt = Clock.System.now(),
            syncStatus = SyncStatus.SYNCED
        )
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DailyChaosTheme {
        HomeScreen()
    }
}