package com.hadzha3.goldbrain.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "index_state")
data class IndexStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val state: String = STATE_IDLE,
    val indexedInRun: Int = 0,
    val failedInRun: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val totalInRun: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val startedAt: Long = 0L,
    val lastUpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SINGLETON_ID = 1
        const val STATE_IDLE = "IDLE"
        const val STATE_RUNNING = "RUNNING"
        const val STATE_ERROR = "ERROR"
    }
}
