package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wadjet.core.common.EgyptianPronunciation
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.component.WadjetCard
import com.wadjet.core.domain.model.Sign
import androidx.compose.ui.res.stringResource
import com.wadjet.feature.dictionary.AlphabetUiState
import com.wadjet.feature.dictionary.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnTab(
    alphabetState: AlphabetUiState,
    onLoadAlphabet: () -> Unit,
    onSignClick: (Sign) -> Unit,
    onLessonClick: (Int) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        if (alphabetState.signs.isEmpty() && !alphabetState.isLoading) {
            onLoadAlphabet()
        }
    }

    val lessons = listOf(
        LessonInfo(1, stringResource(R.string.lesson_1_title), stringResource(R.string.lesson_1_desc)),
        LessonInfo(2, stringResource(R.string.lesson_2_title), stringResource(R.string.lesson_2_desc)),
        LessonInfo(3, stringResource(R.string.lesson_3_title), stringResource(R.string.lesson_3_desc)),
        LessonInfo(4, stringResource(R.string.lesson_4_title), stringResource(R.string.lesson_4_desc)),
        LessonInfo(5, stringResource(R.string.lesson_5_title), stringResource(R.string.lesson_5_desc)),
    )

    var visibleCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        lessons.forEachIndexed { _, _ -> delay(120); visibleCount++ }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Alphabet grid
        if (alphabetState.signs.isNotEmpty()) {
            item(key = "alphabet_header") {
                Column {
                    Text(
                        text = stringResource(R.string.learn_alphabet_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = WadjetColors.Gold,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.learn_alphabet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = WadjetColors.TextMuted,
                    )
                }
            }
            item(key = "alphabet_grid") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    alphabetState.signs.forEach { sign ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(64.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(WadjetColors.Surface)
                                .border(1.dp, WadjetColors.Border, MaterialTheme.shapes.small)
                                .clickable { onSignClick(sign) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                        ) {
                            Text(
                                text = sign.glyph,
                                fontSize = 28.sp,
                                fontFamily = NotoSansEgyptianHieroglyphs,
                                color = WadjetColors.Gold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = sign.code,
                                style = MaterialTheme.typography.labelSmall,
                                color = WadjetColors.Sand,
                                textAlign = TextAlign.Center,
                            )
                            sign.transliteration?.let { t ->
                                Text(
                                    text = t,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WadjetColors.TextMuted,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            // Inline TTS button
                            val canPronounce = sign.isPhonetic || sign.type !in listOf("determinative")
                            val ttsText = sign.speechText?.takeIf { it.isNotBlank() }
                                ?: sign.reading?.takeIf { it.isNotBlank() && canPronounce }
                                    ?.let { EgyptianPronunciation.toSpeech(it) }
                                ?: sign.transliteration?.takeIf { it.isNotBlank() && canPronounce }
                                    ?.let { EgyptianPronunciation.toSpeech(it) }
                            if (!ttsText.isNullOrBlank()) {
                                IconButton(
                                    onClick = { onSpeak(ttsText) },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = WadjetColors.Gold.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item(key = "lessons_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.learn_lessons_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = WadjetColors.Gold,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        itemsIndexed(lessons) { index, lesson ->
            FadeUp(visible = index < visibleCount) {
                LessonCard(lesson = lesson, onClick = { onLessonClick(lesson.level) })
            }
        }
    }
}

private data class LessonInfo(val level: Int, val title: String, val description: String)

@Composable
private fun LessonCard(lesson: LessonInfo, onClick: () -> Unit) {
    WadjetCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.learn_level, lesson.level),
                style = MaterialTheme.typography.labelSmall,
                color = WadjetColors.Gold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleMedium,
                color = WadjetColors.Text,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodySmall,
                color = WadjetColors.TextMuted,
            )
        }
    }
}
