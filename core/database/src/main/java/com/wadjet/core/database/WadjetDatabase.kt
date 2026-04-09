package com.wadjet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.database.entity.SignFtsEntity

@Database(
    entities = [SignEntity::class, SignFtsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WadjetDatabase : RoomDatabase() {
    abstract fun signDao(): SignDao
}
