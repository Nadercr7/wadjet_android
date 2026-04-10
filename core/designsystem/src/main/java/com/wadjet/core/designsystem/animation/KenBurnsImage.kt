package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wadjet.core.designsystem.R

@Composable
fun KenBurnsImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kenBurns")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "kenBurnsScale",
    )
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "kenBurnsOffset",
    )

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        placeholder = painterResource(R.drawable.ic_placeholder_landmark),
        error = painterResource(R.drawable.ic_placeholder_error),
        fallback = painterResource(R.drawable.ic_placeholder_landmark),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
            }
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
    )
}
