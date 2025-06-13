package com.dailychaos.project.presentation.ui.screen.chaos.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.ChaosLevelBadge
import com.dailychaos.project.presentation.ui.component.ErrorMessage
import com.dailychaos.project.presentation.ui.component.LoadingIndicator
import com.dailychaos.project.util.toFriendlyDateString
import com.dailychaos.project.util.toTimeString
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaosDetailScreen(
    viewModel: ChaosDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChaosDetailViewModel.DetailScreenEvent.DeleteSuccess -> onNavigateBack()
                is ChaosDetailViewModel.DetailScreenEvent.ShareSuccess -> {
                    snackbarHostState.showSnackbar("Successfully shared to the community!")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chaos Entry Detail") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    uiState.entry?.let {
                        IconButton(onClick = { onNavigateToEdit(it.id) }) { Icon(Icons.Default.Edit, "Edit") }
                        IconButton(onClick = { viewModel.onEvent(ChaosDetailEvent.Share) }) { Icon(Icons.Default.Share, "Share") }
                        IconButton(onClick = { viewModel.onEvent(ChaosDetailEvent.Delete) }) { Icon(Icons.Default.Delete, "Delete") }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorMessage(
                    message = uiState.error!!,
                    onRetryClick = { viewModel.onEvent(ChaosDetailEvent.Retry) },
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                uiState.entry != null -> {
                    val entry = uiState.entry!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${entry.createdAt.toFriendlyDateString()} at ${entry.createdAt.toTimeString()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ChaosLevelBadge(chaosLevel = entry.chaosLevel)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(entry.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text(entry.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)

                        if (entry.miniWins.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text("🏆 Mini Wins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            entry.miniWins.forEach { win ->
                                Text("• $win", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
                            }
                        }

                        if (entry.tags.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text("🏷️ Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(entry.tags) { tag ->
                                    // CORRECTED USAGE of AssistChip
                                    AssistChip(
                                        onClick = { /* Non-interactive, so do nothing */ },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(ChaosDetailEvent.DismissDeleteDialog) },
                    title = { Text("Delete Entry?") },
                    text = { Text("This action cannot be undone. Are you sure you want to delete this chaos entry?") },
                    confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), onClick = { viewModel.onEvent(ChaosDetailEvent.ConfirmDelete) }) { Text("Delete") } },
                    dismissButton = { TextButton(onClick = { viewModel.onEvent(ChaosDetailEvent.DismissDeleteDialog) }) { Text("Cancel") } }
                )
            }
            if (uiState.showShareConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(ChaosDetailEvent.DismissShareDialog) },
                    title = { Text("Share to Community?") },
                    text = { Text("Your entry will be shared anonymously. Your party members might find a chaos twin in you!") },
                    confirmButton = { Button(onClick = { viewModel.onEvent(ChaosDetailEvent.ConfirmShare) }) { Text("Share Anonymously") } },
                    dismissButton = { TextButton(onClick = { viewModel.onEvent(ChaosDetailEvent.DismissShareDialog) }) { Text("Cancel") } }
                )
            }
        }
    }
}