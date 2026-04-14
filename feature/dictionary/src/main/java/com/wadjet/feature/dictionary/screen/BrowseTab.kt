package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.GardinerCodeStyle
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.ShimmerGrid
import com.wadjet.core.designsystem.component.WadjetTextField
import com.wadjet.core.domain.model.Sign
import com.wadjet.feature.dictionary.BrowseUiState
import com.wadjet.feature.dictionary.SIGN_TYPES

@Composable
fun BrowseTab(
    state: BrowseUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onTypeSelect: (String?) -> Unit,
    onSignClick: (Sign) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    // Trigger load more when near bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        WadjetTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = "Search signs…",
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = WadjetColors.TextMuted,
                )
            },
        )

        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            item {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("All") },
                    colors = chipColors(state.selectedCategory == null),
                )
            }
            items(state.categories) { cat ->
                FilterChip(
                    selected = state.selectedCategory == cat.code,
                    onClick = { onCategorySelect(cat.code) },
                    label = { Text("${cat.code} ${cat.name}") },
                    colors = chipColors(state.selectedCategory == cat.code),
                )
            }
        }

        // Type filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            items(SIGN_TYPES) { type ->
                val isSelected = (type == "All" && state.selectedType == null) || type == state.selectedType
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeSelect(type) },
                    label = { Text(type.replaceFirstChar { it.uppercase() }) },
                    colors = chipColors(isSelected),
                )
            }
        }

        // Sign grid
        if (state.isLoading) {
            ShimmerGrid(
                itemCount = 6,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else if (state.signs.isEmpty()) {
            if (state.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    com.wadjet.core.designsystem.component.ErrorState(
                        message = state.error ?: "Couldn't load hieroglyphs. Check your connection",
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    com.wadjet.core.designsystem.component.EmptyState(
                        glyph = "\uD80C\uDEB9",
                        title = "No signs found",
                        subtitle = "Try a different search or filter",
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.signs, key = { it.code }) { sign ->
                    SignGridItem(sign = sign, onClick = { onSignClick(sign) })
                }
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            com.wadjet.core.designsystem.component.WadjetSectionLoader(
                                text = "Loading more signs...",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignGridItem(sign: Sign, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(WadjetColors.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = sign.glyph,
            style = HieroglyphStyle,
            textAlign = TextAlign.Center,
        )
        Text(
            text = sign.code,
            style = GardinerCodeStyle,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = sign.description,
            style = MaterialTheme.typography.labelSmall,
            color = WadjetColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun chipColors(selected: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = WadjetColors.Gold,
    selectedLabelColor = WadjetColors.Night,
    containerColor = WadjetColors.Surface,
    labelColor = WadjetColors.TextMuted,
)
