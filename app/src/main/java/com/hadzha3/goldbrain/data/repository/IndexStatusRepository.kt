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
    val lastUpdatedAt: Long
)

class IndexStatusRepository(
    private val dao: IndexStateDao
) {
    fun observe(): Flow<IndexStatus> =
        dao.observe().map { entity ->
            val state = entity ?: IndexStateEntity()
            IndexStatus(
                isRunning =
                    state.state == IndexStateEntity.STATE_RUNNING,
                hasError =
                    state.state == IndexStateEntity.STATE_ERROR,
                indexedInRun = state.indexedInRun,
                failedInRun = state.failedInRun,
                lastUpdatedAt = state.lastUpdatedAt
            )
        }

    suspend fun markRunning() {
        dao.upsert(
            IndexStateEntity(
                state = IndexStateEntity.STATE_RUNNING
            )
        )
    }

    suspend fun markFinished(
        indexed: Int,
        failed: Int,
        hasMore: Boolean
    ) {
        dao.upsert(
            IndexStateEntity(
                state =
                    if (hasMore) {
                        IndexStateEntity.STATE_RUNNING
                    } else {
                        IndexStateEntity.STATE_IDLE
                    },
                indexedInRun = indexed,
                failedInRun = failed
            )
        )
    }

    suspend fun markError(
        indexed: Int = 0,
        failed: Int = 1
    ) {
        dao.upsert(
            IndexStateEntity(
                state = IndexStateEntity.STATE_ERROR,
                indexedInRun = indexed,
                failedInRun = failed
            )
        )
    }
}
