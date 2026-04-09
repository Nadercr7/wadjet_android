package com.wadjet.core.domain.model

data class ScanResult(
    val numDetections: Int,
    val glyphs: List<DetectedGlyph>,
    val transliteration: String?,
    val gardinerSequence: String?,
    val readingDirection: String?,
    val translationEn: String?,
    val translationAr: String?,
    val annotatedImageBase64: String?,
    val detectionMs: Long,
    val classificationMs: Long,
    val transliterationMs: Long?,
    val translationMs: Long?,
    val totalMs: Long,
    val mode: String,
    val pipeline: String?,
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
    val pipeline: String?,
    val totalMs: Long,
    val createdAt: Long,
)
