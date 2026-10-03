package com.hadzha3.goldbrain.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ignored_media")
data class IgnoredMediaEntity(
    @PrimaryKey val uri: String,
    val ignoredAt: Long
)
