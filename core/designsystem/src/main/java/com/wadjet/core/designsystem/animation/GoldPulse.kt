package com.wadjet.core.designsystem.animation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

fun Modifier.goldPulse(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "goldPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "goldPulseAlpha",
    )
    this.shadow(
        elevation = 16.dp,
        shape = CircleShape,
        spotColor = WadjetColors.Gold.copy(alpha = alpha),
    )
}
