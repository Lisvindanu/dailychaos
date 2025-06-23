// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/support/SupportScreen.kt
package com.dailychaos.project.presentation.ui.screen.community.support

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.domain.model.SupportComment
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import com.dailychaos.project.presentation.ui.component.ParchmentCard
import com.dailychaos.project.presentation.ui.component.UserAvatar
import com.dailychaos.project.util.timeAgo

/**
 * Support Screen - Comment System untuk Community Posts
 * "Tempat dimana adventurers saling memberikan dukungan melalui komentar"
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SupportScreen(
    postId: String,
    viewModel: SupportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize dengan postId
    LaunchedEffect(postId) {
        viewModel.initialize(postId)
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(SupportEvent.ClearError)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onEvent(SupportEvent.RefreshComments) }
    )

    // Comment Dialog
    if (uiState.showCommentDialog) {
        CommentDialog(
            commentText = uiState.commentText,
            selectedSupportType = uiState.selectedSupportType,
            selectedSupportLevel = uiState.selectedSupportLevel,
            isAnonymous = uiState.isAnonymous,
            isPosting = uiState.isPostingComment,
            onCommentTextChange = { viewModel.onEvent(SupportEvent.UpdateCommentText(it)) },
            onSupportTypeSelect = { viewModel.onEvent(SupportEvent.SelectSupportType(it)) },
            onSupportLevelSelect = { viewModel.onEvent(SupportEvent.SelectSupportLevel(it)) },
            onAnonymousToggle = { viewModel.onEvent(SupportEvent.ToggleAnonymous(it)) },
            onPostComment = { viewModel.onEvent(SupportEvent.PostComment) },
            onDismiss = { viewModel.onEvent(SupportEvent.HideCommentDialog) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Support & Comments")
                        Text(
                            "${uiState.totalComments} messages of support",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(SupportEvent.ShowCommentDialog) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Support Comment")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Loading support messages..."
                    )
                }
                uiState.error != null && uiState.comments.isEmpty() -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetryClick = { viewModel.onEvent(SupportEvent.LoadComments) },
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.comments.isEmpty() -> {
                    EmptyCommentsState(
                        onAddComment = { viewModel.onEvent(SupportEvent.ShowCommentDialog) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Support Statistics Header
                        item {
                            SupportStatisticsCard(
                                totalComments = uiState.totalComments,
                                supportTypeBreakdown = uiState.supportTypeBreakdown
                            )
                        }

                        // Comments List
                        items(uiState.comments) { comment ->
                            SupportCommentCard(
                                comment = comment,
                                isExpanded = uiState.expandedCommentId == comment.id,
                                onExpandToggle = {
                                    if (uiState.expandedCommentId == comment.id) {
                                        viewModel.onEvent(SupportEvent.CollapseComment(comment.id))
                                    } else {
                                        viewModel.onEvent(SupportEvent.ExpandComment(comment.id))
                                    }
                                },
                                onLike = { viewModel.onEvent(SupportEvent.LikeComment(comment.id)) },
                                onReport = { viewModel.onEvent(SupportEvent.ReportComment(comment.id, "Inappropriate")) },
                                onReply = { viewModel.onEvent(SupportEvent.ReplyToComment(comment.id)) }
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Account for FAB
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SupportStatisticsCard(
    totalComments: Int,
    supportTypeBreakdown: Map<SupportType, Int>
) {
    ParchmentCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "💙 Community Support Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "$totalComments adventurers have shared their support",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (supportTypeBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    supportTypeBreakdown.forEach { (type, count) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                getSupportEmoji(type),
                                fontSize = 20.sp
                            )
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportCommentCard(
    comment: SupportComment,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onLike: () -> Unit,
    onReport: () -> Unit,
    onReply: () -> Unit
) {
    ParchmentCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    username = comment.anonymousUsername,
                    size = 32.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        comment.anonymousUsername,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        comment.createdAt.timeAgo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Support type badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            getSupportEmoji(comment.supportType),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            getSupportText(comment.supportType),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Text(
                comment.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3
            )

            if (comment.content.length > 150) {
                TextButton(
                    onClick = onExpandToggle,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (isExpanded) "Show less" else "Show more",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (comment.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (comment.isLikedByCurrentUser)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (comment.likeCount > 0) {
                        Text(
                            comment.likeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Support level indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(comment.supportLevel) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    // More actions
                    IconButton(
                        onClick = onReport,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCommentsState(
    onAddComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "💬",
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No Support Messages Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Be the first to send some support to this fellow adventurer!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddComment
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Support")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentDialog(
    commentText: String,
    selectedSupportType: SupportType,
    selectedSupportLevel: Int,
    isAnonymous: Boolean,
    isPosting: Boolean,
    onCommentTextChange: (String) -> Unit,
    onSupportTypeSelect: (SupportType) -> Unit,
    onSupportLevelSelect: (Int) -> Unit,
    onAnonymousToggle: (Boolean) -> Unit,
    onPostComment: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Send Support Message")
        },
        text = {
            Column {
                // Comment text input
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    label = { Text("Your support message") },
                    placeholder = { Text("Share some encouraging words...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Support Type Selection
                Text(
                    "Choose Support Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ FIX: Use chunked to fit support types in rows
                val supportTypes = SupportType.values().toList()
                supportTypes.chunked(2).forEach { rowTypes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowTypes.forEach { type ->
                            FilterChip(
                                selected = selectedSupportType == type,
                                onClick = { onSupportTypeSelect(type) },
                                label = {
                                    Text("${getSupportEmoji(type)} ${getSupportText(type)}")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Add spacer if odd number of items in row
                        if (rowTypes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Support Level Selection
                Text(
                    "Support Intensity:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { level ->
                        IconButton(
                            onClick = { onSupportLevelSelect(level + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "${level + 1} stars",
                                tint = if (level < selectedSupportLevel)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (selectedSupportLevel) {
                            1 -> "Gentle"
                            2 -> "Warm"
                            3 -> "Strong"
                            4 -> "Powerful"
                            5 -> "Maximum"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Anonymous toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = onAnonymousToggle
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Post anonymously",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPostComment,
                enabled = commentText.isNotBlank() && !isPosting
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Support")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions
private fun getSupportEmoji(supportType: SupportType): String {
    return when (supportType) {
        SupportType.HEART -> "💝"
        SupportType.HUG -> "🤗"
        SupportType.STRENGTH -> "💪"
        SupportType.HOPE -> "🌟"
    }
}

private fun getSupportText(supportType: SupportType): String {
    return when (supportType) {
        SupportType.HEART -> "Heart"
        SupportType.HUG -> "Hug"
        SupportType.STRENGTH -> "Strength"
        SupportType.HOPE -> "Hope"
    }
}