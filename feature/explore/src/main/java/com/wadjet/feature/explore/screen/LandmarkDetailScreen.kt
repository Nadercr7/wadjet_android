package com.wadjet.feature.explore.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.core.designsystem.WadjetColors
import androidx.compose.ui.res.painterResource
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.KenBurnsImage
import com.wadjet.core.designsystem.component.ShimmerDetail
import com.wadjet.core.domain.model.LandmarkChild
import com.wadjet.core.domain.model.LandmarkDetail
import com.wadjet.core.domain.model.LandmarkImage
import com.wadjet.core.domain.model.Recommendation
import com.wadjet.feature.explore.DetailUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LandmarkDetailScreen(
    state: DetailUiState,
    onTabSelected: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onRecommendationTap: (String) -> Unit,
    onChildTap: (String) -> Unit,
    onChatAbout: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = state.detail?.name ?: "Loading...",
                    color = WadjetColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WadjetColors.Gold)
                }
            },
            actions = {
                state.detail?.let { detail ->
                    IconButton(onClick = {
                        val shareText = "${detail.name}\n${detail.city ?: ""}\n\nExplore on Wadjet"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share"))
                    }) {
                        Icon(Icons.Default.Share, "Share", tint = WadjetColors.Gold)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Night),
        )

        when {
            state.isLoading -> {
                ShimmerDetail(modifier = Modifier.padding(16.dp))
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    com.wadjet.core.designsystem.component.ErrorState(
                        message = state.error ?: "Couldn't load landmark details. Check your connection",
                        onRetry = onBack,
                    )
                }
            }
            state.detail != null -> {
                DetailContent(
                    detail = state.detail,
                    isFavorite = state.isFavorite,
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                    onToggleFavorite = onToggleFavorite,
                    onRecommendationTap = onRecommendationTap,
                    onChildTap = onChildTap,
                    onChatAbout = onChatAbout,
                    onDirections = { coords ->
                        val gmmUri = Uri.parse("google.navigation:q=${coords.first},${coords.second}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${coords.first},${coords.second}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    detail: LandmarkDetail,
    isFavorite: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onRecommendationTap: (String) -> Unit,
    onChildTap: (String) -> Unit,
    onChatAbout: (String) -> Unit,
    onDirections: (Pair<Double, Double>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero image carousel
        if (detail.images.isNotEmpty()) {
            ImageCarousel(images = detail.images)
        } else if (detail.thumbnail != null) {
            KenBurnsImage(
                url = detail.thumbnail!!,
                contentDescription = detail.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = detail.name,
                style = MaterialTheme.typography.headlineSmall,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.Bold,
            )
            detail.nameAr?.let { ar ->
                Text(
                    text = ar,
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Sand,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Badges
            FadeUp(visible = true) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    detail.type?.let { DetailBadge(text = it, color = WadjetColors.Gold) }
                    detail.city?.let { DetailBadge(text = it, color = WadjetColors.Sand) }
                    detail.era?.let { DetailBadge(text = it, color = WadjetColors.Dust) }
                    detail.dynasty?.let { DetailBadge(text = it, color = WadjetColors.Sand) }
                }
            }

            // Parent breadcrumb
            detail.parent?.let { parent ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WadjetColors.Surface,
                    onClick = { onChildTap(parent.slug) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text("Part of: ", style = MaterialTheme.typography.labelSmall, color = WadjetColors.TextMuted)
                        Text(parent.name, style = MaterialTheme.typography.labelSmall, color = WadjetColors.Gold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                detail.coordinates?.let { coords ->
                    ActionButton(
                        icon = Icons.Default.Map,
                        label = "Maps",
                        onClick = { onDirections(coords) },
                        modifier = Modifier.weight(1f),
                    )
                }
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Chat",
                    onClick = { onChatAbout(detail.slug) },
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (isFavorite) "Saved" else "Save",
                    onClick = onToggleFavorite,
                    color = if (isFavorite) Color(0xFFFF4444) else WadjetColors.Gold,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Tabs
            val tabs = buildList {
                if (!detail.description.isNullOrBlank() || detail.sections.isNotEmpty()) add("Overview")
                if (!detail.historicalSignificance.isNullOrBlank()) add("History")
                if (!detail.visitingTips.isNullOrBlank()) add("Tips")
                if (detail.images.size > 1) add("Gallery")
            }

            if (tabs.isNotEmpty()) {
                val safeTabIndex = selectedTab.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
                PrimaryTabRow(
                    selectedTabIndex = safeTabIndex,
                    containerColor = WadjetColors.Night,
                    contentColor = WadjetColors.Gold,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = safeTabIndex == index,
                            onClick = { onTabSelected(index) },
                            text = {
                                Text(
                                    title,
                                    color = if (safeTabIndex == index) WadjetColors.Gold
                                    else WadjetColors.TextMuted,
                                )
                            },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                when (tabs.getOrNull(safeTabIndex)) {
                    "Overview" -> OverviewTab(detail)
                    "History" -> HistoryTab(detail)
                    "Tips" -> TipsTab(detail)
                    "Gallery" -> GalleryTab(detail.images)
                }
            }

            // Recommendations
            if (detail.recommendations.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                RecommendationsRow(
                    recommendations = detail.recommendations,
                    onTap = onRecommendationTap,
                )
            }

            // Children
            if (detail.children.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Related Sites",
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                ChildrenRow(children = detail.children, onTap = onChildTap)
            }

            // Wikipedia
            if (!detail.wikipediaExtract.isNullOrBlank() || !detail.wikipediaUrl.isNullOrBlank()) {
                val context = LocalContext.current
                Spacer(Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WadjetColors.Surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Wikipedia", style = MaterialTheme.typography.titleSmall, color = WadjetColors.Gold, fontWeight = FontWeight.SemiBold)
                        detail.wikipediaExtract?.let { extract ->
                            Spacer(Modifier.height(4.dp))
                            Text(extract, style = MaterialTheme.typography.bodySmall, color = WadjetColors.Text, maxLines = 6, overflow = TextOverflow.Ellipsis)
                        }
                        detail.wikipediaUrl?.let { url ->
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                                shape = RoundedCornerShape(8.dp),
                                color = WadjetColors.Gold.copy(alpha = 0.15f),
                            ) {
                                Text("Read on Wikipedia →", color = WadjetColors.Gold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ImageCarousel(images: List<LandmarkImage>) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) { page ->
            AsyncImage(
                model = images[page].url,
                contentDescription = images[page].caption,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(DesignR.drawable.ic_placeholder_landmark),
                error = painterResource(DesignR.drawable.ic_placeholder_error),
                fallback = painterResource(DesignR.drawable.ic_placeholder_landmark),
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Page indicator
        if (images.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) WadjetColors.Gold
                                else WadjetColors.Text.copy(alpha = 0.4f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = WadjetColors.Gold,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WadjetColors.Surface,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
private fun OverviewTab(detail: LandmarkDetail) {
    Column {
        detail.description?.let { desc ->
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = WadjetColors.Text,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )
        }
        detail.sections.forEach { section ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = section.content,
                style = MaterialTheme.typography.bodyMedium,
                color = WadjetColors.Text,
            )
        }
        detail.highlights?.let { highlights ->
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Highlights",
                style = MaterialTheme.typography.titleSmall,
                color = WadjetColors.Gold,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(highlights, style = MaterialTheme.typography.bodyMedium, color = WadjetColors.Text)
        }

        BulletList("Notable Pharaohs", detail.notablePharaohs)
        BulletList("Notable Tombs", detail.notableTombs)
        BulletList("Notable Features", detail.notableFeatures)
        BulletList("Key Artifacts", detail.keyArtifacts)
        BulletList("Architectural Features", detail.architecturalFeatures)
    }
}

@Composable
private fun HistoryTab(detail: LandmarkDetail) {
    Text(
        text = detail.historicalSignificance ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = WadjetColors.Text,
    )
}

@Composable
private fun TipsTab(detail: LandmarkDetail) {
    Text(
        text = detail.visitingTips ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = WadjetColors.Text,
    )
}

@Composable
private fun GalleryTab(images: List<LandmarkImage>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
    ) {
        items(images) { image ->
            AsyncImage(
                model = image.url,
                contentDescription = image.caption,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(DesignR.drawable.ic_placeholder_landmark),
                error = painterResource(DesignR.drawable.ic_placeholder_error),
                fallback = painterResource(DesignR.drawable.ic_placeholder_landmark),
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun RecommendationsRow(
    recommendations: List<Recommendation>,
    onTap: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(recommendations) { rec ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WadjetColors.Surface,
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onTap(rec.slug) },
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = rec.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WadjetColors.Text,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (rec.reasons.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = rec.reasons.first(),
                            style = MaterialTheme.typography.labelSmall,
                            color = WadjetColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BulletList(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Spacer(Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = WadjetColors.Gold,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(4.dp))
    items.forEach { item ->
        Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
            Text("•  ", color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
            Text(item, style = MaterialTheme.typography.bodyMedium, color = WadjetColors.Text)
        }
    }
}

@Composable
private fun ChildrenRow(
    children: List<LandmarkChild>,
    onTap: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(children) { child ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WadjetColors.Surface,
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onTap(child.slug) },
            ) {
                Column {
                    child.thumbnail?.let { thumb ->
                        AsyncImage(
                            model = thumb,
                            contentDescription = child.name,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(DesignR.drawable.ic_placeholder_landmark),
                            error = painterResource(DesignR.drawable.ic_placeholder_error),
                            fallback = painterResource(DesignR.drawable.ic_placeholder_landmark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        )
                    }
                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = WadjetColors.Text,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailBadge(text: String, color: Color) {
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
