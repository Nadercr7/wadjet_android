package com.wadjet.app.screen

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wadjet.app.R
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.core.domain.model.Sign

@Composable
fun HieroglyphsHubScreen(
    state: HieroglyphsHubUiState,
    onNavigateToScan: () -> Unit,
    onNavigateToDictionary: () -> Unit,
    onNavigateToWrite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.hub_title),
            style = MaterialTheme.typography.headlineMedium,
            color = WadjetColors.Gold,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.hub_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = WadjetColors.TextMuted,
        )

        // Recent Scans carousel
        if (state.recentScans.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionHeader(stringResource(R.string.hub_recent_scans))
            Spacer(Modifier.height(8.dp))
            RecentScansRow(scans = state.recentScans, onScanClick = onNavigateToScan)
        }

        // Learning Progress card
        Spacer(Modifier.height(24.dp))
        FadeUp(visible = true) {
            LearningProgressCard(
                totalSigns = state.totalSigns,
                isLoading = state.isLoading,
            )
        }

        // Suggested Signs
        if (state.suggestedSigns.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionHeader(stringResource(R.string.hub_suggested_signs))
            Spacer(Modifier.height(8.dp))
            SuggestedSignsRow(signs = state.suggestedSigns, onClick = onNavigateToDictionary)
        }

        Spacer(Modifier.height(24.dp))
        SectionHeader(stringResource(R.string.hub_tools))
        Spacer(Modifier.height(8.dp))

        FadeUp(visible = true) {
            HubCard(
                icon = Icons.Outlined.CameraAlt,
                title = stringResource(R.string.hub_scan_title),
                description = stringResource(R.string.hub_scan_desc),
                onClick = onNavigateToScan,
            )
        }

        Spacer(Modifier.height(12.dp))

        FadeUp(visible = true) {
            HubCard(
                icon = Icons.Outlined.MenuBook,
                title = stringResource(R.string.hub_dictionary_title),
                description = stringResource(R.string.hub_dictionary_desc),
                onClick = onNavigateToDictionary,
            )
        }

        Spacer(Modifier.height(12.dp))

        FadeUp(visible = true) {
            HubCard(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.hub_write_title),
                description = stringResource(R.string.hub_write_desc),
                onClick = onNavigateToWrite,
            )
        }

        Spacer(Modifier.height(24.dp))

        // How Scanning Works explainer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WadjetColors.Surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.hub_how_scanning_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                ExplainerStep(number = "1", text = stringResource(R.string.hub_how_step1))
                Spacer(Modifier.height(6.dp))
                ExplainerStep(number = "2", text = stringResource(R.string.hub_how_step2))
                Spacer(Modifier.height(6.dp))
                ExplainerStep(number = "3", text = stringResource(R.string.hub_how_step3))
                Spacer(Modifier.height(6.dp))
                ExplainerStep(number = "4", text = stringResource(R.string.hub_how_step4))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = WadjetColors.Gold,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun RecentScansRow(
    scans: List<ScanHistorySummary>,
    onScanClick: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(scans, key = { it.id }) { scan ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WadjetColors.Surface,
                modifier = Modifier
                    .width(140.dp)
                    .clickable(onClick = onScanClick),
            ) {
                Column {
                    AsyncImage(
                        model = scan.thumbnailPath,
                        contentDescription = stringResource(R.string.hub_scan_thumbnail_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = scan.gardinerSequence ?: scan.transliteration ?: stringResource(R.string.hub_scan_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = WadjetColors.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.hub_glyph_count, scan.glyphCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = WadjetColors.TextMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningProgressCard(
    totalSigns: Int,
    isLoading: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = WadjetColors.Surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.hub_progress_title),
                style = MaterialTheme.typography.titleSmall,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hub_signs_in_dictionary, totalSigns),
                style = MaterialTheme.typography.bodyMedium,
                color = WadjetColors.Text,
            )
            Spacer(Modifier.height(8.dp))
            if (!isLoading) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = WadjetColors.Gold,
                    trackColor = WadjetColors.Gold.copy(alpha = 0.15f),
                )
            }
        }
    }
}

@Composable
private fun SuggestedSignsRow(
    signs: List<Sign>,
    onClick: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(signs, key = { it.code }) { sign ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WadjetColors.Surface,
                modifier = Modifier
                    .width(120.dp)
                    .clickable(onClick = onClick),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Text(
                        text = sign.glyph,
                        fontFamily = NotoSansEgyptianHieroglyphs,
                        fontSize = 36.sp,
                        color = WadjetColors.Gold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = sign.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.TextMuted,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = sign.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.Text,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun HubCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = WadjetColors.Surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WadjetColors.Gold.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WadjetColors.Gold,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun ExplainerStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = RoundedCornerShape(50),
            color = WadjetColors.Gold.copy(alpha = 0.15f),
            modifier = Modifier.size(22.dp),
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 7.dp, top = 3.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = WadjetColors.TextMuted,
            modifier = Modifier.weight(1f),
        )
    }
}
