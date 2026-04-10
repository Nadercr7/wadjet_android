package com.wadjet.feature.scan.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.wadjet.core.designsystem.component.ShimmerCardList
import com.wadjet.core.designsystem.component.ErrorState
import com.wadjet.core.domain.model.ScanHistorySummary
import com.wadjet.feature.scan.HistoryUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    state: HistoryUiState,
    onScanTap: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        TopAppBar(
            title = { Text("Scan History", color = WadjetColors.Text) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Night),
        )

        when {
            state.isLoading -> {
                ShimmerCardList(
                    itemCount = 5,
                    modifier = Modifier.padding(16.dp),
                )
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(
                        message = state.error ?: "Couldn't load scan history. Check your connection",
                        onRetry = onRefresh,
                    )
                }
            }

            state.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    com.wadjet.core.designsystem.component.EmptyState(
                        glyph = "\uD80C\uDC80",
                        title = "No scans yet",
                        subtitle = "Scan hieroglyphs to see them here",
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.items,
                        key = { it.id },
                    ) { item ->
                        SwipeToDeleteHistoryItem(
                            item = item,
                            onClick = { onScanTap(item.id) },
                            onDelete = { onDelete(item.id) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteHistoryItem(
    item: ScanHistorySummary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF4444)
                    else -> Color.Transparent
                },
                label = "deleteColor",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        HistoryCard(item = item, onClick = onClick)
    }
}

@Composable
private fun HistoryCard(
    item: ScanHistorySummary,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = WadjetColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            AsyncImage(
                model = File(item.thumbnailPath),
                contentDescription = "Scan thumbnail",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(DesignR.drawable.ic_placeholder_glyph),
                error = painterResource(DesignR.drawable.ic_placeholder_error),
                fallback = painterResource(DesignR.drawable.ic_placeholder_glyph),
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WadjetColors.Night),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Gardiner sequence or transliteration
                Text(
                    text = item.transliteration ?: item.gardinerSequence ?: "Scan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WadjetColors.Text,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "${item.glyphCount} glyphs",
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.TextMuted,
                    )
                    Text(
                        text = "${(item.confidenceAvg * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.Gold,
                    )
                    Text(
                        text = "${item.totalMs.toLong()}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.TextMuted,
                    )
                }

                // Translation preview
                val translation = item.translationEn
                if (!translation.isNullOrBlank()) {
                    Text(
                        text = translation,
                        style = MaterialTheme.typography.bodySmall,
                        color = WadjetColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                // Date
                Text(
                    text = formatDate(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = WadjetColors.Dust,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
