package com.wadjet.feature.explore.image

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.wadjet.core.designsystem.R as DesignR

/**
 * Landmark thumbnail backed by Pexels.
 *
 * Looks the landmark up on Pexels (cached per-slug) and renders the result.
 * The backend [primaryUrl] is ignored because the backend-provided Wikimedia
 * URLs are largely stale (404). Pexels yields consistently high-quality,
 * uniform photos for every landmark.
 */
@Composable
fun LandmarkThumbnail(
    slug: String,
    name: String,
    primaryUrl: String?, // kept for API stability; unused
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    viewModel: ThumbnailFallbackViewModel = hiltViewModel(key = "thumb-$slug"),
) {
    val resolver = viewModel.resolver
    val initialCached = remember(slug) { resolver.cached(slug) }
    var pexelsUrl by remember(slug) { mutableStateOf(initialCached) }

    LaunchedEffect(slug) {
        if (pexelsUrl == null) {
            pexelsUrl = resolver.resolve(slug, name)
        }
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = pexelsUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            placeholder = painterResource(DesignR.drawable.ic_placeholder_landmark),
            error = painterResource(DesignR.drawable.ic_placeholder_landmark),
            fallback = painterResource(DesignR.drawable.ic_placeholder_landmark),
            modifier = Modifier.matchParentSize(),
        )
    }
}
