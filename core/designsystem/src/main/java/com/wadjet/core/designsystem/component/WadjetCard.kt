package com.wadjet.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

@Composable
fun WadjetCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = WadjetColors.Surface,
            ),
            border = BorderStroke(1.dp, WadjetColors.Border),
            shape = RoundedCornerShape(12.dp),
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = WadjetColors.Surface,
            ),
            border = BorderStroke(1.dp, WadjetColors.Border),
            shape = RoundedCornerShape(12.dp),
            content = content,
        )
    }
}

@Composable
fun WadjetCardGlow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isHovered || isPressed) {
            WadjetColors.Gold.copy(alpha = 0.5f)
        } else {
            WadjetColors.Border
        },
        label = "borderColor",
    )
    val elevation by animateDpAsState(
        targetValue = if (isHovered || isPressed) 8.dp else 0.dp,
        label = "elevation",
    )

    Card(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = elevation,
            shape = RoundedCornerShape(12.dp),
            spotColor = WadjetColors.GoldGlow,
        ),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(containerColor = WadjetColors.Surface),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        content = content,
    )
}
