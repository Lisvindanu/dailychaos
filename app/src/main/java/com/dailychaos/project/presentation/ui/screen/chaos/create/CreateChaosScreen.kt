package com.dailychaos.project.presentation.ui.screen.chaos.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.*
import com.dailychaos.project.util.KonoSubaQuotes

/**
 * Create Chaos Screen
 *
 * "Screen untuk membuat chaos entry baru dengan UI yang friendly dan motivational"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChaosScreen(
    onNavigateBack: () -> Unit = {},
    onChaosSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chaosLevel by remember { mutableIntStateOf(5) }
    var miniWins by remember { mutableStateOf(listOf<String>()) }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var shareToCommnunity by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val currentQuote = remember { KonoSubaQuotes.getQuoteForChaosLevel(ChaosLevel.fromValue(chaosLevel)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "✍️ Record Today's Chaos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = {
                            // TODO: Save chaos entry
                            onChaosSaved()
                        },
                        enabled = title.isNotBlank() && description.length >= 10
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (title.isNotBlank() && description.length >= 10)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Motivational quote
            KonoSubaQuote(
                quote = currentQuote.text,
                character = currentQuote.character.displayName
            )

            // Title field
            CustomTextField(
                value = title,
                onValueChange = { title = it },
                label = "What happened today?",
                placeholder = "Apa yang terjadi hari ini?",
                isError = title.isBlank(),
                errorMessage = if (title.isBlank()) "Title tidak boleh kosong" else null
            )

            // Chaos Level Picker
            ChaosLevelPicker(
                chaosLevel = chaosLevel,
                onChaosLevelChange = { chaosLevel = it }
            )

            // Description field
            CustomTextField(
                value = description,
                onValueChange = { description = it },
                label = "Tell your story",
                placeholder = "Ceritakan chaos-mu... Ingat, bahkan party Kazuma punya hari buruk!",
                maxLines = 6,
                isError = description.length < 10 && description.isNotEmpty(),
                errorMessage = if (description.length < 10 && description.isNotEmpty())
                    "Description minimal 10 karakter" else null
            )

            // Mini Wins section
            MiniWinInput(
                miniWins = miniWins,
                onAddMiniWin = { miniWins = miniWins + it },
                onRemoveMiniWin = { index ->
                    miniWins = miniWins.toMutableList().apply { removeAt(index) }
                }
            )

            // Tags section
            TagInputField(
                tags = tags,
                onAddTag = { tags = tags + it },
                onRemoveTag = { tag -> tags = tags - tag }
            )

            // Share options
            ShareOptionsSection(
                shareToCommnunity = shareToCommnunity,
                onShareToggle = { shareToCommnunity = it }
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        // TODO: Save chaos entry with share option
                        onChaosSaved()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank() && description.length >= 10
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Chaos")
                }
            }

            // Helpful tips
            HelpfulTipsCard()

            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareOptionsSection(
    shareToCommnunity: Boolean,
    onShareToggle: (Boolean) -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤝",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = shareToCommnunity,
                    onCheckedChange = onShareToggle
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Share to Community",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Let others find their chaos twin in you (posted anonymously)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpfulTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Writing Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val tips = listOf(
                "Be honest about your feelings - this is your safe space",
                "Include small wins, even if they seem tiny",
                "Use humor if it helps - chaos can be funny in hindsight",
                "Remember: even Kazuma's party has disasters but they stick together"
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateChaosScreenPreview() {
    DailyChaosTheme {
        CreateChaosScreen()
    }
}