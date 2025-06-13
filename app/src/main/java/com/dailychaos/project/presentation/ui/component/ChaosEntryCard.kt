package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.domain.model.SyncStatus
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import kotlinx.datetime.Clock

/**
 * Chaos Entry Card Component
 *
 * "Seperti quest log Kazuma - tapi lebih aesthetic!"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaosEntryCard(
    entry: ChaosEntry,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    showActions: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    val chaosLevel = entry.getChaosLevelEnum()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                isExpanded = !isExpanded
                onCardClick()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Title and chaos level
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.title.ifBlank { "Untitled Chaos" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chaos level chip
                    ChaosLevelChip(
                        level = entry.chaosLevel,
                        emoji = chaosLevel.emoji
                    )
                }

                // Sync status indicator
                SyncStatusIndicator(
                    status = entry.syncStatus,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            // Mini wins (if expanded and has wins)
            if (isExpanded && entry.hasMiniWins()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🏆 Mini Wins",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                entry.miniWins.forEach { win ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = win,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tags (if expanded and has tags)
            if (isExpanded && entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entry.tags) { tag ->
                        TagChip(tag = tag)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp and word count
                Column {
                    Text(
                        text = formatTimestamp(entry.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (isExpanded) {
                        Text(
                            text = "${entry.getWordCount()} words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Action buttons (if expanded and showActions)
                if (isExpanded && showActions) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChaosLevelChip(
    level: Int,
    emoji: String
) {
    val backgroundColor = when (level) {
        in 1..2 -> MaterialTheme.colorScheme.secondaryContainer
        in 3..4 -> MaterialTheme.colorScheme.tertiaryContainer
        in 5..6 -> MaterialTheme.colorScheme.primaryContainer
        in 7..8 -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.error
    }

    val textColor = when (level) {
        in 1..2 -> MaterialTheme.colorScheme.onSecondaryContainer
        in 3..4 -> MaterialTheme.colorScheme.onTertiaryContainer
        in 5..6 -> MaterialTheme.colorScheme.onPrimaryContainer
        in 7..8 -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onError
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$emoji Level $level",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun SyncStatusIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        SyncStatus.SYNCED -> Icons.Default.CloudDone to MaterialTheme.colorScheme.primary
        SyncStatus.SYNCING -> Icons.Default.CloudSync to MaterialTheme.colorScheme.secondary
        SyncStatus.PENDING -> Icons.Default.CloudQueue to MaterialTheme.colorScheme.tertiary
        SyncStatus.FAILED -> Icons.Default.CloudOff to MaterialTheme.colorScheme.error
        SyncStatus.LOCAL_ONLY -> Icons.Default.Storage to MaterialTheme.colorScheme.outline
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        modifier = modifier.size(20.dp),
        tint = color
    )
}

@Composable
private fun TagChip(tag: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(instant: kotlinx.datetime.Instant): String {
    // TODO: Implement proper timestamp formatting
    return "2 hours ago" // Placeholder
}

@Preview(showBackground = true)
@Composable
fun ChaosEntryCardPreview() {
    DailyChaosTheme {
        val sampleEntry = ChaosEntry(
            id = "1",
            title = "Kazuma Style Chaos",
            description = "Hari ini seperti main game dengan party yang aneh tapi somehow berhasil. Ada Aqua yang nangis, Megumin yang explosion, dan Darkness yang... ya gitu deh.",
            chaosLevel = 7,
            miniWins = listOf("Berhasil selesaikan quest", "Ga ada yang mati", "Dapet gold lumayan"),
            tags = listOf("gaming", "party", "adventure"),
            createdAt = Clock.System.now(),
            syncStatus = SyncStatus.SYNCED
        )

        ChaosEntryCard(
            entry = sampleEntry,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChaosEntryCardCollapsedPreview() {
    DailyChaosTheme {
        val sampleEntry = ChaosEntry(
            id = "2",
            title = "Megumin Explosion Day",
            description = "EXPLOSION! EXPLOSION! EXPLOSION! Hari ini full commit ke satu hal dan hasilnya spectacular!",
            chaosLevel = 10,
            syncStatus = SyncStatus.PENDING
        )

        ChaosEntryCard(
            entry = sampleEntry,
            modifier = Modifier.padding(16.dp),
            showActions = false
        )
    }
}