package com.hadzha3.goldbrain.data.repository

import com.hadzha3.goldbrain.data.local.IndexFailureDao
import com.hadzha3.goldbrain.data.local.IndexFailureEntity
import kotlinx.coroutines.flow.Flow

class IndexFailureRepository(
    private val dao: IndexFailureDao,
    private val nowProvider: () -> Long =
        System::currentTimeMillis
) {
    fun count(): Flow<Int> =
        dao.observeCount()

    suspend fun deferredUris(): Set<String> =
        dao.deferredUris(
            nowProvider()
        ).toHashSet()

    suspend fun recordFailure(
        uri: String,
        error: Throwable
    ) {
        val previous =
            dao.get(uri)

        val count =
            (previous?.failureCount ?: 0) +
                1

        val now =
            nowProvider()

        dao.upsert(
            IndexFailureEntity(
                uri = uri,
                failureCount = count,
                nextRetryAt =
                    now +
                        retryDelayMs(
                            count
                        ),
                lastFailedAt = now,
                lastError =
                    (
                        error.message
                            ?: error
                                .javaClass
                                .simpleName
                    )
                        .take(
                            MAX_ERROR_CHARS
                        )
            )
        )
    }

    suspend fun clear(
        uri: String
    ) {
        dao.delete(uri)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    companion object {
        private const val HOUR_MS =
            60L * 60L * 1000L

        private const val MAX_ERROR_CHARS =
            300

        internal fun retryDelayMs(
            failureCount: Int
        ): Long =
            when {
                failureCount <= 1 ->
                    HOUR_MS

                failureCount == 2 ->
                    6L * HOUR_MS

                failureCount == 3 ->
                    24L * HOUR_MS

                else ->
                    72L * HOUR_MS
            }
    }
}
