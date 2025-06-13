package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/**
 * Mini Win Input Component
 *
 * "Component untuk menambah dan menampilkan mini wins (kemenangan kecil)"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniWinInput(
    miniWins: List<String>,
    onAddMiniWin: (String) -> Unit,
    onRemoveMiniWin: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var newMiniWin by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // Title with celebration emoji
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mini Wins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Kemenangan kecil yang patut dirayakan hari ini",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMiniWin,
                onValueChange = { newMiniWin = it },
                placeholder = { Text("Selesaikan tugas penting...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newMiniWin.isNotBlank()) {
                        onAddMiniWin(newMiniWin.trim())
                        newMiniWin = ""
                    }
                },
                enabled = newMiniWin.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add mini win",
                    tint = if (newMiniWin.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mini wins list
        if (miniWins.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(miniWins) { index, miniWin ->
                    MiniWinItem(
                        text = miniWin,
                        onRemove = { onRemoveMiniWin(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniWinItem(
    text: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}