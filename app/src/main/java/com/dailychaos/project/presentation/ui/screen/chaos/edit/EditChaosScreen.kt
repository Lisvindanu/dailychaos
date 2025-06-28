package com.dailychaos.project.presentation.ui.screen.chaos.edit

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailychaos.project.domain.model.ChaosLevel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import kotlinx.coroutines.launch

/**
 * Edit Chaos Screen
 * "Screen untuk mengedit chaos entry yang sudah ada"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChaosScreen(
    onNavigateBack: () -> Unit = {},
    onChaosUpdated: () -> Unit = {},
    viewModel: EditChaosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Navigate back when save is successful
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onChaosUpdated()
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }

    // Handle back press with unsaved changes
    BackHandler(enabled = viewModel.hasUnsavedChanges()) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Ada perubahan yang belum disimpan. Yakin mau keluar?",
                actionLabel = "Keluar",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "✏️ Edit Chaos Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Ada perubahan yang belum disimpan. Yakin mau keluar?",
                                    actionLabel = "Keluar",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onNavigateBack()
                                }
                            }
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveChanges() },
                        enabled = uiState.canSave
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Simpan",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.originalEntry == null -> {
                    ErrorContent(
                        message = "Chaos entry tidak ditemukan",
                        onRetry = { /* Reload */ }
                    )
                }
                else -> {
                    EditChaosContent(
                        uiState = uiState,
                        onTitleChange = viewModel::updateTitle,
                        onDescriptionChange = viewModel::updateDescription,
                        onChaosLevelChange = viewModel::updateChaosLevel,
                        onCurrentMiniWinChange = viewModel::updateCurrentMiniWin,
                        onAddMiniWin = viewModel::addMiniWin,
                        onRemoveMiniWin = viewModel::removeMiniWin,
                        onCurrentTagChange = viewModel::updateCurrentTag,
                        onAddTag = viewModel::addTag,
                        onRemoveTag = viewModel::removeTag
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                "Loading chaos entry...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onRetry) {
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun EditChaosContent(
    uiState: EditChaosUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onChaosLevelChange: (Int) -> Unit,
    onCurrentMiniWinChange: (String) -> Unit,
    onAddMiniWin: () -> Unit,
    onRemoveMiniWin: (Int) -> Unit,
    onCurrentTagChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val miniWinFocusRequester = remember { FocusRequester() }
    val tagFocusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Input
        item {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Judul Chaos") },
                placeholder = { Text("Berikan judul yang memorable!") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.title.isBlank()
            )
        }

        // Description Input
        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Cerita Chaos") },
                placeholder = { Text("Ceritakan petualangan chaos hari ini... (min. 10 karakter)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                supportingText = {
                    Text(
                        "${uiState.description.length} karakter",
                        color = if (uiState.description.length < 10)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                isError = uiState.description.length < 10
            )
        }

        // Chaos Level Selector
        item {
            ChaosLevelSelector(
                selectedLevel = uiState.chaosLevel,
                onLevelSelected = onChaosLevelChange
            )
        }

        // Mini Wins Section
        item {
            MiniWinsSection(
                miniWins = uiState.miniWins,
                currentMiniWin = uiState.currentMiniWin,
                onCurrentMiniWinChange = onCurrentMiniWinChange,
                onAddMiniWin = onAddMiniWin,
                onRemoveMiniWin = onRemoveMiniWin,
                focusRequester = miniWinFocusRequester
            )
        }

        // Tags Section
        item {
            TagsSection(
                tags = uiState.tags,
                currentTag = uiState.currentTag,
                onCurrentTagChange = onCurrentTagChange,
                onAddTag = onAddTag,
                onRemoveTag = onRemoveTag,
                focusRequester = tagFocusRequester
            )
        }

        // Add some bottom padding
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChaosLevelSelector(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "Chaos Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..10).forEach { level ->
                        val chaosLevel = ChaosLevel.fromValue(level)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedLevel == level)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                                .clickable { onLevelSelected(level) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                chaosLevel.emoji,
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val currentLevel = ChaosLevel.fromValue(selectedLevel)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Level $selectedLevel: ${currentLevel.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            ChaosLevel.getRandomQuote(currentLevel),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniWinsSection(
    miniWins: List<String>,
    currentMiniWin: String,
    onCurrentMiniWinChange: (String) -> Unit,
    onAddMiniWin: () -> Unit,
    onRemoveMiniWin: (Int) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "Mini Wins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Mini wins chips
        if (miniWins.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(miniWins.size) { index ->
                    InputChip(
                        selected = false,
                        onClick = { onRemoveMiniWin(index) },
                        label = { Text(miniWins[index]) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Add mini win input
        OutlinedTextField(
            value = currentMiniWin,
            onValueChange = onCurrentMiniWinChange,
            placeholder = { Text("Tambah mini win...") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = onAddMiniWin,
                    enabled = currentMiniWin.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (currentMiniWin.isNotBlank()) {
                        onAddMiniWin()
                    }
                }
            )
        )
    }
}

@Composable
private fun TagsSection(
    tags: List<String>,
    currentTag: String,
    onCurrentTagChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Tag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "Tags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Tags chips
        if (tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    InputChip(
                        selected = false,
                        onClick = { onRemoveTag(tag) },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Add tag input
        OutlinedTextField(
            value = currentTag,
            onValueChange = onCurrentTagChange,
            placeholder = { Text("Tambah tag...") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = onAddTag,
                    enabled = currentTag.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (currentTag.isNotBlank()) {
                        onAddTag()
                    }
                }
            )
        )
    }
}

@Composable
private fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

@Preview(showBackground = true)
@Composable
fun EditChaosScreenPreview() {
    DailyChaosTheme {
        EditChaosScreen()
    }
}