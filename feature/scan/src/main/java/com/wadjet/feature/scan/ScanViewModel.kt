package com.wadjet.feature.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
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
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScanEvent>()
    val events: SharedFlow<ScanEvent> = _events.asSharedFlow()

    private var mediaPlayer: MediaPlayer? = null

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
            val voice = if (isHieroglyphic) EgyptianPronunciation.VOICE else null
            val style = if (isHieroglyphic) EgyptianPronunciation.STYLE else null
            scanRepository.speak(ttsText, lang, ctx, voice, style).onSuccess { bytes ->
                if (bytes != null) {
                    playWavBytes(key, bytes)
                } else {
                    // 204 — signal local TTS fallback
                    _state.update {
                        it.copy(
                            ttsStates = it.ttsStates + (key to TtsState.IDLE),
                            error = "LOCAL_TTS:$lang:$text",
                        )
                    }
                }
            }.onFailure {
                Timber.e(it, "Scan TTS failed for key=$key")
                _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) }
            }
        }
    }

    private fun playWavBytes(key: String, bytes: ByteArray) {
        stopMediaPlayer()
        try {
            val tmp = File.createTempFile("tts_", ".wav", context.cacheDir)
            tmp.writeBytes(bytes)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tmp.absolutePath)
                prepare()
                setOnCompletionListener {
                    _state.update { s -> s.copy(ttsStates = s.ttsStates + (key to TtsState.IDLE)) }
                    stopMediaPlayer()
                }
                start()
            }
            _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.PLAYING)) }
        } catch (e: Exception) {
            Timber.e(e, "MediaPlayer failed")
            _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.IDLE)) }
        }
    }

    private fun stopTts(key: String) {
        stopMediaPlayer()
        _state.update { it.copy(ttsStates = it.ttsStates + (key to TtsState.IDLE)) }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.apply { if (isPlaying) stop(); release() }
        mediaPlayer = null
    }

    fun onImageCaptured(file: File) {
        viewModelScope.launch {
            // Check free-tier limits
            userRepository.getLimits().onSuccess { limits ->
                if (limits.scansToday >= limits.scansPerDay) {
                    _state.update { it.copy(error = "Daily scan limit reached (${limits.scansPerDay}). Try again tomorrow.") }
                    _events.emit(ScanEvent.ShowToast("Daily scan limit reached"))
                    return@launch
                }
            }

            _state.update { it.copy(cameraActive = false, isLoading = true, error = null, scanStep = ScanStep.DETECTING) }

            // Compress image
            val compressed = compressImage(file)

            // Animate through steps
            _state.update { it.copy(scanStep = ScanStep.CLASSIFYING) }

            scanRepository.scanImage(compressed)
                .onSuccess { result ->
                    _state.update { it.copy(scanStep = ScanStep.TRANSLITERATING) }
                    kotlinx.coroutines.delay(400)
                    _state.update { it.copy(scanStep = ScanStep.TRANSLATING) }
                    kotlinx.coroutines.delay(400)
                    _state.update { it.copy(scanStep = ScanStep.DONE) }
                    kotlinx.coroutines.delay(300)

                    // Save to Room
                    val thumbnailPath = saveThumbnail(compressed)
                    val saveResult = scanRepository.saveScanResult(result, thumbnailPath)
                    val scanId = saveResult.getOrNull()

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
                            error = error.message ?: "Scan failed",
                            isLoading = false,
                            scanStep = ScanStep.IDLE,
                        )
                    }
                    _events.emit(ScanEvent.ShowToast(error.message ?: "Scan failed"))
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
                    _state.update { it.copy(error = "Failed to read selected image") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process selected image")
                _state.update { it.copy(error = "Failed to process image") }
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

    private fun compressImage(file: File): File {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        val maxDim = 1024
        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return file

        val outFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()
        return outFile
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
