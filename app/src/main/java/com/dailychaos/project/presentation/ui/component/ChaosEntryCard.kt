package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.domain.model.SyncStatus
import com.dailychaos.project.util.timeAgo

/**
 * Chaos Entry Card Component
 *
 * "Card untuk menampilkan chaos entry personal dengan style yang cozy"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaosEntryCard(
    chaosEntry: ChaosEntry,
    onCardClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ParchmentCard(
        modifier = modifier.clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chaosEntry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = chaosEntry.createdAt.timeAgo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Chaos Level Badge
                ChaosLevelBadge(chaosLevel = chaosEntry.chaosLevel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = chaosEntry.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mini Wins
            if (chaosEntry.miniWins.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chaosEntry.miniWins.take(3)) { miniWin ->
                        MiniWinChip(text = miniWin)
                    }
                    if (chaosEntry.miniWins.size > 3) {
                        item {
                            Text(
                                text = "+${chaosEntry.miniWins.size - 3}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Actions
            if (onShareClick != null || onEditClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    onEditClick?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    onShareClick?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Sync Status Indicator
            if (chaosEntry.syncStatus != SyncStatus.SYNCED) {
                Spacer(modifier = Modifier.height(8.dp))
                SyncStatusIndicator(syncStatus = chaosEntry.syncStatus)
            }
        }
    }
}

@Composable
fun ChaosLevelBadge(chaosLevel: Int) {
    val chaosLevelEnum = ChaosLevel.fromValue(chaosLevel)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            chaosLevel <= 3 -> Color(0xFF81C784)
            chaosLevel <= 6 -> Color(0xFFFFB74D)
            chaosLevel <= 8 -> Color(0xFFE57373)
            else -> Color(0xFFBA68C8)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chaosLevelEnum.emoji,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = chaosLevel.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MiniWinChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆",
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}