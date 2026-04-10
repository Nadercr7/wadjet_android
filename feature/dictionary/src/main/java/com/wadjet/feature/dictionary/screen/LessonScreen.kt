package com.wadjet.feature.dictionary.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.wadjet.core.domain.model.Exercise
import com.wadjet.core.domain.model.Sign
import com.wadjet.feature.dictionary.LessonUiState

@Composable
fun LessonScreen(
    state: LessonUiState,
    onSelectAnswer: (String) -> Unit,
    onRevealAnswer: () -> Unit,
    onNextExercise: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                com.wadjet.core.designsystem.component.WadjetSectionLoader(
                    text = "Loading lesson...",
                )
            }
        }
        state.error != null -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                com.wadjet.core.designsystem.component.ErrorState(
                    message = state.error,
                    onRetry = onRetry,
                )
            }
        }
        state.isCompleted -> {
            CompletionView(
                correct = state.correctCount,
                total = state.answeredCount,
                onRetry = onRetry,
                onBack = onBack,
            )
        }
        state.lesson != null -> {
            val lesson = state.lesson
            val exercises = lesson.exercises
            val current = exercises.getOrNull(state.currentExerciseIndex)

            Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = WadjetColors.Gold,
                )
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )

                // Progress bar
                if (exercises.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { (state.currentExerciseIndex + 1).toFloat() / exercises.size },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = WadjetColors.Gold,
                        trackColor = WadjetColors.Surface,
                    )
                    Text(
                        text = "${state.currentExerciseIndex + 1} / ${exercises.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WadjetColors.TextMuted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Teaching signs section
                if (lesson.signs.isNotEmpty() && state.currentExerciseIndex == 0 && !state.isAnswerRevealed) {
                    Spacer(Modifier.height(16.dp))
                    Text("Signs in this lesson:", style = MaterialTheme.typography.labelMedium, color = WadjetColors.Gold)
                    Spacer(Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(120.dp),
                    ) {
                        items(lesson.signs) { sign ->
                            TeachingSignItem(sign)
                        }
                    }
                }

                // Exercise
                if (current != null) {
                    Spacer(Modifier.height(24.dp))
                    ExerciseView(
                        exercise = current,
                        selectedAnswer = state.selectedAnswer,
                        isRevealed = state.isAnswerRevealed,
                        onSelectAnswer = onSelectAnswer,
                    )

                    Spacer(Modifier.weight(1f))

                    if (!state.isAnswerRevealed) {
                        WadjetButton(
                            text = "Check Answer",
                            onClick = onRevealAnswer,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.selectedAnswer != null,
                        )
                    } else {
                        WadjetButton(
                            text = if (state.currentExerciseIndex + 1 >= exercises.size) "See Results" else "Next",
                            onClick = onNextExercise,
                            modifier = Modifier.fillMaxWidth(),
                        )
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
            .clip(RoundedCornerShape(8.dp))
            .background(WadjetColors.Surface)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(sign.glyph, style = HieroglyphStyle.copy(fontSize = 28.sp))
        Text(sign.code, style = GardinerCodeStyle.copy(fontSize = 10.sp))
    }
}

@Composable
private fun ExerciseView(
    exercise: Exercise,
    selectedAnswer: String?,
    isRevealed: Boolean,
    onSelectAnswer: (String) -> Unit,
) {
    Column {
        Text(
            text = exercise.question,
            style = MaterialTheme.typography.titleMedium,
            color = WadjetColors.Text,
        )
        Spacer(Modifier.height(16.dp))

        when (exercise.type) {
            "multiple_choice", "matching" -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(240.dp),
                ) {
                    items(exercise.options) { option ->
                        val isSelected = selectedAnswer == option.code
                        val isCorrect = option.code == exercise.correctAnswer

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isRevealed && isCorrect -> WadjetColors.Success.copy(alpha = 0.2f)
                                isRevealed && isSelected && !isCorrect -> WadjetColors.Error.copy(alpha = 0.2f)
                                isSelected -> WadjetColors.Gold.copy(alpha = 0.2f)
                                else -> WadjetColors.Surface
                            },
                            label = "optionBg",
                        )
                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isRevealed && isCorrect -> WadjetColors.Success
                                isRevealed && isSelected && !isCorrect -> WadjetColors.Error
                                isSelected -> WadjetColors.Gold
                                else -> WadjetColors.Border
                            },
                            label = "optionBorder",
                        )

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable(enabled = !isRevealed) { onSelectAnswer(option.code) }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (option.glyph.isNotBlank()) {
                                Text(option.glyph, style = HieroglyphStyle.copy(fontSize = 36.sp))
                            }
                            Text(
                                text = option.label.ifBlank { option.code },
                                style = MaterialTheme.typography.labelMedium,
                                color = WadjetColors.Text,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        // Hint
        if (!exercise.hint.isNullOrBlank() && !isRevealed) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hint: ${exercise.hint}",
                style = MaterialTheme.typography.bodySmall,
                color = WadjetColors.Sand,
            )
        }
    }
}

@Composable
private fun CompletionView(
    correct: Int,
    total: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "𓂋",
                style = HieroglyphStyle.copy(fontSize = 64.sp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Lesson Complete!",
                style = MaterialTheme.typography.headlineMedium,
                color = WadjetColors.Gold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$correct / $total correct",
                style = MaterialTheme.typography.titleLarge,
                color = WadjetColors.Text,
            )
            Spacer(Modifier.height(24.dp))
            Row {
                WadjetGhostButton(text = "Retry", onClick = onRetry)
                Spacer(Modifier.width(12.dp))
                WadjetButton(text = "Done", onClick = onBack)
            }
        }
    }
}
