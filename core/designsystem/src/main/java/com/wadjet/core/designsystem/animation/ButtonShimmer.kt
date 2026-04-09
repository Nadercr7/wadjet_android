package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.wadjet.core.designsystem.WadjetColors

/**
 * Perimeter shimmer light sweep on buttons.
 * Based on CSS `.btn-shimmer:hover { animation: shimmer 1.5s ease-in-out }`.
 * Draws a gold highlight that sweeps along the button border.
 */
fun Modifier.buttonShimmer(
    color: Color = WadjetColors.GoldLight,
    strokeWidth: Float = 2f,
    durationMs: Int = 2000,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "btnShimmer")
    val progress by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "btnShimmerProgress",
    )

    this.drawWithContent {
        drawContent()

        val w = size.width
        val h = size.height
        val highlightWidth = w * 0.3f
        val centerX = progress * (w + highlightWidth) - highlightWidth / 2

        // Top edge shimmer
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.6f),
                    color,
                    color.copy(alpha = 0.6f),
                    Color.Transparent,
                ),
                startX = centerX - highlightWidth / 2,
                endX = centerX + highlightWidth / 2,
            ),
            start = Offset(0f, 0f),
            end = Offset(w, 0f),
            strokeWidth = strokeWidth,
        )

        // Bottom edge shimmer (mirrored, delayed feel)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.4f),
                    color.copy(alpha = 0.7f),
                    color.copy(alpha = 0.4f),
                    Color.Transparent,
                ),
                startX = w - centerX - highlightWidth / 2,
                endX = w - centerX + highlightWidth / 2,
            ),
            start = Offset(0f, h),
            end = Offset(w, h),
            strokeWidth = strokeWidth,
        )
    }
}
