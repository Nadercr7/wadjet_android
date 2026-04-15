package com.wadjet.feature.stories.screen

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.core.designsystem.WadjetColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.domain.model.GlyphAnnotation
import com.wadjet.core.domain.model.Interaction
import com.wadjet.core.domain.model.InteractionResult
import com.wadjet.core.domain.model.Paragraph
import com.wadjet.core.ui.LocalAnimatedVisibilityScope
import com.wadjet.core.ui.LocalSharedTransitionScope
import com.wadjet.feature.stories.R
import com.wadjet.feature.stories.ReaderUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun StoryReaderScreen(
    state: ReaderUiState,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onReadAgain: () -> Unit,
    onSubmitAnswer: (Int, String) -> Unit,
    onUpdateWriteInput: (Int, String) -> Unit,
    onSpeak: () -> Unit,
    onRetryImage: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ttsInstance = remember {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.US
        }
        tts
    }
    DisposableEffect(Unit) { onDispose { ttsInstance?.shutdown() } }

    // Handle LOCAL_TTS fallback
    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        if (err.startsWith("LOCAL_TTS:")) {
            val text = err.removePrefix("LOCAL_TTS:")
            val isArabic = text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
            ttsInstance?.language = if (isArabic) Locale("ar") else Locale.US
            ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            onDismissError()
        }
    }

    // Show general errors via toast (handled by ViewModel's ToastController)
    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        if (!err.startsWith("LOCAL_TTS:")) {
            onDismissError()
        }
    }

    val story = state.story
    val chapter = state.chapter

    Scaffold(
        containerColor = WadjetColors.Night,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = story?.titleEn ?: stringResource(R.string.reader_loading_title),
                            color = WadjetColors.Gold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignR.string.action_back), tint = WadjetColors.Text)
                        }
                    },
                    actions = {
                        if (state.totalChapters > 0) {
                            Text(
                                text = stringResource(R.string.reader_chapter_counter, state.currentChapter + 1, state.totalChapters),
                                color = WadjetColors.Sand,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        }
                        IconButton(onClick = onSpeak) {
                            Icon(
                                imageVector = if (state.isSpeaking) Icons.Default.Stop
                                else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (state.isSpeaking) stringResource(R.string.reader_stop_narrate) else stringResource(R.string.reader_narrate),
                                tint = if (state.isSpeaking) WadjetColors.Gold else WadjetColors.Sand,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WadjetColors.Surface),
                )
                // Chapter progress bar
                LinearProgressIndicator(
                    progress = { state.chapterProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = WadjetColors.Gold,
                    trackColor = WadjetColors.Border,
                )
            }
        },
        modifier = modifier,
    ) { padding ->
        if (state.isLoading || story == null || chapter == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.reader_loading), color = WadjetColors.TextMuted)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Scene image
            item {
                SceneImage(
                    imageUrl = state.sceneImageUrl,
                    isLoading = state.isLoadingImage,
                    loadFailed = state.imageLoadFailed,
                    onRetry = onRetryImage,
                )
            }

            // Chapter title
            item {
                FadeUp(visible = true) {
                    Text(
                        text = chapter.titleEn,
                        color = WadjetColors.Gold,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Paragraphs + interactions interleaved
            val paragraphs = chapter.paragraphs
            val interactions = chapter.interactions

            paragraphs.forEachIndexed { pIdx, paragraph ->
                item(key = "p_$pIdx") {
                    ParagraphBlock(
                        paragraph = paragraph,
                        isNarrating = state.narratingParagraphIndex == pIdx,
                    )
                }

                // Show interactions that go after this paragraph
                interactions.forEachIndexed { iIdx, interaction ->
                    if (interaction.afterParagraph == pIdx) {
                        item(key = "i_$iIdx") {
                            InteractionBlock(
                                index = iIdx,
                                interaction = interaction,
                                result = state.interactionResults[iIdx],
                                answer = state.interactionAnswers[iIdx],
                                writeInput = state.writeInputs[iIdx] ?: "",
                                onSubmit = { answer -> onSubmitAnswer(iIdx, answer) },
                                onWriteInputChanged = { text -> onUpdateWriteInput(iIdx, text) },
                            )
                        }
                    }
                }
            }

            // Navigation buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (state.canGoPrev) {
                        ChapterNavButton(
                            text = stringResource(R.string.reader_prev_chapter),
                            onClick = onPrevChapter,
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    if (state.canGoNext) {
                        ChapterNavButton(
                            text = stringResource(R.string.reader_next_chapter),
                            onClick = onNextChapter,
                        )
                    }
                }
            }

            // Completion screen (last chapter)
            if (!state.canGoNext && state.totalChapters > 0) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    FadeUp(visible = true) {
                        // Build glyph code→unicode lookup from story interactions
                        val glyphUnicodes = remember(story, state.glyphsLearned) {
                            val map = mutableMapOf<String, String>()
                            story.chapters.forEach { ch ->
                                ch.interactions.forEach { inter ->
                                    when (inter) {
                                        is Interaction.GlyphDiscovery ->
                                            if (inter.glyphCode in state.glyphsLearned) map[inter.glyphCode] = inter.unicode
                                        is Interaction.ChooseGlyph -> inter.options.forEach { opt ->
                                            if (opt.code in state.glyphsLearned) map[opt.code] = opt.glyph
                                        }
                                        is Interaction.WriteWord ->
                                            if (inter.gardinerCode in state.glyphsLearned) map[inter.gardinerCode] = inter.targetGlyph
                                        else -> {}
                                    }
                                }
                            }
                            map
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            WadjetColors.Gold.copy(alpha = 0.15f),
                                            WadjetColors.Surface,
                                        ),
                                    ),
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val stSharedScope = LocalSharedTransitionScope.current
                            val stAnimScope = LocalAnimatedVisibilityScope.current
                            with(stSharedScope) {
                            Text(
                                text = story.coverGlyph,
                                fontSize = 64.sp,
                                fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs,
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "story-${story.id}"),
                                    animatedVisibilityScope = stAnimScope,
                                ),
                            )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.reader_story_complete),
                                style = MaterialTheme.typography.headlineSmall,
                                color = WadjetColors.Gold,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${state.score}", color = WadjetColors.Gold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.reader_score_label), color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${state.glyphsLearned.size}", color = WadjetColors.Gold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.reader_glyphs_label), color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            // Individual glyph badges
                            if (glyphUnicodes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    glyphUnicodes.forEach { (code, unicode) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(MaterialTheme.shapes.small)
                                                .background(WadjetColors.Gold.copy(alpha = 0.1f))
                                                .border(1.dp, WadjetColors.Gold.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                        ) {
                                            Text(
                                                text = unicode,
                                                fontSize = 28.sp,
                                                fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs,
                                            )
                                            Text(
                                                text = code,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WadjetColors.Sand,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ChapterNavButton(text = stringResource(R.string.reader_read_again), onClick = onReadAgain)
                                ChapterNavButton(text = stringResource(R.string.reader_back_to_stories), onClick = onBack)
                            }
                        }
                    }
                }
            }


        }
    }
}

