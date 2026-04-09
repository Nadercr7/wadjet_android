package com.wadjet.core.data.repository

import com.wadjet.core.database.dao.ScanResultDao
import com.wadjet.core.database.entity.ScanResultEntity
import com.wadjet.core.domain.model.DetectedGlyph
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.model.ScanResult
import com.wadjet.core.domain.repository.ScanRepository
import com.wadjet.core.network.api.ScanApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val scanApi: ScanApiService,
    private val scanResultDao: ScanResultDao,
    private val json: Json,
) : ScanRepository {

    override suspend fun scanImage(imageFile: File, mode: String): Result<ScanResult> = runCatching {
        val filePart = MultipartBody.Part.createFormData(
            "file",
            imageFile.name,
            imageFile.asRequestBody("image/jpeg".toMediaType()),
        )
        val modePart = mode.toRequestBody("text/plain".toMediaType())

        val response = scanApi.scan(filePart, modePart)
        if (response.isSuccessful) {
            response.body()!!.toDomain()
        } else {
            throw ApiException("Scan failed: ${response.code()}")
        }
    }

    override suspend fun saveScanResult(result: ScanResult, thumbnailPath: String): Result<Int> = runCatching {
        val confidenceAvg = if (result.glyphs.isNotEmpty()) {
            result.glyphs.map { it.classConfidence }.average().toFloat()
        } else 0f

        val resultsJson = json.encodeToString(result.toSerializable())

        val entity = ScanResultEntity(
            thumbnailPath = thumbnailPath,
            resultsJson = resultsJson,
            glyphCount = result.numDetections,
            confidenceAvg = confidenceAvg,
            transliteration = result.transliteration,
            gardinerSequence = result.gardinerSequence,
            translationEn = result.translationEn,
            translationAr = result.translationAr,
            pipeline = result.pipeline,
            totalMs = result.totalMs,
        )
        scanResultDao.insert(entity).toInt()
    }

    override fun getScanHistory(): Flow<List<ScanHistorySummary>> =
        scanResultDao.getAll().map { entities -> entities.map { it.toSummary() } }

    override suspend fun getScanResultJson(scanId: Int): Result<String> = runCatching {
        scanResultDao.getById(scanId)?.resultsJson
            ?: throw ApiException("Scan result not found: $scanId")
    }

    override suspend fun deleteScan(scanId: Int): Result<Unit> = runCatching {
        val entity = scanResultDao.getById(scanId)
        if (entity != null) {
            // Delete thumbnail file
            try {
                File(entity.thumbnailPath).delete()
            } catch (e: Exception) {
                Timber.w(e, "Failed to delete thumbnail: ${entity.thumbnailPath}")
            }
            scanResultDao.deleteById(scanId)
        }
    }

    private fun com.wadjet.core.network.model.ScanResponse.toDomain() = ScanResult(
        numDetections = numDetections,
        glyphs = glyphs.map {
            DetectedGlyph(
                bbox = it.bbox,
                detectionConfidence = it.detectionConfidence,
                gardinerCode = it.gardinerCode,
                classConfidence = it.classConfidence,
            )
        },
        transliteration = transliteration,
        gardinerSequence = gardinerSequence,
        readingDirection = readingDirection,
        translationEn = translationEn,
        translationAr = translationAr,
        annotatedImageBase64 = annotatedImage,
        detectionMs = detectionMs,
        classificationMs = classificationMs,
        transliterationMs = transliterationMs,
        translationMs = translationMs,
        totalMs = totalMs,
        mode = mode,
        pipeline = pipeline,
    )

    private fun ScanResultEntity.toSummary() = ScanHistorySummary(
        id = id,
        firestoreId = firestoreId,
        thumbnailPath = thumbnailPath,
        glyphCount = glyphCount,
        confidenceAvg = confidenceAvg,
        transliteration = transliteration,
        gardinerSequence = gardinerSequence,
        translationEn = translationEn,
        pipeline = pipeline,
        totalMs = totalMs,
        createdAt = createdAt,
    )

    // Serializable wrapper for storing ScanResult as JSON in Room
    @kotlinx.serialization.Serializable
    private data class ScanResultSerializable(
        val numDetections: Int,
        val glyphs: List<GlyphSerializable>,
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

    @kotlinx.serialization.Serializable
    private data class GlyphSerializable(
        val bbox: List<Float>,
        val detectionConfidence: Float,
        val gardinerCode: String,
        val classConfidence: Float,
    )

    private fun ScanResult.toSerializable() = ScanResultSerializable(
        numDetections = numDetections,
        glyphs = glyphs.map {
            GlyphSerializable(
                bbox = it.bbox,
                detectionConfidence = it.detectionConfidence,
                gardinerCode = it.gardinerCode,
                classConfidence = it.classConfidence,
            )
        },
        transliteration = transliteration,
        gardinerSequence = gardinerSequence,
        readingDirection = readingDirection,
        translationEn = translationEn,
        translationAr = translationAr,
        annotatedImageBase64 = annotatedImageBase64,
        detectionMs = detectionMs,
        classificationMs = classificationMs,
        transliterationMs = transliterationMs,
        translationMs = translationMs,
        totalMs = totalMs,
        mode = mode,
        pipeline = pipeline,
    )
}

