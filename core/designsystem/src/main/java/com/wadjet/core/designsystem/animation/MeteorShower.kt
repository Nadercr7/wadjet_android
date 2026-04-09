package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import com.wadjet.core.designsystem.WadjetColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Diagonal falling gold meteor streaks.
 * Based on CSS `@keyframes meteor { rotate(-215deg) translateX(0) → translateX(-500px) opacity:0 }`.
 */
@Composable
fun MeteorShower(
    modifier: Modifier = Modifier,
    meteorCount: Int = 5,
    angleDeg: Float = -215f,
    durationMs: Int = 3000,
) {
    val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
    val dx = cos(angleRad)
    val dy = sin(angleRad)

    val transition = rememberInfiniteTransition(label = "meteorShower")

    // Stagger each meteor with different phase offsets
    val meteors = (0 until meteorCount).map { index ->
        val offset = (index.toFloat() / meteorCount)
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs + index * 400,
                    delayMillis = index * (durationMs / meteorCount),
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "meteor_$index",
        )
        Triple(progress, offset, index)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxTravel = size.width * 0.8f
        val meteorLength = size.width * 0.15f

        meteors.forEach { (progress, offset, index) ->
            // Distribute start positions along the width
            val startX = size.width * (0.2f + offset * 0.7f)
            val startY = -20f + (index * 30f)

            val travel = progress * maxTravel
            val headX = startX + dx * travel
            val headY = startY + dy * travel
            val tailX = headX - dx * meteorLength
            val tailY = headY - dy * meteorLength

            // Fade out in last 30%
            val alpha = if (progress > 0.7f) {
                1f - ((progress - 0.7f) / 0.3f)
            } else {
                1f
            }

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WadjetColors.Gold.copy(alpha = alpha),
                        WadjetColors.GoldLight.copy(alpha = alpha * 0.6f),
                        WadjetColors.Gold.copy(alpha = 0f),
                    ),
                    start = Offset(headX, headY),
                    end = Offset(tailX, tailY),
                ),
                start = Offset(headX, headY),
                end = Offset(tailX, tailY),
                strokeWidth = 2f,
                cap = StrokeCap.Round,
            )
        }
    }
}
