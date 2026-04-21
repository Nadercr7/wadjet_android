package com.wadjet.core.data.di

import com.wadjet.core.data.repository.AuthRepositoryImpl
import com.wadjet.core.data.repository.ChatRepositoryImpl
import com.wadjet.core.data.repository.DictionaryRepositoryImpl
import com.wadjet.core.data.repository.ExploreRepositoryImpl
import com.wadjet.core.data.repository.FeedbackRepositoryImpl
import com.wadjet.core.data.repository.ScanRepositoryImpl
import com.wadjet.core.data.repository.StoriesRepositoryImpl
import com.wadjet.core.data.repository.TranslateRepositoryImpl
import com.wadjet.core.data.repository.TtsPreferencesRepositoryImpl
import com.wadjet.core.data.repository.UserRepositoryImpl
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.domain.repository.ChatRepository
import com.wadjet.core.domain.repository.DictionaryRepository
import com.wadjet.core.domain.repository.ExploreRepository
import com.wadjet.core.domain.repository.FeedbackRepository
import com.wadjet.core.domain.repository.ScanRepository
import com.wadjet.core.domain.repository.StoriesRepository
import com.wadjet.core.domain.repository.TranslateRepository
import com.wadjet.core.domain.repository.TtsPreferencesRepository
import com.wadjet.core.domain.repository.UserRepository
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

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindStoriesRepository(impl: StoriesRepositoryImpl): StoriesRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindTranslateRepository(impl: TranslateRepositoryImpl): TranslateRepository

    @Binds
    @Singleton
    abstract fun bindTtsPreferencesRepository(impl: TtsPreferencesRepositoryImpl): TtsPreferencesRepository
}
