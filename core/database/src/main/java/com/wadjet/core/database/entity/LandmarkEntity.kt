package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "landmarks")
data class LandmarkEntity(
    @PrimaryKey val slug: String,
    val name: String,
    @ColumnInfo(name = "name_ar") val nameAr: String? = null,
    val city: String? = null,
    val type: String? = null,
    val era: String? = null,
    val thumbnail: String? = null,
    val featured: Boolean = false,
    val popularity: Int = 0,
    @ColumnInfo(name = "detail_json") val detailJson: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
)
