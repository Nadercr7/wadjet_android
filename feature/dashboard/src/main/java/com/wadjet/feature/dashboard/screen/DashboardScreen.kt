package com.wadjet.feature.dashboard.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.domain.model.DashboardStoryProgress
import com.wadjet.core.domain.model.FavoriteItem
import com.wadjet.core.domain.model.ScanHistoryItem
import com.wadjet.core.domain.model.UserStats
import com.wadjet.feature.dashboard.DashboardUiState
import com.wadjet.feature.dashboard.FAV_TABS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onFavTabSelected: (String) -> Unit,
    onRemoveFavorite: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = WadjetColors.Night,
        topBar = {
            TopAppBar(
                title = {
                    Text("Dashboard", color = WadjetColors.Gold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Text)
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = WadjetColors.Sand)
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
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // User header
                item {
                    UserHeader(
                        name = state.user?.displayName ?: "Explorer",
                        email = state.user?.email ?: "",
                        avatarUrl = state.user?.avatarUrl,
                    )
                }

                // Stat cards 2×2
                item { StatsGrid(stats = state.stats) }

                // Recent Scans
                if (state.recentScans.isNotEmpty()) {
                    item {
                        SectionLabel("Recent Scans")
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.recentScans.take(10), key = { it.id }) { scan ->
                                ScanCard(scan = scan)
                            }
                        }
                    }
                }

                // Favorites
                item {
                    SectionLabel("Favorites")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FAV_TABS.forEach { tab ->
                            FilterChip(
                                selected = state.selectedFavTab == tab,
                                onClick = { onFavTabSelected(tab) },
                                label = { Text(tab.replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WadjetColors.Gold,
                                    selectedLabelColor = WadjetColors.Night,
                                    containerColor = WadjetColors.Surface,
                                    labelColor = WadjetColors.TextMuted,
                                ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val filteredFavs = state.filteredFavorites
                if (filteredFavs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WadjetColors.Surface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No favorites yet", color = WadjetColors.TextMuted)
                        }
                    }
                } else {
                    items(filteredFavs, key = { it.id }) { fav ->
                        FavoriteRow(
                            favorite = fav,
                            onRemove = { onRemoveFavorite(fav.itemType, fav.itemId) },
                        )
                    }
                }

                // Story Progress
                if (state.storyProgress.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionLabel("Story Progress")
                    }
                    items(state.storyProgress, key = { it.storyId }) { sp ->
                        StoryProgressRow(progress = sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserHeader(
    name: String,
    email: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(WadjetColors.Surface)
                .border(2.dp, WadjetColors.Gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "W",
                color = WadjetColors.Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                color = WadjetColors.Text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = email,
                color = WadjetColors.TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun StatsGrid(
    stats: UserStats,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Scans Today", "${stats.scansToday}", Modifier.weight(1f))
            StatCard("Total Scans", "${stats.totalScans}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Stories Done", "${stats.storiesCompleted}", Modifier.weight(1f))
            StatCard("Glyphs Learned", "${stats.glyphsLearned}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WadjetColors.Surface)
            .border(1.dp, WadjetColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            color = WadjetColors.Gold,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = WadjetColors.TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ScanCard(
    scan: ScanHistoryItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(WadjetColors.Surface)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("𓀀", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${scan.glyphCount} glyphs",
            color = WadjetColors.Text,
            style = MaterialTheme.typography.labelSmall,
        )
        scan.confidenceAvg?.let {
            Text(
                text = "${(it * 100).toInt()}%",
                color = WadjetColors.Sand,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = WadjetColors.Gold,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WadjetColors.Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = WadjetColors.Gold,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = favorite.itemId,
                color = WadjetColors.Text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = favorite.itemType,
                color = WadjetColors.TextMuted,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = "Remove",
            color = WadjetColors.Error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable(onClick = onRemove),
        )
    }
}

@Composable
private fun StoryProgressRow(
    progress: DashboardStoryProgress,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WadjetColors.Surface)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = progress.storyId.replace("-", " ").replaceFirstChar { it.uppercase() },
                color = WadjetColors.Text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (progress.completed) {
                Text("✓ Done", color = WadjetColors.Success, style = MaterialTheme.typography.labelSmall)
            } else {
                Text("Ch ${progress.chapterIndex + 1}", color = WadjetColors.Sand, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (progress.completed) 1f else (progress.chapterIndex + 1).toFloat() / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = WadjetColors.Gold,
            trackColor = WadjetColors.Border,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Score: ${progress.score} · Glyphs: ${progress.glyphsLearned}",
            color = WadjetColors.TextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
