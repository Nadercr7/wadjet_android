package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BilingualText(
    val en: String = "",
    val ar: String = "",
)

// --- List response ---

@Serializable
data class StoriesListResponse(
    val stories: List<StorySummaryDto> = emptyList(),
    val count: Int = 0,
)

@Serializable
data class StorySummaryDto(
    val id: String = "",
    val title: BilingualText = BilingualText(),
    val subtitle: BilingualText = BilingualText(),
    @SerialName("cover_glyph") val coverGlyph: String = "",
    val difficulty: String = "",
    @SerialName("estimated_minutes") val estimatedMinutes: Int = 0,
    @SerialName("chapter_count") val chapterCount: Int = 0,
    @SerialName("glyphs_taught") val glyphsTaught: List<String> = emptyList(),
)

// --- Full story response ---

@Serializable
data class StoryFullDto(
    val id: String = "",
    val title: BilingualText = BilingualText(),
    val subtitle: BilingualText = BilingualText(),
    @SerialName("cover_glyph") val coverGlyph: String = "",
    val difficulty: String = "",
    @SerialName("estimated_minutes") val estimatedMinutes: Int = 0,
    @SerialName("glyphs_taught") val glyphsTaught: List<String> = emptyList(),
    val chapters: List<ChapterDto> = emptyList(),
)

@Serializable
data class ChapterDto(
    val index: Int = 0,
    val title: BilingualText = BilingualText(),
    @SerialName("scene_image_prompt") val sceneImagePrompt: String? = null,
    @SerialName("tts_voice") val ttsVoice: String? = null,
    @SerialName("tts_style") val ttsStyle: String? = null,
    val paragraphs: List<ParagraphDto> = emptyList(),
    val interactions: List<InteractionDto> = emptyList(),
)

@Serializable
data class ParagraphDto(
    val text: BilingualText = BilingualText(),
    @SerialName("glyph_annotations") val glyphAnnotations: List<GlyphAnnotationDto> = emptyList(),
)

@Serializable
data class GlyphAnnotationDto(
    val word: BilingualText = BilingualText(),
    @SerialName("gardiner_code") val gardinerCode: String = "",
    val glyph: String = "",
    val meaning: BilingualText = BilingualText(),
    val transliteration: String = "",
)

@Serializable
data class InteractionDto(
    val type: String = "",
    @SerialName("after_paragraph") val afterParagraph: Int = 0,
    // choose_glyph
    val question: BilingualText? = null,
    val options: List<GlyphOptionDto>? = null,
    val correct: String? = null,
    val explanation: BilingualText? = null,
    // write_word
    @SerialName("target_word") val targetWord: BilingualText? = null,
    @SerialName("target_glyph") val targetGlyph: String? = null,
    @SerialName("gardiner_code") val gardinerCode: String? = null,
    val hint: BilingualText? = null,
    // glyph_discovery
    val glyph: String? = null,
    val unicode: String? = null,
    val prompt: BilingualText? = null,
    val meaning: BilingualText? = null,
    val transliteration: String? = null,
    // story_decision
    val choices: List<StoryChoiceDto>? = null,
)

@Serializable
data class GlyphOptionDto(
    val code: String = "",
    val glyph: String = "",
)

@Serializable
data class StoryChoiceDto(
    val id: String = "",
    val text: BilingualText = BilingualText(),
    val outcome: BilingualText = BilingualText(),
)

// --- Interact ---

@Serializable
data class InteractRequest(
    @SerialName("chapter_index") val chapterIndex: Int,
    @SerialName("interaction_index") val interactionIndex: Int,
    val answer: String,
)

@Serializable
data class InteractResponse(
    val correct: Boolean = false,
    val type: String = "",
    val explanation: String? = null,
    val outcome: BilingualText? = null,
    @SerialName("correct_answer") val correctAnswer: String? = null,
    @SerialName("target_glyph") val targetGlyph: String? = null,
    @SerialName("gardiner_code") val gardinerCode: String? = null,
    val hint: String? = null,
    @SerialName("choice_id") val choiceId: String? = null,
)

// --- Chapter image ---

@Serializable
data class ChapterImageResponse(
    @SerialName("image_url") val imageUrl: String? = null,
    val status: String = "",
)
