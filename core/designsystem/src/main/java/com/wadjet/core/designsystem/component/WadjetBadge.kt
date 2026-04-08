package com.wadjet.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

enum class BadgeVariant { Gold, Muted, Success, Error }

@Composable
fun WadjetBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Gold,
) {
    val (bg, fg) = when (variant) {
        BadgeVariant.Gold -> WadjetColors.Gold.copy(alpha = 0.15f) to WadjetColors.Gold
        BadgeVariant.Muted -> WadjetColors.SurfaceAlt to WadjetColors.TextMuted
        BadgeVariant.Success -> WadjetColors.Success.copy(alpha = 0.15f) to WadjetColors.Success
        BadgeVariant.Error -> WadjetColors.Error.copy(alpha = 0.15f) to WadjetColors.Error
    }

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}
