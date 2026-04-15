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
    val description: String = "",
    val type: String = "",
    @ColumnInfo(name = "type_name") val typeName: String = "",
    val category: String = "",
    @ColumnInfo(name = "category_name") val categoryName: String = "",
    val reading: String? = null,
    @ColumnInfo(name = "is_phonetic") val isPhonetic: Boolean = false,
    @ColumnInfo(name = "fun_fact") val funFact: String? = null,
    @ColumnInfo(name = "speech_text") val speechText: String? = null,
    @ColumnInfo(name = "pronunciation_sound") val pronunciationSound: String? = null,
    @ColumnInfo(name = "pronunciation_example") val pronunciationExample: String? = null,
    @ColumnInfo(name = "logographic_value") val logographicValue: String? = null,
    @ColumnInfo(name = "determinative_class") val determinativeClass: String? = null,
    @ColumnInfo(name = "example_usages_json") val exampleUsagesJson: String? = null,
    @ColumnInfo(name = "related_signs_json") val relatedSignsJson: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
)
