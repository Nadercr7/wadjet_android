package com.wadjet.feature.settings.screen

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.settings.R
import java.util.Locale

/** Language tags offered in-app; must stay in sync with res/xml/locales_config.xml. */
private const val TAG_EN = "en"
private const val TAG_AR = "ar"

/** The app-level language currently in effect ("en" or "ar"). */
fun currentAppLanguage(): String {
    val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val effective = tags.ifEmpty { Locale.getDefault().language }
    return if (effective.startsWith(TAG_AR)) TAG_AR else TAG_EN
}

/**
 * Applies the chosen per-app language. AppCompat persists it (autoStoreLocales)
 * and recreates the activity so the whole UI re-inflates in the new locale.
 */
fun setAppLanguage(tag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}

/** Full-settings card with English / Arabic choices. */
@Composable
fun LanguageSection(modifier: Modifier = Modifier) {
    var selected by rememberSaveable { mutableStateOf(currentAppLanguage()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(WadjetColors.Surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LanguageOption(
            label = stringResource(R.string.quick_settings_english),
            isSelected = selected == TAG_EN,
            modifier = Modifier.weight(1f),
        ) {
            selected = TAG_EN
            setAppLanguage(TAG_EN)
        }
        LanguageOption(
            label = stringResource(R.string.quick_settings_arabic),
            isSelected = selected == TAG_AR,
            modifier = Modifier.weight(1f),
        ) {
            selected = TAG_AR
            setAppLanguage(TAG_AR)
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (isSelected) WadjetColors.Gold.copy(alpha = 0.15f) else WadjetColors.Night)
            .border(
                width = 1.dp,
                color = if (isSelected) WadjetColors.Gold else WadjetColors.Border,
                shape = MaterialTheme.shapes.small,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) WadjetColors.Gold else WadjetColors.Text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
