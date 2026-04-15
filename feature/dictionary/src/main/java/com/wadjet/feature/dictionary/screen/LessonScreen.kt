package com.wadjet.feature.dictionary.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.designsystem.GardinerCodeStyle
import com.wadjet.core.designsystem.HieroglyphStyle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.component.WadjetGhostButton
import com.wadjet.core.domain.model.ExampleWord
import com.wadjet.core.domain.model.PracticeWord
import com.wadjet.core.domain.model.Sign
import androidx.compose.ui.res.stringResource
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.feature.dictionary.LessonUiState
import com.wadjet.feature.dictionary.R

@Composable
fun LessonScreen(
    state: LessonUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                com.wadjet.core.designsystem.component.WadjetSectionLoader(
                    text = stringResource(R.string.lesson_loading),
                )
            }
        }
        state.error != null -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                com.wadjet.core.designsystem.component.ErrorState(
                    message = state.error ?: stringResource(R.string.lesson_error),
                    onRetry = onRetry,
                )
            }
        }
        state.lesson != null -> {
            val lesson = state.lesson

            LazyColumn(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                item {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = WadjetColors.Gold,
                    )
                    if (lesson.subtitle.isNotBlank()) {
                        Text(
                            text = lesson.subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = WadjetColors.Sand,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Text(
                        text = lesson.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = WadjetColors.TextMuted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Tip
                val tip = lesson.tip
                if (!tip.isNullOrBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(WadjetColors.Gold.copy(alpha = 0.08f))
                                .padding(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.lesson_tip, tip),
                                style = MaterialTheme.typography.bodySmall,
                                color = WadjetColors.Gold,
                            )
                        }
                    }
                }

                // Intro paragraphs
                if (lesson.introParagraphs.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lesson.introParagraphs.forEach { p ->
                                Text(
                                    text = p,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WadjetColors.Text,
                                )
                            }
                        }
                    }
                }

                // Signs grid
                if (lesson.signs.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.lesson_signs_label), style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(
                                ((lesson.signs.size + 3) / 4 * 80).coerceAtMost(240).dp,
                            ),
                        ) {
                            items(lesson.signs) { sign ->
                                TeachingSignItem(sign)
                            }
                        }
                    }
                }

                // Example words
                if (lesson.exampleWords.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.lesson_examples_label), style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                    }
                    items(lesson.exampleWords.size) { index ->
                        ExampleWordItem(lesson.exampleWords[index])
                    }
                }

                // Practice words
                if (lesson.practiceWords.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.lesson_practice_label), style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                    }
                    items(lesson.practiceWords.size) { index ->
                        PracticeWordItem(lesson.practiceWords[index])
                    }
                }

                // Navigation
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        WadjetGhostButton(text = stringResource(DesignR.string.action_back), onClick = onBack)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeachingSignItem(sign: Sign) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(WadjetColors.Surface)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(sign.glyph, style = HieroglyphStyle.copy(fontSize = 28.sp))
        Text(sign.code, style = GardinerCodeStyle.copy(fontSize = 10.sp))
    }
}

@Composable
private fun ExampleWordItem(word: ExampleWord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(WadjetColors.Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(word.hieroglyphs, style = HieroglyphStyle.copy(fontSize = 24.sp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(word.transliteration, style = MaterialTheme.typography.bodyMedium, color = WadjetColors.Sand)
            Text(word.translation, style = MaterialTheme.typography.bodySmall, color = WadjetColors.TextMuted)
        }
    }
}

@Composable
private fun PracticeWordItem(word: PracticeWord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(WadjetColors.Surface)
            .padding(12.dp),
    ) {
        Text(word.hieroglyphs, style = HieroglyphStyle.copy(fontSize = 24.sp))
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text(word.transliteration, style = MaterialTheme.typography.bodyMedium, color = WadjetColors.Sand)
            Spacer(Modifier.width(8.dp))
            Text("— ${word.translation}", style = MaterialTheme.typography.bodySmall, color = WadjetColors.TextMuted)
        }
        if (word.hint.isNotBlank()) {
            Text(
                text = stringResource(R.string.lesson_word_hint, word.hint),
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.Gold.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
