package com.wadjet.feature.explore.screen

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.core.designsystem.WadjetColors
import androidx.compose.ui.res.painterResource
import com.wadjet.core.designsystem.animation.goldPulse
import com.wadjet.core.designsystem.animation.shineSweep
import com.wadjet.core.designsystem.component.ShimmerCardList
import com.wadjet.core.domain.model.Landmark
import com.wadjet.feature.explore.ExploreUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreUiState,
    onCategorySelected: (String) -> Unit,
    onCitySelected: (String?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onLandmarkTap: (String) -> Unit,
    onToggleFavorite: (Landmark) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onIdentify: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Detect scroll to bottom for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3 && totalItems > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }.collect { if (it) onLoadMore() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Explore", color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            actions = {
                IconButton(
                    onClick = onIdentify,
                    modifier = Modifier.goldPulse(),
                ) {
                    Icon(Icons.Default.FileUpload, "Identify from photo", tint = WadjetColors.Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Night),
        )

        // Search bar
        SearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchChanged,
        )

        // Category chips
        CategoryChips(
            categories = state.categories,
            selected = state.selectedCategory,
            onSelect = onCategorySelected,
        )

        // City filter
        if (state.cities.isNotEmpty()) {
            CityFilter(
                cities = state.cities,
                selected = state.selectedCity,
                onSelect = onCitySelected,
            )
        }

        // Content
        val pullState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            state = pullState,
            indicator = {
                androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = WadjetColors.Gold,
                )
            },
        ) {
            if (state.isLoading && state.landmarks.isEmpty()) {
                ShimmerCardList(
                    itemCount = 5,
                    modifier = Modifier.padding(16.dp),
                )
            } else if (state.landmarks.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    com.wadjet.core.designsystem.component.EmptyState(
                        glyph = "𓉐",
                        title = if (state.searchQuery.isNotBlank()) "No results for \"${state.searchQuery}\""
                            else "No landmarks found",
                        subtitle = if (state.searchQuery.isNotBlank()) "Try a different search term"
                            else "Explore Egypt's ancient wonders — check back soon",
                    )
                }
            } else {
                val featured = remember(state.landmarks) {
                    state.landmarks.filter { it.featured }
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (featured.isNotEmpty() && state.searchQuery.isBlank()) {
                        item(key = "featured_header") {
                            Text(
                                "Featured",
                                style = MaterialTheme.typography.titleMedium,
                                color = WadjetColors.Gold,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        item(key = "featured_carousel") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(featured, key = { "ft_${it.slug}" }) { landmark ->
                                    FeaturedCard(
                                        landmark = landmark,
                                        onClick = { onLandmarkTap(landmark.slug) },
                                    )
                                }
                            }
                        }
                        item(key = "all_header") {
                            Text(
                                "All Landmarks",
                                style = MaterialTheme.typography.titleMedium,
                                color = WadjetColors.Gold,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                    items(state.landmarks, key = { it.slug }) { landmark ->
                        LandmarkCard(
                            landmark = landmark,
                            isFavorite = state.favorites.contains(landmark.slug),
                            onClick = { onLandmarkTap(landmark.slug) },
                            onToggleFavorite = { onToggleFavorite(landmark) },
                        )
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                com.wadjet.core.designsystem.component.WadjetSectionLoader(
                                    text = "Loading more...",
                                )
                            }
                        }
                    }
                }
            }

            // Error display
            state.error?.let { error ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    com.wadjet.core.designsystem.component.ErrorState(
                        message = error ?: "Couldn't load landmarks. Check your connection",
                        onRetry = onRefresh,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search landmarks...", color = WadjetColors.TextMuted) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = WadjetColors.TextMuted) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, "Clear", tint = WadjetColors.TextMuted)
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = WadjetColors.Surface,
            unfocusedContainerColor = WadjetColors.Surface,
            focusedTextColor = WadjetColors.Text,
            unfocusedTextColor = WadjetColors.Text,
            cursorColor = WadjetColors.Gold,
            focusedIndicatorColor = WadjetColors.Gold,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WadjetColors.Gold,
                    selectedLabelColor = WadjetColors.Night,
                    containerColor = WadjetColors.Surface,
                    labelColor = WadjetColors.TextMuted,
                ),
            )
        }
    }
}

@Composable
private fun CityFilter(
    cities: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("All Cities") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WadjetColors.Gold,
                    selectedLabelColor = WadjetColors.Night,
                    containerColor = WadjetColors.Surface,
                    labelColor = WadjetColors.TextMuted,
                ),
            )
        }
        items(cities) { city ->
            FilterChip(
                selected = city == selected,
                onClick = { onSelect(city) },
                label = { Text(city) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WadjetColors.Gold,
                    selectedLabelColor = WadjetColors.Night,
                    containerColor = WadjetColors.Surface,
                    labelColor = WadjetColors.TextMuted,
                ),
            )
        }
    }
}

@Composable
private fun LandmarkCard(
    landmark: Landmark,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = WadjetColors.Surface,
        modifier = Modifier
            .fillMaxWidth()
            .shineSweep()
            .clickable(onClick = onClick),
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                AsyncImage(
                    model = landmark.thumbnail,
                    contentDescription = landmark.name,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(DesignR.drawable.ic_placeholder_landmark),
                    error = painterResource(DesignR.drawable.ic_placeholder_error),
                    fallback = painterResource(DesignR.drawable.ic_placeholder_landmark),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                )
                // Favorite heart
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(48.dp)
                        .background(WadjetColors.Night.copy(alpha = 0.6f), CircleShape),
                ) {
                    val heartColor by animateColorAsState(
                        targetValue = if (isFavorite) Color(0xFFFF4444) else WadjetColors.Text,
                        label = "heartColor",
                    )
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = heartColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Text content
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = landmark.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = WadjetColors.Text,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (landmark.featured) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(text = "★ Featured", color = WadjetColors.Gold)
                    }
                }
                landmark.nameAr?.let { ar ->
                    Text(
                        text = ar,
                        style = MaterialTheme.typography.bodySmall,
                        color = WadjetColors.Sand,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    landmark.city?.let { city ->
                        Badge(text = city, color = WadjetColors.Gold)
                    }
                    landmark.type?.let { type ->
                        Badge(text = type, color = WadjetColors.Sand)
                    }
                    landmark.era?.let { era ->
                        Badge(text = era, color = WadjetColors.Dust)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(
    landmark: Landmark,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = WadjetColors.Surface,
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
    ) {
        Column {
            AsyncImage(
                model = landmark.thumbnail,
                contentDescription = landmark.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(DesignR.drawable.ic_placeholder_landmark),
                error = painterResource(DesignR.drawable.ic_placeholder_error),
                fallback = painterResource(DesignR.drawable.ic_placeholder_landmark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = landmark.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WadjetColors.Text,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                landmark.city?.let { city ->
                    Text(
                        text = city,
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
