package com.hadzha3.goldbrain.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IndexStateDao {
    @Upsert
    suspend fun upsert(
        state: IndexStateEntity
    )

    @Query("SELECT * FROM index_state WHERE id = 1 LIMIT 1")
    fun observe(): Flow<IndexStateEntity?>

    @Query("SELECT * FROM index_state WHERE id = 1 LIMIT 1")
    suspend fun get(): IndexStateEntity?

    @Query("DELETE FROM index_state")
    suspend fun clear()
}
