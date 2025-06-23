package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.dailychaos.project.domain.model.SyncStatus
import com.dailychaos.project.presentation.theme.*
import com.dailychaos.project.presentation.theme.SemanticColors.InfoLight

/**
 * Sync Status Indicator Component
 *
 * "Indikator status sinkronisasi data dengan server"
 */

@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (syncStatus) {
        SyncStatus.SYNCED -> Triple(
            Icons.Default.CheckCircle,
            KazumaGreen,
            "Synced"
        )
        SyncStatus.PENDING -> Triple(
            Icons.Default.Schedule,
            DarknessGold,
            "Pending"
        )
        SyncStatus.SYNCING -> Triple(
            Icons.Default.Sync,
            ChaosBlue,
            "Syncing"
        )
        SyncStatus.FAILED -> Triple(
            Icons.Default.Error,
            ExplosionRed,
            "Failed"
        )
        SyncStatus.LOCAL_ONLY -> Triple(
            Icons.Default.CloudOff,
            InfoLight,
            "Local"
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}