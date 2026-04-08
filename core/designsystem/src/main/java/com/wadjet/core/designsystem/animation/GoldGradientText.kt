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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import com.wadjet.core.designsystem.WadjetColors

@Composable
fun GoldGradientText(
    text: String,
    style: TextStyle = MaterialTheme.typography.displaySmall,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "goldGradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "goldGradientOffset",
    )
    Text(
        text = text,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    WadjetColors.GoldDark,
                    WadjetColors.Gold,
                    WadjetColors.GoldLight,
                    WadjetColors.Gold,
                    WadjetColors.GoldDark,
                ),
                start = Offset(offset, 0f),
                end = Offset(offset + 500f, 0f),
            )
        ),
    )
}
