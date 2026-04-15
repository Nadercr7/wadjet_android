package com.wadjet.feature.landing.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.landing.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.BorderBeam
import com.wadjet.core.designsystem.animation.DotPattern
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.GoldGradientText
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetCard
import com.wadjet.feature.landing.LandingUiState
import kotlinx.coroutines.delay

@Composable
fun LandingScreen(
    state: LandingUiState,
    onNavigateToScan: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToDictionary: () -> Unit,
    onNavigateToWrite: () -> Unit,
    onNavigateToIdentify: () -> Unit,
    onNavigateToStories: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToStoryReader: (String) -> Unit,
) {
    // Staggered reveal
    var visibleSections by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..7) {
            delay(120L)
            visibleSections = i
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 1. Greeting + title
        item {
            FadeUp(visible = visibleSections >= 1) {
                Column {
                    val greeting = if (state.userName != null) {
                        stringResource(R.string.landing_greeting_name, state.userName!!)
                    } else {
                        stringResource(R.string.landing_greeting)
                    }
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = WadjetColors.TextMuted,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GoldGradientText(
                        text = stringResource(DesignR.string.app_name_display),
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.landing_tagline),
                        style = MaterialTheme.typography.titleMedium,
                        color = WadjetColors.Sand,
                    )
                }
            }
        }

        // 2. Hieroglyphs path card
        item {
            FadeUp(visible = visibleSections >= 2) {
                PathCardRich(
                    glyph = "𓂀",
                    title = stringResource(R.string.landing_path_hiero_title),
                    subtitle = stringResource(R.string.landing_path_hiero_subtitle),
                    features = listOf(
                        stringResource(R.string.landing_feature_scan),
                        stringResource(R.string.landing_feature_translate),
                        stringResource(R.string.landing_feature_dictionary),
                        stringResource(R.string.landing_feature_write),
                    ),
                    buttonText = stringResource(R.string.landing_start_scanning),
                    onClick = onNavigateToScan,
                )
            }
        }

        // 3. Landmarks path card
        item {
            FadeUp(visible = visibleSections >= 3) {
                PathCardRich(
                    glyph = "𓉐",
                    title = stringResource(R.string.landing_path_landmark_title),
                    subtitle = stringResource(R.string.landing_path_landmark_subtitle),
                    features = listOf(
                        stringResource(R.string.landing_feature_explore),
                        stringResource(R.string.landing_feature_identify),
                        stringResource(R.string.landing_feature_ai_desc),
                        stringResource(R.string.landing_feature_discover),
                    ),
                    buttonText = stringResource(R.string.landing_start_exploring),
                    onClick = onNavigateToExplore,
                )
            }
        }

        // 4. Quick actions 2×2 grid
        item {
            FadeUp(visible = visibleSections >= 4) {
                Column {
                    Text(
                        text = stringResource(R.string.landing_quick_actions),
                        style = MaterialTheme.typography.titleMedium,
                        color = WadjetColors.TextMuted,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        QuickAction(Modifier.weight(1f), "𓂀", stringResource(R.string.landing_quick_scan), onNavigateToScan)
                        QuickAction(Modifier.weight(1f), "𓊹", stringResource(R.string.landing_quick_dictionary), onNavigateToDictionary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        QuickAction(Modifier.weight(1f), "�", stringResource(R.string.landing_quick_write), onNavigateToWrite)
                        QuickAction(Modifier.weight(1f), "𓇯", stringResource(R.string.landing_quick_explore), onNavigateToExplore)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        QuickAction(Modifier.weight(1f), "𓉐", stringResource(R.string.landing_quick_identify), onNavigateToIdentify)
                        QuickAction(Modifier.weight(1f), "𓁟", stringResource(R.string.landing_quick_stories), onNavigateToStories)
                    }
                }
            }
        }

        // 5. Continue section (conditional)
        val hasRecent = state.recentScan != null
        val hasStory = state.inProgressStory != null
        if (hasRecent || hasStory) {
            item {
                FadeUp(visible = visibleSections >= 5) {
                    Column {
                        Text(
                            text = stringResource(R.string.landing_continue_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = WadjetColors.TextMuted,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            state.recentScan?.let { scan ->
                                item {
                                    ContinueScanCard(
                                        scan = scan,
                                        onClick = onNavigateToScan,
                                    )
                                }
                            }
                            state.inProgressStory?.let { story ->
                                item {
                                    ContinueStoryCard(
                                        story = story,
                                        chapter = state.inProgressStoryChapter,
                                        onClick = { onNavigateToStoryReader(story.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Thoth quick chat
        item {
            FadeUp(visible = visibleSections >= 6) {
                ThothChatEntry(onClick = onNavigateToChat)
            }
        }

        // 7. Footer
        item {
            FadeUp(visible = visibleSections >= 7) {
                Text(
                    text = stringResource(DesignR.string.footer_credit),
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.Dust,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PathCardRich(
    glyph: String,
    title: String,
    subtitle: String,
    features: List<String>,
    buttonText: String,
    onClick: () -> Unit,
) {
    BorderBeam {
        Box {
            DotPattern(
                modifier = Modifier.matchParentSize(),
                baseAlpha = 0.04f,
                glowAlpha = 0.1f,
            )
            Column(modifier = Modifier.padding(20.dp)) {
                // Icon in a gold/10 rounded box
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = WadjetColors.Gold.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = glyph,
                            fontSize = 28.sp,
                            fontFamily = NotoSansEgyptianHieroglyphs,
                            color = WadjetColors.Gold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WadjetColors.TextMuted,
                )
                Spacer(modifier = Modifier.height(16.dp))
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "●",
                            color = WadjetColors.Gold.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WadjetColors.Text,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                WadjetButton(
                    text = buttonText,
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    onClick: () -> Unit,
) {
    WadjetCard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = WadjetColors.Gold.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = icon,
                        fontSize = 22.sp,
                        fontFamily = NotoSansEgyptianHieroglyphs,
                        color = WadjetColors.Gold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = WadjetColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ContinueScanCard(
    scan: com.wadjet.core.domain.model.ScanHistorySummary,
    onClick: () -> Unit,
) {
    WadjetCard(
        modifier = Modifier.width(220.dp),
        onClick = onClick,
    ) {
        Column {
            AsyncImage(
                model = scan.thumbnailPath,
                contentDescription = stringResource(R.string.landing_recent_scan_desc),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(DesignR.drawable.ic_placeholder_glyph),
                error = painterResource(DesignR.drawable.ic_placeholder_error),
                fallback = painterResource(DesignR.drawable.ic_placeholder_glyph),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = scan.transliteration ?: stringResource(R.string.landing_scan_result_fallback),
                    style = MaterialTheme.typography.titleSmall,
                    color = WadjetColors.Gold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.landing_glyphs_detected, scan.glyphCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun ContinueStoryCard(
    story: com.wadjet.core.domain.model.StorySummary,
    chapter: Int,
    onClick: () -> Unit,
) {
    WadjetCard(
        modifier = Modifier.width(220.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = story.coverGlyph,
                fontSize = 32.sp,
                fontFamily = NotoSansEgyptianHieroglyphs,
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = story.titleEn,
                style = MaterialTheme.typography.titleSmall,
                color = WadjetColors.Gold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Chapter ${chapter + 1} of ${story.chapterCount}",
                style = MaterialTheme.typography.bodySmall,
                color = WadjetColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ThothChatEntry(onClick: () -> Unit) {
    WadjetCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = WadjetColors.Gold.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "𓅝",
                        fontSize = 22.sp,
                        fontFamily = NotoSansEgyptianHieroglyphs,
                        color = WadjetColors.Gold,
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.landing_thoth_prompt),
                    style = MaterialTheme.typography.titleSmall,
                    color = WadjetColors.Text,
                )
                Text(
                    text = stringResource(R.string.landing_thoth_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
            }
            Text(
                text = "→",
                color = WadjetColors.Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
