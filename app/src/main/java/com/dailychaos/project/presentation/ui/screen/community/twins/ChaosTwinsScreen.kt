package com.dailychaos.project.presentation.ui.screen.community.twins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import com.dailychaos.project.presentation.ui.component.ChaosTwinsCard
import com.dailychaos.project.presentation.ui.component.EmptyState
import com.dailychaos.project.util.generateAnonymousUsername
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaosTwinsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPost: (String) -> Unit
) {
    // ViewModel and State would be implemented here
    val mockTwins = List(5) { i ->
        CommunityPost(
            id = "twin_post_$i",
            anonymousUsername = String.generateAnonymousUsername(), // CORRECTED
            title = "Similar struggles found!",
            description = "My project manager is a real-life Darkness, enjoying every critical bug report I submit. It's a masochistic paradise of suffering.",
            chaosLevel = Random.nextInt(7, 11),
            createdAt = Clock.System.now().minus(i.days)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chaos Twins") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (mockTwins.isEmpty()) {
            EmptyState(
                illustration = "🤷‍♀️",
                title = "No Twins Found Yet",
                subtitle = "Share more of your chaos entries to find adventurers with similar quests!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockTwins) { post ->
                    ChaosTwinsCard(
                        twinPost = post,
                        similarity = Random.nextFloat(),
                        onViewClick = { onNavigateToPost(post.id) },
                        onSupportClick = { /* TODO: ViewModel Event */ }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChaosTwinsScreenPreview() {
    DailyChaosTheme {
        ChaosTwinsScreen({}, {})
    }
}