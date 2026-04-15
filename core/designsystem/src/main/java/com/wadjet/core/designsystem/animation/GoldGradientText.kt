package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import com.wadjet.core.designsystem.WadjetColors

@Composable
fun GoldGradientText(
    text: String,
    style: TextStyle = MaterialTheme.typography.displaySmall,
) {
    var textWidth by remember { mutableFloatStateOf(500f) }
    val infiniteTransition = rememberInfiniteTransition(label = "goldGradient")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "goldGradientOffset",
    )
    val sweepOffset = progress * textWidth * 2f
    Text(
        text = text,
        onTextLayout = { textWidth = it.size.width.toFloat() },
        style = style.copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    WadjetColors.GoldDark,
                    WadjetColors.Gold,
                    WadjetColors.GoldLight,
                    WadjetColors.Gold,
                    WadjetColors.GoldDark,
                ),
                start = Offset(sweepOffset - textWidth, 0f),
                end = Offset(sweepOffset, 0f),
            )
        ),
    )
}
