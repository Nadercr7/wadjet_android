package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

/**
 * Animated arc stroke orbiting around the border of its parent.
 * Based on CSS `@keyframes border-beam { offset-distance: 0% → 100% }`.
 */
fun Modifier.borderBeam(
    beamLength: Float = 0.15f,
    strokeWidth: Dp = 2.dp,
    durationMs: Int = 4000,
    beamColor: Color = WadjetColors.Gold,
    beamColorEnd: Color = WadjetColors.GoldLight,
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()

        val w = size.width
        val h = size.height
        val path = Path().apply {
            addRect(Rect(0f, 0f, w, h))
        }
        val pathMeasure = PathMeasure().apply { setPath(path, true) }
        val totalLength = pathMeasure.length

        // Not using composed — will use drawWithContent only.
        // We need the animated value. Use a trick: store nothing, rely on recomposition.
    }
)

/**
 * Composable wrapper approach — better for Compose infinite transitions.
 */
@Composable
fun BorderBeam(
    modifier: Modifier = Modifier,
    beamLength: Float = 0.15f,
    strokeWidth: Dp = 2.dp,
    durationMs: Int = 4000,
    beamColor: Color = WadjetColors.Gold,
    beamColorEnd: Color = WadjetColors.GoldLight,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "borderBeam")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "borderBeamProgress",
    )

    Box(
        modifier = modifier.drawWithContent {
            drawContent()

            val w = size.width
            val h = size.height
            val perimeter = 2 * (w + h)
            val beamStart = progress * perimeter
            val beamEnd = beamStart + beamLength * perimeter
            val strokePx = strokeWidth.toPx()

            // Build perimeter path
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            val measure = PathMeasure().apply { setPath(path, true) }
            val segmentPath = Path()

            // Extract the beam segment (wraps around)
            val actualStart = beamStart % perimeter
            val actualEnd = beamEnd % perimeter

            if (actualEnd > actualStart) {
                measure.getSegment(actualStart, actualEnd, segmentPath, true)
            } else {
                // Wraps around the corner
                measure.getSegment(actualStart, perimeter, segmentPath, true)
                measure.getSegment(0f, actualEnd, segmentPath, true)
            }

            drawPath(
                path = segmentPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        beamColor.copy(alpha = 0f),
                        beamColor,
                        beamColorEnd,
                        beamColor.copy(alpha = 0f),
                    ),
                ),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        },
    ) {
        content()
    }
}
