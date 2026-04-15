package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanResponse(
    @SerialName("glyph_count") val numDetections: Int = 0,
    val glyphs: List<DetectedGlyphDto> = emptyList(),
    val transliteration: String? = null,
    @SerialName("gardiner_sequence") val gardinerSequence: String? = null,
    @SerialName("reading_direction") val readingDirection: String? = null,
    @SerialName("layout_mode") val layoutMode: String? = null,
    @SerialName("translation_en") val translationEn: String? = null,
    @SerialName("translation_ar") val translationAr: String? = null,
    val timing: TimingDto? = null,
    val mode: String = "auto",
    @SerialName("detection_source") val detectionSource: String? = null,
    @SerialName("ai_reading") val aiReading: AiReadingDto? = null,
    @SerialName("ai_unverified") val aiUnverified: Boolean? = null,
    @SerialName("quality_hints") val qualityHints: List<String> = emptyList(),
    @SerialName("confidence_summary") val confidenceSummary: ConfidenceSummaryDto? = null,
)

@Serializable
data class TimingDto(
    @SerialName("detection_ms") val detectionMs: Double = 0.0,
    @SerialName("classification_ms") val classificationMs: Double = 0.0,
    @SerialName("transliteration_ms") val transliterationMs: Double = 0.0,
    @SerialName("translation_ms") val translationMs: Double = 0.0,
    @SerialName("total_ms") val totalMs: Double = 0.0,
)

@Serializable
data class AiReadingDto(
    val notes: String? = null,
    val provider: String? = null,
)

@Serializable
data class ConfidenceSummaryDto(
    val avg: Float = 0f,
    val min: Float = 0f,
    val max: Float = 0f,
    @SerialName("low_count") val lowCount: Int = 0,
)

@Serializable
data class DetectedGlyphDto(
    val bbox: List<Float> = emptyList(),
    @SerialName("detection_confidence") val detectionConfidence: Float = 0f,
    @SerialName("gardiner_code") val gardinerCode: String = "",
    @SerialName("class_confidence") val classConfidence: Float = 0f,
)
