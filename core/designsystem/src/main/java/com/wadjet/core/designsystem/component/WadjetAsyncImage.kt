package com.wadjet.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors

/**
 * I-01: painter-based image (no SubcomposeAsyncImage) — this component is used
 * in Explore/Dictionary/Dashboard/Stories list cells where per-image
 * subcomposition is a scroll-perf hazard.
 */
@Composable
fun WadjetAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(300)
            .build(),
    )
    val state by painter.state.collectAsState()

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = WadjetColors.Gold,
                        strokeWidth = 2.dp,
                    )
                }
            }
            is AsyncImagePainter.State.Error -> {
                if (placeholder != null) {
                    placeholder()
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(WadjetColors.SurfaceAlt),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "𓂀",
                            style = HieroglyphStyle,
                            color = WadjetColors.Gold.copy(alpha = 0.5f),
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}
