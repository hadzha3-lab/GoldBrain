package com.hadzha3.goldbrain.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface IndexLookupDao {
    @Query(
        """
        SELECT uri
        FROM memories
        WHERE uri IN (:uris)

        UNION

        SELECT uri
        FROM index_failures
        WHERE uri IN (:uris)
          AND nextRetryAt > :now

        UNION

        SELECT uri
        FROM ignored_media
        WHERE uri IN (:uris)
        """
    )
    suspend fun excludedUris(
        uris: List<String>,
        now: Long
    ): List<String>
}
