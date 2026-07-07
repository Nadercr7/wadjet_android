package com.wadjet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline cache for story content (E-02). Stores the raw network DTO JSON so the
 * list and reader keep working without a connection once fetched.
 */
@Entity(tableName = "story_cache")
data class StoryCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    /** JSON of StorySummaryDto; empty string when only the full story was cached. */
    @ColumnInfo(name = "summary_json") val summaryJson: String,
    /** JSON of StoryFullDto; null until the story has been opened once. */
    @ColumnInfo(name = "full_json") val fullJson: String?,
    /** Server list position — cached list must keep server order (premium gating is positional). */
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
