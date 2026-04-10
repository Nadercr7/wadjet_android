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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.GardinerCodeStyle
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.StreamingDots
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetTextField
import com.wadjet.core.domain.model.PaletteSign
import com.wadjet.feature.dictionary.WRITE_MODES
import com.wadjet.feature.dictionary.WriteUiState

@Composable
fun WriteTab(
    state: WriteUiState,
    onInputChange: (String) -> Unit,
    onModeSelect: (String) -> Unit,
    onConvert: () -> Unit,
    onClear: () -> Unit,
    onAppendGlyph: (PaletteSign) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showPalette by remember { mutableStateOf(false) }

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

        // Mode selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WRITE_MODES.forEach { (mode, label) ->
                FilterChip(
                    selected = state.selectedMode == mode,
                    onClick = { onModeSelect(mode) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WadjetColors.Gold,
                        selectedLabelColor = WadjetColors.Night,
                        containerColor = WadjetColors.Surface,
                        labelColor = WadjetColors.TextMuted,
                    ),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Convert button
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WadjetButton(
                text = "Convert",
                onClick = onConvert,
                modifier = Modifier.weight(1f),
                isLoading = state.isLoading,
                enabled = state.inputText.isNotBlank(),
            )
            WadjetButton(
                text = if (showPalette) "Hide Palette" else "Palette",
                onClick = { showPalette = !showPalette },
                modifier = Modifier.weight(1f),
            )
        }

        // Error
        if (state.error != null) {
            Text(
                text = state.error,
                color = WadjetColors.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Realtime preview
        if (state.result == null && state.inputText.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            if (state.isPreviewLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    StreamingDots()
                }
            } else if (state.preview != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WadjetColors.Surface.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.preview.hieroglyphs,
                        style = HieroglyphStyle.copy(fontSize = 36.sp),
                        textAlign = TextAlign.Center,
                        color = WadjetColors.Gold.copy(alpha = 0.6f),
                    )
                }
            }
        }

        // Result
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

            // MdC output
            val mdc = result.mdc
            if (!mdc.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("MdC:", style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                Text(
                    text = mdc,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = WadjetColors.Sand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(WadjetColors.Surface)
                        .padding(8.dp),
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
                        Text(g.gardinerCode, style = GardinerCodeStyle)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            g.transliteration?.let { t ->
                                Text(t, style = MaterialTheme.typography.bodySmall, color = WadjetColors.Sand)
                            }
                            g.phoneticValue?.let { p ->
                                Text("/$p/", style = MaterialTheme.typography.bodySmall, color = WadjetColors.Sand)
                            }
                            g.meaning?.let { m ->
                                Text(m, style = MaterialTheme.typography.bodySmall, color = WadjetColors.TextMuted)
                            }
                        }
                    }
                }
            }

            // Copy + Share
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
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

        // Glyph Palette
        if (showPalette && state.palette.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Glyph Palette", style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
            Spacer(Modifier.height(8.dp))
            // Using a fixed-height grid inside a scroll view
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(240.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                items(state.palette) { sign ->
                    PaletteItem(sign = sign, onClick = { onAppendGlyph(sign) })
                }
            }
        }
    }
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
