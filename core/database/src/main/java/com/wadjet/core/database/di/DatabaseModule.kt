package com.wadjet.core.database.di

import android.content.Context
import androidx.room.Room
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.database.dao.LandmarkDao
import com.wadjet.core.database.dao.ScanResultDao
import com.wadjet.core.database.dao.SignDao
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
            .addMigrations(WadjetDatabase.MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSignDao(db: WadjetDatabase): SignDao = db.signDao()

    @Provides
    fun provideScanResultDao(db: WadjetDatabase): ScanResultDao = db.scanResultDao()

    @Provides
    fun provideLandmarkDao(db: WadjetDatabase): LandmarkDao = db.landmarkDao()
}
