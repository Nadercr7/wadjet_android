package com.wadjet.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.data.ApiException
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
import com.wadjet.core.network.model.StoryFullDto
import com.wadjet.core.network.model.StorySummaryDto
import com.wadjet.core.database.dao.StoryCacheDao
import com.wadjet.core.database.dao.StoryProgressDao
import com.wadjet.core.database.entity.StoryCacheEntity
import com.wadjet.core.database.entity.StoryProgressEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    private val storyProgressDao: StoryProgressDao,
    private val storyCacheDao: StoryCacheDao,
) : StoriesRepository {

    private val cacheJson = Json { ignoreUnknownKeys = true }

    override suspend fun getStories(): Result<List<StorySummary>> = suspendRunCatching {
        try {
            val response = storiesApi.getStories()
            if (response.isSuccessful) {
                val dtos = response.body()?.stories ?: emptyList()
                cacheSummaries(dtos)
                dtos.map { it.toDomain() }
            } else {
                throw ApiException("Failed to load stories: ${response.code()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // E-02: offline fallback — serve previously fetched stories from Room
            val cached = storyCacheDao.getAll().mapNotNull { row ->
                runCatching { cacheJson.decodeFromString<StorySummaryDto>(row.summaryJson).toDomain() }.getOrNull()
            }
            if (cached.isEmpty()) throw e
            Timber.w(e, "getStories: network failed, serving %d cached stories", cached.size)
            cached
        }
    }

    private fun StorySummaryDto.toDomain() = StorySummary(
        id = id,
        titleEn = title.en,
        titleAr = title.ar,
        subtitleEn = subtitle.en,
        subtitleAr = subtitle.ar,
        coverGlyph = coverGlyph,
        difficulty = difficulty,
        estimatedMinutes = estimatedMinutes,
        chapterCount = chapterCount,
        glyphsTaught = glyphsTaught,
    )

    private suspend fun cacheSummaries(dtos: List<StorySummaryDto>) {
        try {
            val now = System.currentTimeMillis()
            val entities = dtos.mapIndexed { index, dto ->
                StoryCacheEntity(
                    id = dto.id,
                    summaryJson = cacheJson.encodeToString(dto),
                    fullJson = storyCacheDao.getById(dto.id)?.fullJson,
                    sortOrder = index,
                    updatedAt = now,
                )
            }
            storyCacheDao.upsertAll(entities)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.w(e, "Failed to cache story summaries")
        }
    }

    override suspend fun getStory(storyId: String): Result<StoryFull> = suspendRunCatching {
        try {
            val response = storiesApi.getStory(storyId)
            if (response.isSuccessful) {
                val dto = response.body() ?: throw ApiException("Empty story response")
                cacheFullStory(dto)
                dto.toDomain()
            } else {
                throw ApiException("Failed to load story: ${response.code()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // E-02: offline fallback — serve the cached full story if we have it
            val cached = storyCacheDao.getById(storyId)?.fullJson?.let { fullJson ->
                runCatching { cacheJson.decodeFromString<StoryFullDto>(fullJson).toDomain() }.getOrNull()
            } ?: throw e
            Timber.w(e, "getStory(%s): network failed, serving cached copy", storyId)
            cached
        }
    }

    private suspend fun cacheFullStory(dto: StoryFullDto) {
        try {
            val existing = storyCacheDao.getById(dto.id)
            storyCacheDao.upsert(
                StoryCacheEntity(
                    id = dto.id,
                    summaryJson = existing?.summaryJson ?: "",
                    fullJson = cacheJson.encodeToString(dto),
                    sortOrder = existing?.sortOrder ?: Int.MAX_VALUE,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.w(e, "Failed to cache story %s", dto.id)
        }
    }

    private fun StoryFullDto.toDomain(): StoryFull {
        val dto = this
        return StoryFull(
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
            val r = response.body() ?: throw ApiException("Empty interact response")
            InteractionResult(
                correct = r.correct,
                type = r.type,
                explanationEn = r.explanation?.en,
                explanationAr = r.explanation?.ar,
                outcomeEn = r.outcome?.en,
                outcomeAr = r.outcome?.ar,
                correctAnswer = r.correctAnswer,
                targetGlyph = r.targetGlyph,
                gardinerCode = r.gardinerCode,
                hintEn = r.hint?.en,
                hintAr = r.hint?.ar,
                choiceId = r.choiceId,
            )
        } else {
            throw ApiException("Interaction failed: ${response.code()}")
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
            throw ApiException("Image generation failed: ${response.code()}")
        }
    }

    override suspend fun speakChapter(text: String, lang: String): Result<ByteArray?> = suspendRunCatching {
        val response = audioApi.speak(SpeakRequest(text = text, lang = lang, context = "story_narration"))
        when (response.code()) {
            200 -> response.body()?.bytes()
            204 -> null
            else -> throw ApiException("TTS failed: ${response.code()}")
        }
    }

    override fun getStoryProgress(storyId: String): Flow<StoryProgress?> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            // No user — try local cache
            val cached = storyProgressDao.getByStoryId(storyId)?.toDomain()
            trySend(cached)
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .collection("story_progress").document(storyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Story progress listen failed, falling back to Room")
                    val dao = storyProgressDao
                    val sid = storyId
                    CoroutineScope(Dispatchers.IO).launch {
                        trySend(dao.getByStoryId(sid)?.toDomain())
                    }
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
            // No user — try local cache
            val cached = storyProgressDao.getAll().associate { it.storyId to it.toDomain() }
            trySend(cached)
            awaitClose()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .collection("story_progress")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "All progress listen failed, falling back to Room")
                    val dao = storyProgressDao
                    CoroutineScope(Dispatchers.IO).launch {
                        trySend(dao.getAll().associate { it.storyId to it.toDomain() })
                    }
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
        // Always persist to Room first (offline-safe)
        storyProgressDao.upsert(progress.toEntity())

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
                    glyphsLearned = Json.encodeToString(progress.glyphsLearned),
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

    private fun StoryProgressEntity.toDomain() = StoryProgress(
        storyId = storyId,
        chapterIndex = chapterIndex,
        glyphsLearned = glyphsLearnedJson
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() },
        score = score,
        completed = completed,
    )

    private fun StoryProgress.toEntity() = StoryProgressEntity(
        storyId = storyId,
        chapterIndex = chapterIndex,
        glyphsLearnedJson = "[${glyphsLearned.joinToString(",") { "\"$it\"" }}]",
        score = score,
        completed = completed,
        updatedAt = System.currentTimeMillis(),
    )
}
