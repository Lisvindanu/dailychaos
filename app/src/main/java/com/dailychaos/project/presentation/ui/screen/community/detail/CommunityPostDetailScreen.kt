// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/detail/CommunityPostDetailScreen.kt
package com.dailychaos.project.presentation.ui.screen.community.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.presentation.ui.component.ChaosLevelBadge
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import com.dailychaos.project.util.toFriendlyDateString
import com.dailychaos.project.util.toTimeString
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen(
    postId: String,
    viewModel: CommunityPostDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSupport: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CommunityPostDetailViewModel.CommunityPostDetailScreenEvent.SupportGiven -> {
                    snackbarHostState.showSnackbar("Support sent to fellow adventurer! 💝")
                }
                is CommunityPostDetailViewModel.CommunityPostDetailScreenEvent.SupportRemoved -> {
                    snackbarHostState.showSnackbar("Support removed")
                }
                is CommunityPostDetailViewModel.CommunityPostDetailScreenEvent.PostReported -> {
                    snackbarHostState.showSnackbar("Post reported. Thank you for keeping our community safe.")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Community Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Only show actions if post is loaded
                    uiState.post?.let {
                        IconButton(onClick = { viewModel.onEvent(CommunityPostDetailEvent.ShowReportDialog) }) {
                            Icon(Icons.Default.Flag, "Report Post")
                        }
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
                uiState.isLoading -> LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    message = "Loading community post..."
                )
                uiState.error != null -> ErrorMessage(
                    message = uiState.error!!,
                    onRetryClick = { viewModel.onEvent(CommunityPostDetailEvent.Retry) },
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                uiState.post != null -> {
                    val post = uiState.post!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Header with date and chaos level
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "${post.createdAt.toFriendlyDateString()} at ${post.createdAt.toTimeString()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "by ${post.anonymousUsername}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            ChaosLevelBadge(chaosLevel = post.chaosLevel)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Title
                        Text(
                            post.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        // Content/Description
                        Text(
                            post.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )

                        // Mini Wins
                        if (post.miniWins.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "🏆 Mini Wins",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            post.miniWins.forEach { win ->
                                Text(
                                    "• $win",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                            }
                        }

                        // Tags
                        if (post.tags.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "🏷️ Tags",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(post.tags) { tag ->
                                    AssistChip(
                                        onClick = { /* Non-interactive */ },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Support Section
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Support this fellow adventurer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "${post.supportCount} adventurers have shown support",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(16.dp))

                                // Support Buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.onEvent(CommunityPostDetailEvent.GiveSupport(SupportType.HEART))
                                        },
                                        enabled = !uiState.isGivingSupport,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (uiState.isGivingSupport) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Default.Favorite, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("💝 Support")
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.onEvent(CommunityPostDetailEvent.GiveSupport(SupportType.HUG))
                                        },
                                        enabled = !uiState.isGivingSupport,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🤗 Hug")
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            // Report Dialog
            if (uiState.showReportDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(CommunityPostDetailEvent.DismissReportDialog) },
                    title = { Text("Report Post") },
                    text = {
                        Text("This will report the post to moderators for review. Are you sure you want to continue?")
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                viewModel.onEvent(CommunityPostDetailEvent.ReportPost("Inappropriate content"))
                            }
                        ) {
                            Text("Report")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            viewModel.onEvent(CommunityPostDetailEvent.DismissReportDialog)
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}