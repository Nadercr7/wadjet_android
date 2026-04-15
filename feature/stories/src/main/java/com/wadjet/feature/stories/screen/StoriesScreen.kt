package com.wadjet.feature.stories.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.shineSweep
import com.wadjet.core.designsystem.component.EmptyState
import com.wadjet.core.designsystem.component.ErrorState
import com.wadjet.core.designsystem.component.ShimmerCardList
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import com.wadjet.core.ui.LocalAnimatedVisibilityScope
import com.wadjet.core.ui.LocalSharedTransitionScope
import com.wadjet.feature.stories.DIFFICULTY_FILTERS
import com.wadjet.feature.stories.R
import com.wadjet.feature.stories.StoriesUiState
import kotlinx.coroutines.delay

private const val FREE_STORY_LIMIT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    state: StoriesUiState,
    onDifficultySelected: (String) -> Unit,
    onStoryTap: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = WadjetColors.Night,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stories_title),
                        color = WadjetColors.Gold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DesignR.string.action_back),
                            tint = WadjetColors.Text,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Surface),
            )
        },
        modifier = modifier,
    ) { padding ->
        val pullState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = pullState,
            indicator = {
                androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = state.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = WadjetColors.Gold,
                )
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Difficulty filter chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(DIFFICULTY_FILTERS) { difficulty ->
                        val selected = state.selectedDifficulty == difficulty
                        val bgColor = animateColorAsState(
                            if (selected) WadjetColors.Gold else WadjetColors.Surface,
                            label = "chipBg",
                        )
                        FilterChip(
                            selected = selected,
                            onClick = { onDifficultySelected(difficulty) },
                            label = {
                                Text(
                                    text = difficulty,
                                    color = if (selected) WadjetColors.Night else WadjetColors.Text,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WadjetColors.Gold,
                                containerColor = WadjetColors.Surface,
                            ),
                        )
                    }
                }

                // Story list
                val filtered = state.filteredStories
                var visibleCount by remember { mutableIntStateOf(0) }
                LaunchedEffect(filtered.size) {
                    visibleCount = 0
                    for (i in filtered.indices) {
                        delay(120)
                        visibleCount = i + 1
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.isLoading && filtered.isEmpty()) {
                        item {
                            ShimmerCardList(
                                itemCount = 5,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    } else if (state.error != null && filtered.isEmpty()) {
                        item {
                            ErrorState(
                                message = state.error ?: stringResource(R.string.stories_error),
                                onRetry = onRefresh,
                                modifier = Modifier.fillParentMaxHeight(0.6f),
                            )
                        }
                    } else if (filtered.isEmpty() && !state.isLoading) {
                        item {
                            EmptyState(
                                glyph = "\uD80C\uDC5F",
                                title = stringResource(R.string.stories_empty_title),
                                subtitle = stringResource(R.string.stories_empty_subtitle),
                                modifier = Modifier.fillParentMaxHeight(0.6f),
                            )
                        }
                    }
                    items(
                        items = filtered,
                        key = { it.id },
                    ) { story ->
                        val index = state.stories.indexOf(story)
                        val itemIndex = filtered.indexOf(story)
                        val isLocked = index >= FREE_STORY_LIMIT
                        val progress = state.progress[story.id]
                        FadeUp(visible = itemIndex < visibleCount) {
                            StoryCard(
                                story = story,
                                progress = progress,
                                isLocked = isLocked,
                                isFavorite = story.id in state.favorites,
                                onClick = { if (!isLocked) onStoryTap(story.id) },
                                onToggleFavorite = { onToggleFavorite(story.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun StoryCard(
    story: StorySummary,
    progress: StoryProgress?,
    isLocked: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressFraction = if (progress != null && story.chapterCount > 0) {
        progress.chapterIndex.toFloat() / story.chapterCount
    } else {
        0f
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(WadjetColors.Surface)
            .clickable(enabled = !isLocked, onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Cover glyph
            with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "story-${story.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        Brush.verticalGradient(
                            colors = when (story.difficulty.lowercase()) {
                                "beginner" -> listOf(WadjetColors.DifficultyBeginner, WadjetColors.DifficultyBeginnerDark)
                                "intermediate" -> listOf(WadjetColors.DifficultyIntermediate, WadjetColors.DifficultyIntermediateDark)
                                "advanced" -> listOf(WadjetColors.DifficultyAdvanced, WadjetColors.DifficultyAdvancedDark)
                                else -> listOf(WadjetColors.DifficultyBeginner, WadjetColors.DifficultyBeginnerDark)
                            },
                        ),
                    )
                    .shineSweep(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = story.coverGlyph,
                    fontSize = 36.sp,
                    fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs,
                )
            }
            } // end with(sharedTransitionScope)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.titleEn,
                    color = if (isLocked) WadjetColors.TextMuted else WadjetColors.Text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DifficultyBadge(story.difficulty)
                    if (story.glyphsTaught.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.stories_glyph_count, story.glyphsTaught.size),
                            color = WadjetColors.Gold,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WadjetColors.Gold.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.stories_estimated_time, story.estimatedMinutes),
                        color = WadjetColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(R.string.stories_chapter_count, story.chapterCount),
                        color = WadjetColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (!isLocked && progressFraction > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = WadjetColors.Gold,
                            trackColor = WadjetColors.Border,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.stories_progress_pct, (progressFraction * 100).toInt()),
                            color = WadjetColors.Sand,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (isLocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.stories_locked_desc),
                            tint = WadjetColors.TextMuted,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.stories_premium),
                            color = WadjetColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
            }

            // Favorite button
            if (!isLocked) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) stringResource(DesignR.string.action_remove_favorite) else stringResource(DesignR.string.action_add_favorite),
                        tint = if (isFavorite) WadjetColors.Error else WadjetColors.TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: String, modifier: Modifier = Modifier) {
    val color = when (difficulty.lowercase()) {
        "beginner" -> WadjetColors.Success
        "intermediate" -> WadjetColors.Warning
        "advanced" -> WadjetColors.Error
        else -> WadjetColors.TextMuted
    }
    Text(
        text = difficulty.replaceFirstChar { it.uppercase() },
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
