package com.hadzha3.goldbrain.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val uri: String,
    val createdAt: Long =
        System.currentTimeMillis(),
    val category: String,
    val title: String,
    val ocrText: String,
    val labels: String,
    @ColumnInfo(defaultValue = "1")
    val isAvailable: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val lastVerifiedAt: Long = 0L,
    @ColumnInfo(defaultValue = "''")
    val userNote: String = "",
    @ColumnInfo(defaultValue = "'UNKNOWN'")
    val sourceType: String = "UNKNOWN",
    @ColumnInfo(defaultValue = "0")
    val sourceConfidence: Int = 0
)
