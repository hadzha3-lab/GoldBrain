package com.hadzha3.goldbrain.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val uri: String,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String,
    val title: String,
    val ocrText: String,
    val labels: String,
    val searchableText: String
)
