package com.wadjet.feature.scan.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.domain.model.DetectedGlyph
import com.wadjet.core.domain.model.ScanResult
import com.wadjet.feature.scan.rememberBase64Bitmap
import com.wadjet.feature.scan.util.gardinerToUnicode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    result: ScanResult,
    onScanAgain: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showArabic by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        TopAppBar(
            title = { Text("Results", color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            actions = {
                IconButton(onClick = { shareResult(context, result) }) {
                    Icon(Icons.Default.Share, "Share", tint = WadjetColors.Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Night),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Annotated image (zoomable)
            AnnotatedImageView(base64 = result.annotatedImageBase64)

            Spacer(modifier = Modifier.height(16.dp))

            // Detected glyphs
            if (result.glyphs.isNotEmpty()) {
                Text(
                    text = "Detected (${result.numDetections})",
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Gold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(result.glyphs) { glyph ->
                        GlyphChip(glyph)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transliteration
            val translit = result.transliteration
            if (!translit.isNullOrBlank()) {
                SectionLabel("Transliteration")
                Text(
                    text = translit,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                    ),
                    color = WadjetColors.Gold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // Translation toggle
            val translationEn = result.translationEn
            val translationAr = result.translationAr
            if (!translationEn.isNullOrBlank() || !translationAr.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SectionLabel(if (showArabic) "AR" else "EN")
                    Spacer(modifier = Modifier.weight(1f))
                    if (!translationAr.isNullOrBlank() && !translationEn.isNullOrBlank()) {
                        Surface(
                            onClick = { showArabic = !showArabic },
                            shape = RoundedCornerShape(8.dp),
                            color = WadjetColors.Gold.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = if (showArabic) "EN ↔ AR" else "AR ↔ EN",
                                color = WadjetColors.Gold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                Text(
                    text = if (showArabic) translationAr.orEmpty() else translationEn.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = WadjetColors.Text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .animateContentSize(),
                )
            }

            // Timing stats
            Spacer(modifier = Modifier.height(16.dp))
            TimingStats(result)

            Spacer(modifier = Modifier.height(24.dp))

            // Scan again
            WadjetButton(
                text = "Scan Again",
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnnotatedImageView(base64: String?) {
    val bitmap = rememberBase64Bitmap(base64)

    if (bitmap != null) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(WadjetColors.Surface)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 4f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Annotated scan result",
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                    ),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(WadjetColors.Surface),
            contentAlignment = Alignment.Center,
        ) {
            Text("No annotated image", color = WadjetColors.TextMuted)
        }
    }
}

@Composable
private fun GlyphChip(glyph: DetectedGlyph) {
    val unicode = gardinerToUnicode(glyph.gardinerCode)
    val confidence = (glyph.classConfidence * 100).toInt()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WadjetColors.Surface,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).width(72.dp),
        ) {
            Text(
                text = unicode,
                style = HieroglyphStyle.copy(fontSize = 32.sp),
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = glyph.gardinerCode,
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.TextMuted,
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { glyph.classConfidence },
                color = when {
                    confidence >= 90 -> WadjetColors.Gold
                    confidence >= 70 -> WadjetColors.GoldLight
                    else -> WadjetColors.Sand
                },
                trackColor = WadjetColors.Night,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
            )
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.TextMuted,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = WadjetColors.Gold,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun TimingStats(result: ScanResult) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WadjetColors.Surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Performance",
                style = MaterialTheme.typography.labelMedium,
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatItem("Detection", "${result.detectionMs}ms")
                StatItem("Classification", "${result.classificationMs}ms")
                StatItem("Total", "${result.totalMs}ms")
            }
            result.mode.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pipeline: ${result.pipeline ?: result.mode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = WadjetColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = WadjetColors.Text, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = WadjetColors.TextMuted)
    }
}

private fun shareResult(context: Context, result: ScanResult) {
    val text = buildString {
        append("Wadjet Scan Results\n\n")
        if (!result.transliteration.isNullOrBlank()) {
            append("Transliteration: ${result.transliteration}\n")
        }
        if (!result.translationEn.isNullOrBlank()) {
            append("Translation: ${result.translationEn}\n")
        }
        if (result.glyphs.isNotEmpty()) {
            append("Glyphs: ${result.glyphs.joinToString(" ") { it.gardinerCode }}\n")
        }
        append("\nScanned with Wadjet — Decode the Secrets of Ancient Egypt")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Scan Results"))
}
