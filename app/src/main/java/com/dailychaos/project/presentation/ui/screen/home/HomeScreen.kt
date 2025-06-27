package com.dailychaos.project.presentation.ui.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import com.dailychaos.project.domain.model.User
import com.dailychaos.project.presentation.ui.component.ChaosEntryCard
import com.dailychaos.project.presentation.ui.component.EmptyState
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.KonoSubaQuote
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateChaos: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToEntry: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    konoSubaApiService: KonoSubaApiService? = null
) {

    LaunchedEffect(Unit) {
        Timber.d("🏠 HomeScreen LOADED - FAB should be visible in bottom nav")
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: Show snackbar or handle error display
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onEvent(HomeUiEvent.Refresh) },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                WelcomeHeaderWithStats(
                    user = uiState.user,
                    todayStats = uiState.todayStats,
                    isStatsLoading = uiState.isUserLoading
                )
            }

            uiState.dailyQuote?.let { quote ->
                item {
                    KonoSubaQuote(
                        quote = quote.text,
                        character = quote.character.displayName,
                        apiService = konoSubaApiService,
                        onRefreshQuote = { viewModel.onEvent(HomeUiEvent.RefreshQuote) },
                        onNextQuote = { viewModel.onEvent(HomeUiEvent.NextQuote) },
                        showRefreshButton = true
                    )
                }
            }

            item {
                AchievementSection(
                    achievements = uiState.achievements,
                    currentStreak = uiState.currentStreak,
                    isLoading = uiState.isAchievementsLoading
                )
            }

            item {
                RecentChaosSection(
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

            uiState.communityHighlight?.let { highlight ->
                item {
                    CommunityHighlightsSection(
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

        if (uiState.hasError && !uiState.isLoading) {
            ErrorMessage(
                message = uiState.errorMessage ?: "Something went wrong",
                onRetryClick = { viewModel.onEvent(HomeUiEvent.ClearError) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun WelcomeHeaderWithStats(
    user: User?,
    todayStats: TodayStats,
    isStatsLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
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
                text = if (user != null) {
                    val nameToShow = if (user.displayName.isNotBlank()) user.displayName else user.anonymousUsername
                    if (nameToShow.isNotBlank()) "Welcome back, $nameToShow!" else "Welcome, Adventurer!"
                } else {
                    "Welcome to Daily Chaos!"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ready for today's adventure?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            if (!isStatsLoading && (todayStats.entriesCount > 0 || todayStats.miniWinsCount > 0)) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (todayStats.entriesCount > 0) {
                        QuickStatItem(
                            emoji = "📝",
                            value = todayStats.entriesCount.toString(),
                            label = "Entries Today",
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (todayStats.miniWinsCount > 0) {
                        QuickStatItem(
                            emoji = "🏆",
                            value = todayStats.miniWinsCount.toString(),
                            label = "Mini Wins",
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (todayStats.supportGivenCount > 0) {
                        QuickStatItem(
                            emoji = "💙",
                            value = todayStats.supportGivenCount.toString(),
                            label = "Support Given",
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    emoji: String,
    value: String,
    label: String,
    contentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun RecentChaosSection(
    recentEntries: List<com.dailychaos.project.domain.model.ChaosEntry>,
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
                LoadingIndicator(
                    message = "Loading recent chaos entries..."
                )
            }
            error != null -> {
                ErrorMessage(
                    message = error,
                    onRetryClick = onRetry
                )
            }
            recentEntries.isNotEmpty() -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(recentEntries.take(5)) { entry ->
                        ChaosEntryCard(
                            chaosEntry = entry,
                            onCardClick = { onEntryClick(entry.id) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
            else -> {
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
private fun AchievementSection(
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
                LoadingIndicator(message = "Loading achievements...")
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
                            AchievementBadge(achievement = achievement)
                        }
                    }
                }
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
private fun AchievementBadge(achievement: Achievement) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
private fun CommunityHighlightsSection(
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
                LoadingIndicator()
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