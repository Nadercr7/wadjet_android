package com.wadjet.core.database.di

import android.content.Context
import androidx.room.Room
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.database.dao.CategoryDao
import com.wadjet.core.database.dao.FavoriteDao
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.dao.ScanResultDao
import com.wadjet.core.database.dao.SignDao
import com.wadjet.core.database.dao.StoryProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WadjetDatabase =
        Room.databaseBuilder(context, WadjetDatabase::class.java, "wadjet.db")
            .addMigrations(WadjetDatabase.MIGRATION_4_5, WadjetDatabase.MIGRATION_5_6)
            .addCallback(WadjetDatabase.FTS5_CALLBACK)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSignDao(db: WadjetDatabase): SignDao = db.signDao()

    @Provides
    fun provideScanResultDao(db: WadjetDatabase): ScanResultDao = db.scanResultDao()

    @Provides
    fun provideLandmarkDao(db: WadjetDatabase): LandmarkDao = db.landmarkDao()

    @Provides
    fun provideCategoryDao(db: WadjetDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideStoryProgressDao(db: WadjetDatabase): StoryProgressDao = db.storyProgressDao()

    @Provides
    fun provideFavoriteDao(db: WadjetDatabase): FavoriteDao = db.favoriteDao()
}
