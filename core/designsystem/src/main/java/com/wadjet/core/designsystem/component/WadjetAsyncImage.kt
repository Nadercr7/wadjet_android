package com.wadjet.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.crossfade
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors

@Composable
fun WadjetAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = coil3.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
            .data(url)
            .crossfade(300)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = WadjetColors.Gold,
                    strokeWidth = 2.dp,
                )
            }
        },
        error = {
            if (placeholder != null) {
                placeholder()
            } else {
                Box(
                    modifier = Modifier.background(WadjetColors.SurfaceAlt),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\uD80C\uDC80",
                        style = HieroglyphStyle,
                        color = WadjetColors.Gold.copy(alpha = 0.5f),
                    )
                }
            }
        },
    )
}
