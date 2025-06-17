// File: app/src/main/java/com/dailychaos/project/domain/repository/ChaosRepository.kt
package com.dailychaos.project.domain.repository

import com.dailychaos.project.domain.model.ChaosEntry
import kotlinx.coroutines.flow.Flow

/**
 * Chaos Repository Interface
 * "Aturan main untuk menyimpan dan mengambil semua cerita chaos!"
 */
interface ChaosRepository {

    suspend fun createChaosEntry(userId: String, entry: ChaosEntry): Result<String>

    fun getChaosEntry(userId: String, entryId: String): Flow<ChaosEntry?>

    fun getAllChaosEntries(userId: String): Flow<List<ChaosEntry>>

    fun getRecentChaosEntries(userId: String, limit: Int): Flow<List<ChaosEntry>>

    suspend fun updateChaosEntry(userId: String, entry: ChaosEntry): Result<Unit>

    suspend fun deleteChaosEntry(userId: String, entryId: String): Result<Unit>

    // Metode terkait sinkronisasi (jika Room sudah diaktifkan nanti)
    // suspend fun syncChaosEntries(userId: String): Result<Unit>
}