package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.domain.model.SupportType
import com.dailychaos.project.util.timeAgo

/**
 * Community Post Card Component
 *
 * "Card untuk menampilkan post dari komunitas dengan fitur support"
 */
@Composable
fun CommunityPostCard(
    communityPost: CommunityPost,
    onCardClick: () -> Unit,
    onSupportClick: (SupportType) -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ParchmentCard(
        modifier = modifier.clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with anonymous user
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    username = communityPost.anonymousUsername,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = communityPost.anonymousUsername,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = communityPost.createdAt.timeAgo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                ChaosLevelBadge(chaosLevel = communityPost.chaosLevel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(
                text = communityPost.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = communityPost.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Support Actions
            SupportActionsRow(
                supportCount = communityPost.supportCount,
                twinCount = communityPost.twinCount,
                onSupportClick = onSupportClick,
                onReportClick = onReportClick
            )
        }
    }
}

@Composable
fun SupportActionsRow(
    supportCount: Int,
    twinCount: Int,
    onSupportClick: (SupportType) -> Unit,
    onReportClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Support buttons
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(SupportType.values()) { supportType ->
                SupportButton(
                    supportType = supportType,
                    onClick = { onSupportClick(supportType) }
                )
            }
        }

        // Stats and report
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (supportCount > 0) {
                Text(
                    text = "💙 $supportCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (twinCount > 0) {
                Text(
                    text = "🤝 $twinCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(
                onClick = onReportClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}