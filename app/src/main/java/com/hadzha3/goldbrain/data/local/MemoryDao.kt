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

    @Query(
        "SELECT * FROM memories " +
            "ORDER BY createdAt DESC, uri DESC " +
            "LIMIT :limit"
    )
    fun observeRecent(
        limit: Int
    ): Flow<List<MemoryEntity>>

    @Query(
        "SELECT * FROM memories " +
            "ORDER BY uri DESC " +
            "LIMIT :limit"
    )
    suspend fun firstSearchPage(
        limit: Int
    ): List<MemoryEntity>

    @Query(
        """
        SELECT * FROM memories
        WHERE uri < :beforeUri
        ORDER BY uri DESC
        LIMIT :limit
        """
    )
    suspend fun searchPageBefore(
        beforeUri: String,
        limit: Int
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

    @Query(
        """
        UPDATE memories
        SET sourceType = :sourceType,
            sourceConfidence = :sourceConfidence
        WHERE uri = :uri
          AND :sourceConfidence > sourceConfidence
        """
    )
    suspend fun updateSourceIfBetter(
        uri: String,
        sourceType: String,
        sourceConfidence: Int
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