@Composable
private fun SceneImage(
    imageUrl: String?,
    isLoading: Boolean,
    loadFailed: Boolean = false,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.medium)
            .background(WadjetColors.SurfaceAlt),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            // Ken Burns effect
            val transition = rememberInfiniteTransition(label = "kenburns")
            val scale = transition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "scale",
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.reader_scene_desc),
                placeholder = painterResource(DesignR.drawable.ic_placeholder_story),
                error = painterResource(DesignR.drawable.ic_placeholder_error),
                fallback = painterResource(DesignR.drawable.ic_placeholder_story),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
                contentScale = ContentScale.Crop,
            )
        } else if (isLoading) {
            com.wadjet.core.designsystem.component.ShimmerEffect(
                modifier = Modifier.fillMaxSize(),
            )
        } else if (loadFailed && onRetry != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "𓁟",
                    fontSize = 36.sp,
                    fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs,
                    color = WadjetColors.Gold.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.reader_image_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = WadjetColors.TextMuted,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    onClick = onRetry,
                    shape = MaterialTheme.shapes.small,
                    color = WadjetColors.Gold.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = stringResource(DesignR.string.action_retry),
                        color = WadjetColors.Gold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        } else {
            Text(
                text = "𓁟",
                fontSize = 48.sp,
                fontFamily = com.wadjet.core.designsystem.NotoSansEgyptianHieroglyphs,
                color = WadjetColors.Gold,
            )
        }
    }
}

