package com.hadzha3.goldbrain.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Upsert
    suspend fun upsert(
        item: MemoryEntity
    )

    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Query(
        "SELECT * FROM memories " +
            "ORDER BY createdAt DESC " +
            "LIMIT :limit"
    )
    fun observeRecent(
        limit: Int
    ): Flow<List<MemoryEntity>>

    @Query(
        "SELECT * FROM memories " +
            "ORDER BY createdAt DESC " +
            "LIMIT :limit OFFSET :offset"
    )
    suspend fun page(
        limit: Int,
        offset: Int
    ): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE uri = :uri LIMIT 1")
    fun observeByUri(
        uri: String
    ): Flow<MemoryEntity?>

    @Query("SELECT COUNT(*) FROM memories")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM memories WHERE isAvailable = 0")
    fun unavailableCount(): Flow<Int>

    @Query("""
        SELECT uri FROM memories
        ORDER BY lastVerifiedAt ASC
        LIMIT :limit
    """)
    suspend fun urisForVerification(
        limit: Int
    ): List<String>

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

    @Query("""
        SELECT userNote FROM memories
        WHERE uri = :uri
        LIMIT 1
    """)
    suspend fun userNote(
        uri: String
    ): String?

    @Query("""
        UPDATE memories
        SET userNote = :note
        WHERE uri = :uri
    """)
    suspend fun updateUserNote(
        uri: String,
        note: String
    )

    @Query(
        "DELETE FROM memories " +
            "WHERE uri = :uri"
    )
    suspend fun deleteByUri(
        uri: String
    )

    @Query("DELETE FROM memories")
    suspend fun clearAll()
}
