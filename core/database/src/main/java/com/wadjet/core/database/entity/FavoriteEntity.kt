package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["item_type", "item_id"])
data class FavoriteEntity(
    @ColumnInfo(name = "item_type") val itemType: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
)
