package com.dailychaos.project.presentation.ui.screen.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dailychaos.project.util.KonoSubaQuotes
import com.dailychaos.project.data.remote.api.KonoSubaApiService
import com.dailychaos.project.domain.model.CharacterCard
import kotlinx.coroutines.delay

// Impor yang diperlukan untuk ViewModel (HiltViewModel atau viewModel())
import androidx.hilt.navigation.compose.hiltViewModel // Paling umum digunakan dengan Hilt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    isUserLoggedIn: Boolean = false,
    apiService: KonoSubaApiService? = null,
    // Mengambil instance SplashViewModel
    viewModel: SplashViewModel = hiltViewModel() // Menggunakan hiltViewModel() jika Anda menggunakan Hilt
) {
    var showContent by remember { mutableStateOf(false) }
    var showCharacterImage by remember { mutableStateOf(false) }
    var showQuote by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    var currentQuote by remember { mutableStateOf(KonoSubaQuotes.getRandomQuote()) }
    var characterCard by remember { mutableStateOf<CharacterCard?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    // Function to load character image from API
    suspend fun loadCharacterImage(character: KonoSubaQuotes.Character) {
        isLoadingImage = true
        try {
            characterCard = apiService?.getCharacterCard(character.displayName)
        } catch (e: Exception) {
            characterCard = null
        } finally {
            isLoadingImage = false
        }
    }

    // Initial load sequence
    LaunchedEffect(Unit) {
        delay(500)
        showContent = true

        delay(800)
        loadCharacterImage(currentQuote.character)
        showCharacterImage = true

        delay(1000)
        showQuote = true

        delay(2000)
        showButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE0B2)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // DEBUG INFO - Simplified
        AnimatedVisibility(
            visible = showContent,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "🐛 DEBUG MODE",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "isUserLoggedIn: $isUserLoggedIn",
                        fontSize = 8.sp,
                        color = Color.White
                    )
                    Text(
                        text = "API Service: ${if (apiService != null) "✅" else "❌"}",
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            // Logo section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = tween(1000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌪️",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Daily Chaos",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B4513),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Powered by KonoSuba API ✨",
                        fontSize = 12.sp,
                        color = Color(0xFF6B4423),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Character Image
            AnimatedVisibility(
                visible = showCharacterImage,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                    animationSpec = tween(600),
                    initialScale = 0.8f
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoadingImage -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFF8B4513)
                            )
                        }

                        characterCard?.imageUrl != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(characterCard!!.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${currentQuote.character.displayName} card",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Text(
                                text = when(currentQuote.character) {
                                    KonoSubaQuotes.Character.KAZUMA -> "⚔️"
                                    KonoSubaQuotes.Character.AQUA -> "💧"
                                    KonoSubaQuotes.Character.MEGUMIN -> "💥"
                                    KonoSubaQuotes.Character.DARKNESS -> "🛡️"
                                    KonoSubaQuotes.Character.PARTY -> "👥"
                                },
                                fontSize = 48.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quote Card
            AnimatedVisibility(
                visible = showQuote,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    animationSpec = tween(800),
                    initialOffsetY = { it / 2 }
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentQuote.text,
                            fontSize = 14.sp,
                            color = Color(0xFF5D4037),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "- ${currentQuote.character.displayName}",
                                fontSize = 12.sp,
                                color = Color(0xFF8B4513),
                                fontWeight = FontWeight.Bold
                            )

                            if (characterCard != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🔥",
                                    fontSize = 10.sp
                                )
                            }
                        }

                        characterCard?.let { card ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "★${card.rarity} • ${card.element ?: "Unknown"}",
                                fontSize = 10.sp,
                                color = Color(0xFF6B4423).copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Buttons
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                    animationSpec = tween(600),
                    initialOffsetY = { it }
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // MAIN BUTTON
                    Button(
                        onClick = {
                            if (isUserLoggedIn) {
                                onNavigateToHome()
                            } else {
                                onNavigateToOnboarding()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B4513)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isUserLoggedIn) "Continue Quest" else "Start Adventure",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // DEBUG BUTTONS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToOnboarding() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Green.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Force Onboard",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { onNavigateToHome() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Force Home",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // TOMBOL DEBUG BARU UNTUK MIGRASI
                    Button(
                        onClick = { viewModel.forceRunMigration() }, // Memanggil fungsi di ViewModel
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Magenta.copy(alpha = 0.7f) // Warna berbeda agar jelas
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f) // Sesuaikan lebar agar lebih menonjol
                            .height(48.dp) // Sesuaikan tinggi agar mudah diakses
                    ) {
                        Text(
                            text = "Run Migration (DEBUG)",
                            fontSize = 14.sp, // Ukuran font lebih besar
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    // ===========================================

                    Spacer(modifier = Modifier.height(8.dp)) // Tambahkan spasi setelah tombol migrasi

                    Text(
                        text = "Ready to track your daily chaos?",
                        fontSize = 12.sp,
                        color = Color(0xFF6B4423).copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}