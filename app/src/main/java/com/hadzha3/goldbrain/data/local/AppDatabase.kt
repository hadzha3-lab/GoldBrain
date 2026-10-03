package com.hadzha3.goldbrain.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MemoryEntity::class,
        IndexStateEntity::class,
        IndexFailureEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun indexStateDao(): IndexStateDao
    abstract fun indexFailureDao(): IndexFailureDao

    companion object {
        const val DATABASE_NAME =
            "goldbrain.db"

        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {
                    database.execSQL(
                        """
                        ALTER TABLE memories
                        ADD COLUMN isAvailable INTEGER
                        NOT NULL DEFAULT 1
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        ALTER TABLE memories
                        ADD COLUMN lastVerifiedAt INTEGER
                        NOT NULL DEFAULT 0
                        """.trimIndent()
                    )

                    database.execSQL(
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
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {
                    database.execSQL(
                        """
                        ALTER TABLE index_state
                        ADD COLUMN totalInRun INTEGER
                        NOT NULL DEFAULT 0
                        """.trimIndent()
                    )

                    database.execSQL(
                        """
                        ALTER TABLE index_state
                        ADD COLUMN startedAt INTEGER
                        NOT NULL DEFAULT 0
                        """.trimIndent()
                    )
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {
                    database.execSQL(
                        """
                        ALTER TABLE memories
                        ADD COLUMN userNote TEXT
                        NOT NULL DEFAULT ''
                        """.trimIndent()
                    )
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {
                    database.execSQL(
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
                }
            }

        fun get(
            context: Context
        ): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also { instance = it }
            }
    }
}
