package com.wadjet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wadjet.core.database.entity.SignEntity

@Dao
interface SignDao {

    @Query("SELECT * FROM signs WHERE category = :category AND type = :type ORDER BY code LIMIT :limit OFFSET :offset")
    suspend fun getByFilter(category: String, type: String, limit: Int, offset: Int): List<SignEntity>

    @Query("SELECT * FROM signs WHERE category = :category ORDER BY code LIMIT :limit OFFSET :offset")
    suspend fun getByCategory(category: String, limit: Int, offset: Int): List<SignEntity>

    @Query("SELECT * FROM signs WHERE type = :type ORDER BY code LIMIT :limit OFFSET :offset")
    suspend fun getByType(type: String, limit: Int, offset: Int): List<SignEntity>

    @Query("SELECT * FROM signs ORDER BY code LIMIT :limit OFFSET :offset")
    suspend fun getAll(limit: Int, offset: Int): List<SignEntity>

    @Query("SELECT * FROM signs WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): SignEntity?

    @Query(
        """
        SELECT signs.* FROM signs
        JOIN signs_fts ON signs.code = signs_fts.code
        WHERE signs_fts MATCH :query
        ORDER BY signs.code
        LIMIT :limit
        """
    )
    suspend fun search(query: String, limit: Int = 50): List<SignEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signs: List<SignEntity>)

    @Query("SELECT COUNT(*) FROM signs")
    suspend fun count(): Int

    @Query("DELETE FROM signs")
    suspend fun deleteAll()
}
