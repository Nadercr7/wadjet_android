package com.wadjet.core.network.api

import com.wadjet.core.network.model.ChapterImageResponse
import com.wadjet.core.network.model.InteractRequest
import com.wadjet.core.network.model.InteractResponse
import com.wadjet.core.network.model.StoriesListResponse
import com.wadjet.core.network.model.StoryFullDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoriesApiService {

    @GET("api/stories")
    suspend fun getStories(): Response<StoriesListResponse>

    @GET("api/stories/{storyId}")
    suspend fun getStory(
        @Path("storyId") storyId: String,
    ): Response<StoryFullDto>

    @POST("api/stories/{storyId}/interact")
    suspend fun interact(
        @Path("storyId") storyId: String,
        @Body body: InteractRequest,
    ): Response<InteractResponse>

    @POST("api/stories/{storyId}/chapters/{index}/image")
    suspend fun generateChapterImage(
        @Path("storyId") storyId: String,
        @Path("index") index: Int,
    ): Response<ChapterImageResponse>
}
