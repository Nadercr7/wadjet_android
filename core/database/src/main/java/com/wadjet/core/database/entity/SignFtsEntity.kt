package com.wadjet.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = SignEntity::class)
@Entity(tableName = "signs_fts")
data class SignFtsEntity(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val description: String,
    @androidx.room.ColumnInfo(name = "category_name") val categoryName: String,
    val reading: String?,
    @androidx.room.ColumnInfo(name = "type_name") val typeName: String,
)
