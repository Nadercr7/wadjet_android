package com.wadjet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wadjet.core.database.dao.CategoryDao
import com.wadjet.core.database.dao.FavoriteDao
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.dao.ScanResultDao
import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.dao.StoryProgressDao
import com.wadjet.core.database.entity.CategoryEntity
import com.wadjet.core.database.entity.FavoriteEntity
import com.wadjet.core.database.entity.LandmarkEntity
import com.wadjet.core.database.entity.ScanResultEntity
import com.wadjet.core.database.entity.SignEntity
import com.wadjet.core.database.entity.SignFtsEntity
import com.wadjet.core.database.entity.StoryProgressEntity

@Database(
    entities = [
        SignEntity::class,
        SignFtsEntity::class,
        ScanResultEntity::class,
        LandmarkEntity::class,
        CategoryEntity::class,
        StoryProgressEntity::class,
        FavoriteEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class WadjetDatabase : RoomDatabase() {
    abstract fun signDao(): SignDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun landmarkDao(): LandmarkDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storyProgressDao(): StoryProgressDao
    abstract fun favoriteDao(): FavoriteDao

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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate FTS4 with unicode61 tokenizer for diacritic/Unicode support
                db.execSQL("DROP TABLE IF EXISTS signs_fts")
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS signs_fts USING fts4(" +
                        "code, glyph, transliteration, description, category_name, reading, type_name, " +
                        "content=`signs`, tokenize=unicode61)"
                )

                // Create categories table for offline caching
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories (" +
                        "code TEXT NOT NULL PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "count INTEGER NOT NULL DEFAULT 0, " +
                        "cachedAt INTEGER NOT NULL DEFAULT 0)"
                )

                // Create story_progress table for offline fallback
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS story_progress (" +
                        "story_id TEXT NOT NULL PRIMARY KEY, " +
                        "chapter_index INTEGER NOT NULL DEFAULT 0, " +
                        "glyphs_learned_json TEXT NOT NULL DEFAULT '[]', " +
                        "score INTEGER NOT NULL DEFAULT 0, " +
                        "completed INTEGER NOT NULL DEFAULT 0, " +
                        "updated_at INTEGER NOT NULL DEFAULT 0)"
                )

                // Create favorites table for offline caching
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS favorites (" +
                        "item_type TEXT NOT NULL, " +
                        "item_id TEXT NOT NULL, " +
                        "cached_at INTEGER NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY(item_type, item_id))"
                )

                // Repopulate FTS from existing signs data
                db.execSQL(
                    "INSERT INTO signs_fts(signs_fts) VALUES('rebuild')"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Fix: replace FTS5 table (unsupported on some devices) with FTS4
                db.execSQL("DROP TABLE IF EXISTS signs_fts")
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS signs_fts USING fts4(" +
                        "code, glyph, transliteration, description, category_name, reading, type_name, " +
                        "content=`signs`, tokenize=unicode61)"
                )
                db.execSQL(
                    "INSERT INTO signs_fts(signs_fts) VALUES('rebuild')"
                )
            }
        }
    }
}
