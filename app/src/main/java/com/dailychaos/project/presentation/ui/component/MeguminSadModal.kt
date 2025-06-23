// File: app/src/main/java/com/dailychaos/project/presentation/ui/component/MeguminSadModal.kt
package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Megumin Sad Modal - Modal konfirmasi dengan Megumin yang sedih
 * "Modal yang nanyain kenapa user mau batalin support untuk orang yang butuh bantuan"
 */
@Composable
fun MeguminSadModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirmRemoval: () -> Unit,
    isRemovingSupport: Boolean = false, // true = removing support, false = duplicate support
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    // Animation states
    var isAnimating by remember { mutableStateOf(true) }

    // Rotation animation for Megumin's hat
    val infiniteTransition = rememberInfiniteTransition(label = "megumin_hat_rotation")
    val hatRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hat_rotation"
    )

    // Scale animation for entrance
    val scaleTransition = rememberInfiniteTransition(label = "megumin_scale")
    val scale by scaleTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "megumin_scale"
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isAnimating = true
            delay(1000)
            isAnimating = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Megumin Image with animations
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(hatRotation),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder untuk gambar Megumin sedih
                    // Ganti dengan resource image yang sesuai
                    Text(
                        text = "😢",
                        fontSize = 80.sp,
                        modifier = Modifier.rotate(-hatRotation) // Counter-rotate the emoji
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (isRemovingSupport) "Megumin sedih..." else "Eh? Udah kasih support ini!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message
                Text(
                    text = if (isRemovingSupport) {
                        "\"Kenapa mau batalin support untuk orang yang butuh bantuan moral? Apa kamu yakin ingin melakukan ini?\""
                    } else {
                        "\"Kamu udah kasih support ini sebelumnya! Mau ganti jenis support yang lain?\""
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Megumin's signature style quote
                Text(
                    text = "- Megumin, Arch Wizard of Support -",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                if (isRemovingSupport) {
                    // Removal confirmation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = onConfirmRemoval,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Hapus Support")
                        }
                    }
                } else {
                    // Duplicate support - just dismiss
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Oke, Megumin!")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fun fact about support
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💡 Fun Fact:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRemovingSupport) {
                                "Setiap support yang diberikan membantu seseorang merasa tidak sendirian dalam chaos mereka. Seperti party Kazuma - chaos tapi saling support!"
                            } else {
                                "Kamu bisa ganti tipe support kapan saja! Dari 💝 ke 🤗, dari 💪 ke 🌟 - yang penting tetap mendukung fellow adventurer!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview untuk development
 */
@Composable
fun MeguminSadModalPreview() {
    MaterialTheme {
        MeguminSadModal(
            isVisible = true,
            onDismiss = {},
            onConfirmRemoval = {},
            isRemovingSupport = true
        )
    }
}