package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.component.WadjetCard
import com.wadjet.core.domain.model.Sign
import com.wadjet.feature.dictionary.AlphabetUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnTab(
    alphabetState: AlphabetUiState,
    onLoadAlphabet: () -> Unit,
    onSignClick: (Sign) -> Unit,
    onLessonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        if (alphabetState.signs.isEmpty() && !alphabetState.isLoading) {
            onLoadAlphabet()
        }
    }

    val lessons = listOf(
        LessonInfo(1, "The Alphabet", "Learn the 26 uniliteral signs — the building blocks of hieroglyphic writing."),
        LessonInfo(2, "Common Words", "Combine signs to read and write frequently used Egyptian words."),
        LessonInfo(3, "Royal Names", "Read cartouches — the oval frames containing pharaoh names."),
        LessonInfo(4, "Determinatives", "Silent signs that hint at a word's meaning category."),
        LessonInfo(5, "Full Sentences", "Put it all together — read complete inscriptions."),
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
                        text = "Egyptian Alphabet",
                        style = MaterialTheme.typography.titleMedium,
                        color = WadjetColors.Gold,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The 25 uniliteral signs — tap any to explore",
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
                        }
                    }
                }
            }
            item(key = "lessons_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lessons",
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
                text = "Level ${lesson.level}",
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
