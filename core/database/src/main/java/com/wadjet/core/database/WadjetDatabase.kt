package com.wadjet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.dao.ScanResultDao
import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.entity.LandmarkEntity
import com.wadjet.core.database.entity.ScanResultEntity
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.database.entity.SignFtsEntity

@Database(
    entities = [
        SignEntity::class,
        SignFtsEntity::class,
        ScanResultEntity::class,
        LandmarkEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class WadjetDatabase : RoomDatabase() {
    abstract fun signDao(): SignDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun landmarkDao(): LandmarkDao
}
