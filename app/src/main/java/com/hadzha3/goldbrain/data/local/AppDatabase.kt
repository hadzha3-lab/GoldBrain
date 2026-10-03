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
        IndexStateEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun indexStateDao(): IndexStateDao

    companion object {
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

        fun get(
            context: Context
        ): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goldbrain.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()
                    .also { instance = it }
            }
    }
}
