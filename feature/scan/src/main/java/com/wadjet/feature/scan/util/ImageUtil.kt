package com.wadjet.feature.scan

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.util.Base64

@Composable
fun rememberBase64Bitmap(base64: String?): ImageBitmap? {
    return remember(base64) {
        if (base64.isNullOrBlank()) return@remember null
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
