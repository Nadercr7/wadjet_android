package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ScanRepository {
    suspend fun scanImage(imageFile: File, mode: String = "auto"): Result<ScanResult>
    suspend fun saveScanResult(result: ScanResult, thumbnailPath: String): Result<Int>
    fun getScanHistory(): Flow<List<ScanHistorySummary>>
    suspend fun getScanResultJson(scanId: Int): Result<String>
    suspend fun getScanResult(scanId: Int): Result<ScanResult>
    suspend fun deleteScan(scanId: Int): Result<Unit>
    suspend fun speak(text: String, lang: String = "en", context: String = "scan"): Result<ByteArray?>
}
