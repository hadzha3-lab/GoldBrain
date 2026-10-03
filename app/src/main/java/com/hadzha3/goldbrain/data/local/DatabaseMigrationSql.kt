package com.hadzha3.goldbrain.data.local

data class SqlMigrationStep(
    val fromVersion: Int,
    val toVersion: Int,
    val statements: List<String>
)

object DatabaseMigrationSql {
    val ALL =
        listOf(
            SqlMigrationStep(
                fromVersion = 1,
                toVersion = 2,
                statements =
                    listOf(
                        """
                        ALTER TABLE memories
                        ADD COLUMN isAvailable INTEGER
                        NOT NULL DEFAULT 1
                        """.trimIndent(),
                        """
                        ALTER TABLE memories
                        ADD COLUMN lastVerifiedAt INTEGER
                        NOT NULL DEFAULT 0
                        """.trimIndent(),
                        """
                        CREATE TABLE IF NOT EXISTS index_state (
                            id INTEGER NOT NULL,
                            state TEXT NOT NULL,
                            indexedInRun INTEGER NOT NULL,
                            failedInRun INTEGER NOT NULL,
                            lastUpdatedAt INTEGER NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent()
                    )
            ),
            SqlMigrationStep(
                fromVersion = 2,
                toVersion = 3,
                statements =
                    listOf(
                        """
                        ALTER TABLE index_state
                        ADD COLUMN totalInRun INTEGER
                        NOT NULL DEFAULT 0
                        """.trimIndent(),
                        """
                        ALTER TABLE index_state
                        ADD COLUMN startedAt INTEGER
                        NOT NULL DEFAULT 0
                        """.trimIndent()
                    )
            ),
            SqlMigrationStep(
                fromVersion = 3,
                toVersion = 4,
                statements =
                    listOf(
                        """
                        ALTER TABLE memories
                        ADD COLUMN userNote TEXT
                        NOT NULL DEFAULT ''
                        """.trimIndent()
                    )
            ),
            SqlMigrationStep(
                fromVersion = 4,
                toVersion = 5,
                statements =
                    listOf(
                        """
                        CREATE TABLE IF NOT EXISTS index_failures (
                            uri TEXT NOT NULL,
                            failureCount INTEGER NOT NULL,
                            nextRetryAt INTEGER NOT NULL,
                            lastFailedAt INTEGER NOT NULL,
                            lastError TEXT NOT NULL,
                            PRIMARY KEY(uri)
                        )
                        """.trimIndent()
                    )
            ),
            SqlMigrationStep(
                fromVersion = 5,
                toVersion = 6,
                statements =
                    listOf(
                        """
                        CREATE TABLE IF NOT EXISTS ignored_media (
                            uri TEXT NOT NULL,
                            ignoredAt INTEGER NOT NULL,
                            PRIMARY KEY(uri)
                        )
                        """.trimIndent()
                    )
            )
        )
}
