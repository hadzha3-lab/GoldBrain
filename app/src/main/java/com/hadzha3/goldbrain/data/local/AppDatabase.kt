package com.hadzha3.goldbrain.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemoryEntity::class,
        IndexStateEntity::class,
        IndexFailureEntity::class,
        IgnoredMediaEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun indexStateDao(): IndexStateDao
    abstract fun indexFailureDao(): IndexFailureDao
    abstract fun ignoredMediaDao(): IgnoredMediaDao
    abstract fun indexLookupDao(): IndexLookupDao

    companion object {
        const val DATABASE_NAME =
            "goldbrain.db"

        @Volatile
        private var instance: AppDatabase? = null

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
                        *DatabaseMigrations.ALL
                    )
                    .setJournalMode(
                        RoomDatabase.JournalMode.TRUNCATE
                    )
                    .build()
                    .also {
                        instance = it
                    }
            }
    }
}
