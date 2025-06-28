package com.dailychaos.project.presentation.ui.screen.community.support

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    postId: String,
    viewModel: SupportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // ✅ Enhanced: Smooth scroll to top when new comment added
    LaunchedEffect(uiState.comments.size) {
        if (uiState.comments.isNotEmpty() && !uiState.isLoading) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(postId) {
        viewModel.initialize(postId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(SupportEvent.ClearError)
        }
    }

    // ✅ Enhanced: Animated comment dialog
    AnimatedVisibility(
        visible = uiState.showCommentDialog,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300, easing = EaseOutCubic),
            initialOffsetY = { it / 3 }
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            animationSpec = tween(200, easing = EaseInCubic),
            targetOffsetY = { it / 3 }
        )
    ) {
        EnhancedCommentDialog(
            commentText = uiState.commentText,
            selectedSupportType = uiState.selectedSupportType,
            selectedSupportLevel = uiState.selectedSupportLevel,
            isAnonymous = uiState.isAnonymous,
            isPosting = uiState.isPostingComment,
            onCommentTextChange = { viewModel.onEvent(SupportEvent.UpdateCommentText(it)) },
            onSupportTypeSelect = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onEvent(SupportEvent.SelectSupportType(it))
            },
            onSupportLevelSelect = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onEvent(SupportEvent.SelectSupportLevel(it))
            },
            onAnonymousToggle = { viewModel.onEvent(SupportEvent.ToggleAnonymous(it)) },
            onPostComment = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onEvent(SupportEvent.PostComment)
            },
            onDismiss = { viewModel.onEvent(SupportEvent.HideCommentDialog) }
        )
    }

    // ✅ NEW: Report confirmation dialog
    ReportConfirmationDialog(
        isVisible = uiState.showReportDialog,
        reportReason = uiState.reportReason,
        onReasonChange = { reason ->
            viewModel.onEvent(SupportEvent.UpdateReportReason(reason))
        },
        onConfirm = {
            uiState.selectedCommentToReport?.let { commentId ->
                viewModel.onEvent(SupportEvent.ConfirmReport(commentId, uiState.reportReason))
            }
            viewModel.onEvent(SupportEvent.HideReportDialog)
        },
        onDismiss = {
            viewModel.onEvent(SupportEvent.HideReportDialog)
        }
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    // ✅ Enhanced: Custom snackbar with better design
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            )
        },
        topBar = {
            // ✅ Enhanced: Better top bar with gradient
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "💬 Support & Comments",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedContent(
                            targetState = uiState.totalComments,
                            transitionSpec = {
                                slideInVertically { it } + fadeIn() togetherWith
                                        slideOutVertically { -it } + fadeOut()
                            },
                            label = "comment_count"
                        ) { count ->
                            Text(
                                if (count == 0) "Be the first to show support! 🌟"
                                else "$count messages of support from fellow adventurers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            // ✅ Enhanced: Animated FAB with better interaction
            val fabScale by animateFloatAsState(
                targetValue = if (uiState.isPostingComment) 0.8f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "fab_scale"
            )

            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(SupportEvent.ShowCommentDialog)
                },
                modifier = Modifier.scale(fabScale),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                text = {
                    AnimatedContent(
                        targetState = uiState.isPostingComment,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "fab_text"
                    ) { isPosting ->
                        if (isPosting) {
                            Text("Sending...")
                        } else {
                            Text("Send Support")
                        }
                    }
                },
                icon = {
                    AnimatedContent(
                        targetState = uiState.isPostingComment,
                        transitionSpec = {
                            scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                        },
                        label = "fab_icon"
                    ) { isPosting ->
                        if (isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Add, "Add Support Comment")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onEvent(SupportEvent.RefreshComments)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // ✅ Enhanced: Better loading state with shimmer effect
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LoadingIndicator(
                            message = "Loading support messages..."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Gathering messages from fellow adventurers...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.error != null && uiState.comments.isEmpty() -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetryClick = { viewModel.onEvent(SupportEvent.LoadComments) },
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.comments.isEmpty() -> {
                    EnhancedEmptyCommentsState(
                        onAddComment = { viewModel.onEvent(SupportEvent.ShowCommentDialog) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // ✅ Enhanced: Animated statistics card
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                                    animationSpec = tween(600, easing = EaseOutCubic),
                                    initialOffsetY = { -it / 2 }
                                )
                            ) {
                                EnhancedSupportStatisticsCard(
                                    totalComments = uiState.totalComments,
                                    supportTypeBreakdown = uiState.supportTypeBreakdown
                                )
                            }
                        }

                        items(
                            items = uiState.comments,
                            key = { it.id }
                        ) { comment ->
                            // ✅ Enhanced: Animated comment cards
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(
                                    animationSpec = tween(durationMillis = 400, delayMillis = 100)
                                ) + slideInVertically(
                                    animationSpec = tween(400, easing = EaseOutCubic),
                                    initialOffsetY = { it / 3 }
                                )
                            ) {
                                EnhancedSupportCommentCard(
                                    comment = comment,
                                    uiState = uiState,
                                    isExpanded = uiState.expandedCommentId == comment.id,
                                    onExpandToggle = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (uiState.expandedCommentId == comment.id) {
                                            viewModel.onEvent(SupportEvent.CollapseComment(comment.id))
                                        } else {
                                            viewModel.onEvent(SupportEvent.ExpandComment(comment.id))
                                        }
                                    },
                                    onLike = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onEvent(SupportEvent.LikeComment(comment.id))
                                    },
                                    onShowMenu = {
                                        viewModel.onEvent(SupportEvent.ShowCommentMenu(comment.id))
                                    },
                                    onHideMenu = {
                                        viewModel.onEvent(SupportEvent.HideCommentMenu)
                                    },
                                    onShowReportDialog = {
                                        viewModel.onEvent(SupportEvent.ShowReportDialog(comment.id))
                                    },
                                    onReply = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onEvent(SupportEvent.ReplyToComment(comment.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ Enhanced: Premium statistics card with animations
@Composable
private fun EnhancedSupportStatisticsCard(
    totalComments: Int,
    supportTypeBreakdown: Map<SupportType, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💙", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Community Support",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AnimatedContent(
                        targetState = totalComments,
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { -it } + fadeOut()
                        },
                        label = "total_comments"
                    ) { count ->
                        Text(
                            if (count == 0) "No messages yet - be the first!"
                            else "$count adventurers sharing their support",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (supportTypeBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    supportTypeBreakdown.forEach { (type, count) ->
                        AnimatedVisibility(
                            visible = count > 0,
                            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    getSupportEmoji(type),
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                AnimatedContent(
                                    targetState = count,
                                    transitionSpec = {
                                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                                    },
                                    label = "support_count"
                                ) { animatedCount ->
                                    Text(
                                        animatedCount.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    getSupportText(type),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ Enhanced: Premium comment card with smooth animations and improved menu
@Composable
private fun EnhancedSupportCommentCard(
    comment: SupportComment,
    uiState: SupportUiState,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onLike: () -> Unit,
    onShowMenu: () -> Unit,
    onHideMenu: () -> Unit,
    onShowReportDialog: () -> Unit,
    onReply: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with user info and support type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    username = comment.anonymousUsername,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        comment.anonymousUsername,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        comment.createdAt.timeAgo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Animated support type badge
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn() + fadeIn()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = getSupportTypeColor(comment.supportType).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                getSupportEmoji(comment.supportType),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                getSupportText(comment.supportType),
                                style = MaterialTheme.typography.labelMedium,
                                color = getSupportTypeColor(comment.supportType),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comment content with smooth expand/collapse
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "comment_content"
            ) { expanded ->
                Text(
                    comment.content,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 22.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = if (!expanded) TextOverflow.Ellipsis else TextOverflow.Visible
                )
            }

            // Show more/less button
            if (comment.content.length > 150) {
                TextButton(
                    onClick = onExpandToggle,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    AnimatedContent(
                        targetState = isExpanded,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "expand_text"
                    ) { expanded ->
                        Text(
                            if (expanded) "Show less" else "Show more",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Support level stars - FIXED
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(comment.supportLevel) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "${index + 1} of ${comment.supportLevel} stars",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    getSupportLevelText(comment.supportLevel),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons with enhanced interaction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button with animation
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val likeScale by animateFloatAsState(
                        targetValue = if (comment.isLikedByCurrentUser) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "like_scale"
                    )

                    IconButton(
                        onClick = onLike,
                        modifier = Modifier
                            .size(40.dp)
                            .scale(likeScale)
                    ) {
                        AnimatedContent(
                            targetState = comment.isLikedByCurrentUser,
                            transitionSpec = {
                                scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                            },
                            label = "like_icon"
                        ) { isLiked ->
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked)
                                    Color(0xFFE91E63)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (comment.likeCount > 0) {
                        AnimatedContent(
                            targetState = comment.likeCount,
                            transitionSpec = {
                                slideInVertically { it } + fadeIn() togetherWith
                                        slideOutVertically { -it } + fadeOut()
                            },
                            label = "like_count"
                        ) { count ->
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ✅ NEW: Improved More actions button with dropdown menu
                Box {
                    IconButton(
                        onClick = onShowMenu,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded = uiState.showCommentMenu == comment.id,
                        onDismissRequest = onHideMenu
                    ) {
                        // ✅ REMOVED: Reply option (redundant dengan Send Support FAB)

                        // Report option only
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Report,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Report", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            onClick = {
                                onHideMenu()
                                onShowReportDialog()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ✅ NEW: Report Confirmation Dialog
@Composable
private fun ReportConfirmationDialog(
    isVisible: Boolean,
    reportReason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Report,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Comment")
                }
            },
            text = {
                Column {
                    Text(
                        "Why are you reporting this comment?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Predefined reasons
                    val reasons = listOf(
                        "Spam or unwanted content",
                        "Harassment or bullying",
                        "Inappropriate content",
                        "False information",
                        "Other"
                    )

                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReasonChange(reason) }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = reportReason == reason,
                                onClick = { onReasonChange(reason) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reason, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = reportReason.isNotEmpty(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Report")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ✅ Enhanced: Premium empty state with better motivation
@Composable
private fun EnhancedEmptyCommentsState(
    onAddComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated emoji
        val infiniteTransition = rememberInfiniteTransition(label = "empty_animation")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emoji_scale"
        )

        Text(
            "💬",
            fontSize = 72.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(scale)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "No Support Messages Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Every adventurer needs support on their journey.\nBe the first to send some encouragement! 🌟",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddComment,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Send First Support Message",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ✅ Enhanced: Premium comment dialog with better UX
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedCommentDialog(
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
    val haptic = LocalHapticFeedback.current

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with better styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💝", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Send Support Message",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Share encouraging words with a fellow adventurer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Enhanced text field with character counter
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    label = { Text("Your support message") },
                    placeholder = { Text("Share some encouraging words...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Express your support genuinely",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AnimatedContent(
                                targetState = commentText.length,
                                label = "char_count"
                            ) { count ->
                                Text(
                                    "$count/1000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (count > 900) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Enhanced support type selection
                Text(
                    "Choose Support Type:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                val supportTypes = SupportType.values().toList()
                supportTypes.chunked(2).forEach { rowTypes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowTypes.forEach { type ->
                            val isSelected = selectedSupportType == type

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSupportTypeSelect(type)
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            getSupportEmoji(type),
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            getSupportText(type),
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getSupportTypeColor(type).copy(alpha = 0.2f),
                                    selectedLabelColor = getSupportTypeColor(type)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = getSupportTypeColor(type),
                                    selectedBorderWidth = 2.dp
                                )
                            )
                        }
                        if (rowTypes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enhanced support intensity selection
                Text(
                    "Support Intensity:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { level ->
                        val isSelected = level < selectedSupportLevel
                        val levelScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "star_scale"
                        )

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSupportLevelSelect(level + 1)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .scale(levelScale)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "${level + 1} stars",
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AnimatedContent(
                        targetState = selectedSupportLevel,
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { -it } + fadeOut()
                        },
                        label = "intensity_text"
                    ) { level ->
                        Column {
                            Text(
                                getSupportLevelText(level),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                getSupportLevelDescription(level),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Enhanced anonymous toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAnonymousToggle(!isAnonymous) }
                            .padding(16.dp)
                    ) {
                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = onAnonymousToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Post anonymously",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (isAnonymous) "Your identity will be hidden"
                                else "Your username will be visible",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Enhanced action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Button(
                        onClick = onPostComment,
                        enabled = commentText.isNotBlank() && !isPosting,
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        AnimatedContent(
                            targetState = isPosting,
                            transitionSpec = {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it } + fadeOut()
                            },
                            label = "button_content"
                        ) { posting ->
                            if (posting) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Sending...",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(getSupportEmoji(selectedSupportType))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Send Support",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// ✅ Enhanced: Helper functions with better theming
// ============================================================================

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

@Composable
private fun getSupportTypeColor(supportType: SupportType): Color {
    return when (supportType) {
        SupportType.HEART -> Color(0xFFE91E63)  // Pink
        SupportType.HUG -> Color(0xFFFF9800)    // Orange
        SupportType.STRENGTH -> Color(0xFF4CAF50) // Green
        SupportType.HOPE -> Color(0xFFFFEB3B)   // Yellow
    }
}

private fun getSupportLevelText(level: Int): String {
    return when (level) {
        1 -> "Gentle"
        2 -> "Warm"
        3 -> "Strong"
        4 -> "Powerful"
        5 -> "Maximum"
        else -> "Unknown"
    }
}

private fun getSupportLevelDescription(level: Int): String {
    return when (level) {
        1 -> "A gentle touch"
        2 -> "Warm encouragement"
        3 -> "Strong support"
        4 -> "Powerful motivation"
        5 -> "Maximum energy!"
        else -> ""
    }
}