package com.wadjet.core.domain.model

data class StorySummary(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val coverGlyph: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val chapterCount: Int,
    val glyphsTaught: List<String>,
)

data class StoryFull(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val coverGlyph: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val glyphsTaught: List<String>,
    val chapters: List<Chapter>,
)

data class Chapter(
    val index: Int,
    val titleEn: String,
    val titleAr: String,
    val ttsVoice: String?,
    val ttsStyle: String?,
    val paragraphs: List<Paragraph>,
    val interactions: List<Interaction>,
)

data class Paragraph(
    val textEn: String,
    val textAr: String,
    val glyphAnnotations: List<GlyphAnnotation>,
)

data class GlyphAnnotation(
    val wordEn: String,
    val wordAr: String,
    val gardinerCode: String,
    val glyph: String,
    val meaningEn: String,
    val meaningAr: String,
    val transliteration: String,
)

sealed interface Interaction {
    val afterParagraph: Int

    data class ChooseGlyph(
        override val afterParagraph: Int,
        val questionEn: String,
        val questionAr: String,
        val options: List<GlyphOption>,
        val correctCode: String,
        val explanationEn: String,
        val explanationAr: String,
    ) : Interaction

    data class WriteWord(
        override val afterParagraph: Int,
        val targetWordEn: String,
        val targetWordAr: String,
        val targetGlyph: String,
        val gardinerCode: String,
        val hintEn: String,
        val hintAr: String,
    ) : Interaction

    data class GlyphDiscovery(
        override val afterParagraph: Int,
        val glyphCode: String,
        val unicode: String,
        val promptEn: String,
        val promptAr: String,
        val meaningEn: String,
        val meaningAr: String,
        val transliteration: String,
    ) : Interaction

    data class StoryDecision(
        override val afterParagraph: Int,
        val promptEn: String,
        val promptAr: String,
        val choices: List<DecisionChoice>,
    ) : Interaction
}

data class GlyphOption(
    val code: String,
    val glyph: String,
)

data class DecisionChoice(
    val id: String,
    val textEn: String,
    val textAr: String,
    val outcomeEn: String,
    val outcomeAr: String,
)

data class InteractionResult(
    val correct: Boolean,
    val type: String,
    val explanation: String?,
    val outcomeEn: String?,
    val outcomeAr: String?,
)

data class StoryProgress(
    val storyId: String,
    val chapterIndex: Int,
    val glyphsLearned: List<String>,
    val score: Int,
    val completed: Boolean,
)
