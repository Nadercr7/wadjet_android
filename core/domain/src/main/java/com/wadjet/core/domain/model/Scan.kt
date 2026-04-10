package com.wadjet.core.domain.model

data class ScanResult(
    val numDetections: Int,
    val glyphs: List<DetectedGlyph>,
    val transliteration: String?,
    val gardinerSequence: String?,
    val readingDirection: String?,
    val layoutMode: String?,
    val translationEn: String?,
    val translationAr: String?,
    val detectionMs: Double,
    val classificationMs: Double,
    val transliterationMs: Double,
    val translationMs: Double,
    val totalMs: Double,
    val mode: String,
    val detectionSource: String?,
    val aiNotes: String?,
    val aiUnverified: Boolean,
    val qualityHints: List<String>,
    val confidenceSummary: ConfidenceSummary?,
)

data class ConfidenceSummary(
    val avg: Float,
    val min: Float,
    val max: Float,
    val lowCount: Int,
)

data class DetectedGlyph(
    val bbox: List<Float>,
    val detectionConfidence: Float,
    val gardinerCode: String,
    val classConfidence: Float,
)

data class ScanHistorySummary(
    val id: Int,
    val firestoreId: String?,
    val thumbnailPath: String,
    val glyphCount: Int,
    val confidenceAvg: Float,
    val transliteration: String?,
    val gardinerSequence: String?,
    val translationEn: String?,
    val detectionSource: String?,
    val totalMs: Double,
    val createdAt: Long,
)
