package com.wadjet.core.domain.model

data class Sign(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val description: String,
    val type: String,
    val typeName: String,
    val category: String,
    val categoryName: String,
    val reading: String?,
    val isPhonetic: Boolean,
    val funFact: String?,
    val speechText: String?,
    val pronunciationSound: String?,
    val pronunciationExample: String?,
    val logographicValue: String? = null,
    val determinativeClass: String? = null,
    val exampleUsages: List<ExampleUsage> = emptyList(),
    val relatedSigns: List<RelatedSign> = emptyList(),
)

data class ExampleUsage(
    val hieroglyphs: String,
    val transliteration: String,
    val translation: String,
)

data class RelatedSign(
    val code: String,
    val glyph: String,
    val transliteration: String,
    val reading: String?,
    val type: String,
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
    val subtitle: String,
    val description: String,
    val tip: String?,
    val introParagraphs: List<String>,
    val signs: List<Sign>,
    val exampleWords: List<ExampleWord>,
    val practiceWords: List<PracticeWord>,
    val prevLessonLevel: Int?,
    val nextLessonLevel: Int?,
    val totalLessons: Int,
)

data class ExampleWord(
    val hieroglyphs: String,
    val codes: List<String>,
    val transliteration: String,
    val translation: String,
    val highlightCodes: List<String>,
    val speechText: String? = null,
)

data class PracticeWord(
    val hieroglyphs: String,
    val transliteration: String,
    val translation: String,
    val hint: String,
    val speechText: String? = null,
)

data class WriteResult(
    val hieroglyphs: String,
    val glyphs: List<WriteGlyph>,
    val mode: String,
)

data class WriteGlyph(
    val code: String,
    val glyph: String,
    val transliteration: String?,
    val description: String?,
    val type: String,
)

data class PaletteSign(
    val code: String,
    val glyph: String,
    val transliteration: String?,
)
