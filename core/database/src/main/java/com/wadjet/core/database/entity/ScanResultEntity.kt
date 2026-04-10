package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "firestore_id") val firestoreId: String? = null,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String,
    @ColumnInfo(name = "results_json") val resultsJson: String,
    @ColumnInfo(name = "glyph_count") val glyphCount: Int,
    @ColumnInfo(name = "confidence_avg") val confidenceAvg: Float,
    @ColumnInfo(name = "transliteration") val transliteration: String? = null,
    @ColumnInfo(name = "gardiner_sequence") val gardinerSequence: String? = null,
    @ColumnInfo(name = "translation_en") val translationEn: String? = null,
    @ColumnInfo(name = "translation_ar") val translationAr: String? = null,
    @ColumnInfo(name = "detection_source") val detectionSource: String? = null,
    @ColumnInfo(name = "total_ms") val totalMs: Double = 0.0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
