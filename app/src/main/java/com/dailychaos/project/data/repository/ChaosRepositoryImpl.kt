// File: app/src/main/java/com/dailychaos/project/data/repository/ChaosRepositoryImpl.kt
package com.dailychaos.project.data.repository

import com.dailychaos.project.data.remote.firebase.FirebaseAuthService
import com.dailychaos.project.data.remote.firebase.FirebaseFirestoreService
import com.dailychaos.project.data.mapper.toChaosEntry
import com.dailychaos.project.data.mapper.toChaosEntryRequest
import com.dailychaos.project.data.mapper.toFirestoreMap
import com.dailychaos.project.domain.model.ChaosEntry
import com.dailychaos.project.domain.repository.ChaosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber // Import Timber for logging

/**
 * Chaos Repository Implementation
 * "Mengelola data chaos, baik dari cloud maupun (nanti) lokal!"
 */
@Singleton
class ChaosRepositoryImpl @Inject constructor(
    private val firestoreService: FirebaseFirestoreService,
    private val authService: FirebaseAuthService // Perlu untuk mendapatkan userId
) : ChaosRepository {

    // Helper untuk mendapatkan userId saat ini
    private suspend fun getCurrentUserId(): String {
        return authService.currentUser?.uid ?: throw IllegalStateException("User not authenticated.")
    }

    override suspend fun createChaosEntry(userId: String, entry: ChaosEntry): Result<String> {
        return try {
            val request = entry.toChaosEntryRequest()
            firestoreService.createChaosEntry(userId, request)
        } catch (e: Exception) {
            Timber.e(e, "Error creating chaos entry in repository")
            Result.failure(e)
        }
    }

    override fun getChaosEntry(userId: String, entryId: String): Flow<ChaosEntry?> {
        return firestoreService.getChaosEntry(userId, entryId).map { map ->
            map?.toChaosEntry()
        }
    }

    override fun getAllChaosEntries(userId: String): Flow<List<ChaosEntry>> {
        return firestoreService.getChaosEntries(userId).map { listMap ->
            listMap.map { it.toChaosEntry() }
        }
    }

    override fun getRecentChaosEntries(userId: String, limit: Int): Flow<List<ChaosEntry>> {
        return firestoreService.getChaosEntries(userId, limit.toLong()).map { listMap ->
            listMap.map { it.toChaosEntry() }
        }
    }

    override suspend fun updateChaosEntry(userId: String, entry: ChaosEntry): Result<Unit> {
        return try {
            firestoreService.updateChaosEntry(userId, entry.id, entry.toFirestoreMap())
        } catch (e: Exception) {
            Timber.e(e, "Error updating chaos entry in repository")
            Result.failure(e)
        }
    }

    override suspend fun deleteChaosEntry(userId: String, entryId: String): Result<Unit> {
        return try {
            firestoreService.deleteChaosEntry(userId, entryId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting chaos entry in repository")
            Result.failure(e)
        }
    }
}