package com.hadzha3.goldbrain.data.repository

import androidx.room.withTransaction
import com.hadzha3.goldbrain.data.local.AppDatabase
import com.hadzha3.goldbrain.data.local.IgnoredMediaEntity

class MemoryIndexMaintenanceRepository(
    private val database: AppDatabase
) {
    private val memoryDao =
        database.memoryDao()

    private val ignoredDao =
        database.ignoredMediaDao()

    suspend fun ignoreAndRemove(
        uri: String
    ) {
        database.withTransaction {
            ignoredDao.upsert(
                IgnoredMediaEntity(
                    uri = uri,
                    ignoredAt =
                        System.currentTimeMillis()
                )
            )

            memoryDao.deleteByUri(
                uri
            )
        }
    }

    suspend fun allow(
        uri: String
    ) {
        ignoredDao.delete(
            uri
        )
    }

    suspend fun ignoredUris(): Set<String> =
        ignoredDao.allUris()
            .toHashSet()

    suspend fun clearIgnored() {
        ignoredDao.clearAll()
    }
}
