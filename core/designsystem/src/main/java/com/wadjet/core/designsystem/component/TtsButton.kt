package com.wadjet.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

enum class TtsState { IDLE, LOADING, PLAYING }

@Composable
fun TtsButton(
    state: TtsState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Speak",
) {
    IconButton(onClick = onClick, modifier = modifier.size(32.dp)) {
        when (state) {
            TtsState.IDLE -> Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = contentDescription,
                tint = WadjetColors.Sand,
                modifier = Modifier.size(18.dp),
            )
            TtsState.LOADING -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = WadjetColors.Gold,
                strokeWidth = 2.dp,
            )
            TtsState.PLAYING -> Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                tint = WadjetColors.Gold,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
