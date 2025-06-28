package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import com.dailychaos.project.domain.model.CharacterCard
import kotlinx.coroutines.delay

/**
 * Enhanced KonoSuba Quote Component with Character Images and Gestures
 *
 * "Menampilkan quote motivasi dari karakter KonoSuba dengan gambar karakter asli + swipe gestures"
 */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KonoSubaQuote(
    quote: String,
    character: String,
    modifier: Modifier = Modifier,
    apiService: KonoSubaApiService? = null,
    onRefreshQuote: (() -> Unit)? = null,
    onNextQuote: (() -> Unit)? = null,
    showRefreshButton: Boolean = true
) {
    var characterCard by remember(character) { mutableStateOf<CharacterCard?>(null) }
    var isLoadingImage by remember(character) { mutableStateOf(false) }
    var hasAttemptedLoad by remember(character) { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    // Load character image when character changes
    LaunchedEffect(character) {
        if (apiService != null && !hasAttemptedLoad) {
            isLoadingImage = true
            hasAttemptedLoad = true
            try {
                delay(100) // Small delay to prevent too many API calls
                characterCard = apiService.getCharacterCard(character)
            } catch (e: Exception) {
                characterCard = null
            } finally {
                isLoadingImage = false
            }
        }
    }

    // Animation for quote changes
    LaunchedEffect(quote) {
        isVisible = false
        delay(150)
        isVisible = true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Swipe right untuk next quote
                        onNextQuote?.invoke()
                    }
                ) { change, _ ->
                    if (change.position.x > 100) {
                        onNextQuote?.invoke()
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),

    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { it / 4 }
            ),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Column {
                // Header with character info and refresh button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = character,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )

                        // Show rarity stars if character card is loaded
                        characterCard?.let { card ->
                            Text(
                                text = "★".repeat(card.rarity),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD700) // Gold color for stars
                            )

                            card.element?.let { element ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = getElementColor(element).copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = element,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = getElementColor(element),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Refresh button
                    if (showRefreshButton && onRefreshQuote != null) {
                        IconButton(
                            onClick = onRefreshQuote,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Quote",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Main content
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character Image/Avatar
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.8f),
                                        Color.White.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoadingImage -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            characterCard?.imageUrl != null -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(characterCard!!.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "$character card",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            else -> {
                                Text(
                                    text = getCharacterEmoji(character),
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Quote Content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Quote text with animation
                        AnimatedContent(
                            targetState = quote,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400)) togetherWith
                                        fadeOut(animationSpec = tween(200))
                            },
                            label = "quote_animation"
                        ) { targetQuote ->
                            Text(
                                text = "\"$targetQuote\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Swipe hint (subtle)
                if (onNextQuote != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Swipe untuk quote lain →",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getCharacterEmoji(character: String): String {
    return when (character.lowercase()) {
        "kazuma" -> "⚔️"
        "aqua" -> "💧"
        "megumin" -> "💥"
        "darkness" -> "🛡️"
        "wiz" -> "🔮"
        "yunyun" -> "🎭"
        "chris" -> "🗡️"
        else -> "👥"
    }
}

private fun getElementColor(element: String): Color {
    return when (element.lowercase()) {
        "water" -> Color(0xFF1976D2)
        "fire", "explosion" -> Color(0xFFD32F2F)
        "earth", "defense" -> Color(0xFF388E3C)
        "wind" -> Color(0xFF00796B)
        "light" -> Color(0xFFFFC107)
        "dark" -> Color(0xFF7B1FA2)
        "adventure" -> Color(0xFFFF5722)
        else -> Color(0xFF616161)
    }
}