@Composable
private fun ParagraphBlock(
    paragraph: Paragraph,
    isNarrating: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var selectedAnnotation by remember { mutableStateOf<GlyphAnnotation?>(null) }

    val borderAlpha = animateFloatAsState(
        targetValue = if (isNarrating) 1f else 0f,
        animationSpec = tween(300),
        label = "narrateBorder",
    )

    Column(
        modifier = modifier.then(
            if (borderAlpha.value > 0f) {
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(WadjetColors.Gold.copy(alpha = 0.06f * borderAlpha.value))
                    .border(
                        width = 1.dp,
                        color = WadjetColors.Gold.copy(alpha = 0.4f * borderAlpha.value),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(8.dp)
            } else {
                Modifier
            },
        ),
    ) {
        val annotatedText = buildAnnotatedString {
            val text = paragraph.textEn
            val annotations = paragraph.glyphAnnotations
            var lastEnd = 0

            annotations.forEach { annotation ->
                val wordStart = text.indexOf(annotation.wordEn, lastEnd)
                if (wordStart >= 0) {
                    // Text before annotation
                    if (wordStart > lastEnd) {
                        withStyle(SpanStyle(color = WadjetColors.Text)) {
                            append(text.substring(lastEnd, wordStart))
                        }
                    }
                    // Annotated word
                    pushStringAnnotation("glyph", annotation.gardinerCode)
                    withStyle(
                        SpanStyle(
                            color = WadjetColors.Gold,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) {
                        append(annotation.wordEn)
                    }
                    pop()
                    lastEnd = wordStart + annotation.wordEn.length
                }
            }
            // Remaining text
            if (lastEnd < text.length) {
                withStyle(SpanStyle(color = WadjetColors.Text)) {
                    append(text.substring(lastEnd))
                }
            }
        }

        val annotationMap = paragraph.glyphAnnotations.associateBy { it.gardinerCode }

        androidx.compose.foundation.text.ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge,
            onClick = { offset ->
                annotatedText.getStringAnnotations("glyph", offset, offset)
                    .firstOrNull()?.let { range ->
                        selectedAnnotation = annotationMap[range.item]
                    }
            },
        )

        // Annotation tooltip
        AnimatedVisibility(
            visible = selectedAnnotation != null,
            enter = fadeIn(),
        ) {
            selectedAnnotation?.let { ann ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(WadjetColors.Surface)
                        .border(1.dp, WadjetColors.Gold.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                        .clickable { selectedAnnotation = null }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ann.glyph,
                            fontSize = 36.sp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(ann.gardinerCode, color = WadjetColors.Sand, style = MaterialTheme.typography.labelSmall)
                            Text(ann.meaningEn, color = WadjetColors.Text, style = MaterialTheme.typography.bodyMedium)
                            Text(ann.transliteration, color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractionBlock(
    index: Int,
    interaction: Interaction,
    result: InteractionResult?,
    answer: String?,
    writeInput: String,
    onSubmit: (String) -> Unit,
    onWriteInputChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(WadjetColors.SurfaceAlt)
            .border(1.dp, WadjetColors.Border, MaterialTheme.shapes.medium)
            .padding(16.dp),
    ) {
        when (interaction) {
            is Interaction.ChooseGlyph -> {
                Text(
                    text = interaction.questionEn,
                    color = WadjetColors.Ivory,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    interaction.options.forEach { option ->
                        val isSelected = answer == option.code
                        val isCorrectAnswer = result != null && option.code == interaction.correctCode
                        val borderColor = when {
                            result != null && isCorrectAnswer -> WadjetColors.Success
                            result != null && isSelected && !result.correct -> WadjetColors.Error
                            isSelected -> WadjetColors.Gold
                            else -> WadjetColors.Border
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 88.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(WadjetColors.Surface)
                                .border(2.dp, borderColor, MaterialTheme.shapes.small)
                                .clickable(enabled = result == null) { onSubmit(option.code) }
                                .semantics { contentDescription = "${option.glyph} ${option.code}" },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = option.glyph, fontSize = 32.sp)
                                Text(
                                    text = option.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WadjetColors.TextMuted,
                                )
                            }
                        }
                    }
                }
            }

            is Interaction.WriteWord -> {
                Text(
                    text = stringResource(R.string.reader_write_prompt, interaction.targetWordEn),
                    color = WadjetColors.Ivory,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.reader_hint, interaction.hintEn),
                    color = WadjetColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val writeEnabled = result?.correct != true
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = interaction.targetGlyph,
                        fontSize = 36.sp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = writeInput,
                        onValueChange = onWriteInputChanged,
                        modifier = Modifier.weight(1f),
                        enabled = writeEnabled,
                        placeholder = { Text(stringResource(R.string.reader_write_placeholder, interaction.gardinerCode), color = WadjetColors.TextMuted) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = WadjetColors.Surface,
                            unfocusedContainerColor = WadjetColors.Surface,
                            focusedTextColor = WadjetColors.Text,
                            unfocusedTextColor = WadjetColors.Text,
                            cursorColor = WadjetColors.Gold,
                            focusedIndicatorColor = WadjetColors.Gold,
                            unfocusedIndicatorColor = WadjetColors.Border,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (writeInput.isNotBlank()) onSubmit(writeInput.trim())
                        }),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { if (writeInput.isNotBlank()) onSubmit(writeInput.trim()) },
                        enabled = writeEnabled && writeInput.isNotBlank(),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            stringResource(DesignR.string.action_submit),
                            tint = if (writeInput.isNotBlank()) WadjetColors.Gold else WadjetColors.TextMuted,
                        )
                    }
                }
            }

            is Interaction.GlyphDiscovery -> {
                Text(
                    text = interaction.promptEn,
                    color = WadjetColors.Ivory,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                val revealed = result != null
                val scale = animateFloatAsState(
                    targetValue = if (revealed) 1f else 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "glyphScale",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (revealed) WadjetColors.Gold.copy(alpha = 0.05f)
                            else WadjetColors.Surface,
                        )
                        .then(
                            if (revealed) Modifier.border(
                                1.dp,
                                WadjetColors.Gold.copy(alpha = 0.3f),
                                MaterialTheme.shapes.small,
                            ) else Modifier,
                        )
                        .clickable(enabled = !revealed) { onSubmit("reveal") }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (revealed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.graphicsLayer(
                                    scaleX = scale.value,
                                    scaleY = scale.value,
                                ),
                            ) {
                                Text(text = interaction.unicode, fontSize = 48.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(interaction.glyphCode, color = WadjetColors.Sand, style = MaterialTheme.typography.labelMedium)
                                    Text(interaction.meaningEn, color = WadjetColors.Text, style = MaterialTheme.typography.bodyMedium)
                                    Text(interaction.transliteration, color = WadjetColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = WadjetColors.Gold,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.reader_glyph_learned),
                                    color = WadjetColors.Gold,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    } else {
                        Text(stringResource(R.string.reader_tap_discover), color = WadjetColors.Gold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            is Interaction.StoryDecision -> {
                Text(
                    text = interaction.promptEn,
                    color = WadjetColors.Ivory,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                interaction.choices.forEach { choice ->
                    val isSelected = answer == choice.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (isSelected) WadjetColors.Gold.copy(alpha = 0.15f) else WadjetColors.Surface)
                            .border(
                                1.dp,
                                if (isSelected) WadjetColors.Gold else WadjetColors.Border,
                                MaterialTheme.shapes.small,
                            )
                            .clickable(enabled = result == null) { onSubmit(choice.id) }
                            .padding(12.dp),
                    ) {
                        Text(
                            text = choice.textEn,
                            color = if (isSelected) WadjetColors.Gold else WadjetColors.Text,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        // Feedback
        if (result != null) {
            Spacer(modifier = Modifier.height(12.dp))
            FeedbackBanner(result = result, interaction = interaction)
        }
    }
}

@Composable
private fun FeedbackBanner(
    result: InteractionResult,
    interaction: Interaction,
    modifier: Modifier = Modifier,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isDecision = interaction is Interaction.StoryDecision

    // Haptic on correct answer
    LaunchedEffect(result.correct) {
        if (result.correct && !isDecision) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }
    val bgColor = when {
        isDecision -> WadjetColors.Gold.copy(alpha = 0.1f)
        result.correct -> WadjetColors.Success.copy(alpha = 0.12f)
        else -> WadjetColors.Error.copy(alpha = 0.12f)
    }
    val iconColor = when {
        isDecision -> WadjetColors.Gold
        result.correct -> WadjetColors.Success
        else -> WadjetColors.Error
    }
    val icon = when {
        isDecision -> Icons.AutoMirrored.Filled.ArrowForward
        result.correct -> Icons.Default.Check
        else -> Icons.Default.Close
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(bgColor)
                .then(
                    if (isDecision) Modifier.border(
                        1.dp,
                        WadjetColors.Gold.copy(alpha = 0.2f),
                        MaterialTheme.shapes.small,
                    ) else Modifier,
                )
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                if (!isDecision) {
                    Text(
                        text = if (result.correct) stringResource(R.string.reader_correct) else stringResource(R.string.reader_incorrect),
                        color = iconColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                val explanation = result.explanationEn
                    ?: result.outcomeEn
                    ?: (interaction as? Interaction.ChooseGlyph)?.explanationEn
                if (!explanation.isNullOrBlank()) {
                    Text(
                        text = explanation,
                        color = if (isDecision) WadjetColors.Gold else WadjetColors.Text,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterNavButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(WadjetColors.Surface)
            .border(1.dp, WadjetColors.Gold, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = WadjetColors.Gold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
