package com.wadjet.core.designsystem.component

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.R
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.GoldGradientText

/**
 * Full-screen branded loader: pulsing logo + gold gradient "WADJET" text + shimmer bar.
 * Matches the web app's branded loading experience.
 */
@Composable
fun WadjetFullLoader(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "fullLoader")

    // Logo pulse scale
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loaderScale",
    )

    // Shimmer bar progress
    val shimmerOffset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loaderShimmer",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Pulsing logo
            Image(
                painter = painterResource(R.drawable.logo_wadjet),
                contentDescription = "Wadjet",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gold gradient animated text
            GoldGradientText(text = "WADJET")

            Spacer(modifier = Modifier.height(24.dp))

            // Shimmer progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WadjetColors.Border),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    WadjetColors.Border,
                                    WadjetColors.Gold,
                                    WadjetColors.GoldLight,
                                    WadjetColors.Gold,
                                    WadjetColors.Border,
                                ),
                                start = Offset(shimmerOffset * 300f, 0f),
                                end = Offset(shimmerOffset * 300f + 200f, 0f),
                            ),
                        ),
                )
            }

            if (message != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WadjetColors.TextMuted,
                )
            }
        }
    }
}
