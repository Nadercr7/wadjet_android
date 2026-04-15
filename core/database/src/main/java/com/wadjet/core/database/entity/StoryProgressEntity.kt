package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_progress")
data class StoryProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "story_id") val storyId: String,
    @ColumnInfo(name = "chapter_index") val chapterIndex: Int = 0,
    @ColumnInfo(name = "glyphs_learned_json") val glyphsLearnedJson: String = "[]",
    val score: Int = 0,
    val completed: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
