package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.component.WadjetCard

@Composable
fun LearnTab(
    onLessonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lessons = listOf(
        LessonInfo(1, "The Alphabet", "Learn the 26 uniliteral signs — the building blocks of hieroglyphic writing."),
        LessonInfo(2, "Common Words", "Combine signs to read and write frequently used Egyptian words."),
        LessonInfo(3, "Royal Names", "Read cartouches — the oval frames containing pharaoh names."),
        LessonInfo(4, "Determinatives", "Silent signs that hint at a word's meaning category."),
        LessonInfo(5, "Full Sentences", "Put it all together — read complete inscriptions."),
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(lessons) { _, lesson ->
            LessonCard(lesson = lesson, onClick = { onLessonClick(lesson.level) })
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
