// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/community/detail/CommunityPostDetailScreen.kt
package com.dailychaos.project.presentation.ui.screen.community.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Comment
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
import com.dailychaos.project.presentation.ui.component.MeguminSadModal
import com.dailychaos.project.presentation.ui.component.MeguminModalType
import com.dailychaos.project.util.SupportUtils
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
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSupport: (String) -> Unit = {} // ✅ ENHANCED: Navigation to support/comment screen
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ FIXED: Updated modal state management
    var showMeguminModal by remember { mutableStateOf(false) }
    var meguminModalType by remember { mutableStateOf(MeguminModalType.RemoveSupport) }

    // Load post when screen starts
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    // ✅ FIXED: Handle events properly
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CommunityPostDetailScreenEvent.SupportGiven -> {
                    snackbarHostState.showSnackbar("Support sent to fellow adventurer! 💝")
                }
                is CommunityPostDetailScreenEvent.SupportTypeChanged -> {
                    snackbarHostState.showSnackbar("Support type changed! Keep supporting! 💙")
                }
                is CommunityPostDetailScreenEvent.SupportRemoved -> {
                    snackbarHostState.showSnackbar("Support removed")
                }
                is CommunityPostDetailScreenEvent.PostReported -> {
                    snackbarHostState.showSnackbar("Post reported. Thank you for keeping our community safe.")
                }
                is CommunityPostDetailScreenEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                CommunityPostDetailScreenEvent.NavigateBack -> {
                    onNavigateBack()
                }
                CommunityPostDetailScreenEvent.ShowMeguminSadModal -> {
                    // ✅ FIXED: Set modal type for remove support
                    meguminModalType = MeguminModalType.RemoveSupport
                    showMeguminModal = true
                }
                is CommunityPostDetailScreenEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // ✅ FIXED: Updated MeguminSadModal with correct parameters
    MeguminSadModal(
        isVisible = showMeguminModal,
        onDismiss = {
            showMeguminModal = false
        },
        onConfirmRemoval = {
            showMeguminModal = false
            viewModel.confirmRemoveSupport()
        },
        modalType = meguminModalType,
        currentSupportType = uiState.currentUserSupportType
    )

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
                        IconButton(onClick = { viewModel.reportPost("Inappropriate content") }) {
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
                    onRetryClick = { viewModel.retry() },
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                uiState.post != null -> {
                    val post = uiState.post!!
                    val currentUserSupportType = uiState.currentUserSupportType

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

                            // ✅ ENHANCED: Comments Preview Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                            Icons.AutoMirrored.Filled.Comment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Community Support",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        "See what fellow adventurers are saying and share your own words of support",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Button(
                                        onClick = { onNavigateToSupport(postId) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("View All Comments & Support")
                                    }
                                }
                            }

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

                        // ✅ ENHANCED: Support Section with better UX
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentUserSupportType != null) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
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
                                        tint = if (currentUserSupportType != null) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (currentUserSupportType != null) {
                                            "You're supporting this adventurer! ${getSupportEmoji(currentUserSupportType)}"
                                        } else {
                                            "Support this fellow adventurer"
                                        },
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

                                // Show current support type if user has given support
                                if (currentUserSupportType != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Your support: ${getSupportEmoji(currentUserSupportType)} ${getSupportText(currentUserSupportType)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // ✅ ENHANCED: Support instruction text
                                Text(
                                    if (currentUserSupportType != null) {
                                        "Tap the same support to remove it, or choose a different one:"
                                    } else {
                                        "Choose how you want to support:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // First row of support buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SupportButton(
                                        supportType = SupportType.HEART,
                                        isSelected = currentUserSupportType == SupportType.HEART,
                                        isLoading = uiState.isGivingSupport,
                                        onClick = { viewModel.giveSupport(SupportType.HEART) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    SupportButton(
                                        supportType = SupportType.HUG,
                                        isSelected = currentUserSupportType == SupportType.HUG,
                                        isLoading = uiState.isGivingSupport,
                                        onClick = { viewModel.giveSupport(SupportType.HUG) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                // Second row of support buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SupportButton(
                                        supportType = SupportType.STRENGTH,
                                        isSelected = currentUserSupportType == SupportType.STRENGTH,
                                        isLoading = uiState.isGivingSupport,
                                        onClick = { viewModel.giveSupport(SupportType.STRENGTH) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    SupportButton(
                                        supportType = SupportType.HOPE,
                                        isSelected = currentUserSupportType == SupportType.HOPE,
                                        isLoading = uiState.isGivingSupport,
                                        onClick = { viewModel.giveSupport(SupportType.HOPE) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // ✅ FIXED: Remove support button with updated modal trigger
                                if (currentUserSupportType != null) {
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = {
                                            // ✅ FIXED: Use updated modal type
                                            meguminModalType = MeguminModalType.RemoveSupport
                                            showMeguminModal = true
                                        },
                                        enabled = !uiState.isGivingSupport,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Remove Support")
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Show error message if any
                        if (uiState.error != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Error",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        uiState.error!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.clearError() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                            contentColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Text("Dismiss")
                                    }
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
// HELPER COMPOSABLES
// ============================================================================

@Composable
private fun SupportButton(
    supportType: SupportType,
    isSelected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColors = if (isSelected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (isSelected) {
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = modifier,
            colors = buttonColors
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("${getSupportEmoji(supportType)} ${getSupportText(supportType)}")
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = !isLoading,
            modifier = modifier,
            colors = buttonColors
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("${getSupportEmoji(supportType)} ${getSupportText(supportType)}")
            }
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS
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