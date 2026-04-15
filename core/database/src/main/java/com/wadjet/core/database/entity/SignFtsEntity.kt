package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = SignEntity::class, tokenizer = "unicode61")
@Entity(tableName = "signs_fts")
data class SignFtsEntity(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val description: String,
    @ColumnInfo(name = "category_name") val categoryName: String,
    val reading: String?,
    @ColumnInfo(name = "type_name") val typeName: String,
)
