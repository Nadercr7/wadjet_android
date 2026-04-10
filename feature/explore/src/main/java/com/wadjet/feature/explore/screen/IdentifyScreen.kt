package com.wadjet.feature.explore.screen

// CAMERA_DISABLED: CameraX imports commented out for image-upload-only mode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.component.BadgeVariant
import com.wadjet.core.designsystem.component.ImageUploadZone
import com.wadjet.core.designsystem.component.WadjetBadge
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetTextButton
import com.wadjet.core.domain.model.IdentifyMatch
import com.wadjet.core.domain.model.IdentifyResult
import com.wadjet.feature.explore.IdentifyUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifyScreen(
    state: IdentifyUiState,
    onImageCaptured: (java.io.File) -> Unit,
    onImageSelected: (android.net.Uri) -> Unit,
    onMatchTap: (String) -> Unit,
    onViewDetails: (String) -> Unit,
    onAskThoth: (String) -> Unit,
    onIdentifyAnother: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(WadjetColors.Night)) {
        // Main content: Image upload zone centered
        if (state.result == null && !state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                FadeUp(visible = true) {
                    ImageUploadZone(
                        onImageSelected = onImageSelected,
                        title = "Upload a photo of an Egyptian landmark",
                        subtitle = "Supports JPG, PNG up to 10MB",
                        analyzeButtonText = "Identify Landmark",
                        isAnalyzing = state.isLoading,
                        onAnalyze = null,
                    )
                }
            }
        }

        // Top bar
        TopAppBar(
            title = { Text("Identify Landmark", color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        // Loading overlay
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WadjetColors.Night.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    com.wadjet.core.designsystem.component.WadjetSectionLoader(
                        text = "Identifying landmark...",
                    )
                }
            }
        }

        // Results
        if (state.result != null && !state.isLoading) {
            IdentifyResults(
                result = state.result,
                onMatchTap = onMatchTap,
                onViewDetails = onViewDetails,
                onAskThoth = onAskThoth,
                onIdentifyAnother = onIdentifyAnother,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        // Error
        state.error?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                com.wadjet.core.designsystem.component.ErrorState(
                    message = error ?: "Couldn't identify this landmark. Try another photo",
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun IdentifyResults(
    result: IdentifyResult,
    onMatchTap: (String) -> Unit,
    onViewDetails: (String) -> Unit,
    onAskThoth: (String) -> Unit,
    onIdentifyAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topMatch = result.topMatch
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = WadjetColors.Surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState),
        ) {
            if (topMatch == null || result.matches.isEmpty()) {
                // No result
                Text(
                    text = "No landmarks identified",
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Try a different angle or get closer to the landmark.",
                    color = WadjetColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                WadjetButton(text = "Identify Another", onClick = onIdentifyAnother)
                return@Column
            }

            // ─── Top match name + large confidence ───
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topMatch.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = WadjetColors.Gold,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                // Large confidence badge
                ConfidenceBadge(confidence = topMatch.confidence)
            }

            Spacer(Modifier.height(8.dp))

            // ─── Source chip + Agreement badge ───
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                result.source?.let { source ->
                    SourceChip(source = source)
                }
                result.agreement?.let { agreement ->
                    AgreementBadge(agreement = agreement)
                }
            }

            // ─── Warnings ───
            if (result.isEgyptian == false) {
                Spacer(Modifier.height(8.dp))
                WarningBanner(
                    icon = Icons.Default.Warning,
                    text = "This may not be an Egyptian landmark",
                    color = WadjetColors.Warning,
                )
            }
            if (result.isKnownLandmark == false) {
                Spacer(Modifier.height(8.dp))
                WarningBanner(
                    icon = Icons.Default.Info,
                    text = "This landmark is not in our database yet",
                    color = WadjetColors.Sand,
                )
            }

            // ─── Description card ───
            val descriptionText = result.description
            if (!descriptionText.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WadjetColors.Night,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WadjetColors.Text,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            // ─── Top-3 matches with progress bars ───
            if (result.matches.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Top Matches",
                    style = MaterialTheme.typography.titleSmall,
                    color = WadjetColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                result.matches.forEachIndexed { index, match ->
                    FadeUp(visible = true) {
                        MatchRow(
                            match = match,
                            rank = index + 1,
                            onClick = { onMatchTap(match.slug) },
                        )
                    }
                    if (index < result.matches.lastIndex) Spacer(Modifier.height(6.dp))
                }
            }

            // ─── Action buttons ───
            Spacer(Modifier.height(16.dp))
            if (topMatch.slug.isNotBlank() && result.isKnownLandmark) {
                WadjetButton(
                    text = "View Full Details",
                    onClick = { onViewDetails(topMatch.slug) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WadjetColors.Gold.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAskThoth(topMatch.slug) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = WadjetColors.Gold,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Ask Thoth about this landmark",
                            style = MaterialTheme.typography.labelLarge,
                            color = WadjetColors.Gold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            WadjetTextButton(
                text = "Identify Another",
                onClick = onIdentifyAnother,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─── Sub-composables ───

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val pct = (confidence * 100).toInt()
    val (bg, fg) = when {
        pct >= 80 -> WadjetColors.Success.copy(alpha = 0.15f) to WadjetColors.Success
        pct >= 50 -> WadjetColors.Warning.copy(alpha = 0.15f) to WadjetColors.Warning
        else -> WadjetColors.Error.copy(alpha = 0.15f) to WadjetColors.Error
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
    ) {
        Text(
            text = "$pct%",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            color = fg,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SourceChip(source: String) {
    val label = when (source.lowercase()) {
        "ensemble" -> "Ensemble"
        "onnx" -> "ONNX"
        "gemini" -> "Gemini"
        "grok" -> "Grok"
        "groq" -> "Groq"
        "cloudflare" -> "Cloudflare"
        else -> source.replaceFirstChar { it.uppercase() }
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = WadjetColors.SurfaceAlt,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = WadjetColors.Sand,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AgreementBadge(agreement: String) {
    val (text, variant) = when (agreement.lowercase()) {
        "full" -> "Verified ✓" to BadgeVariant.Success
        "partial" -> "Consensus" to BadgeVariant.Gold
        "tiebreak" -> "Uncertain" to BadgeVariant.Muted
        "single" -> "Single Source" to BadgeVariant.Muted
        "best_confidence" -> "Best Guess" to BadgeVariant.Muted
        else -> agreement.replaceFirstChar { it.uppercase() } to BadgeVariant.Muted
    }
    WadjetBadge(text = text, variant = variant)
}

@Composable
private fun WarningBanner(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Composable
private fun MatchRow(
    match: IdentifyMatch,
    rank: Int,
    onClick: () -> Unit,
) {
    val confPct = (match.confidence * 100).toInt()
    val barColor = when {
        confPct >= 80 -> WadjetColors.Success
        confPct >= 50 -> WadjetColors.Warning
        else -> WadjetColors.Error
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = WadjetColors.Night,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank
                Surface(
                    shape = CircleShape,
                    color = if (rank == 1) WadjetColors.Gold else WadjetColors.Surface,
                    modifier = Modifier.size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "$rank",
                            fontSize = 12.sp,
                            color = if (rank == 1) WadjetColors.Night else WadjetColors.TextMuted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WadjetColors.Text,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    match.source?.let { src ->
                        Text(
                            text = src.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = WadjetColors.TextMuted,
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$confPct%",
                    style = MaterialTheme.typography.labelMedium,
                    color = barColor,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { match.confidence.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = barColor,
                trackColor = WadjetColors.SurfaceAlt,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}
