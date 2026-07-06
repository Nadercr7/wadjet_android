package com.wadjet.feature.explore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.wadjet.core.common.StringResolver
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.IdentifyMatch
import com.wadjet.core.domain.model.IdentifyResult
import com.wadjet.core.domain.model.LandmarkDetail
import com.wadjet.core.domain.repository.ExploreRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class IdentifyUiState(
    val cameraActive: Boolean = true,
    val isLoading: Boolean = false,
    val result: IdentifyResult? = null,
    val detailPreview: LandmarkDetail? = null,
    val isLoadingDetail: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class IdentifyViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
    private val strings: StringResolver,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(IdentifyUiState())
    val state: StateFlow<IdentifyUiState> = _state.asStateFlow()

    fun onImageCaptured(file: File) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            // Check free-tier limits
            userRepository.getLimits().onSuccess { limits ->
                if (limits.scansToday >= limits.scansPerDay) {
                    _state.update { it.copy(error = strings.get(R.string.identify_error_daily_limit, limits.scansPerDay)) }
                    return@launch
                }
            }

            _state.update { it.copy(cameraActive = false, isLoading = true, error = null) }
            val compressed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { compressImage(file) }
            exploreRepository.identifyLandmark(compressed)
                .onSuccess { result ->
                    if (compressed !== file) compressed.delete()
                    _state.update { it.copy(result = result, isLoading = false) }
                    autoFetchDetail(result)
                }
                .onFailure { error ->
                    Timber.e(error, "Identify failed")
                    if (compressed !== file) compressed.delete()
                    _state.update {
                        it.copy(
                            error = error.message ?: strings.get(R.string.identify_error_failed),
                            isLoading = false,
                            cameraActive = true,
                        )
                    }
                }
        }
    }

    private fun autoFetchDetail(result: IdentifyResult) {
        val topMatch = result.topMatch ?: return
        if (topMatch.confidence < 0.60f || !result.isKnownLandmark) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDetail = true) }
            exploreRepository.getLandmarkDetail(topMatch.slug)
                .onSuccess { detail ->
                    _state.update { it.copy(detailPreview = detail, isLoadingDetail = false) }
                }
                .onFailure {
                    Timber.w(it, "Auto-fetch detail failed for ${topMatch.slug}")
                    _state.update { it.copy(isLoadingDetail = false) }
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
                    _state.update { it.copy(error = strings.get(R.string.identify_error_read_image)) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process selected image")
                _state.update { it.copy(error = strings.get(R.string.identify_error_process_image)) }
            }
        }
    }

    fun reset() {
        _state.update { IdentifyUiState() }
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
        val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return file
        val bitmap = uprightBitmap(decoded, file)

        val outFile = File(context.cacheDir, "identify_${System.currentTimeMillis()}.jpg")
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


    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "identify_pick_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output -> inputStream.copyTo(output) }
            inputStream.close()
            file
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy URI to file")
            null
        }
    }
}
