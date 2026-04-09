package com.wadjet.core.domain.model

data class Sign(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val phoneticValue: String?,
    val meaning: String,
    val type: String,
    val category: String,
    val categoryName: String,
    val examples: List<String>,
    val funFact: String?,
    val speech: String?,
    val pronunciationSound: String?,
    val pronunciationDesc: String?,
)

data class SignPage(
    val signs: List<Sign>,
    val total: Int,
    val page: Int,
    val totalPages: Int,
)

data class Category(
    val code: String,
    val name: String,
    val count: Int,
)

data class Lesson(
    val level: Int,
    val title: String,
    val description: String,
    val signs: List<Sign>,
    val exercises: List<Exercise>,
)

data class Exercise(
    val type: String,
    val question: String,
    val options: List<ExerciseOption>,
    val correctAnswer: String,
    val hint: String?,
)

data class ExerciseOption(
    val code: String,
    val glyph: String,
    val label: String,
)

data class WriteResult(
    val hieroglyphs: String,
    val glyphs: List<WriteGlyph>,
    val mode: String,
    val mdc: String?,
)

data class WriteGlyph(
    val gardinerCode: String,
    val glyph: String,
    val transliteration: String?,
    val phoneticValue: String?,
    val meaning: String?,
)

data class PaletteSign(
    val code: String,
    val glyph: String,
    val transliteration: String?,
    val category: String,
)
