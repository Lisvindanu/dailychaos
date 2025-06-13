package com.dailychaos.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Chaos Entry - Entri jurnal chaos harian
 */
@Serializable
data class ChaosEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val chaosLevel: Int = 5,
    val miniWins: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST,
    val isSharedToCommunity: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val localId: Long = 0L // Untuk Room database
) {
    /**
     * Get chaos level as enum
     */
    fun getChaosLevelEnum(): ChaosLevel = ChaosLevel.fromValue(chaosLevel)

    /**
     * Check if entry has mini wins
     */
    fun hasMiniWins(): Boolean = miniWins.isNotEmpty()

    /**
     * Get formatted date
     */
    fun getFormattedDate(): String {
        // TODO: Implement proper date formatting
        return createdAt.toString()
    }

    /**
     * Check if entry needs sync
     */
    fun needsSync(): Boolean = syncStatus in listOf(SyncStatus.PENDING, SyncStatus.FAILED)

    /**
     * Get word count
     */
    fun getWordCount(): Int = description.split("\\s+".toRegex()).size

    /**
     * Check if entry is valid for sharing
     */
    fun isValidForSharing(): Boolean {
        return description.length >= 50 && // Minimal 50 karakter
                title.isNotBlank() &&
                !isSharedToCommunity
    }
}