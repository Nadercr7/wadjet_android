package com.wadjet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wadjet.core.database.entity.LandmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandmarkDao {

    @Query("SELECT * FROM landmarks ORDER BY popularity DESC, name ASC")
    fun getAll(): Flow<List<LandmarkEntity>>

    @Query(
        """
        SELECT * FROM landmarks
        WHERE (:category IS NULL OR type = :category)
        AND (:city IS NULL OR city = :city)
        ORDER BY popularity DESC, name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun getFiltered(
        category: String?,
        city: String?,
        limit: Int,
        offset: Int,
    ): List<LandmarkEntity>

    @Query(
        """
        SELECT * FROM landmarks
        WHERE name LIKE '%' || :query || '%'
           OR city LIKE '%' || :query || '%'
           OR type LIKE '%' || :query || '%'
           OR name_ar LIKE '%' || :query || '%'
        ORDER BY popularity DESC
        LIMIT :limit
        """,
    )
    suspend fun search(query: String, limit: Int = 50): List<LandmarkEntity>

    @Query("SELECT * FROM landmarks WHERE slug = :slug LIMIT 1")
    suspend fun getBySlug(slug: String): LandmarkEntity?

    @Query("SELECT DISTINCT city FROM landmarks WHERE city IS NOT NULL ORDER BY city ASC")
    suspend fun getCities(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(landmarks: List<LandmarkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(landmark: LandmarkEntity)

    @Query("SELECT COUNT(*) FROM landmarks")
    suspend fun count(): Int

    @Query("DELETE FROM landmarks")
    suspend fun deleteAll()
}
