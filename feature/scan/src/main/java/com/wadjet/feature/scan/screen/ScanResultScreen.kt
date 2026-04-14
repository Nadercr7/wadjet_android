package com.wadjet.feature.scan.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.shineSweep
import com.wadjet.core.designsystem.component.BadgeVariant
import com.wadjet.core.designsystem.component.TtsButton
import com.wadjet.core.designsystem.component.TtsState
import com.wadjet.core.designsystem.component.WadjetBadge
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.domain.model.DetectedGlyph
import com.wadjet.core.domain.model.ScanResult
import com.wadjet.feature.scan.util.gardinerToUnicode
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanResultScreen(
    result: ScanResult,
    ttsStates: Map<String, TtsState>,
    onSpeak: (key: String, text: String, lang: String) -> Unit,
    onScanAgain: () -> Unit,
    onNavigateToDictionarySign: (code: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var showArabic by remember { mutableStateOf(false) }
    var selectedGlyph by remember { mutableStateOf<DetectedGlyph?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Haptic confirm on result arrival
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
    }

    // Stagger animation
    var visibleSections by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { repeat(10) { delay(120); visibleSections++ } }

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
            // AI Unverified warning
            if (result.aiUnverified) {
                FadeUp(visible = visibleSections >= 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WadjetColors.Warning.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = WadjetColors.Warning,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI verification unavailable — results may be less accurate",
                                style = MaterialTheme.typography.bodySmall,
                                color = WadjetColors.Warning,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Quality hints (shown when 0 detections)
            if (result.qualityHints.isNotEmpty()) {
                FadeUp(visible = visibleSections >= 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WadjetColors.Surface,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tips", style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                            Spacer(modifier = Modifier.height(4.dp))
                            result.qualityHints.forEach { hint ->
                                Text("• $hint", style = MaterialTheme.typography.bodySmall, color = WadjetColors.Text)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence badge + reading direction/layout badges
            FadeUp(visible = visibleSections >= 1) {
                val avgConf = if (result.glyphs.isNotEmpty()) {
                    (result.glyphs.sumOf { (it.classConfidence * 100).toInt() } / result.glyphs.size)
                } else 0
                val variant = when {
                    avgConf >= 85 -> BadgeVariant.Success
                    avgConf >= 60 -> BadgeVariant.Gold
                    else -> BadgeVariant.Error
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Pipeline source badge
                    result.detectionSource?.let { source ->
                        val sourceLabel = when {
                            source.contains("gemini", ignoreCase = true) -> "AI Vision (Gemini)"
                            source.contains("ai_vision", ignoreCase = true) -> "AI Vision"
                            source.contains("onnx_fallback", ignoreCase = true) -> "ONNX (Fallback)"
                            source.contains("onnx", ignoreCase = true) && source.contains("ai", ignoreCase = true) -> "ONNX + AI Verified"
                            source.contains("onnx", ignoreCase = true) -> "ONNX"
                            else -> source.replaceFirstChar { it.uppercase() }
                        }
                        WadjetBadge(text = "Detected by: $sourceLabel", variant = BadgeVariant.Gold)
                    }
                    WadjetBadge(text = "Confidence: $avgConf%", variant = variant)
                    result.readingDirection?.let { dir ->
                        WadjetBadge(text = dir.replaceFirstChar { it.uppercase() }, variant = BadgeVariant.Muted)
                    }
                    result.layoutMode?.let { mode ->
                        WadjetBadge(text = mode.replaceFirstChar { it.uppercase() }, variant = BadgeVariant.Muted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence summary card
            result.confidenceSummary?.let { cs ->
                FadeUp(visible = visibleSections >= 2) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WadjetColors.Surface,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            StatItem("Avg", "${(cs.avg * 100).toInt()}%")
                            StatItem("Min", "${(cs.min * 100).toInt()}%")
                            StatItem("Max", "${(cs.max * 100).toInt()}%")
                            if (cs.lowCount > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${cs.lowCount}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WadjetColors.Error,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text("Low", style = MaterialTheme.typography.labelSmall, color = WadjetColors.TextMuted)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Detected glyphs — FlowRow grid
            if (result.glyphs.isNotEmpty()) {
                FadeUp(visible = visibleSections >= 3) {
                    Column {
                        GoldDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Detected (${result.numDetections})",
                            style = MaterialTheme.typography.titleMedium,
                            color = WadjetColors.Gold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            result.glyphs.forEach { glyph ->
                                GlyphChip(
                                    glyph = glyph,
                                    onClick = { selectedGlyph = glyph },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transliteration + Gardiner sequence
            val translit = result.transliteration
            if (!translit.isNullOrBlank()) {
                FadeUp(visible = visibleSections >= 4) {
                    Column {
                        GoldDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SectionLabel("Transliteration")
                            TtsButton(
                                state = ttsStates["translit"] ?: TtsState.IDLE,
                                onClick = { onSpeak("translit", translit, "en") },
                            )
                        }
                        Text(
                            text = translit,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                            ),
                            color = WadjetColors.Gold,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        result.gardinerSequence?.let { seq ->
                            Text(
                                text = "Gardiner: $seq",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = WadjetColors.Sand,
                            )
                        }
                    }
                }
            }

            // Translation
            val translationEn = result.translationEn
            val translationAr = result.translationAr
            if (!translationEn.isNullOrBlank() || !translationAr.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                FadeUp(visible = visibleSections >= 5) {
                    Column {
                        GoldDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            SectionLabel(if (showArabic) "AR" else "EN")
                            TtsButton(
                                state = ttsStates[if (showArabic) "ar" else "en"] ?: TtsState.IDLE,
                                onClick = {
                                    if (showArabic) onSpeak("ar", translationAr.orEmpty(), "ar")
                                    else onSpeak("en", translationEn.orEmpty(), "en")
                                },
                            )
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

                        if (showArabic) {
                            CompositionLocalProvider(
                                LocalLayoutDirection provides LayoutDirection.Rtl,
                            ) {
                                Text(
                                    text = translationAr.orEmpty(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = WadjetColors.Text,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .animateContentSize(),
                                )
                            }
                        } else {
                            Text(
                                text = translationEn.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = WadjetColors.Text,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .animateContentSize(),
                            )
                        }
                    }
                }
            }

            // AI Notes card (collapsible)
            val aiNotes = result.aiNotes
            if (!aiNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                FadeUp(visible = visibleSections >= 6) {
                    var aiNotesExpanded by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WadjetColors.Gold.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .animateContentSize(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { aiNotesExpanded = !aiNotesExpanded },
                            ) {
                                SectionLabel("AI Notes")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (aiNotesExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (aiNotesExpanded) "Collapse" else "Expand",
                                    tint = WadjetColors.Gold,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            if (aiNotesExpanded) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WadjetColors.Text,
                                )
                            }
                        }
                    }
                }
            }

            // Timing stats
            Spacer(modifier = Modifier.height(16.dp))
            FadeUp(visible = visibleSections >= 7) {
                Column {
                    GoldDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    TimingStats(result)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FadeUp(visible = visibleSections >= 8) {
                WadjetButton(
                    text = "Scan Again",
                    onClick = onScanAgain,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Glyph detail bottom sheet
        selectedGlyph?.let { glyph ->
            GlyphDetailSheet(
                glyph = glyph,
                onDismiss = { selectedGlyph = null },
                onViewInDictionary = {
                    selectedGlyph = null
                    onNavigateToDictionarySign(glyph.gardinerCode)
                },
                sheetState = sheetState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlyphDetailSheet(
    glyph: DetectedGlyph,
    onDismiss: () -> Unit,
    onViewInDictionary: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
) {
    val unicode = gardinerToUnicode(glyph.gardinerCode)
    val confidence = (glyph.classConfidence * 100).toInt()
    val detConfidence = (glyph.detectionConfidence * 100).toInt()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WadjetColors.Surface,
        contentColor = WadjetColors.Text,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Large glyph unicode
            Text(
                text = unicode,
                style = HieroglyphStyle.copy(fontSize = 64.sp),
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Gardiner code
            Text(
                text = glyph.gardinerCode,
                style = MaterialTheme.typography.titleLarge,
                color = WadjetColors.Sand,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Unicode codepoint
            if (unicode != glyph.gardinerCode) {
                val codepoint = unicode.codePointAt(0)
                Text(
                    text = "U+${codepoint.toString(16).uppercase().padStart(5, '0')}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = WadjetColors.TextMuted,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Confidence bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ConfidenceBar(label = "Classification", value = confidence, fraction = glyph.classConfidence)
                ConfidenceBar(label = "Detection", value = detConfidence, fraction = glyph.detectionConfidence)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // View in Dictionary button
            WadjetButton(
                text = "View in Dictionary",
                onClick = onViewInDictionary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConfidenceBar(label: String, value: Int, fraction: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp),
    ) {
        Text(
            text = "$value%",
            style = MaterialTheme.typography.titleMedium,
            color = when {
                value >= 70 -> WadjetColors.Success
                value >= 40 -> WadjetColors.Gold
                else -> WadjetColors.Error
            },
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = when {
                value >= 70 -> WadjetColors.Success
                value >= 40 -> WadjetColors.Gold
                else -> WadjetColors.Error
            },
            trackColor = WadjetColors.Night,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WadjetColors.TextMuted,
        )
    }
}

@Composable
private fun GlyphChip(glyph: DetectedGlyph, onClick: () -> Unit) {
    val unicode = gardinerToUnicode(glyph.gardinerCode)
    val confidence = (glyph.classConfidence * 100).toInt()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WadjetColors.Surface,
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).width(80.dp),
        ) {
            Text(
                text = unicode,
                style = HieroglyphStyle.copy(fontSize = 32.sp),
                color = WadjetColors.Gold,
                modifier = Modifier.semantics { contentDescription = "Hieroglyph ${glyph.gardinerCode}" },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = glyph.gardinerCode,
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.Sand,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { glyph.classConfidence },
                color = when {
                    confidence > 70 -> WadjetColors.Success
                    confidence >= 40 -> WadjetColors.Gold
                    else -> WadjetColors.Error
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
private fun GoldDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = WadjetColors.Gold.copy(alpha = 0.2f),
    )
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
                StatItem("Detection", "${result.detectionMs.toLong()}ms")
                StatItem("Classification", "${result.classificationMs.toLong()}ms")
                StatItem("Total", "${result.totalMs.toLong()}ms")
            }
            result.mode.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Source: ${result.detectionSource ?: result.mode}",
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
