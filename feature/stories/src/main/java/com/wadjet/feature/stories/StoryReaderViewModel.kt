package com.wadjet.feature.stories

import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.domain.model.Chapter
import com.wadjet.core.domain.model.Interaction
import com.wadjet.core.domain.model.InteractionResult
import com.wadjet.core.domain.model.StoryFull
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

data class ReaderUiState(
    val story: StoryFull? = null,
    val currentChapter: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val sceneImageUrl: String? = null,
    val isLoadingImage: Boolean = false,
    val imageLoadFailed: Boolean = false,
    val interactionResults: Map<Int, InteractionResult> = emptyMap(),
    val interactionAnswers: Map<Int, String> = emptyMap(),
    val writeInputs: Map<Int, String> = emptyMap(),
    val score: Int = 0,
    val glyphsLearned: MutableSet<String> = mutableSetOf(),
    val isSpeaking: Boolean = false,
    val isNarrating: Boolean = false,
    val narratingParagraphIndex: Int = -1,
    val showAnnotation: Int? = null, // paragraph-level annotation index
) {
    val chapter: Chapter? get() = story?.chapters?.getOrNull(currentChapter)
    val totalChapters: Int get() = story?.chapters?.size ?: 0
    val canGoPrev: Boolean get() = currentChapter > 0
    val canGoNext: Boolean get() = currentChapter < totalChapters - 1
    val chapterProgress: Float
        get() = if (totalChapters > 0) (currentChapter + 1).toFloat() / totalChapters else 0f
}

