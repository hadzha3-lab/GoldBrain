package com.hadzha3.goldbrain.data.repository

import com.hadzha3.goldbrain.data.local.IndexStateDao
import com.hadzha3.goldbrain.data.local.IndexStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class IndexStatus(
    val isRunning: Boolean,
    val hasError: Boolean,
    val indexedInRun: Int,
    val failedInRun: Int,
    val totalInRun: Int,
    val startedAt: Long,
    val lastUpdatedAt: Long
) {
    val processedInRun: Int
        get() =
            indexedInRun +
                failedInRun

    val progressFraction: Float
        get() =
            if (totalInRun <= 0) {
                if (isRunning) 0f else 1f
            } else {
                (
                    processedInRun
                        .toFloat() /
                        totalInRun
                            .toFloat()
                ).coerceIn(
                    0f,
                    1f
                )
            }
}

class IndexStatusRepository(
    private val dao: IndexStateDao
) {
    fun observe(): Flow<IndexStatus> =
        dao.observe().map { entity ->
            (entity ?: IndexStateEntity())
                .toStatus()
        }

    suspend fun startRun(
        total: Int
    ) {
        val now =
            System.currentTimeMillis()

        dao.upsert(
            IndexStateEntity(
                state =
                    IndexStateEntity.STATE_RUNNING,
                indexedInRun = 0,
                failedInRun = 0,
                totalInRun = total,
                startedAt = now,
                lastUpdatedAt = now
            )
        )
    }

    suspend fun markBatchFinished(
        indexed: Int,
        failed: Int,
        hasMore: Boolean
    ) {
        val current =
            dao.get() ?:
                IndexStateEntity(
                    state =
                        IndexStateEntity.STATE_RUNNING
                )

        val now =
            System.currentTimeMillis()

        dao.upsert(
            current.copy(
                state =
                    if (hasMore) {
                        IndexStateEntity.STATE_RUNNING
                    } else {
                        IndexStateEntity.STATE_IDLE
                    },
                indexedInRun =
                    current.indexedInRun +
                        indexed,
                failedInRun =
                    current.failedInRun +
                        failed,
                lastUpdatedAt = now
            )
        )
    }

    suspend fun markError(
        indexed: Int = 0,
        failed: Int = 1
    ) {
        val current =
            dao.get() ?:
                IndexStateEntity()

        dao.upsert(
            current.copy(
                state =
                    IndexStateEntity.STATE_ERROR,
                indexedInRun =
                    current.indexedInRun +
                        indexed,
                failedInRun =
                    current.failedInRun +
                        failed,
                lastUpdatedAt =
                    System.currentTimeMillis()
            )
        )
    }

    suspend fun reset() {
        dao.clear()
    }

    private fun IndexStateEntity.toStatus() =
        IndexStatus(
            isRunning =
                state ==
                    IndexStateEntity.STATE_RUNNING,
            hasError =
                state ==
                    IndexStateEntity.STATE_ERROR,
            indexedInRun =
                indexedInRun,
            failedInRun =
                failedInRun,
            totalInRun =
                totalInRun,
            startedAt =
                startedAt,
            lastUpdatedAt =
                lastUpdatedAt
        )
}
