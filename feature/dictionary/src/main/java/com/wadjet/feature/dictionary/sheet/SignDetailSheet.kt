package com.wadjet.feature.dictionary.sheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.GardinerCodeStyle
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.domain.model.Sign

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignDetailSheet(
    sign: Sign,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Large glyph
        Text(
            text = sign.glyph,
            style = HieroglyphStyle.copy(fontSize = 80.sp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Code + type badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = sign.code,
                style = GardinerCodeStyle.copy(fontSize = 18.sp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = WadjetColors.Gold.copy(alpha = 0.15f),
            ) {
                Text(
                    text = sign.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = WadjetColors.Gold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = WadjetColors.SurfaceAlt,
            ) {
                Text(
                    text = sign.categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = WadjetColors.TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transliteration
        if (sign.transliteration.isNotBlank()) {
            DetailRow("Transliteration", sign.transliteration)
        }

        // Phonetic
        val phonetic = sign.phoneticValue
        if (!phonetic.isNullOrBlank()) {
            DetailRow("Phonetic", phonetic)
        }

        // Meaning
        if (sign.meaning.isNotBlank()) {
            DetailRow("Meaning", sign.meaning)
        }

        // Pronunciation guide
        if (!sign.pronunciationSound.isNullOrBlank()) {
            DetailRow("Pronunciation", "${sign.pronunciationSound} — ${sign.pronunciationDesc.orEmpty()}")
        }

        // Fun fact
        val funFact = sign.funFact
        if (!funFact.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WadjetColors.Gold.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Fun Fact",
                        style = MaterialTheme.typography.labelMedium,
                        color = WadjetColors.Gold,
                    )
                    Text(
                        text = funFact,
                        style = MaterialTheme.typography.bodySmall,
                        color = WadjetColors.Text,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        // Examples
        if (sign.examples.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Examples",
                style = MaterialTheme.typography.labelMedium,
                color = WadjetColors.Gold,
                modifier = Modifier.fillMaxWidth(),
            )
            sign.examples.forEach { example ->
                Text(
                    text = "• $example",
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.Text,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
            }
        }

        // Action buttons
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            // TTS — prefer speech field (richer pronunciation), fall back to phonetic
            val ttsText = sign.speech?.takeIf { it.isNotBlank() } ?: phonetic
            if (!ttsText.isNullOrBlank()) {
                IconButton(onClick = { onSpeak(ttsText) }) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Pronounce",
                        tint = WadjetColors.Gold,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            // Copy
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("glyph", "${sign.glyph} ${sign.code} — ${sign.meaning}"))
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = WadjetColors.Gold,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Share
            IconButton(onClick = {
                val shareText = "${sign.glyph} ${sign.code}\n${sign.meaning}\nTransliteration: ${sign.transliteration}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Share Sign"))
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = WadjetColors.Gold,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WadjetColors.TextMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = WadjetColors.Text,
        )
    }
}
