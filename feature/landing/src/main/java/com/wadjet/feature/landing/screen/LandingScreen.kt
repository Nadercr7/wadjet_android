package com.wadjet.feature.landing.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetCard

@Composable
fun LandingScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToDictionary: () -> Unit,
    onNavigateToStories: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineLarge,
                color = WadjetColors.Gold,
            )
        }

        // Hieroglyphs path card
        item {
            PathCard(
                glyph = "𓂀",
                title = "Hieroglyphs",
                subtitle = "Decode Ancient Egypt",
                buttonText = "Start Scanning",
                onClick = onNavigateToScan,
            )
        }

        // Landmarks path card
        item {
            PathCard(
                glyph = "𓉐",
                title = "Landmarks",
                subtitle = "Explore Sites & Monuments",
                buttonText = "Start Exploring",
                onClick = onNavigateToExplore,
            )
        }

        // Quick actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = WadjetColors.TextMuted,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = "𓂀",
                    label = "Scan",
                    onClick = onNavigateToScan,
                )
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = "𓊹",
                    label = "Dictionary",
                    onClick = onNavigateToDictionary,
                )
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = "𓇯",
                    label = "Explore",
                    onClick = onNavigateToExplore,
                )
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = "𓁟",
                    label = "Stories",
                    onClick = onNavigateToStories,
                )
            }
        }
    }
}

@Composable
private fun PathCard(
    glyph: String,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    WadjetCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = glyph,
                style = MaterialTheme.typography.displayMedium,
                fontFamily = NotoSansEgyptianHieroglyphs,
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = WadjetColors.Gold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = WadjetColors.TextMuted,
            )
            Spacer(modifier = Modifier.height(16.dp))
            WadjetButton(text = buttonText, onClick = onClick)
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
    WadjetCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = NotoSansEgyptianHieroglyphs,
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.TextMuted,
            )
        }
    }
}
