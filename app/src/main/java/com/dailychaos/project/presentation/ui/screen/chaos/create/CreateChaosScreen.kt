package com.dailychaos.project.presentation.ui.screen.chaos.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.BuildConfig
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.*
import com.dailychaos.project.util.KonoSubaQuotes
import timber.log.Timber

/**
 * Create Chaos Screen - Enhanced with Debug UI
 *
 * "Screen untuk membuat chaos entry baru dengan comprehensive debugging UI!"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChaosScreen(
    onNavigateBack: () -> Unit = {},
    onChaosSaved: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreateChaosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Log screen state changes
    LaunchedEffect(uiState) {
        Timber.d("🎯 CreateChaosScreen UI State changed:")
        Timber.d("  - Is Saving: ${uiState.isSaving}")
        Timber.d("  - Is Savable: ${uiState.isSavable}")
        Timber.d("  - Has Error: ${uiState.error != null}")
        Timber.d("  - Title length: ${uiState.title.length}")
        Timber.d("  - Description length: ${uiState.description.length}")
    }

    // Get quote based on current chaos level
    val currentQuote = remember(uiState.chaosLevel) {
        KonoSubaQuotes.getQuoteForChaosLevel(ChaosLevel.fromValue(uiState.chaosLevel))
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        Timber.d("🔄 Setting up save success event listener")
        viewModel.saveSuccessEvent.collect { entryId ->
            Timber.d("🎉 Save success event received in CreateChaosScreen: $entryId")
            onChaosSaved(entryId)
        }
    }

    LaunchedEffect(Unit) {
        Timber.d("🔄 Setting up navigate back event listener")
        viewModel.navigateBackEvent.collect {
            Timber.d("⬅️ Navigate back event received in CreateChaosScreen")
            onNavigateBack()
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
                        onClick = {
                            Timber.d("⬅️ Back button clicked")
                            viewModel.navigateBack()
                        },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = {
                            Timber.d("💾 Save button clicked")
                            Timber.d("💾 Current state - Title: '${uiState.title}', Is Savable: ${uiState.isSavable}")
                            viewModel.onEvent(CreateChaosEvent.SaveChaosEntry)
                        },
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
            // Enhanced Error Display
            uiState.error?.let { error ->
                EnhancedErrorCard(
                    error = error,
                    onDismiss = { viewModel.onEvent(CreateChaosEvent.ClearError) },
                    onRetryAuth = { viewModel.recheckAuthentication() },
                    onTriggerAnonymousAuth = { viewModel.triggerAnonymousAuth() }
                )
            }

            // Debug Info Card (only in debug mode)
            if (BuildConfig.DEBUG) {
                DebugInfoCard(uiState = uiState)
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
                        Timber.d("✏️ Title changed to: '$it'")
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
                        Timber.d("🎚️ Chaos level changed to: $it")
                        viewModel.onEvent(CreateChaosEvent.ChaosLevelChanged(it))
                    }
                }
            )

            // Description field
            CustomTextField(
                value = uiState.description,
                onValueChange = {
                    if (!uiState.isSaving) {
                        Timber.d("✏️ Description changed, length: ${it.length}")
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
                        Timber.d("🏆 Adding mini win: '$it'")
                        viewModel.onEvent(CreateChaosEvent.AddMiniWin(it))
                    }
                },
                onRemoveMiniWin = { index ->
                    if (!uiState.isSaving) {
                        Timber.d("🗑️ Removing mini win at index: $index")
                        viewModel.onEvent(CreateChaosEvent.RemoveMiniWin(index))
                    }
                }
            )

            // Tags section
            TagInputField(
                tags = uiState.tags,
                onAddTag = {
                    if (!uiState.isSaving) {
                        Timber.d("🏷️ Adding tag: '$it'")
                        viewModel.onEvent(CreateChaosEvent.AddTag(it))
                    }
                },
                onRemoveTag = { tag ->
                    if (!uiState.isSaving) {
                        Timber.d("🗑️ Removing tag: '$tag'")
                        viewModel.onEvent(CreateChaosEvent.RemoveTag(tag))
                    }
                }
            )

            // Share options
            ShareOptionsSection(
                shareToCommnunity = uiState.shareToCommunity,
                onShareToggle = {
                    if (!uiState.isSaving) {
                        Timber.d("🤝 Share to community toggled: $it")
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
                    onClick = {
                        Timber.d("❌ Cancel button clicked")
                        viewModel.navigateBack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        Timber.d("💾 Save Chaos button clicked")
                        viewModel.onEvent(CreateChaosEvent.SaveChaosEntry)
                    },
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
private fun EnhancedErrorCard(
    error: String,
    onDismiss: () -> Unit,
    onRetryAuth: () -> Unit,
    onTriggerAnonymousAuth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }

            // Debug buttons - show for authentication errors
            if (error.contains("Authentication", ignoreCase = true) ||
                error.contains("login", ignoreCase = true) ||
                error.contains("auth", ignoreCase = true)) {

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Timber.d("🔄 Recheck Auth button clicked")
                            onRetryAuth()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔄 Recheck Auth")
                    }
                    OutlinedButton(
                        onClick = {
                            Timber.d("🔐 Try Anonymous button clicked")
                            onTriggerAnonymousAuth()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔐 Anonymous")
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugInfoCard(uiState: CreateChaosUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🐛 Debug Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = """
                    • Title: "${uiState.title}" (${uiState.title.length} chars)
                    • Description: ${uiState.description.length} chars
                    • Chaos Level: ${uiState.chaosLevel}
                    • Mini Wins: ${uiState.miniWins.size}
                    • Tags: ${uiState.tags.size}
                    • Share: ${uiState.shareToCommunity}
                    • Is Savable: ${uiState.isSavable}
                    • Is Saving: ${uiState.isSaving}
                    • Has Error: ${uiState.error != null}
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
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

