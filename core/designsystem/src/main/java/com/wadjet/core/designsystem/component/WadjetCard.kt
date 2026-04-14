package com.wadjet.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
    val cardColors = CardDefaults.cardColors(
        containerColor = WadjetColors.Surface,
    )
    val cardBorder = BorderStroke(1.dp, WadjetColors.Border)
    val cardShape = MaterialTheme.shapes.medium

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = cardColors,
            border = cardBorder,
            shape = cardShape,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            colors = cardColors,
            border = cardBorder,
            shape = cardShape,
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
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) {
            WadjetColors.Gold.copy(alpha = 0.5f)
        } else {
            WadjetColors.Border
        },
        label = "borderColor",
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 0.dp,
        label = "elevation",
    )

    Card(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = elevation,
            shape = MaterialTheme.shapes.medium,
            spotColor = WadjetColors.GoldGlow,
        ),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(containerColor = WadjetColors.Surface),
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.medium,
        content = content,
    )
}
