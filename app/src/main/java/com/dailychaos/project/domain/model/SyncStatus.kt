package com.dailychaos.project.domain.model

/**
 * Sync Status - Status sinkronisasi data
 */
enum class SyncStatus {
    SYNCED,        // Data sudah sync dengan server
    PENDING,       // Menunggu sync
    SYNCING,       // Sedang sync
    FAILED,        // Sync gagal
    LOCAL_ONLY     // Hanya ada di lokal
}