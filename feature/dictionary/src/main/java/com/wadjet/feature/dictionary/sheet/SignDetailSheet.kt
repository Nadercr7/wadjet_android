package com.wadjet.feature.dictionary.sheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.GardinerCodeStyle
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import androidx.compose.ui.res.stringResource
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.core.domain.model.Sign
import com.wadjet.feature.dictionary.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignDetailSheet(
    sign: Sign,
    isFavorite: Boolean,
    onSpeak: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val copiedToast = stringResource(DesignR.string.copied_toast)
    val shareChooserTitle = stringResource(R.string.sign_share_chooser)

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
            DetailRow(stringResource(R.string.sign_transliteration_label), sign.transliteration)
        }

        // Reading
        val reading = sign.reading
        if (!reading.isNullOrBlank()) {
            DetailRow(stringResource(R.string.sign_reading_label), reading)
        }

        // Description
        if (sign.description.isNotBlank()) {
            DetailRow(stringResource(R.string.sign_description_label), sign.description)
        }

        // Pronunciation guide
        if (!sign.pronunciationSound.isNullOrBlank()) {
            DetailRow(stringResource(R.string.sign_pronunciation_label), "${sign.pronunciationSound} — ${sign.pronunciationExample.orEmpty()}")
        }

        // Fun fact
        val funFact = sign.funFact
        if (!funFact.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = WadjetColors.Gold.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.sign_fun_fact_label),
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

        // Action buttons
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            // Favorite
            IconButton(onClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onToggleFavorite()
            }) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(if (isFavorite) DesignR.string.action_remove_favorite else DesignR.string.action_add_favorite),
                    tint = if (isFavorite) WadjetColors.Error else WadjetColors.Gold,
                    modifier = Modifier.size(28.dp),
                )
            }

            // TTS — prefer speechText, fall back to reading (only for pronounceable signs)
            val canPronounce = sign.isPhonetic || sign.type !in listOf("determinative")
            val ttsText = sign.speechText?.takeIf { it.isNotBlank() }
                ?: sign.reading?.takeIf { it.isNotBlank() && canPronounce }
            if (!ttsText.isNullOrBlank()) {
                IconButton(onClick = { onSpeak(ttsText) }) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = stringResource(R.string.sign_pronounce_desc),
                        tint = WadjetColors.Gold,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            // Copy
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("glyph", "${sign.glyph} ${sign.code} — ${sign.description}"))
                onShowToast(copiedToast)
            }) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(DesignR.string.action_copy),
                    tint = WadjetColors.Gold,
                    modifier = Modifier.size(28.dp),
                )
            }

            // Share
            IconButton(onClick = {
                val shareText = "${sign.glyph} ${sign.code}\n${sign.description}\nTransliteration: ${sign.transliteration}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, shareChooserTitle))
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(DesignR.string.action_share),
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
