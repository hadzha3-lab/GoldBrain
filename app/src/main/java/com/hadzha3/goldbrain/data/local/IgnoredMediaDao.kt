package com.hadzha3.goldbrain.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface IgnoredMediaDao {
    @Upsert
    suspend fun upsert(
        item: IgnoredMediaEntity
    )

    @Query(
        "DELETE FROM ignored_media " +
            "WHERE uri = :uri"
    )
    suspend fun delete(
        uri: String
    )

    @Query("DELETE FROM ignored_media")
    suspend fun clearAll()
}
