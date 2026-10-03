package com.hadzha3.goldbrain.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val ALL: Array<Migration> =
        DatabaseMigrationSql.ALL
            .map { step ->
                object :
                    Migration(
                        step.fromVersion,
                        step.toVersion
                    ) {
                    override fun migrate(
                        database:
                            SupportSQLiteDatabase
                    ) {
                        step.statements
                            .forEach(
                                database::execSQL
                            )
                    }
                }
            }
            .toTypedArray()
}
