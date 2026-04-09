package com.wadjet.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = SignEntity::class)
@Entity(tableName = "signs_fts")
data class SignFtsEntity(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val meaning: String,
    @androidx.room.ColumnInfo(name = "category_name") val categoryName: String,
)
