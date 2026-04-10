package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryResponse(
    val signs: List<SignDetailDto>,
    val total: Int,
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class SignDetailDto(
    val code: String,
    @SerialName("unicode_char") val unicodeChar: String = "",
    val transliteration: String = "",
    val description: String = "",
    val type: String = "",
    @SerialName("type_name") val typeName: String = "",
    val category: String = "",
    @SerialName("category_name") val categoryName: String = "",
    val reading: String? = null,
    @SerialName("logographic_value") val logographicValue: String? = null,
    @SerialName("determinative_class") val determinativeClass: String? = null,
    @SerialName("is_phonetic") val isPhonetic: Boolean = false,
    val pronunciation: PronunciationGuideDto? = null,
    @SerialName("fun_fact") val funFact: String? = null,
    @SerialName("speech_text") val speechText: String? = null,
    // Single-sign detail extras
    @SerialName("example_usages") val exampleUsages: List<ExampleUsageDto>? = null,
    @SerialName("related_signs") val relatedSigns: List<RelatedSignDto>? = null,
)

@Serializable
data class PronunciationGuideDto(
    val sound: String = "",
    val example: String = "",
)

@Serializable
data class ExampleUsageDto(
    val hieroglyphs: String = "",
    val transliteration: String = "",
    val translation: String = "",
)

@Serializable
data class RelatedSignDto(
    val code: String = "",
    @SerialName("unicode_char") val unicodeChar: String = "",
    val transliteration: String = "",
    val reading: String? = null,
    val type: String = "",
)

@Serializable
data class CategoriesResponse(
    val categories: List<CategoryDto>,
)

@Serializable
data class CategoryDto(
    val code: String,
    val name: String,
    val count: Int = 0,
)

@Serializable
data class AlphabetResponse(
    val signs: List<SignDetailDto> = emptyList(),
    val count: Int = 0,
)

@Serializable
data class LessonResponse(
    val level: Int,
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val tip: String? = null,
    @SerialName("intro_paragraphs") val introParagraphs: List<String> = emptyList(),
    @SerialName("prev_lesson") val prevLesson: LessonNavDto? = null,
    @SerialName("next_lesson") val nextLesson: LessonNavDto? = null,
    @SerialName("total_lessons") val totalLessons: Int = 5,
    val signs: List<SignDetailDto> = emptyList(),
    val count: Int = 0,
    @SerialName("example_words") val exampleWords: List<ExampleWordDto> = emptyList(),
    @SerialName("practice_words") val practiceWords: List<PracticeWordDto> = emptyList(),
)

@Serializable
data class LessonNavDto(
    val level: Int,
    val title: String = "",
)

@Serializable
data class ExampleWordDto(
    val hieroglyphs: String = "",
    val codes: List<String> = emptyList(),
    val transliteration: String = "",
    val translation: String = "",
    @SerialName("highlight_codes") val highlightCodes: List<String> = emptyList(),
    @SerialName("speech_text") val speechText: String? = null,
)

@Serializable
data class PracticeWordDto(
    val hieroglyphs: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val hint: String = "",
    @SerialName("speech_text") val speechText: String? = null,
)
