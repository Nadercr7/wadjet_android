package com.wadjet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wadjet.core.database.entity.StoryProgressEntity

@Dao
interface StoryProgressDao {

    @Query("SELECT * FROM story_progress WHERE story_id = :storyId LIMIT 1")
    suspend fun getByStoryId(storyId: String): StoryProgressEntity?

    @Query("SELECT * FROM story_progress")
    suspend fun getAll(): List<StoryProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: StoryProgressEntity)

    @Query("DELETE FROM story_progress WHERE story_id = :storyId")
    suspend fun delete(storyId: String)
}
