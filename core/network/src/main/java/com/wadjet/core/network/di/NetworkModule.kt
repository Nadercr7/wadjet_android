package com.wadjet.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.wadjet.core.network.AuthInterceptor
import com.wadjet.core.network.TokenManager
import com.wadjet.core.network.api.AuthApiService
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.ChatApiService
import com.wadjet.core.network.api.DictionaryApiService
import com.wadjet.core.network.api.LandmarkApiService
import com.wadjet.core.network.api.ScanApiService
import com.wadjet.core.network.api.StoriesApiService
import com.wadjet.core.network.api.WriteApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (com.wadjet.core.network.BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Wadjet-Android/1.0.0")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // SSE-safe
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
        @Named("baseUrl") baseUrl: String,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDictionaryApiService(retrofit: Retrofit): DictionaryApiService =
        retrofit.create(DictionaryApiService::class.java)

    @Provides
    @Singleton
    fun provideWriteApiService(retrofit: Retrofit): WriteApiService =
        retrofit.create(WriteApiService::class.java)

    @Provides
    @Singleton
    fun provideAudioApiService(retrofit: Retrofit): AudioApiService =
        retrofit.create(AudioApiService::class.java)

    @Provides
    @Singleton
    fun provideScanApiService(retrofit: Retrofit): ScanApiService =
        retrofit.create(ScanApiService::class.java)

    @Provides
    @Singleton
    fun provideLandmarkApiService(retrofit: Retrofit): LandmarkApiService =
        retrofit.create(LandmarkApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideStoriesApiService(retrofit: Retrofit): StoriesApiService =
        retrofit.create(StoriesApiService::class.java)
}
