package com.hadzha3.goldbrain.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IndexFailureDao {
    @Query(
        "SELECT * FROM index_failures " +
            "WHERE uri = :uri LIMIT 1"
    )
    suspend fun get(
        uri: String
    ): IndexFailureEntity?

    @Upsert
    suspend fun upsert(
        failure: IndexFailureEntity
    )

    @Query(
        "DELETE FROM index_failures " +
            "WHERE uri = :uri"
    )
    suspend fun delete(
        uri: String
    )

    @Query(
        "SELECT uri FROM index_failures " +
            "WHERE nextRetryAt > :now"
    )
    suspend fun deferredUris(
        now: Long
    ): List<String>

    @Query(
        "SELECT COUNT(*) " +
            "FROM index_failures"
    )
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM index_failures")
    suspend fun clearAll()
}
