package com.dailychaos.project.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailychaos.project.presentation.theme.DailyChaosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyChaosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DailyChaosApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChaosApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Chaos Journal", "Community", "Profile")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Chaos 🌪️",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Book
                                    1 -> Icons.Default.People
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ChaosJournalScreen()
                1 -> CommunityScreen()
                2 -> ProfileScreen()
            }
        }
    }
}

@Composable
fun ChaosJournalScreen() {
    val chaosEntries = remember {
        listOf(
            ChaosEntry("Kazuma Style Chaos", "Hari ini seperti main game dengan party yang aneh tapi somehow berhasil", "Medium", "2 hours ago"),
            ChaosEntry("Aqua Moment", "Percaya diri tinggi tapi hasil zero, classic Aqua energy", "Low", "4 hours ago"),
            ChaosEntry("Megumin Explosion", "All or nothing approach hari ini, EXPLOSION!", "High", "1 day ago")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎭 Daily Chaos Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"Bahkan party paling disfungsional pun masih bisa selamatkan hari\"",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Button(
                onClick = { /* TODO: Add new chaos entry */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Today's Chaos")
            }
        }

        items(chaosEntries) { entry ->
            ChaosEntryCard(entry = entry)
        }
    }
}

@Composable
fun CommunityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Community Coming Soon!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Find your chaos twins and share the adventure",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Anonymous Adventurer",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Level 1 Chaos Survivor",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChaosEntryCard(entry: ChaosEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ChaosLevelChip(level = entry.chaosLevel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ChaosLevelChip(level: String) {
    val (color, emoji) = when (level) {
        "Low" -> MaterialTheme.colorScheme.secondaryContainer to "😌"
        "Medium" -> MaterialTheme.colorScheme.tertiaryContainer to "🌪️"
        "High" -> MaterialTheme.colorScheme.errorContainer to "💥"
        else -> MaterialTheme.colorScheme.surfaceVariant to "❓"
    }

    AssistChip(
        onClick = { },
        label = { Text("$emoji $level") },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color
        )
    )
}

// Data class for chaos entries
data class ChaosEntry(
    val title: String,
    val description: String,
    val chaosLevel: String,
    val timestamp: String
)

@Preview(showBackground = true)
@Composable
fun DailyChaosAppPreview() {
    DailyChaosTheme {
        DailyChaosApp()
    }
}