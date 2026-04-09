package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.InteractionResult
import com.wadjet.core.domain.model.StoryFull
import com.wadjet.core.domain.model.StoryProgress
import com.wadjet.core.domain.model.StorySummary
import kotlinx.coroutines.flow.Flow

interface StoriesRepository {

    suspend fun getStories(): Result<List<StorySummary>>

    suspend fun getStory(storyId: String): Result<StoryFull>

    suspend fun interact(
        storyId: String,
        chapterIndex: Int,
        interactionIndex: Int,
        answer: String,
    ): Result<InteractionResult>

    suspend fun generateChapterImage(storyId: String, chapterIndex: Int): Result<String?>

    suspend fun speakChapter(text: String): Result<ByteArray?>

    fun getStoryProgress(storyId: String): Flow<StoryProgress?>

    fun getAllProgress(): Flow<Map<String, StoryProgress>>

    suspend fun saveProgress(progress: StoryProgress)
}
