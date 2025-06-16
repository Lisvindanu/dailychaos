// File: app/src/main/java/com/dailychaos/project/presentation/ui/screen/auth/profile/ProfileScreen.kt
package com.dailychaos.project.presentation.ui.screen.auth.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load user data saat screen pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header dengan settings button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Adventurer Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )

                IconButton(
                    onClick = onNavigateToSettings,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF5D4037)
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF8B4513)
                            )
                            Text(
                                text = "Loading your adventure data...",
                                modifier = Modifier.padding(top = 16.dp),
                                color = Color(0xFF5D4037),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    // Error state
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Oops! Something went wrong",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = uiState.error!!,
                                fontSize = 14.sp,
                                color = Color(0xFF5D4037),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.loadUserProfile() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8B4513)
                                    )
                                ) {
                                    Text("Retry")
                                }

                                OutlinedButton(
                                    onClick = onNavigateToLogin
                                ) {
                                    Text("Login Again")
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Success state - show profile
                    ProfileContent(
                        uiState = uiState,
                        onLogout = {
                            viewModel.logout()
                            onNavigateToLogin()
                        }
                    )
                }
            }
        }
    }


@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Profile Picture & Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Avatar dengan huruf pertama dari displayName
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B4513)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.userProfile?.displayName?.firstOrNull()?.uppercase() ?: "A",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Nama utama: Selalu displayName
            Text(
                text = uiState.userProfile?.displayName ?: "Adventurer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(top = 16.dp)
            )

            // --- PERUBAHAN DI SINI ---
            // Teks sekunder: @username atau email, tergantung tipe akun
            val secondaryText = when (uiState.userProfile?.authType) {
                "username" -> uiState.userProfile.username?.let { "@$it" }
                "email" -> uiState.userProfile.email
                else -> uiState.userProfile?.username?.let { "@$it" } ?: uiState.userProfile?.email
            }

            if (!secondaryText.isNullOrBlank()) {
                Text(
                    text = secondaryText,
                    fontSize = 14.sp,
                    color = Color(0xFF8B4513),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // --- AKHIR PERUBAHAN ---
        }

        // Stats Cards (Tidak ada perubahan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = uiState.userProfile?.chaosEntries?.toString() ?: "0",
                label = "Entries",
                icon = "📔"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                value = uiState.userProfile?.dayStreak?.toString() ?: "0",
                label = "Day Streak 🔥",
                icon = "⚡"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                value = uiState.userProfile?.supportGiven?.toString() ?: "0",
                label = "Support Given",
                icon = "💙"
            )
        }

        // Achievement Badges (Tidak ada perubahan)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🏆 Achievements",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val achievements = listOf(
                    AchievementBadge("🌟", "First Chaos", true),
                    AchievementBadge("🔥", "Week Streak", uiState.userProfile?.dayStreak ?: 0 >= 7),
                    AchievementBadge("👥", "Community Twin", uiState.userProfile?.supportGiven ?: 0 > 0),
                    AchievementBadge("📅", "Month Streak", uiState.userProfile?.dayStreak ?: 0 >= 30)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementItem(achievement)
                    }
                }
            }
        }

        // Logout Button (Tidak ada perubahan)
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFD32F2F),
                containerColor = Color.Transparent
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD32F2F), Color(0xFFD32F2F))
                )
            )
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF8B4513),
                textAlign = TextAlign.Center
            )
        }
    }
}

data class AchievementBadge(
    val icon: String,
    val name: String,
    val isUnlocked: Boolean
)

@Composable
private fun AchievementItem(achievement: AchievementBadge) {
    Card(
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                Color(0xFFFFD700).copy(alpha = 0.3f)
            } else {
                Color.Gray.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = achievement.icon,
                fontSize = 24.sp,
                modifier = Modifier.alpha(if (achievement.isUnlocked) 1f else 0.3f)
            )
            Text(
                text = achievement.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (achievement.isUnlocked) Color(0xFF5D4037) else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}