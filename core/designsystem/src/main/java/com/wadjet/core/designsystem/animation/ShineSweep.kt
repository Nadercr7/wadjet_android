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
import com.wadjet.core.designsystem.WadjetColors

/**
 * Diagonal shine line sweeping across a surface.
 * Based on CSS `@keyframes shine { background-position: 0% → 100% → 0% }`.
 */
fun Modifier.shineSweep(
    color: Color = WadjetColors.GoldLight,
    durationMs: Int = 3000,
    shineWidth: Float = 0.15f,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shineSweep")
    val progress by transition.animateFloat(
        initialValue = -shineWidth,
        targetValue = 1f + shineWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shineSweepProgress",
    )

    this.drawWithContent {
        drawContent()

        val diagonal = size.width + size.height
        val shineWidthPx = diagonal * shineWidth

        // Diagonal sweep from top-left to bottom-right
        val center = progress * diagonal
        val startX = center - shineWidthPx
        val endX = center + shineWidthPx

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.05f),
                    color.copy(alpha = 0.12f),
                    color.copy(alpha = 0.05f),
                    Color.Transparent,
                ),
                start = Offset(startX, 0f),
                end = Offset(endX, size.height),
            ),
        )
    }
}
