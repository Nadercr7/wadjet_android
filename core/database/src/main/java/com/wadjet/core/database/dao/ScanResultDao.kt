package com.wadjet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wadjet.core.database.entity.ScanResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Query("SELECT * FROM scan_results ORDER BY created_at DESC")
    fun getAll(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getById(id: Int): ScanResultEntity?

    @Query("SELECT * FROM scan_results WHERE firestore_id = :firestoreId")
    suspend fun getByFirestoreId(firestoreId: String): ScanResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScanResultEntity): Long

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM scan_results")
    suspend fun count(): Int
}
