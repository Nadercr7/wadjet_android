package com.wadjet.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.wadjet.core.designsystem.WadjetColors

@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
        ),
        label = "shimmerTranslate",
    )
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .drawBehind {
                val gradientWidth = size.width * 0.5f
                val offset = progress * (size.width + gradientWidth) - gradientWidth
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            WadjetColors.Surface,
                            WadjetColors.GoldMuted.copy(alpha = 0.15f),
                            WadjetColors.Surface,
                        ),
                        start = Offset(offset, 0f),
                        end = Offset(offset + gradientWidth, 0f),
                    ),
                )
            }
    )
}
