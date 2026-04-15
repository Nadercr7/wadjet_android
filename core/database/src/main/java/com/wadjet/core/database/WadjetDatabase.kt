package com.wadjet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
    exportSchema = true,
)
abstract class WadjetDatabase : RoomDatabase() {
    abstract fun signDao(): SignDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun landmarkDao(): LandmarkDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to signs table
                db.execSQL("ALTER TABLE signs ADD COLUMN logographic_value TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE signs ADD COLUMN determinative_class TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE signs ADD COLUMN example_usages_json TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE signs ADD COLUMN related_signs_json TEXT DEFAULT NULL")

                // Rebuild FTS table with new indexed columns (reading, type_name)
                db.execSQL("DROP TABLE IF EXISTS signs_fts")
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS signs_fts USING fts4(" +
                        "code, glyph, transliteration, description, category_name, reading, type_name, " +
                        "content=`signs`)"
                )
            }
        }
    }
}
