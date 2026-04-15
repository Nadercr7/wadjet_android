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
 * Animated gold gradient sweep over backgrounds.
 * Based on CSS `@keyframes gradient-sweep { background-position: 300% 0 }`.
 * Applies a sweeping gold gradient overlay.
 */
fun Modifier.goldGradientSweep(
    durationMs: Int = 4000,
    alpha: Float = 0.08f,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "goldGradientSweep")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "goldSweepOffset",
    )

    this.drawWithContent {
        drawContent()

        val sweepWidth = size.width * 0.3f
        val totalRange = size.width + sweepWidth
        val offset = progress * totalRange - sweepWidth

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    WadjetColors.GoldDark.copy(alpha = alpha * 0.5f),
                    WadjetColors.Gold.copy(alpha = alpha),
                    WadjetColors.GoldLight.copy(alpha = alpha),
                    WadjetColors.Gold.copy(alpha = alpha * 0.5f),
                    Color.Transparent,
                ),
                start = Offset(offset, 0f),
                end = Offset(offset + sweepWidth, size.height),
            ),
        )
    }
}
