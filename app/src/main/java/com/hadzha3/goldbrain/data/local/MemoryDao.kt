package com.hadzha3.goldbrain.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Upsert
    suspend fun upsert(item: MemoryEntity)

    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE uri = :uri LIMIT 1")
    fun observeByUri(uri: String): Flow<MemoryEntity?>

    @Query("SELECT COUNT(*) FROM memories")
    fun count(): Flow<Int>

    @Query("SELECT uri FROM memories")
    suspend fun allUris(): List<String>

    @Query("""
        SELECT uri FROM memories
        ORDER BY lastVerifiedAt ASC
        LIMIT :limit
    """)
    suspend fun urisForVerification(limit: Int): List<String>

    @Query("""
        UPDATE memories
        SET isAvailable = :available,
            lastVerifiedAt = :verifiedAt
        WHERE uri = :uri
    """)
    suspend fun updateAvailability(
        uri: String,
        available: Boolean,
        verifiedAt: Long
    )
}
