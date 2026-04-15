package com.wadjet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wadjet.core.database.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE item_type = :itemType")
    suspend fun getByType(itemType: String): List<FavoriteEntity>

    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<FavoriteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE item_type = :itemType AND item_id = :itemId")
    suspend fun delete(itemType: String, itemId: String)

    @Query("DELETE FROM favorites WHERE item_type = :itemType")
    suspend fun deleteByType(itemType: String)
}
