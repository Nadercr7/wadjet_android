package com.wadjet.core.designsystem.animation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.wadjet.core.designsystem.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val FRAME_COUNT = 48
private const val FRONT_FRAME = 12   // sway phase where the yaw passes ~0deg (front-facing)
private const val FRAME_MS = 90L     // ~11 fps -> ~4.3s gentle sway loop

/**
 * Pre-rendered 3D "Golden Eye of Wadjet" stela turntable — a gentle ±25° sway
 * loop baked from the SAME model as the web hero (assets/eye3d/frame_###.png,
 * 48 frames, ~0.3 MB). No 3D engine in the base APK.
 *
 * Honors reduced-motion (freezes on the front-facing frame) and falls back to
 * the static Eye vector if the frames can't be decoded.
 */
@Composable
fun TurntableEye(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val reduce = isReducedMotionEnabled()
    var frames by remember { mutableStateOf<List<ImageBitmap>?>(null) }
    var index by remember { mutableIntStateOf(FRONT_FRAME) }

    LaunchedEffect(Unit) {
        frames = withContext(Dispatchers.IO) {
            runCatching {
                (0 until FRAME_COUNT).map { i ->
                    context.assets.open("eye3d/frame_%03d.png".format(i)).use {
                        BitmapFactory.decodeStream(it).asImageBitmap()
                    }
                }
            }.getOrNull()
        }
    }

    val loaded = frames
    if (loaded.isNullOrEmpty()) {
        // Fallback: the static gold Eye mark so the hero never breaks.
        Image(
            painter = painterResource(R.drawable.ic_logo_eye),
            contentDescription = contentDescription,
            modifier = modifier,
        )
        return
    }

    if (!reduce) {
        LaunchedEffect(loaded) {
            while (true) {
                delay(FRAME_MS)
                index = (index + 1) % loaded.size
            }
        }
    }

    Image(
        bitmap = loaded[index.coerceIn(0, loaded.size - 1)],
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}
