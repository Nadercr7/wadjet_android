package com.wadjet.core.data.repository

import com.wadjet.core.data.ApiException
import com.wadjet.core.database.dao.StoryProgressDao
import com.wadjet.core.database.entity.StoryProgressEntity
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.StoriesApiService
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.model.BilingualText
import com.wadjet.core.network.model.ChapterDto
import com.wadjet.core.network.model.ChapterImageResponse
import com.wadjet.core.network.model.GlyphAnnotationDto
import com.wadjet.core.network.model.GlyphOptionDto
import com.wadjet.core.network.model.InteractResponse
import com.wadjet.core.network.model.InteractionDto
import com.wadjet.core.network.model.ParagraphDto
import com.wadjet.core.network.model.StoriesListResponse
import com.wadjet.core.network.model.StoryChoiceDto
import com.wadjet.core.network.model.StoryFullDto
import com.wadjet.core.network.model.StorySummaryDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class StoriesRepositoryImplTest {

    private val storiesApi: StoriesApiService = mockk()
    private val audioApi: AudioApiService = mockk()
    private val userApi: UserApiService = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val storyProgressDao: StoryProgressDao = mockk(relaxed = true)

    private lateinit var repository: StoriesRepositoryImpl

    private val testSummaryDto = StorySummaryDto(
        id = "story-1",
        title = BilingualText(en = "The Eye of Horus", ar = "عين حورس"),
        subtitle = BilingualText(en = "A tale of ancient magic", ar = "حكاية سحر قديم"),
        coverGlyph = "𓂀",
        difficulty = "beginner",
        estimatedMinutes = 15,
        chapterCount = 3,
        glyphsTaught = listOf("D10", "G5", "A1"),
    )

    private val testStoryFullDto = StoryFullDto(
        id = "story-1",
        title = BilingualText(en = "The Eye of Horus", ar = "عين حورس"),
        subtitle = BilingualText(en = "A tale of ancient magic", ar = "حكاية سحر قديم"),
        coverGlyph = "𓂀",
        difficulty = "beginner",
        estimatedMinutes = 15,
        glyphsTaught = listOf("D10", "G5"),
        chapters = listOf(
            ChapterDto(
                index = 0,
                title = BilingualText(en = "Chapter 1", ar = "الفصل الأول"),
                ttsVoice = "Aoede",
                ttsStyle = "narrative",
                paragraphs = listOf(
                    ParagraphDto(
                        text = BilingualText(en = "In ancient Egypt...", ar = "في مصر القديمة..."),
                        glyphAnnotations = listOf(
                            GlyphAnnotationDto(
                                word = BilingualText(en = "eye", ar = "عين"),
                                gardinerCode = "D10",
                                glyph = "𓂀",
                                meaning = BilingualText(en = "eye of Horus", ar = "عين حورس"),
                                transliteration = "wḏꜣt",
                            ),
                        ),
                    ),
                ),
                interactions = listOf(
                    InteractionDto(
                        type = "choose_glyph",
                        afterParagraph = 0,
                        question = BilingualText(en = "Which glyph means eye?", ar = "أي رمز يعني عين؟"),
                        options = listOf(
                            GlyphOptionDto(code = "D10", glyph = "𓂀"),
                            GlyphOptionDto(code = "A1", glyph = "𓀀"),
                        ),
                        correct = "D10",
                        explanation = BilingualText(en = "D10 is the Eye of Horus", ar = "D10 هو عين حورس"),
                    ),
                    InteractionDto(
                        type = "write_word",
                        afterParagraph = 0,
                        targetWord = BilingualText(en = "eye", ar = "عين"),
                        targetGlyph = "𓂀",
                        gardinerCode = "D10",
                        hint = BilingualText(en = "It's the eye symbol", ar = "إنه رمز العين"),
                    ),
                    InteractionDto(
                        type = "glyph_discovery",
                        afterParagraph = 0,
                        glyph = "𓂀",
                        unicode = "𓂀",
                        prompt = BilingualText(en = "Discover this glyph", ar = "اكتشف هذا الرمز"),
                        meaning = BilingualText(en = "eye", ar = "عين"),
                        transliteration = "wḏꜣt",
                    ),
                    InteractionDto(
                        type = "story_decision",
                        afterParagraph = 0,
                        prompt = BilingualText(en = "What do you do?", ar = "ماذا تفعل؟"),
                        choices = listOf(
                            StoryChoiceDto(
                                id = "choice-1",
                                text = BilingualText(en = "Go left", ar = "اذهب يسارًا"),
                                outcome = BilingualText(en = "You found a treasure", ar = "وجدت كنزًا"),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    @Before
    fun setup() {
        every { firebaseAuth.currentUser } returns null
        repository = StoriesRepositoryImpl(storiesApi, audioApi, userApi, firebaseAuth, firestore, storyProgressDao)
    }

    @Test
    fun `getStories maps response to domain summaries`() = runTest {
        coEvery { storiesApi.getStories() } returns Response.success(
            StoriesListResponse(stories = listOf(testSummaryDto), count = 1),
        )

        val result = repository.getStories()
        assertTrue(result.isSuccess)

        val stories = result.getOrThrow()
        assertEquals(1, stories.size)
        assertEquals("story-1", stories[0].id)
        assertEquals("The Eye of Horus", stories[0].titleEn)
        assertEquals("عين حورس", stories[0].titleAr)
        assertEquals("beginner", stories[0].difficulty)
        assertEquals(3, stories[0].chapterCount)
        assertEquals(listOf("D10", "G5", "A1"), stories[0].glyphsTaught)
    }

    @Test
    fun `getStories returns empty list on null body`() = runTest {
        coEvery { storiesApi.getStories() } returns Response.success(
            StoriesListResponse(stories = emptyList(), count = 0),
        )

        val result = repository.getStories()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `getStories fails on HTTP error`() = runTest {
        coEvery { storiesApi.getStories() } returns Response.error(
            500,
            "error".toResponseBody(),
        )

        val result = repository.getStories()
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ApiException)
    }

    @Test
    fun `getStory maps full story with chapters and interactions`() = runTest {
        coEvery { storiesApi.getStory("story-1") } returns Response.success(testStoryFullDto)

        val result = repository.getStory("story-1")
        assertTrue(result.isSuccess)

        val story = result.getOrThrow()
        assertEquals("story-1", story.id)
        assertEquals("The Eye of Horus", story.titleEn)
        assertEquals(1, story.chapters.size)

        val chapter = story.chapters[0]
        assertEquals(0, chapter.index)
        assertEquals("Chapter 1", chapter.titleEn)
        assertEquals("Aoede", chapter.ttsVoice)
        assertEquals(1, chapter.paragraphs.size)
        assertEquals("In ancient Egypt...", chapter.paragraphs[0].textEn)

        // Verify glyph annotations
        assertEquals(1, chapter.paragraphs[0].glyphAnnotations.size)
        assertEquals("D10", chapter.paragraphs[0].glyphAnnotations[0].gardinerCode)
        assertEquals("wḏꜣt", chapter.paragraphs[0].glyphAnnotations[0].transliteration)

        // Verify 4 interaction types
        assertEquals(4, chapter.interactions.size)
    }

    @Test
    fun `getStory choose_glyph interaction mapped correctly`() = runTest {
        coEvery { storiesApi.getStory("story-1") } returns Response.success(testStoryFullDto)

        val interaction = repository.getStory("story-1").getOrThrow().chapters[0].interactions[0]
        assertTrue(interaction is com.wadjet.core.domain.model.Interaction.ChooseGlyph)
        val cg = interaction as com.wadjet.core.domain.model.Interaction.ChooseGlyph
        assertEquals("Which glyph means eye?", cg.questionEn)
        assertEquals(2, cg.options.size)
        assertEquals("D10", cg.correctCode)
    }

    @Test
    fun `getStory write_word interaction mapped correctly`() = runTest {
        coEvery { storiesApi.getStory("story-1") } returns Response.success(testStoryFullDto)

        val interaction = repository.getStory("story-1").getOrThrow().chapters[0].interactions[1]
        assertTrue(interaction is com.wadjet.core.domain.model.Interaction.WriteWord)
        val ww = interaction as com.wadjet.core.domain.model.Interaction.WriteWord
        assertEquals("eye", ww.targetWordEn)
        assertEquals("𓂀", ww.targetGlyph)
        assertEquals("D10", ww.gardinerCode)
    }

    @Test
    fun `getStory glyph_discovery interaction mapped correctly`() = runTest {
        coEvery { storiesApi.getStory("story-1") } returns Response.success(testStoryFullDto)

        val interaction = repository.getStory("story-1").getOrThrow().chapters[0].interactions[2]
        assertTrue(interaction is com.wadjet.core.domain.model.Interaction.GlyphDiscovery)
        val gd = interaction as com.wadjet.core.domain.model.Interaction.GlyphDiscovery
        assertEquals("𓂀", gd.glyphCode)
        assertEquals("wḏꜣt", gd.transliteration)
    }

    @Test
    fun `getStory story_decision interaction mapped correctly`() = runTest {
        coEvery { storiesApi.getStory("story-1") } returns Response.success(testStoryFullDto)

        val interaction = repository.getStory("story-1").getOrThrow().chapters[0].interactions[3]
        assertTrue(interaction is com.wadjet.core.domain.model.Interaction.StoryDecision)
        val sd = interaction as com.wadjet.core.domain.model.Interaction.StoryDecision
        assertEquals("What do you do?", sd.promptEn)
        assertEquals(1, sd.choices.size)
        assertEquals("choice-1", sd.choices[0].id)
    }

    @Test
    fun `interact maps response correctly`() = runTest {
        coEvery { storiesApi.interact(any(), any()) } returns Response.success(
            InteractResponse(
                correct = true,
                type = "choose_glyph",
                explanation = BilingualText(en = "Correct!", ar = "صحيح!"),
                correctAnswer = "D10",
                targetGlyph = "𓂀",
                gardinerCode = "D10",
            ),
        )

        val result = repository.interact("story-1", 0, 0, "D10")
        assertTrue(result.isSuccess)

        val ir = result.getOrThrow()
        assertTrue(ir.correct)
        assertEquals("choose_glyph", ir.type)
        assertEquals("Correct!", ir.explanationEn)
        assertEquals("D10", ir.correctAnswer)
    }

    @Test
    fun `interact fails on HTTP error`() = runTest {
        coEvery { storiesApi.interact(any(), any()) } returns Response.error(
            500,
            "error".toResponseBody(),
        )

        val result = repository.interact("story-1", 0, 0, "D10")
        assertTrue(result.isFailure)
    }

    @Test
    fun `generateChapterImage returns URL on success`() = runTest {
        coEvery { storiesApi.generateChapterImage("story-1", 0) } returns Response.success(
            ChapterImageResponse(imageUrl = "https://img.wadjet.test/ch0.png", status = "ok"),
        )

        val result = repository.generateChapterImage("story-1", 0)
        assertTrue(result.isSuccess)
        assertEquals("https://img.wadjet.test/ch0.png", result.getOrThrow())
    }

    @Test
    fun `saveProgress persists to Room`() = runTest {
        val progress = StoryProgress(
            storyId = "story-1",
            chapterIndex = 2,
            glyphsLearned = listOf("D10", "A1"),
            score = 85,
            completed = false,
        )

        repository.saveProgress(progress)

        coVerify {
            storyProgressDao.upsert(match {
                it.storyId == "story-1" && it.chapterIndex == 2 && it.score == 85
            })
        }
    }

    @Test
    fun `saveProgress syncs to REST when user is logged in`() = runTest {
        val mockUser: FirebaseUser = mockk()
        every { mockUser.uid } returns "user-123"
        every { firebaseAuth.currentUser } returns mockUser

        // Firestore set().await() — mock a completed Task<Void>
        val mockDoc = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>()
        val mockUserDoc = mockk<com.google.firebase.firestore.DocumentReference>()
        every { firestore.collection("users") } returns mockk {
            every { document("user-123") } returns mockUserDoc
        }
        every { mockUserDoc.collection("story_progress") } returns mockCollection
        every { mockCollection.document("story-1") } returns mockDoc

        // Create a completed Task that await() can resolve immediately
        val completedTask = com.google.android.gms.tasks.Tasks.forResult<Void>(null)
        every { mockDoc.set(any()) } returns completedTask

        coEvery { userApi.saveProgress(any()) } returns Response.success(
            com.wadjet.core.network.model.OkResponse(ok = true),
        )

        val progress = StoryProgress(
            storyId = "story-1",
            chapterIndex = 1,
            glyphsLearned = listOf("A1"),
            score = 50,
            completed = false,
        )

        repository.saveProgress(progress)

        coVerify { storyProgressDao.upsert(any()) }
        coVerify { userApi.saveProgress(any()) }
    }

    @Test
    fun `unknown interaction type is filtered out`() = runTest {
        val storyWithUnknown = testStoryFullDto.copy(
            chapters = listOf(
                ChapterDto(
                    index = 0,
                    title = BilingualText(en = "Ch1", ar = "ف1"),
                    paragraphs = emptyList(),
                    interactions = listOf(
                        InteractionDto(type = "unknown_type", afterParagraph = 0),
                    ),
                ),
            ),
        )
        coEvery { storiesApi.getStory("story-1") } returns Response.success(storyWithUnknown)

        val result = repository.getStory("story-1")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().chapters[0].interactions.size)
    }
}
