package com.wadjet.feature.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.wadjet.core.common.audio.AudioPlaybackManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.wadjet.core.common.StringResolver
import androidx.lifecycle.viewModelScope
import com.wadjet.core.common.EgyptianPronunciation
import com.wadjet.core.designsystem.component.TtsState
import com.wadjet.core.domain.model.ScanResult
import com.wadjet.core.domain.repository.ScanRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class ScanStep { IDLE, DETECTING, CLASSIFYING, TRANSLITERATING, TRANSLATING, DONE }

data class ScanUiState(
    val cameraActive: Boolean = true,
    val scanStep: ScanStep = ScanStep.IDLE,
    val result: ScanResult? = null,
    val savedScanId: Int? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val ttsStates: Map<String, TtsState> = emptyMap(),
)

sealed class ScanEvent {
    data class ShowToast(val message: String) : ScanEvent()
    data class NavigateToResult(val scanId: Int) : ScanEvent()
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val userRepository: UserRepository,
    private val audioPlayer: AudioPlaybackManager,
    private val strings: StringResolver,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScanEvent>()
    val events: SharedFlow<ScanEvent> = _events.asSharedFlow()

    fun speak(key: String, text: String, lang: String = "en") {
        val current = _state.value.ttsStates[key]
        if (current == TtsState.PLAYING || current == TtsState.LOADING) {
            stopTts(key)
            return
        }
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.LOADING)) }
        viewModelScope.launch {
            val isHieroglyphic = key == "translit"
            val ttsText = if (isHieroglyphic) EgyptianPronunciation.toSpeech(text) else text
            val ctx = if (isHieroglyphic) EgyptianPronunciation.CONTEXT else "scan_pronunciation"
            scanRepository.speak(ttsText, lang, ctx).onSuccess { bytes ->
                if (bytes != null) {
                    playWavBytes(key, bytes)
                } else {
                    speakLocally(key, text)
                }
            }.onFailure {
                Timber.e(it, "Scan TTS failed for key=$key; falling back to local voice")
                speakLocally(key, text)
            }
        }
    }

    private fun playWavBytes(key: String, bytes: ByteArray) {
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.PLAYING)) }
        audioPlayer.playWavBytes(
            bytes = bytes,
            onCompletion = { _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) } },
            onError = { _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) } },
        )
    }

    private fun stopTts(key: String) {
        audioPlayer.stop()
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.IDLE)) }
    }

    // H-05: degrade to the on-device voice when server TTS is unavailable
    private fun speakLocally(key: String, text: String) {
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.PLAYING)) }
        audioPlayer.speakLocal(text) {
            _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) }
        }
    }

    fun onImageCaptured(file: File) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            // Check free-tier limits
            userRepository.getLimits().onSuccess { limits ->
                if (limits.scansToday >= limits.scansPerDay) {
                    _state.update { it.copy(error = strings.get(R.string.scan_error_daily_limit, limits.scansPerDay)) }
                    _events.emit(ScanEvent.ShowToast(strings.get(R.string.scan_toast_daily_limit)))
                    return@launch
                }
            }

            _state.update { it.copy(cameraActive = false, isLoading = true, error = null, scanStep = ScanStep.DETECTING) }

            // Compress image on IO thread to avoid ANR
            val compressed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { compressImage(file) }

            // Animate through steps
            _state.update { it.copy(scanStep = ScanStep.CLASSIFYING) }

            scanRepository.scanImage(compressed)
                .onSuccess { result ->
                    // F-04: no fake staged delays — the server already finished the whole
                    // pipeline when this returns; go straight to DONE.
                    _state.update { it.copy(scanStep = ScanStep.DONE) }

                    // Save to Room
                    val thumbnailPath = saveThumbnail(compressed)
                    val saveResult = scanRepository.saveScanResult(result, thumbnailPath)
                    val scanId = saveResult.getOrNull()

                    // Clean up compressed temp file
                    if (compressed !== file) compressed.delete()

                    _state.update {
                        it.copy(
                            result = result,
                            savedScanId = scanId,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "Scan failed")
                    _state.update {
                        it.copy(
                            error = error.message ?: strings.get(R.string.scan_error_failed),
                            isLoading = false,
                            scanStep = ScanStep.IDLE,
                        )
                    }
                    _events.emit(ScanEvent.ShowToast(error.message ?: strings.get(R.string.scan_error_failed)))
                }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val file = uriToFile(uri)
                if (file != null) {
                    onImageCaptured(file)
                } else {
                    _state.update { it.copy(error = strings.get(R.string.scan_error_read_image)) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process selected image")
                _state.update { it.copy(error = strings.get(R.string.scan_error_process_image)) }
            }
        }
    }

    fun resetScan() {
        _state.update {
            ScanUiState(cameraActive = true)
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        // Clean up temp files in cacheDir
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("scan_") || file.name.startsWith("tts_") || file.name.startsWith("picked_")) {
                file.delete()
            }
        }
    }

    private fun compressImage(file: File): File {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        val maxDim = 1024
        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return file
        val bitmap = uprightBitmap(decoded, file)

        val outFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()
        return outFile
    }

    /**
     * F-01: BitmapFactory ignores the EXIF Orientation tag and the re-encoded JPEG
     * carries none, so portrait phone photos reached the server sideways and
     * degraded detection. Bake the rotation into the pixels before upload.
     */
    private fun uprightBitmap(source: android.graphics.Bitmap, sourceFile: File): android.graphics.Bitmap {
        val orientation = androidx.exifinterface.media.ExifInterface(sourceFile.absolutePath)
            .getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL,
            )
        val matrix = android.graphics.Matrix()
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.preScale(-1f, 1f) }
            androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.preScale(-1f, 1f) }
            else -> return source
        }
        val rotated = android.graphics.Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotated != source) source.recycle()
        return rotated
    }


    private fun saveThumbnail(file: File): String {
        val thumbDir = File(context.filesDir, "scan_thumbnails")
        thumbDir.mkdirs()
        val thumbFile = File(thumbDir, "thumb_${System.currentTimeMillis()}.jpg")

        val options = BitmapFactory.Options().apply { inSampleSize = 4 }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return file.absolutePath

        FileOutputStream(thumbFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        bitmap.recycle()
        return thumbFile.absolutePath
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "picked_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output -> inputStream.copyTo(output) }
            inputStream.close()
            file
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy URI to file")
            null
        }
    }
}
