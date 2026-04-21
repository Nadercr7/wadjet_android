package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

/**
 * Repeating gold dot grid background with subtle pulsing glow.
 * Based on CSS `@keyframes dot-glow { opacity: 0.4 → 1 }`.
 */
@Composable
fun DotPattern(
    modifier: Modifier = Modifier,
    dotColor: Color = WadjetColors.Gold,
    dotRadius: Dp = 1.5.dp,
    spacing: Dp = 24.dp,
    baseAlpha: Float = 0.08f,
    glowAlpha: Float = 0.18f,
    durationMs: Int = 3000,
) {
    val reduceMotion = isReducedMotionEnabled()
    val transition = rememberInfiniteTransition(label = "dotPattern")
    val alpha by transition.animateFloat(
        initialValue = if (reduceMotion) baseAlpha else baseAlpha,
        targetValue = if (reduceMotion) baseAlpha else glowAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotAlpha",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val dotRadiusPx = dotRadius.toPx()
        val spacingPx = spacing.toPx()
        val cols = (size.width / spacingPx).toInt() + 1
        val rows = (size.height / spacingPx).toInt() + 1

        val points = buildList(cols * rows) {
            for (row in 0..rows) {
                for (col in 0..cols) {
                    add(Offset(col * spacingPx, row * spacingPx))
                }
            }
        }
        drawPoints(
            points = points,
            pointMode = PointMode.Points,
            color = dotColor.copy(alpha = alpha),
            strokeWidth = dotRadiusPx * 2,
        )
    }
}
