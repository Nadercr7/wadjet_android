package com.wadjet.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.settings.R
import com.wadjet.feature.settings.SettingsViewModel

@Composable
fun SettingsQuickDialog(
    onDismiss: () -> Unit,
    onOpenFullSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.quick_settings_title),
                color = WadjetColors.Gold,
            )
        },
        text = {
            Column {
                // TTS Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_enable_tts),
                        color = WadjetColors.Text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = state.ttsEnabled,
                        onCheckedChange = viewModel::setTtsEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WadjetColors.Gold,
                            checkedTrackColor = WadjetColors.Gold.copy(alpha = 0.3f),
                            uncheckedThumbColor = WadjetColors.TextMuted,
                            uncheckedTrackColor = WadjetColors.Surface,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = WadjetColors.Border)
                Spacer(modifier = Modifier.height(8.dp))

                // Clear Cache
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_cache_label),
                        color = WadjetColors.Text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.settings_cache_size, state.cacheSizeMb),
                        color = WadjetColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenFullSettings) {
                Text(
                    text = stringResource(R.string.quick_settings_full_settings),
                    color = WadjetColors.Gold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(DesignR.string.action_cancel),
                    color = WadjetColors.TextMuted,
                )
            }
        },
        containerColor = WadjetColors.Surface,
    )
}
