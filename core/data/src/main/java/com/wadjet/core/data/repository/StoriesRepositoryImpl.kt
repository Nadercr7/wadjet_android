package com.wadjet.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.domain.model.Chapter
import com.wadjet.core.domain.model.DecisionChoice
import com.wadjet.core.domain.model.GlyphAnnotation
import com.wadjet.core.domain.model.GlyphOption
import com.wadjet.core.domain.model.Interaction
import com.wadjet.core.domain.model.InteractionResult
import com.wadjet.core.domain.model.Paragraph
import com.wadjet.core.domain.model.StoryFull
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import com.wadjet.core.domain.repository.StoriesRepository
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.StoriesApiService
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.model.InteractRequest
import com.wadjet.core.network.model.InteractionDto
import com.wadjet.core.network.model.SaveProgressRequest
import com.wadjet.core.network.model.SpeakRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoriesRepositoryImpl @Inject constructor(
    private val storiesApi: StoriesApiService,
    private val audioApi: AudioApiService,
    private val userApi: UserApiService,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : StoriesRepository {

    override suspend fun getStories(): Result<List<StorySummary>> = suspendRunCatching {
        val response = storiesApi.getStories()
        if (response.isSuccessful) {
            response.body()?.stories?.map { dto ->
                StorySummary(
                    id = dto.id,
                    titleEn = dto.title.en,
                    titleAr = dto.title.ar,
                    subtitleEn = dto.subtitle.en,
                    subtitleAr = dto.subtitle.ar,
                    coverGlyph = dto.coverGlyph,
                    difficulty = dto.difficulty,
                    estimatedMinutes = dto.estimatedMinutes,
                    chapterCount = dto.chapterCount,
                    glyphsTaught = dto.glyphsTaught,
                )
            } ?: emptyList()
        } else {
            throw Exception("Failed to load stories: ${response.code()}")
        }
    }

    override suspend fun getStory(storyId: String): Result<StoryFull> = suspendRunCatching {
        val response = storiesApi.getStory(storyId)
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Empty story response")
            StoryFull(
                id = dto.id,
                titleEn = dto.title.en,
                titleAr = dto.title.ar,
                subtitleEn = dto.subtitle.en,
                subtitleAr = dto.subtitle.ar,
                coverGlyph = dto.coverGlyph,
                difficulty = dto.difficulty,
                estimatedMinutes = dto.estimatedMinutes,
                glyphsTaught = dto.glyphsTaught,
                chapters = dto.chapters.map { ch ->
                    Chapter(
                        index = ch.index,
                        titleEn = ch.title.en,
                        titleAr = ch.title.ar,
                        sceneImageUrl = ch.sceneImageUrl,
                        ttsVoice = ch.ttsVoice,
                        ttsStyle = ch.ttsStyle,
                        paragraphs = ch.paragraphs.map { p ->
                            Paragraph(
                                textEn = p.text.en,
                                textAr = p.text.ar,
                                glyphAnnotations = p.glyphAnnotations.map { a ->
                                    GlyphAnnotation(
                                        wordEn = a.word.en,
                                        wordAr = a.word.ar,
                                        gardinerCode = a.gardinerCode,
                                        glyph = a.glyph,
                                        meaningEn = a.meaning.en,
                                        meaningAr = a.meaning.ar,
                                        transliteration = a.transliteration,
                                    )
                                },
                            )
                        },
                        interactions = ch.interactions.mapNotNull { it.toDomain() },
                    )
                },
            )
        } else {
            throw Exception("Failed to load story: ${response.code()}")
        }
    }

    override suspend fun interact(
        storyId: String,
        chapterIndex: Int,
        interactionIndex: Int,
        answer: String,
    ): Result<InteractionResult> = suspendRunCatching {
        val response = storiesApi.interact(
            storyId = storyId,
            body = InteractRequest(
                chapterIndex = chapterIndex,
                interactionIndex = interactionIndex,
                answer = answer,
            ),
        )
        if (response.isSuccessful) {
            val r = response.body() ?: throw Exception("Empty interact response")
            InteractionResult(
                correct = r.correct,
                type = r.type,
                explanationEn = r.explanation?.en,
                explanationAr = r.explanation?.ar,
                outcomeEn = r.outcome?.en,
                outcomeAr = r.outcome?.ar,
            )
        } else {
            throw Exception("Interaction failed: ${response.code()}")
        }
    }

    override suspend fun generateChapterImage(
        storyId: String,
        chapterIndex: Int,
    ): Result<String?> = suspendRunCatching {
        val response = storiesApi.generateChapterImage(storyId, chapterIndex)
        if (response.isSuccessful) {
            response.body()?.imageUrl
        } else {
            throw Exception("Image generation failed: ${response.code()}")
        }
    }

    override suspend fun speakChapter(text: String, voice: String?, style: String?): Result<ByteArray?> = suspendRunCatching {
        val response = audioApi.speak(SpeakRequest(text = text, lang = "en", context = "story_narration", voice = voice, style = style))
        when (response.code()) {
            200 -> response.body()?.bytes()
            204 -> null
            else -> throw Exception("TTS failed: ${response.code()}")
        }
    }

    override fun getStoryProgress(storyId: String): Flow<StoryProgress?> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            trySend(null)
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .collection("story_progress").document(storyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Story progress listen failed")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                @Suppress("UNCHECKED_CAST")
                val progress = StoryProgress(
                    storyId = snapshot.getString("story_id") ?: storyId,
                    chapterIndex = (snapshot.getLong("chapter_index") ?: 0).toInt(),
                    glyphsLearned = (snapshot.get("glyphs_learned") as? List<String>) ?: emptyList(),
                    score = (snapshot.getLong("score") ?: 0).toInt(),
                    completed = snapshot.getBoolean("completed") ?: false,
                )
                trySend(progress)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllProgress(): Flow<Map<String, StoryProgress>> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            trySend(emptyMap())
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .collection("story_progress")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "All progress listen failed")
                    trySend(emptyMap())
                    return@addSnapshotListener
                }
                @Suppress("UNCHECKED_CAST")
                val map = snapshot?.documents?.associate { doc ->
                    val id = doc.id
                    id to StoryProgress(
                        storyId = id,
                        chapterIndex = (doc.getLong("chapter_index") ?: 0).toInt(),
                        glyphsLearned = (doc.get("glyphs_learned") as? List<String>) ?: emptyList(),
                        score = (doc.getLong("score") ?: 0).toInt(),
                        completed = doc.getBoolean("completed") ?: false,
                    )
                } ?: emptyMap()
                trySend(map)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveProgress(progress: StoryProgress) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val data = mapOf(
            "story_id" to progress.storyId,
            "chapter_index" to progress.chapterIndex,
            "glyphs_learned" to progress.glyphsLearned,
            "score" to progress.score,
            "completed" to progress.completed,
            "updated_at" to com.google.firebase.Timestamp.now(),
        )
        try {
            firestore.collection("users").document(uid)
                .collection("story_progress").document(progress.storyId)
                .set(data).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to save story progress to Firestore")
        }
        // Also sync to REST API
        try {
            userApi.saveProgress(
                SaveProgressRequest(
                    storyId = progress.storyId,
                    chapterIndex = progress.chapterIndex,
                    glyphsLearned = progress.glyphsLearned,
                    score = progress.score,
                    completed = progress.completed,
                ),
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to sync progress to REST API")
        }
    }

    private fun InteractionDto.toDomain(): Interaction? = when (type) {
        "choose_glyph" -> Interaction.ChooseGlyph(
            afterParagraph = afterParagraph,
            questionEn = question?.en ?: "",
            questionAr = question?.ar ?: "",
            options = options?.map { GlyphOption(code = it.code, glyph = it.glyph) } ?: emptyList(),
            correctCode = correct ?: "",
            explanationEn = explanation?.en ?: "",
            explanationAr = explanation?.ar ?: "",
        )
        "write_word" -> Interaction.WriteWord(
            afterParagraph = afterParagraph,
            targetWordEn = targetWord?.en ?: "",
            targetWordAr = targetWord?.ar ?: "",
            targetGlyph = targetGlyph ?: "",
            gardinerCode = gardinerCode ?: "",
            hintEn = hint?.en ?: "",
            hintAr = hint?.ar ?: "",
        )
        "glyph_discovery" -> Interaction.GlyphDiscovery(
            afterParagraph = afterParagraph,
            glyphCode = glyph ?: "",
            unicode = unicode ?: glyph ?: "",
            promptEn = prompt?.en ?: "",
            promptAr = prompt?.ar ?: "",
            meaningEn = meaning?.en ?: "",
            meaningAr = meaning?.ar ?: "",
            transliteration = transliteration ?: "",
        )
        "story_decision" -> Interaction.StoryDecision(
            afterParagraph = afterParagraph,
            promptEn = prompt?.en ?: question?.en ?: "",
            promptAr = prompt?.ar ?: question?.ar ?: "",
            choices = choices?.map {
                DecisionChoice(
                    id = it.id,
                    textEn = it.text.en,
                    textAr = it.text.ar,
                    outcomeEn = it.outcome.en,
                    outcomeAr = it.outcome.ar,
                )
            } ?: emptyList(),
        )
        else -> {
            Timber.w("Unknown interaction type: $type")
            null
        }
    }
}
