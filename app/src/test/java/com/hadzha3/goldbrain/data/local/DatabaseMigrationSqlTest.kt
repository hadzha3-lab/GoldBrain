package com.hadzha3.goldbrain.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

class DatabaseMigrationSqlTest {
    @Test
    fun migrationChainIsContinuousFromV1ToV8() {
        var expectedVersion = 1

        DatabaseMigrationSql.ALL
            .forEach { step ->
                assertEquals(
                    expectedVersion,
                    step.fromVersion
                )

                assertEquals(
                    expectedVersion + 1,
                    step.toVersion
                )

                expectedVersion =
                    step.toVersion
            }

        assertEquals(
            8,
            expectedVersion
        )
    }

    @Test
    fun v1DataSurvivesEveryMigrationToV8() {
        Class.forName(
            "org.sqlite.JDBC"
        )

        DriverManager
            .getConnection(
                "jdbc:sqlite::memory:"
            )
            .use { database ->
                createV1Schema(
                    database
                )

                insertLegacyMemory(
                    database
                )

                DatabaseMigrationSql.ALL
                    .forEach { step ->
                        step.statements
                            .forEach {
                                sql ->
                                database
                                    .createStatement()
                                    .use {
                                        statement ->
                                        statement.execute(
                                            sql
                                        )
                                    }
                            }
                    }

                database
                    .createStatement()
                    .use { statement ->
                        statement
                            .executeQuery(
                                """
                                SELECT
                                    uri,
                                    title,
                                    isAvailable,
                                    lastVerifiedAt,
                                    userNote,
                                    ocrText,
                                    sourceType,
                                    sourceConfidence
                                FROM memories
                                WHERE uri = 'content://legacy/1'
                                """.trimIndent()
                            )
                            .use { row ->
                                assertTrue(
                                    row.next()
                                )

                                assertEquals(
                                    "content://legacy/1",
                                    row.getString(
                                        "uri"
                                    )
                                )

                                assertEquals(
                                    "Старый чек",
                                    row.getString(
                                        "title"
                                    )
                                )

                                assertEquals(
                                    1,
                                    row.getInt(
                                        "isAvailable"
                                    )
                                )

                                assertEquals(
                                    0L,
                                    row.getLong(
                                        "lastVerifiedAt"
                                    )
                                )

                                assertEquals(
                                    "",
                                    row.getString(
                                        "userNote"
                                    )
                                )

                                val compactOcr =
                                    row.getString(
                                        "ocrText"
                                    )

                                assertTrue(
                                    compactOcr.length <=
                                        2048
                                )

                                assertTrue(
                                    compactOcr.startsWith(
                                        "НАЧАЛО"
                                    )
                                )

                                assertTrue(
                                    compactOcr.endsWith(
                                        "ИТОГО 1000"
                                    )
                                )

                                assertEquals(
                                    "UNKNOWN",
                                    row.getString(
                                        "sourceType"
                                    )
                                )

                                assertEquals(
                                    0,
                                    row.getInt(
                                        "sourceConfidence"
                                    )
                                )
                            }
                    }

                assertTrue(
                    tableExists(
                        database,
                        "index_state"
                    )
                )

                assertTrue(
                    tableExists(
                        database,
                        "index_failures"
                    )
                )

                assertTrue(
                    tableExists(
                        database,
                        "ignored_media"
                    )
                )

                assertTrue(
                    columnExists(
                        database,
                        table =
                            "index_state",
                        column =
                            "totalInRun"
                    )
                )

                assertTrue(
                    columnExists(
                        database,
                        table =
                            "index_state",
                        column =
                            "startedAt"
                    )
                )

                assertTrue(
                    !columnExists(
                        database,
                        table =
                            "memories",
                        column =
                            "searchableText"
                    )
                )
            }
    }

    private fun createV1Schema(
        database: Connection
    ) {
        database
            .createStatement()
            .use { statement ->
                statement.execute(
                    """
                    CREATE TABLE memories (
                        uri TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        ocrText TEXT NOT NULL,
                        labels TEXT NOT NULL,
                        searchableText TEXT NOT NULL,
                        PRIMARY KEY(uri)
                    )
                    """.trimIndent()
                )
            }
    }

    private fun insertLegacyMemory(
        database: Connection
    ) {
        database
            .prepareStatement(
                """
                INSERT INTO memories (
                    uri,
                    createdAt,
                    category,
                    title,
                    ocrText,
                    labels,
                    searchableText
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            )
            .use { statement ->
                statement.setString(
                    1,
                    "content://legacy/1"
                )

                statement.setLong(
                    2,
                    1_700_000_000_000L
                )

                statement.setString(
                    3,
                    "Чек"
                )

                statement.setString(
                    4,
                    "Старый чек"
                )

                statement.setString(
                    5,
                    "НАЧАЛО " +
                        "текст ".repeat(
                            2_000
                        ) +
                        "ИТОГО 1000"
                )

                statement.setString(
                    6,
                    "Receipt"
                )

                statement.setString(
                    7,
                    "чек receipt итого 1000"
                )

                statement.executeUpdate()
            }
    }

    private fun tableExists(
        database: Connection,
        table: String
    ): Boolean =
        database
            .prepareStatement(
                """
                SELECT 1
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = ?
                LIMIT 1
                """.trimIndent()
            )
            .use { statement ->
                statement.setString(
                    1,
                    table
                )

                statement
                    .executeQuery()
                    .use {
                        it.next()
                    }
            }

    private fun columnExists(
        database: Connection,
        table: String,
        column: String
    ): Boolean =
        database
            .createStatement()
            .use { statement ->
                statement
                    .executeQuery(
                        "PRAGMA table_info($table)"
                    )
                    .use { rows ->
                        var found = false

                        while (
                            rows.next()
                        ) {
                            if (
                                rows.getString(
                                    "name"
                                ) == column
                            ) {
                                found = true
                                break
                            }
                        }

                        found
                    }
            }
}
