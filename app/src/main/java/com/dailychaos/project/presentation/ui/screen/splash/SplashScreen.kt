// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/splash/SplashScreen.kt
package com.dailychaos.project.presentation.ui.screen.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit, // Jika user sudah login
    isUserLoggedIn: Boolean = false
) {
    var showContent by remember { mutableStateOf(false) }
    var showQuote by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    var currentQuoteIndex by remember { mutableStateOf(0) }

    val konoSubaQuotes = listOf(
        "\"Tank damage untuk party? With pleasure!\" - Darkness",
        "\"I am Megumin! Arch Wizard of the Crimson Magic Clan!\"",
        "\"My job here is done.\" - Aqua (doing nothing useful)",
        "\"Gender equality means I can drop-kick anyone!\" - Kazuma",
        "\"I cast Explosion!\" - Megumin (every single time)"
    )

    // Animasi masuk
    LaunchedEffect(Unit) {
        delay(500)
        showContent = true
        delay(1000)
        showQuote = true
        delay(2000)
        showButton = true
    }

    // Auto-rotate quotes
    LaunchedEffect(showQuote) {
        if (showQuote) {
            while (true) {
                delay(3000)
                currentQuoteIndex = (currentQuoteIndex + 1) % konoSubaQuotes.size
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0), // Warm beige
                        Color(0xFFFFE0B2)  // Light orange
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo dengan animasi
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = tween(1000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tornado icon
                    Text(
                        text = "🌪️",
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Daily Chaos",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B4513),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Tank damage untuk party? With pleasure!",
                        fontSize = 14.sp,
                        color = Color(0xFF6B4423),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Quote section dengan animasi
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
                        containerColor = Color.White.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    // Animasi pergantian quote
                    AnimatedContent(
                        targetState = currentQuoteIndex,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith
                                    fadeOut(animationSpec = tween(500))
                        },
                        label = "quote_animation"
                    ) { index ->
                        Text(
                            text = konoSubaQuotes[index],
                            fontSize = 16.sp,
                            color = Color(0xFF5D4037),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Button dengan animasi
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
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B4513)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = if (isUserLoggedIn) "Continue Adventure" else "Start Adventure",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Skip button untuk testing
                    TextButton(
                        onClick = { onNavigateToOnboarding() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Skip",
                            color = Color(0xFF8B4513).copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Loading indicator (opsional, muncul saat content belum ready)
        if (!showContent) {
            CircularProgressIndicator(
                color = Color(0xFF8B4513),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}