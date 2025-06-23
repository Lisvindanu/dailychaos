// File: app/src/main/java/com/dailychaos/project/presentation/ui/component/MeguminSadModal.kt
package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dailychaos.project.domain.model.SupportType
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
    modalType: MeguminModalType = MeguminModalType.RemoveSupport,
    currentSupportType: SupportType? = null,
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
                // Megumin Image with animations - using emoji placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(hatRotation),
                    contentAlignment = Alignment.Center
                ) {
                    // Emoji placeholder untuk gambar Megumin
                    Text(
                        text = when (modalType) {
                            MeguminModalType.DuplicateSupport -> "🤔"
                            MeguminModalType.RemoveSupport -> "😢"
                            MeguminModalType.SelfSupport -> "😱"
                        },
                        fontSize = 80.sp,
                        modifier = Modifier.rotate(-hatRotation) // Counter-rotate the emoji
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title based on modal type
                Text(
                    text = when (modalType) {
                        MeguminModalType.DuplicateSupport -> "Megumin Bingung! 🤔"
                        MeguminModalType.RemoveSupport -> "Megumin Sedih! 😢"
                        MeguminModalType.SelfSupport -> "Megumin Kaget! 😱"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Context-aware messaging
                Text(
                    text = getMeguminMessage(modalType, currentSupportType),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Megumin's signature
                Text(
                    text = "- Megumin, Arch Wizard of Support -",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons based on modal type
                when (modalType) {
                    MeguminModalType.DuplicateSupport -> {
                        // Duplicate support - show cancel/confirm options
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
                    }

                    MeguminModalType.RemoveSupport -> {
                        // Manual remove support - show cancel/confirm options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
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
                    }

                    MeguminModalType.SelfSupport -> {
                        // Self support - just dismiss
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Oke, Megumin!")
                        }
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
                            text = when (modalType) {
                                MeguminModalType.RemoveSupport -> {
                                    "Setiap support yang diberikan membantu seseorang merasa tidak sendirian dalam chaos mereka. Seperti party Kazuma - chaos tapi saling support!"
                                }
                                MeguminModalType.DuplicateSupport -> {
                                    "Kamu bisa ganti tipe support kapan saja! Dari 💝 ke 🤗, dari 💪 ke 🌟 - yang penting tetap mendukung fellow adventurer!"
                                }
                                MeguminModalType.SelfSupport -> {
                                    "Self-support itu bagus untuk confidence, tapi di sini kita support orang lain! That's the adventurer way!"
                                }
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
 * Modal types untuk different scenarios
 */
enum class MeguminModalType {
    DuplicateSupport,  // User trying to give same support type
    RemoveSupport,     // User manually removing support
    SelfSupport        // User trying to support their own post
}

/**
 * Get context-aware Megumin message
 */
private fun getMeguminMessage(
    modalType: MeguminModalType,
    currentSupportType: SupportType?
): String {
    return when (modalType) {
        MeguminModalType.DuplicateSupport -> {
            val emoji = getSupportEmoji(currentSupportType)
            "\"Kamu udah kasih support $emoji ini sebelumnya! Mau batalin support-nya?\""
        }

        MeguminModalType.RemoveSupport -> {
            "\"Kenapa mau batalin support untuk orang yang butuh bantuan moral? Apa kamu yakin ingin melakukan ini?\""
        }

        MeguminModalType.SelfSupport -> {
            "\"Heh?! Kamu mau support post sendiri? That's not how adventuring works, bodoh!\""
        }
    }
}

/**
 * Helper function to get support emoji
 */
private fun getSupportEmoji(supportType: SupportType?): String {
    return when (supportType) {
        SupportType.HEART -> "💝"
        SupportType.HUG -> "🤗"
        SupportType.STRENGTH -> "💪"
        SupportType.HOPE -> "🌟"
        null -> "❤️"
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
            modalType = MeguminModalType.RemoveSupport,
            currentSupportType = SupportType.HEART
        )
    }
}