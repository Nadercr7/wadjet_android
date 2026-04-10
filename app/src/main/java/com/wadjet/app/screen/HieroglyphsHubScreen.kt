package com.wadjet.app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp

@Composable
fun HieroglyphsHubScreen(
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
            text = "Hieroglyphs",
            style = MaterialTheme.typography.headlineMedium,
            color = WadjetColors.Gold,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Scan, browse, and write ancient Egyptian hieroglyphs",
            style = MaterialTheme.typography.bodyMedium,
            color = WadjetColors.TextMuted,
        )

        Spacer(Modifier.height(24.dp))

        FadeUp(visible = true) {
            HubCard(
                icon = Icons.Outlined.CameraAlt,
                title = "Scan Hieroglyphs",
                description = "Detect & translate hieroglyphs from photos using AI",
                onClick = onNavigateToScan,
            )
        }

        Spacer(Modifier.height(12.dp))

        FadeUp(visible = true) {
            HubCard(
                icon = Icons.Outlined.MenuBook,
                title = "Dictionary",
                description = "Browse 1,000+ hieroglyphic signs with meanings & phonetics",
                onClick = onNavigateToDictionary,
            )
        }

        Spacer(Modifier.height(12.dp))

        FadeUp(visible = true) {
            HubCard(
                icon = Icons.Outlined.Edit,
                title = "Write in Hieroglyphs",
                description = "Type English, get hieroglyphs — powered by AI transliteration",
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
                    text = "How Scanning Works",
                    style = MaterialTheme.typography.titleSmall,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                ExplainerStep(number = "1", text = "Take or upload a photo of hieroglyphs")
                Spacer(Modifier.height(6.dp))
                ExplainerStep(number = "2", text = "AI detects individual glyphs with bounding boxes")
                Spacer(Modifier.height(6.dp))
                ExplainerStep(number = "3", text = "Each glyph is classified with Gardiner code & meaning")
                Spacer(Modifier.height(6.dp))
                ExplainerStep(number = "4", text = "Full transliteration & translation generated")
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
