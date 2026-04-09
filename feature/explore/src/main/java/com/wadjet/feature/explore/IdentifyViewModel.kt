package com.wadjet.feature.explore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.IdentifyMatch
import com.wadjet.core.domain.model.IdentifyResult
import com.wadjet.core.domain.repository.ExploreRepository
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
    val error: String? = null,
)

@HiltViewModel
class IdentifyViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(IdentifyUiState())
    val state: StateFlow<IdentifyUiState> = _state.asStateFlow()

    fun onImageCaptured(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(cameraActive = false, isLoading = true, error = null) }
            val compressed = compressImage(file)
            exploreRepository.identifyLandmark(compressed)
                .onSuccess { result ->
                    _state.update { it.copy(result = result, isLoading = false) }
                }
                .onFailure { error ->
                    Timber.e(error, "Identify failed")
                    _state.update {
                        it.copy(
                            error = error.message ?: "Identification failed",
                            isLoading = false,
                            cameraActive = true,
                        )
                    }
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
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return file

        val outFile = File(context.cacheDir, "identify_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()
        return outFile
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
