package com.dailychaos.project.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.dailychaos.project.presentation.theme.DailyChaosTheme

/**
 * Tag Input Field Component
 *
 * "Component untuk menambah dan menampilkan tags pada chaos entry"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagInputField(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "work, family, friends..."
) {
    var newTag by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏷️",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                placeholder = { Text(placeholder) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                        onAddTag(newTag.trim())
                        newTag = ""
                    }
                },
                enabled = newTag.isNotBlank() && !tags.contains(newTag.trim())
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add tag",
                    tint = if (newTag.isNotBlank() && !tags.contains(newTag.trim()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tags display
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    TagChip(
                        text = tag,
                        onRemove = { onRemoveTag(tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove tag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun TagInputFieldPreview() {
    DailyChaosTheme {
        Surface {
            TagInputField(
                tags = listOf("work", "stress", "productivity"),
                onAddTag = {},
                onRemoveTag = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}