package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signs")
data class SignEntity(
    @PrimaryKey
    val code: String,
    val glyph: String,
    val transliteration: String = "",
    @ColumnInfo(name = "phonetic_value") val phoneticValue: String? = null,
    val meaning: String = "",
    val type: String = "",
    val category: String = "",
    @ColumnInfo(name = "category_name") val categoryName: String = "",
    val examples: String? = null,
    @ColumnInfo(name = "fun_fact") val funFact: String? = null,
    val speech: String? = null,
    @ColumnInfo(name = "pronunciation_sound") val pronunciationSound: String? = null,
    @ColumnInfo(name = "pronunciation_desc") val pronunciationDesc: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
)
