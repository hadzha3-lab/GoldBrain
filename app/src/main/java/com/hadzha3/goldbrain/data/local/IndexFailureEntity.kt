package com.hadzha3.goldbrain.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "index_failures")
data class IndexFailureEntity(
    @PrimaryKey val uri: String,
    val failureCount: Int,
    val nextRetryAt: Long,
    val lastFailedAt: Long,
    val lastError: String
)
