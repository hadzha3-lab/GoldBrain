package com.hadzha3.goldbrain.data

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

    @Query("""
        SELECT * FROM memories
        WHERE searchableText LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<MemoryEntity>>

    @Query("SELECT COUNT(*) FROM memories")
    fun count(): Flow<Int>
}
