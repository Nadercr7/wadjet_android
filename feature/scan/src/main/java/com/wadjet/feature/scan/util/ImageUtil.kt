package com.wadjet.feature.scan

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * I-04: decode off the main thread — the previous implementation decoded the
 * full bitmap inside composition, janking the first frame of the result screen.
 * Emits null until the decode completes.
 */
@Composable
fun rememberBase64Bitmap(base64: String?): ImageBitmap? {
    val bitmap: State<ImageBitmap?> = produceState<ImageBitmap?>(initialValue = null, base64) {
        value = if (base64.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.Default) {
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    return bitmap.value
}
