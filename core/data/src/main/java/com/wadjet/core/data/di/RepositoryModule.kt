package com.wadjet.core.data.di

import com.wadjet.core.data.repository.AuthRepositoryImpl
import com.wadjet.core.data.repository.DictionaryRepositoryImpl
import com.wadjet.core.data.repository.ExploreRepositoryImpl
import com.wadjet.core.data.repository.ScanRepositoryImpl
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.ExploreRepository
import com.wadjet.core.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDictionaryRepository(impl: DictionaryRepositoryImpl): DictionaryRepository

    @Binds
    @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    @Binds
    @Singleton
    abstract fun bindExploreRepository(impl: ExploreRepositoryImpl): ExploreRepository
}
