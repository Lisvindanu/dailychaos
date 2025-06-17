package com.dailychaos.project.presentation.ui.screen.chaos.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.*
import com.dailychaos.project.util.KonoSubaQuotes

/**
 * Create Chaos Screen - Updated with ViewModel Integration
 *
 * "Screen untuk membuat chaos entry baru dengan real Firebase integration!"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChaosScreen(
    onNavigateBack: () -> Unit = {},
    onChaosSaved: (String) -> Unit = {}, // Now receives entryId
    modifier: Modifier = Modifier,
    viewModel: CreateChaosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Get quote based on current chaos level
    val currentQuote = remember(uiState.chaosLevel) {
        KonoSubaQuotes.getQuoteForChaosLevel(ChaosLevel.fromValue(uiState.chaosLevel))
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect { entryId ->
            onChaosSaved(entryId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateBackEvent.collect {
            onNavigateBack()
        }
    }

    // Show error snackbar if there's an error
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar here or handle error display
        }
    }

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
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = { viewModel.onEvent(CreateChaosEvent.SaveChaosEntry) },
                        enabled = uiState.isSavable
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save",
                                tint = if (uiState.isSavable)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            // Show error message if exists
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.onEvent(CreateChaosEvent.ClearError) }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Motivational quote
            KonoSubaQuote(
                quote = currentQuote.text,
                character = currentQuote.character.displayName
            )

            // Title field
            CustomTextField(
                value = uiState.title,
                onValueChange = {
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.TitleChanged(it))
                    }
                },
                label = "What happened today?",
                placeholder = "Apa yang terjadi hari ini?",
                isError = uiState.title.isNotBlank() && viewModel.validateTitle(uiState.title) != null,
                errorMessage = if (uiState.title.isNotBlank()) viewModel.validateTitle(uiState.title) else null
            )

            // Chaos Level Picker
            ChaosLevelPicker(
                chaosLevel = uiState.chaosLevel,
                onChaosLevelChange = {
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.ChaosLevelChanged(it))
                    }
                }
            )

            // Description field
            CustomTextField(
                value = uiState.description,
                onValueChange = {
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.DescriptionChanged(it))
                    }
                },
                label = "Tell your story",
                placeholder = "Ceritakan chaos-mu... Ingat, bahkan party Kazuma punya hari buruk!",
                maxLines = 6,
                isError = uiState.description.isNotBlank() && viewModel.validateDescription(uiState.description) != null,
                errorMessage = if (uiState.description.isNotBlank()) viewModel.validateDescription(uiState.description) else null
            )

            // Mini Wins section
            MiniWinInput(
                miniWins = uiState.miniWins,
                onAddMiniWin = {
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.AddMiniWin(it))
                    }
                },
                onRemoveMiniWin = { index ->
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.RemoveMiniWin(index))
                    }
                }
            )

            // Tags section
            TagInputField(
                tags = uiState.tags,
                onAddTag = {
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.AddTag(it))
                    }
                },
                onRemoveTag = { tag ->
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.RemoveTag(tag))
                    }
                }
            )

            // Share options
            ShareOptionsSection(
                shareToCommnunity = uiState.shareToCommunity,
                onShareToggle = {
                    if (!uiState.isSaving) {
                        viewModel.onEvent(CreateChaosEvent.ShareToggled(it))
                    }
                }
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.onEvent(CreateChaosEvent.SaveChaosEntry) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isSavable
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Chaos")
                    }
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