@HiltViewModel
class StoryReaderViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val toastController: com.wadjet.core.common.ToastController,
    private val sharedPreferences: SharedPreferences,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val storyId: String = savedStateHandle.get<String>("storyId") ?: ""

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadStory()
        restoreProgress()
    }

    fun goToChapter(index: Int) {
        val total = _state.value.totalChapters
        if (index < 0 || index >= total) return
        stopNarration()
        saveCurrentProgress()
        _state.update {
            it.copy(
                currentChapter = index,
                sceneImageUrl = null,
                imageLoadFailed = false,
                interactionResults = emptyMap(),
                interactionAnswers = emptyMap(),
                writeInputs = emptyMap(),
                showAnnotation = null,
            )
        }
        loadChapterImage()
    }

    fun nextChapter() = goToChapter(_state.value.currentChapter + 1)
    fun prevChapter() = goToChapter(_state.value.currentChapter - 1)

    fun restartStory() {
        _state.update {
            it.copy(
                score = 0,
                glyphsLearned = mutableSetOf(),
            )
        }
        goToChapter(0)
    }

    fun submitAnswer(interactionIndex: Int, answer: String) {
        val existing = _state.value.interactionResults[interactionIndex]
        val interaction = _state.value.chapter?.interactions?.getOrNull(interactionIndex)
        // Allow retry for WriteWord when previous answer was incorrect
        if (existing != null) {
            if (existing.correct) return
            if (interaction !is Interaction.WriteWord) return
            _state.update { it.copy(interactionResults = it.interactionResults - interactionIndex) }
        }

        _state.update {
            it.copy(interactionAnswers = it.interactionAnswers + (interactionIndex to answer))
        }

        viewModelScope.launch {
            storiesRepository.interact(
                storyId = storyId,
                chapterIndex = _state.value.currentChapter,
                interactionIndex = interactionIndex,
                answer = answer,
            ).onSuccess { result ->
                _state.update { state ->
                    val newScore = if (result.correct) state.score + 1 else state.score
                    val newGlyphs = state.glyphsLearned.toMutableSet()
                    // Add glyph to learned set for discovery/choose types
                    val interaction = state.chapter?.interactions?.getOrNull(interactionIndex)
                    when (interaction) {
                        is Interaction.GlyphDiscovery -> newGlyphs.add(interaction.glyphCode)
                        is Interaction.ChooseGlyph -> if (result.correct) newGlyphs.add(interaction.correctCode)
                        is Interaction.WriteWord -> if (result.correct) newGlyphs.add(interaction.gardinerCode)
                        else -> {}
                    }
                    state.copy(
                        interactionResults = state.interactionResults + (interactionIndex to result),
                        score = newScore,
                        glyphsLearned = newGlyphs,
                    )
                }
            }.onFailure { error ->
                Timber.e(error, "Interaction failed")
                _state.update { it.copy(error = error.message) }
            }
        }
    }

    fun updateWriteInput(interactionIndex: Int, text: String) {
        _state.update {
            it.copy(writeInputs = it.writeInputs + (interactionIndex to text))
        }
    }

    private var narrationJob: Job? = null

    fun speakChapter() {
        if (_state.value.isNarrating || _state.value.isSpeaking) {
            stopNarration()
            return
        }
        val chapter = _state.value.chapter ?: return
        val paragraphs = chapter.paragraphs
        if (paragraphs.isEmpty()) return

        _state.update { it.copy(isNarrating = true, isSpeaking = true, narratingParagraphIndex = 0) }
        toastController.info("Generating narration\u2026")
        narrationJob = viewModelScope.launch {
            for ((idx, paragraph) in paragraphs.withIndex()) {
                if (!_state.value.isNarrating) break
                _state.update { it.copy(narratingParagraphIndex = idx) }

                val spoken = speakAndWait(paragraph.textEn, chapter.ttsVoice, chapter.ttsStyle)
                if (!spoken || !_state.value.isNarrating) break
            }
            _state.update { it.copy(isNarrating = false, isSpeaking = false, narratingParagraphIndex = -1) }
        }
    }

    private fun stopNarration() {
        narrationJob?.cancel()
        narrationJob = null
        stopSpeaking()
        _state.update { it.copy(isNarrating = false, narratingParagraphIndex = -1) }
    }

    /**
     * Speaks text via server TTS and suspends until playback finishes.
     * Returns true if playback completed, false if it should fall back to local TTS / failed.
     */
    private suspend fun speakAndWait(text: String, voice: String?, style: String?): Boolean {
        return suspendCancellableCoroutine { cont ->
            viewModelScope.launch {
                storiesRepository.speakChapter(text, voice = voice, style = style)
                    .onSuccess { bytes ->
                        if (bytes != null) {
                            playWavBytesAndWait(bytes) { cont.resume(true) }
                        } else {
                            // LOCAL_TTS fallback
                            _state.update { it.copy(error = "LOCAL_TTS:$text") }
                            // Give local TTS time to speak — roughly 80ms per word
                            val estimatedMs = text.split(" ").size * 80L + 500L
                            kotlinx.coroutines.delay(estimatedMs)
                            if (cont.isActive) cont.resume(true)
                        }
                    }
                    .onFailure {
                        Timber.e(it, "Narration TTS failed for paragraph")
                        if (cont.isActive) cont.resume(false)
                    }
            }
            cont.invokeOnCancellation { stopSpeaking() }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun loadStory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            storiesRepository.getStory(storyId)
                .onSuccess { story ->
                    _state.update { it.copy(story = story, isLoading = false) }
                    loadChapterImage()
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load story")
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun loadChapterImage() {
        val chapter = _state.value.chapter ?: return
        val chapterIdx = _state.value.currentChapter

        // 1. Pre-generated URL from API
        chapter.sceneImageUrl?.let { url ->
            _state.update { it.copy(sceneImageUrl = url, isLoadingImage = false, imageLoadFailed = false) }
            return
        }

        // 2. Check SharedPreferences cache
        val cacheKey = "scene_img_${storyId}_$chapterIdx"
        sharedPreferences.getString(cacheKey, null)?.let { cached ->
            _state.update { it.copy(sceneImageUrl = cached, isLoadingImage = false, imageLoadFailed = false) }
            return
        }

        // 3. Generate on-demand via POST
        _state.update { it.copy(isLoadingImage = true, imageLoadFailed = false) }
        toastController.info("Generating scene image\u2026")
        viewModelScope.launch {
            storiesRepository.generateChapterImage(storyId, chapterIdx)
                .onSuccess { url ->
                    if (url != null) {
                        sharedPreferences.edit().putString(cacheKey, url).apply()
                    }
                    _state.update { it.copy(sceneImageUrl = url, isLoadingImage = false, imageLoadFailed = url == null) }
                }
                .onFailure {
                    Timber.w(it, "Chapter image generation failed")
                    _state.update { it.copy(isLoadingImage = false, imageLoadFailed = true) }
                }
        }
    }

    fun retryChapterImage() {
        loadChapterImage()
    }

    private fun restoreProgress() {
        viewModelScope.launch {
            storiesRepository.getStoryProgress(storyId).collect { progress ->
                if (progress != null && _state.value.story != null && _state.value.currentChapter == 0) {
                    _state.update {
                        it.copy(
                            currentChapter = progress.chapterIndex,
                            score = progress.score,
                            glyphsLearned = progress.glyphsLearned.toMutableSet(),
                        )
                    }
                    loadChapterImage()
                }
            }
        }
    }

    private fun saveCurrentProgress() {
        viewModelScope.launch {
            storiesRepository.saveProgress(
                StoryProgress(
                    storyId = storyId,
                    chapterIndex = _state.value.currentChapter,
                    glyphsLearned = _state.value.glyphsLearned.toList(),
                    score = _state.value.score,
                    completed = _state.value.currentChapter >= _state.value.totalChapters - 1,
                ),
            )
        }
    }

    private fun playWavBytesAndWait(bytes: ByteArray, onDone: () -> Unit) {
        try {
            val tempFile = File.createTempFile("story_tts_", ".wav")
            tempFile.writeBytes(bytes)
            tempFile.deleteOnExit()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    onDone()
                }
                setOnErrorListener { _, _, _ ->
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    onDone()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Timber.e(e, "MediaPlayer failed")
            onDone()
        }
    }

    private fun stopSpeaking() {
        try {
            mediaPlayer?.apply { if (isPlaying) stop(); release() }
        } catch (_: Exception) {}
        mediaPlayer = null
        _state.update { it.copy(isSpeaking = false) }
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentProgress()
        stopNarration()
    }
}
