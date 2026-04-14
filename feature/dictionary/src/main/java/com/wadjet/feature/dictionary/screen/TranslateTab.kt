package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.core.domain.model.TranslationResult
import com.wadjet.feature.dictionary.TranslateUiState

@Composable
fun TranslateTab(
    state: TranslateUiState,
    onInputChange: (String) -> Unit,
    onGardinerChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = WadjetColors.Gold,
        unfocusedBorderColor = WadjetColors.Dust,
        cursorColor = WadjetColors.Gold,
        focusedTextColor = WadjetColors.Text,
        unfocusedTextColor = WadjetColors.Text,
        focusedLabelColor = WadjetColors.Gold,
        unfocusedLabelColor = WadjetColors.TextMuted,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "Translate Hieroglyphs",
            style = MaterialTheme.typography.titleLarge,
            color = WadjetColors.Gold,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Enter MdC transliteration to get English and Arabic translations",
            style = MaterialTheme.typography.bodySmall,
            color = WadjetColors.TextMuted,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.input,
            onValueChange = onInputChange,
            label = { Text("Transliteration (MdC)") },
            placeholder = { Text("e.g. anx wDA snb", color = WadjetColors.Dust) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.gardinerInput,
            onValueChange = onGardinerChange,
            label = { Text("Gardiner Sequence (optional)") },
            placeholder = { Text("e.g. S34-R11-S29-N35-S29", color = WadjetColors.Dust) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onTranslate() }),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WadjetButton(
                text = if (state.isLoading) "Translating…" else "Translate",
                onClick = onTranslate,
                enabled = state.input.isNotBlank() && !state.isLoading,
                modifier = Modifier.weight(1f),
            )
            if (state.result != null || state.input.isNotBlank()) {
                WadjetGhostButton(
                    text = "Clear",
                    onClick = onClear,
                    modifier = Modifier.weight(0.5f),
                )
            }
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = WadjetColors.Error.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.Error,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        state.result?.let { result ->
            Spacer(modifier = Modifier.height(20.dp))
            TranslationResultCard(result)
        }
    }
}

@Composable
private fun TranslationResultCard(result: TranslationResult) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = WadjetColors.Surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Transliteration echo
            Text(
                text = result.transliteration,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = WadjetColors.Gold,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // English
            if (result.english.isNotBlank()) {
                Text(
                    text = "English",
                    style = MaterialTheme.typography.labelMedium,
                    color = WadjetColors.Sand,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.english,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WadjetColors.Text,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Arabic
            if (result.arabic.isNotBlank()) {
                Text(
                    text = "العربية",
                    style = MaterialTheme.typography.labelMedium,
                    color = WadjetColors.Sand,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.arabic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WadjetColors.Text,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Context
            if (result.context.isNotBlank()) {
                Text(
                    text = "Context",
                    style = MaterialTheme.typography.labelMedium,
                    color = WadjetColors.Sand,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.context,
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Provider + cache info
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (result.provider.isNotBlank()) {
                    Text(
                        text = result.provider,
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.Dust,
                    )
                }
                if (result.fromCache) {
                    Text(
                        text = "cached",
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.Gold.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
