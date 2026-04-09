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
    val glyph: String,
    val transliteration: String = "",
    @SerialName("phonetic_value") val phoneticValue: String? = null,
    val meaning: String = "",
    val type: String = "",
    val category: String = "",
    @SerialName("category_name") val categoryName: String = "",
    val examples: List<String>? = null,
    @SerialName("fun_fact") val funFact: String? = null,
    val speech: String? = null,
    @SerialName("pronunciation_guide") val pronunciationGuide: PronunciationGuideDto? = null,
)

@Serializable
data class PronunciationGuideDto(
    val sound: String = "",
    val description: String = "",
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
    val alphabet: List<SignDetailDto>,
)

@Serializable
data class LessonResponse(
    val level: Int,
    val title: String = "",
    val description: String = "",
    val signs: List<SignDetailDto> = emptyList(),
    val exercises: List<ExerciseDto> = emptyList(),
)

@Serializable
data class ExerciseDto(
    val type: String = "",
    val question: String = "",
    val options: List<ExerciseOptionDto> = emptyList(),
    @SerialName("correct_answer") val correctAnswer: String = "",
    val hint: String? = null,
)

@Serializable
data class ExerciseOptionDto(
    val code: String = "",
    val glyph: String = "",
    val label: String = "",
)
