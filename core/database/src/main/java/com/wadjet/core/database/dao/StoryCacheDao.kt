package com.wadjet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wadjet.core.database.entity.StoryCacheEntity

@Dao
interface StoryCacheDao {

    @Query("SELECT * FROM story_cache WHERE summary_json != '' ORDER BY sort_order")
    suspend fun getAll(): List<StoryCacheEntity>

    @Query("SELECT * FROM story_cache WHERE id = :id")
    suspend fun getById(id: String): StoryCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StoryCacheEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: StoryCacheEntity)
}
