package com.wadjet.feature.stories.screen

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import com.wadjet.feature.stories.DIFFICULTY_FILTERS
import com.wadjet.feature.stories.StoriesUiState

private const val FREE_STORY_LIMIT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    state: StoriesUiState,
    onDifficultySelected: (String) -> Unit,
    onStoryTap: (String) -> Unit,
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
                        text = "Stories",
                        color = WadjetColors.Gold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WadjetColors.Text,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Surface),
            )
        },
        modifier = modifier,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val filtered = state.filteredStories
                    items(
                        items = filtered,
                        key = { it.id },
                    ) { story ->
                        val index = state.stories.indexOf(story)
                        val isLocked = index >= FREE_STORY_LIMIT
                        val progress = state.progress[story.id]
                        StoryCard(
                            story = story,
                            progress = progress,
                            isLocked = isLocked,
                            onClick = { if (!isLocked) onStoryTap(story.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryCard(
    story: StorySummary,
    progress: StoryProgress?,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressFraction = if (progress != null && story.chapterCount > 0) {
        progress.chapterIndex.toFloat() / story.chapterCount
    } else {
        0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WadjetColors.Surface)
            .clickable(enabled = !isLocked, onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Cover glyph
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WadjetColors.SurfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = story.coverGlyph,
                    fontSize = 36.sp,
                )
            }

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
                    Text(
                        text = "${story.estimatedMinutes}min",
                        color = WadjetColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "${story.chapterCount} chapters",
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
                            text = "${(progressFraction * 100).toInt()}%",
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
                            contentDescription = "Locked",
                            tint = WadjetColors.TextMuted,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Premium",
                            color = WadjetColors.TextMuted,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                        )
                    }
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
