package com.hadzha3.goldbrain.data.repository

import com.hadzha3.goldbrain.data.local.IndexLookupDao

class IndexLookupRepository(
    private val dao: IndexLookupDao,
    private val nowProvider: () -> Long =
        System::currentTimeMillis
) {
    suspend fun excludedAmong(
        uris: List<String>
    ): Set<String> {
        if (uris.isEmpty()) {
            return emptySet()
        }

        return dao.excludedUris(
            uris = uris,
            now = nowProvider()
        ).toHashSet()
    }
}
