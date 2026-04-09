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
import com.wadjet.core.designsystem.WadjetColors

/**
 * Repeating gold dot grid background with subtle pulsing glow.
 * Based on CSS `@keyframes dot-glow { opacity: 0.4 → 1 }`.
 */
@Composable
fun DotPattern(
    modifier: Modifier = Modifier,
    dotColor: Color = WadjetColors.Gold,
    dotRadius: Float = 1.5f,
    spacing: Float = 24f,
    baseAlpha: Float = 0.08f,
    glowAlpha: Float = 0.18f,
    durationMs: Int = 3000,
) {
    val transition = rememberInfiniteTransition(label = "dotPattern")
    val alpha by transition.animateFloat(
        initialValue = baseAlpha,
        targetValue = glowAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotAlpha",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cols = (size.width / spacing).toInt() + 1
        val rows = (size.height / spacing).toInt() + 1

        for (row in 0..rows) {
            for (col in 0..cols) {
                drawCircle(
                    color = dotColor.copy(alpha = alpha),
                    radius = dotRadius,
                    center = Offset(col * spacing, row * spacing),
                )
            }
        }
    }
}
