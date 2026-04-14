package com.wadjet.feature.dictionary.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.GardinerCodeStyle
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetTextField
import com.wadjet.core.domain.model.PaletteSign
import com.wadjet.feature.dictionary.WriteUiState

@Composable
fun WriteTab(
    state: WriteUiState,
    onInputChange: (String) -> Unit,
    onConvert: () -> Unit,
    onClear: () -> Unit,
    onAppendGlyph: (PaletteSign) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Text input
        WadjetTextField(
            value = state.inputText,
            onValueChange = onInputChange,
            label = "Enter text",
            placeholder = "Type English text or transliteration…",
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Convert button (full width, no palette)
        WadjetButton(
            text = "Convert",
            onClick = onConvert,
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
            enabled = state.inputText.isNotBlank(),
        )

        // Error
        if (state.error != null) {
            Text(
                text = state.error,
                color = WadjetColors.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Result — only shown after Convert tap
        val result = state.result
        if (result != null) {
            Spacer(Modifier.height(20.dp))

            // Large output
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WadjetColors.Surface)
                    .animateContentSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = result.hieroglyphs,
                    style = HieroglyphStyle.copy(fontSize = 48.sp),
                    textAlign = TextAlign.Center,
                )
            }

            // Glyph breakdown
            if (result.glyphs.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Breakdown:", style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                result.glyphs.forEach { g ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(g.glyph, style = HieroglyphStyle.copy(fontSize = 24.sp))
                        Spacer(Modifier.width(8.dp))
                        Text(g.code, style = GardinerCodeStyle)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            g.transliteration?.let { t ->
                                Text(t, style = MaterialTheme.typography.bodySmall, color = WadjetColors.Sand)
                            }
                            g.description?.let { d ->
                                Text(d, style = MaterialTheme.typography.bodySmall, color = WadjetColors.TextMuted)
                            }
                        }
                    }
                }
            }

            // Copy + Share
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    // Build TTS text: join transliterations as words, then map
                    // Egyptological ASCII conventions to pronounceable English
                    val transliterationText = result.glyphs
                        .mapNotNull { it.transliteration?.takeIf(String::isNotBlank) }
                        .joinToString(" ")
                    val ttsText = transliterationToSpeech(transliterationText).ifBlank {
                        result.glyphs.mapNotNull { it.description }.joinToString(", ")
                    }
                    if (ttsText.isNotBlank()) onSpeak(ttsText)
                }) {
                    Icon(Icons.Default.VolumeUp, "Read aloud", tint = WadjetColors.Gold, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("hieroglyphs", result.hieroglyphs))
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, "Copy", tint = WadjetColors.Gold, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, result.hieroglyphs)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                }) {
                    Icon(Icons.Default.Share, "Share", tint = WadjetColors.Gold, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

/**
 * Maps Egyptological transliteration ASCII conventions to pronounceable English.
 * e.g. "anx" → "ankh", "nTr" → "netcher", "Htp" → "hetep"
 */
private fun transliterationToSpeech(text: String): String {
    // Order matters: replace multi-char sequences before single-char
    return text
        .replace("nTr", "netcher")
        .replace("Htp", "hetep")
        .replace("x", "kh")
        .replace("X", "kh")
        .replace("S", "sh")
        .replace("D", "dj")
        .replace("T", "ch")
        .replace("H", "h")
        .replace("A", "ah")
        .replace("aA", "ah")
}

@Composable
private fun PaletteItem(sign: PaletteSign, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WadjetColors.Surface)
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(sign.glyph, style = HieroglyphStyle.copy(fontSize = 22.sp))
        Text(sign.code, style = GardinerCodeStyle.copy(fontSize = 8.sp))
    }
}
