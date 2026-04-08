package com.wadjet.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.ErrorOutline,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = WadjetColors.TextMuted,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = WadjetColors.TextMuted,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            WadjetButton(text = "Try Again", onClick = onRetry)
        }
    }
}
