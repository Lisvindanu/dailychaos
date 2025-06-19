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


    suspend fun updateChaosEntry(userId: String, entry: ChaosEntry): Result<Unit>

    suspend fun deleteChaosEntry(userId: String, entryId: String): Result<Unit>

    suspend fun getRecentChaosEntries(userId: String, limit: Int = 10): Flow<List<ChaosEntry>>
    suspend fun getAllChaosEntries(userId: String): Flow<List<ChaosEntry>>
    suspend fun getChaosEntry(userId: String, entryId: String): Flow<ChaosEntry?>
    suspend fun getChaosEntriesByDateRange(userId: String, startDate: String, endDate: String): Flow<List<ChaosEntry>>
}